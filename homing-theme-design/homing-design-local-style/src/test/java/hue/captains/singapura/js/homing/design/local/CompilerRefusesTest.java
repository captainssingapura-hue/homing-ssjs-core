package hue.captains.singapura.js.homing.design.local;

import hue.captains.singapura.js.homing.component.taxonomy.Role;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
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
 * What the compiler refuses of a local style, proved by compiling (RFC 0067, appendix Local Styles): each case a
 * hypothetical declaration handed to javac against this module and the tree, one that must compile and one for each
 * refusal the types promise, which must fail on the line it is marked on and nowhere else.
 */
class CompilerRefusesTest {

    private static final String PREAMBLE = """
            package hypothetical;
            import hue.captains.singapura.js.homing.component.taxonomy.*;
            import hue.captains.singapura.js.homing.design.local.*;
            import hue.captains.singapura.js.homing.design.color.ColorScheme;
            import java.util.List;
            public class Hypothetical {
                record Text() implements L1_ComponentBranch<Root> { static final Text INSTANCE = new Text(); public Root parent() { return Root.INSTANCE; } }
            """;

    private static final String MARK = "// refused here";

    /** A local-style branch, two virtual leaves each with its own interface for extension, and two local styles of it. */
    private static final String STYLED = """
            static class Scheme implements ColorScheme { static final Scheme INSTANCE = new Scheme(); }
            record Frame() implements L1_ComponentBranch<Root>, LocalStyleBranch { static final Frame INSTANCE = new Frame(); public Root parent() { return Root.INSTANCE; } }
            record Other() implements L1_ComponentBranch<Root>, LocalStyleBranch { static final Other INSTANCE = new Other(); public Root parent() { return Root.INSTANCE; } }
            record Edge() implements VirtualComponent<Frame> { static final Edge INSTANCE = new Edge(); public Frame parent() { return Frame.INSTANCE; }
                interface Concrete<D extends LocalStyle<?, Frame>> extends ConcreteComponent<Edge, Frame, D> {} }
            record Mark() implements VirtualComponent<Frame> { static final Mark INSTANCE = new Mark(); public Frame parent() { return Frame.INSTANCE; }
                interface Concrete<D extends LocalStyle<?, Frame>> extends ConcreteComponent<Mark, Frame, D> {} }
            record Plain() implements LocalStyle<Scheme, Frame> { @Override public Scheme scheme() { return Scheme.INSTANCE; } public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(); } static final Plain INSTANCE = new Plain(); }
            record Bold() implements LocalStyle<Scheme, Frame> { @Override public Scheme scheme() { return Scheme.INSTANCE; } public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(); } static final Bold INSTANCE = new Bold(); }
            record Elsewhere() implements LocalStyle<Scheme, Other> { @Override public Scheme scheme() { return Scheme.INSTANCE; } public List<ConcreteComponent<?, Other, ?>> concretes() { return List.of(); } static final Elsewhere INSTANCE = new Elsewhere(); }
            """;

    @Test
    void aLocalStyleOfVirtualLeaves_compiles() {
        var errors = compile(STYLED + """
                record PlainEdge() implements Edge.Concrete<Plain> {}
                record BoldMark() implements Mark.Concrete<Bold> {}
                record DirectMark() implements ConcreteComponent<Mark, Frame, Plain> {}
                """);
        assertTrue(errors.isEmpty(), "a concrete leaf through its leaf's interface, or naming all three itself: " + errors);
    }

    @Test
    void aLocalStyleOfABranchThatIsNoLocalStyleBranch_doesNotCompile() {
        refusedOnItsLine(STYLED + """
                record Loose() implements LocalStyle<Scheme, Text> { public Scheme scheme() { return Scheme.INSTANCE; } public List<ConcreteComponent<?, Text, ?>> concretes() { return List.of(); } } // refused here
                """);
    }

    @Test
    void aConcreteLeafOfOneBranchsLeafWithAnotherBranchsLocalStyle_doesNotCompile() {
        refusedOnItsLine(STYLED + """
                record Mixed() implements ConcreteComponent<Edge, Frame, Elsewhere> {} // refused here
                """);
    }

    @Test
    void aLocalStyleInWhatIsNoScheme_doesNotCompile() {
        refusedOnItsLine(STYLED + """
                record Schemeless() implements LocalStyle<String, Frame> { public String scheme() { return ""; } public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(); } } // refused here
                """);
    }

    @Test
    void aConcreteLeafOfTwoLocalStyles_doesNotCompile() {
        refusedOnItsLine(STYLED + """
                record Both() implements Edge.Concrete<Plain>, Edge.Concrete<Bold> {} // refused here
                """);
    }

    @Test
    void aConcreteLeafOfTwoLeaves_doesNotCompile() {
        refusedOnItsLine(STYLED + """
                record Twice() implements Edge.Concrete<Plain>, Mark.Concrete<Plain> {} // refused here
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
        return Stream.of(Role.class, NodeName.class, StatelessFunctionalObject.class, ColorScheme.class, LocalStyle.class)
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
