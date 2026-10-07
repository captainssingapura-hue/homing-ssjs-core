package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import org.junit.jupiter.api.Test;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the compiler refuses, proved by compiling: each case is a hypothetical declaration,
 * written as a repository would write it, handed to javac against this module - one that must
 * compile, and one for each refusal the declaration language promises, which must fail on the
 * line it is marked on and nowhere else.
 */
class CompilerRefusesTest {

    private static final String PREAMBLE = """
            package hypothetical;
            import hue.captains.singapura.js.homing.component.taxonomy.*;
            import java.util.List;
            public class Hypothetical {
                record Text() implements Kind<Root> { static final Text INSTANCE = new Text(); public Root parent() { return Root.INSTANCE; } }
                record Naming() implements RoleFamily<AnyRole> { static final Naming INSTANCE = new Naming(); public AnyRole parent() { return AnyRole.INSTANCE; } }
                record Title() implements Role<Naming> { static final Title INSTANCE = new Title(); public Naming family() { return Naming.INSTANCE; } }
                record Caption() implements Component<Text> { static final Caption INSTANCE = new Caption(); public Text parent() { return Text.INSTANCE; } }
            """;

    private static final String MARK = "// refused here";

    @Test
    void aWellFormedDeclaration_compiles() {
        var errors = compile("""
                record Note() implements Component<Text> {
                    private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
                    public Text parent() { return Text.INSTANCE; }
                    public List<Slot<?>> parts() {
                        return List.of(DSL.part(Caption.INSTANCE).as(Title.INSTANCE).optional());
                    }
                }
                """);
        assertTrue(errors.isEmpty(), errors.toString());
    }

    @Test
    void aPartNeverToldHowMany_doesNotCompile() {
        refusedOnItsLine("""
                record Note() implements Component<Text> {
                    private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
                    public Text parent() { return Text.INSTANCE; }
                    public List<Slot<?>> parts() {
                        return List.of(DSL.part(Caption.INSTANCE).as(Title.INSTANCE)); // refused here
                    }
                }
                """);
    }

    @Test
    void aPartNeverGivenARole_doesNotCompile() {
        refusedOnItsLine("""
                record Note() implements Component<Text> {
                    private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
                    public Text parent() { return Text.INSTANCE; }
                    public List<Slot<?>> parts() {
                        return List.of(DSL.part(Caption.INSTANCE).one()); // refused here
                    }
                }
                """);
    }

    @Test
    void aPartPlayedByAKind_doesNotCompile() {
        refusedOnItsLine("""
                record Note() implements Component<Text> {
                    private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
                    public Text parent() { return Text.INSTANCE; }
                    public List<Slot<?>> parts() {
                        return List.of(DSL.part(Text.INSTANCE).as(Title.INSTANCE).one()); // refused here
                    }
                }
                """);
    }

    @Test
    void aPartCastAsAnythingButARole_doesNotCompile() {
        refusedOnItsLine("""
                record Note() implements Component<Text> {
                    private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
                    public Text parent() { return Text.INSTANCE; }
                    public List<Slot<?>> parts() {
                        return List.of(DSL.part(Caption.INSTANCE).as(Naming.INSTANCE).one()); // refused here
                    }
                }
                """);
    }

    @Test
    void aComponentUnderAComponent_doesNotCompile() {
        refusedOnItsLine("""
                record Inner() implements Component<Caption> { public Caption parent() { return Caption.INSTANCE; } } // refused here
                """);
    }

    @Test
    void aRoleFiledAtTheRoot_doesNotCompile() {
        refusedOnItsLine("""
                record Loose() implements Role<AnyRole> { public AnyRole family() { return AnyRole.INSTANCE; } } // refused here
                """);
    }

    @Test
    void aRoleFiledUnderAKind_doesNotCompile() {
        refusedOnItsLine("""
                record Misfiled() implements Role<Text> { public Text family() { return Text.INSTANCE; } } // refused here
                """);
    }

    @Test
    void aThirdCardinality_doesNotCompile() {
        refusedOnItsLine("""
                record Some() implements Cardinality { // refused here
                    public int least() { return 0; }
                    public boolean allows(int n) { return true; }
                    public String multiplicity() { return "?"; }
                }
                """);
    }

    // ── javac, against this module ─────────────────────────────────────────

    private static void refusedOnItsLine(String body) {
        String source = PREAMBLE + body + "}\n";
        long marked = lineOf(source, MARK);
        var errors = compile(body);
        assertFalse(errors.isEmpty(), "must not compile:\n" + body);
        assertTrue(errors.stream().allMatch(e -> e.getLineNumber() == marked),
                "refused on the marked line " + marked + " and nowhere else: " + describe(errors));
    }

    private static List<Diagnostic<? extends JavaFileObject>> compile(String body) {
        var javac = ToolProvider.getSystemJavaCompiler();
        assertNotNull(javac, "the tests run on a JDK");
        String source = PREAMBLE + body + "}\n";
        var diagnostics = new DiagnosticCollector<JavaFileObject>();
        var file = new SimpleJavaFileObject(URI.create("string:///hypothetical/Hypothetical.java"), JavaFileObject.Kind.SOURCE) {
            @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) { return source; }
        };
        try {
            Path out = Files.createTempDirectory("hypothetical");
            var fileManager = javac.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8);
            var options = List.of("-classpath", classpath(), "-d", out.toString(), "-proc:none", "--release", "21");
            javac.getTask(null, fileManager, diagnostics, options, null, List.of(file)).call();
            fileManager.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        return diagnostics.getDiagnostics().stream().filter(d -> d.getKind() == Diagnostic.Kind.ERROR).collect(Collectors.toList());
    }

    /** This module's classes, and what they are written against. */
    private static String classpath() {
        return Stream.of(Role.class, NodeName.class, StatelessFunctionalObject.class)
                .map(c -> {
                    try { return Path.of(c.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(); }
                    catch (URISyntaxException e) { throw new IllegalStateException(e); }
                })
                .distinct()
                .collect(Collectors.joining(File.pathSeparator));
    }

    private static long lineOf(String source, String mark) {
        var lines = source.split("\n", -1);
        for (int i = 0; i < lines.length; i++) if (lines[i].contains(mark)) return i + 1;
        throw new IllegalArgumentException("no line marked");
    }

    private static String describe(List<Diagnostic<? extends JavaFileObject>> errors) {
        return errors.stream().map(d -> "line " + d.getLineNumber() + ": " + d.getMessage(null)).collect(Collectors.joining("; "));
    }
}
