package hue.captains.singapura.js.homing.conformance.self;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * No reflection at run time, as a rule over core's sources: nothing on a run-time path turns a name into a class, or
 * finds a module, a class or an instance by reflection - what a run time reaches, it reaches through what was built at
 * startup from the declarations it was handed. It looks in every module's {@code src/main/java} for the uses that
 * would: a class by its name, an instance by its {@code INSTANCE} field or by a constructor, a resource by a name that
 * is not written down.
 *
 * <p>Build-time tools keep reflection freely, and are exempt by module, each with its reason. What a run-time module
 * still has is classified, by file and by use, each with its reason: allowed, where the code reads a class it holds or
 * a resource its declaration names, and turns no name into a class; or reported, where it finds an instance by
 * reflection on a run-time path and the owner has yet to decide. Anything else is a finding, and fails core's
 * build.</p>
 *
 * <p>A rule's shape - a name, its uses, its exemptions, its findings - so RFC 0070's Java conformance can adopt it; a
 * test holds it until then ({@link NoReflectionAtRunTimeTest}).</p>
 */
record NoReflectionAtRunTime() implements StatelessFunctionalObject {

    static final NoReflectionAtRunTime INSTANCE = new NoReflectionAtRunTime();

    /** A use of reflection the rule looks for, by its name, and how a line of source shows it. */
    record Use(String name, Pattern shows) implements ValueObject {}

    /** A module the rule does not look in - a build-time tool's - and why. */
    record Exempt(String module, String why) implements ValueObject {}

    /** What the rule makes of one use in one file of a run-time module, and why. */
    record Classified(String module, String file, String use, Verdict verdict, String why) implements ValueObject {}

    enum Verdict {
        /** The rule does not forbid it: a class the code holds, or a resource its declaration names - no name turned into a class. */
        ALLOWED,
        /** An instance found by reflection on a run-time path: reported to the owner, kept until they decide. */
        REPORTED
    }

    /** A use the rule forbids: its module, its file from the repository's root, its line. */
    record Finding(String module, String path, int line, String use, String text) implements ValueObject {
        @Override public String toString() { return path + ":" + line + " [" + use + "] " + text; }
    }

    private static final List<Use> USES = List.of(
            new Use("class-by-name", Pattern.compile("\\bClass\\s*\\.\\s*forName\\s*\\(")),
            new Use("instance-field", Pattern.compile("\\.getField\\(\\s*\"INSTANCE\"\\s*\\)")),
            new Use("new-instance", Pattern.compile("\\.newInstance\\(")),
            new Use("resource-by-computed-name", Pattern.compile("getResourceAsStream\\((?!\\s*\"[^\"]*\"\\s*\\))")));

    private static final List<Exempt> EXEMPT = List.of(
            new Exempt("homing-conformance-rules", "the conformance rules read classes by name at build: crates' seeds, declared debts, orphans"),
            new Exempt("homing-conformance-engine", "the conformance engine, which the gates run at build"),
            new Exempt("homing-conformance-report-gen", "a generator: writes the conformance report's code at build"),
            new Exempt("homing-codec", "a generator: the codecs' code is written at build"),
            new Exempt("homing-codec-ecma", "a generator: the codecs' code is written at build"),
            new Exempt("homing-codec-java", "a generator: the codecs' code is written at build"),
            new Exempt("homing-ssjs-test-fixtures", "the base of the JS modules' tests: a jar only tests depend on"));

    private static final String SEMANTIC_CLASS = "homing-theme-design/homing-semantic-class";

    private static final List<Classified> CLASSIFIED = List.of(
            new Classified("homing-component-taxonomy", "Meanings.java", "resource-by-computed-name", Verdict.ALLOWED,
                    "a node's meanings file, named after the class of a node it is handed: read only for a node one holds"),
            new Classified("homing-core", "ResourceReader.java", "resource-by-computed-name", Verdict.ALLOWED,
                    "reads the path it is handed; naming it is its caller's"),
            new Classified("homing-doc-model", "ClasspathMarkdownDoc.java", "resource-by-computed-name", Verdict.ALLOWED,
                    "a doc's bytes, at the path its own class names"),
            new Classified("homing-doc-model", "ResourceMarkdownDoc.java", "resource-by-computed-name", Verdict.ALLOWED,
                    "a doc's bytes, at the path its declaration names"),
            new Classified("homing-doc-model", "ImageDoc.java", "resource-by-computed-name", Verdict.ALLOWED,
                    "an image's bytes, at the path its declaration names"),
            new Classified(SEMANTIC_CLASS, "StyleFamilies.java", "resource-by-computed-name", Verdict.ALLOWED,
                    "the family files the instance's jar was built with, at the paths the styling names"),
            new Classified(SEMANTIC_CLASS, "WriteStyleFamilies.java", "class-by-name", Verdict.ALLOWED,
                    "a command line the instance's jar runs when it is built, naming the instance's class"),
            new Classified(SEMANTIC_CLASS, "WriteStyleFamilies.java", "instance-field", Verdict.ALLOWED,
                    "a command line the instance's jar runs when it is built, naming the instance's class"),
            new Classified(SEMANTIC_CLASS, "ResolveCli.java", "class-by-name", Verdict.ALLOWED,
                    "a command line, run by hand: resolves the class it is given"),
            new Classified(SEMANTIC_CLASS, "ResolveCli.java", "instance-field", Verdict.ALLOWED,
                    "a command line, run by hand: resolves the class it is given"),
            new Classified(SEMANTIC_CLASS, "CheckScheme.java", "instance-field", Verdict.ALLOWED,
                    "a scheme jar's own test calls it, and nothing at run time: it holds a scheme's INSTANCE to its type"),
            new Classified(SEMANTIC_CLASS, "CheckDeclaration.java", "class-by-name", Verdict.ALLOWED,
                    "the style jar's own check, at build (check, then leftOut): the classes of the jar's own entries"),
            new Classified(SEMANTIC_CLASS, "CheckDeclaration.java", "instance-field", Verdict.REPORTED,
                    "instance(), through casting(): reached at a site's startup - a styling installed, StyleRegistryBuilder "
                            + "asks Bundles.casting - a component's and a role's instance found from an implementation's type arguments"),
            new Classified(SEMANTIC_CLASS, "CheckDeclaration.java", "new-instance", Verdict.REPORTED,
                    "instance(), through casting(): as above, a record of no components made by its constructor"),
            new Classified("homing-component-base", "UiComponent.java", "instance-field", Verdict.REPORTED,
                    "module(): the module found by its declaration's enclosing class's INSTANCE; here only ComponentTrees' "
                            + "normalize and validate call it, which only tests call - but any run time may"),
            new Classified("homing-theme-design/homing-design-core", "Trees.java", "instance-field", Verdict.REPORTED,
                    "targetInstance(): each leaf of the sealed tree of targets by its INSTANCE, through Target.leaves() "
                            + "and SemanticTokens.leaves(), which flattening a sheet walks"));

    /** Folders that hold no module's own sources: what a build made, what was fetched, what a tool keeps. */
    private static final Set<String> NOT_SOURCES = Set.of("target", "node_modules");

    /** The rule's name, as RFC 0070's Java conformance would know it. */
    String name() { return "no-reflection-at-run-time"; }

    List<Use> uses() { return USES; }

    List<Exempt> exempt() { return EXEMPT; }

    List<Classified> classified() { return CLASSIFIED; }

    /** Every use in core's sources the rule forbids: in no exempt module, and classified nowhere. */
    List<Finding> findings(Path root) {
        return found(root).stream().filter(f -> classification(f) == null).toList();
    }

    /** Every use classified as reported: on a run-time path, kept until the owner decides. */
    List<Finding> reported(Path root) {
        return found(root).stream().filter(f -> classification(f) != null && classification(f).verdict() == Verdict.REPORTED).toList();
    }

    /**
     * What the rule's lists say that no longer holds: an exempt module that is not there, a classification that matches
     * no use - its code retired or moved, so the line is stale, and goes.
     */
    List<String> stale(Path root) {
        var out = new ArrayList<String>();
        var modules = sources(root).stream().map(s -> module(root, s)).toList();
        for (Exempt e : EXEMPT)
            if (!modules.contains(e.module())) out.add("exempt " + e.module() + ": no such module");
        var found = found(root);
        for (Classified c : CLASSIFIED)
            if (found.stream().noneMatch(f -> c.equals(classification(f))))
                out.add("classified " + c.module() + "/" + c.file() + " [" + c.use() + "]: no such use");
        return out;
    }

    /** Every use in the modules not exempt, classified or not. */
    private static List<Finding> found(Path root) {
        var out = new ArrayList<Finding>();
        for (Path sources : sources(root)) {
            String module = module(root, sources);
            if (EXEMPT.stream().anyMatch(e -> e.module().equals(module))) continue;
            for (Path file : javaFiles(sources)) {
                List<String> lines = lines(file);
                for (int i = 0; i < lines.size(); i++) {
                    String line = lines.get(i);
                    if (comment(line)) continue;
                    for (Use use : USES)
                        if (use.shows().matcher(line).find())
                            out.add(new Finding(module, slashed(root.relativize(file)), i + 1, use.name(), line.strip()));
                }
            }
        }
        return out;
    }

    private static Classified classification(Finding f) {
        String file = f.path().substring(f.path().lastIndexOf('/') + 1);
        for (Classified c : CLASSIFIED)
            if (c.module().equals(f.module()) && c.file().equals(file) && c.use().equals(f.use())) return c;
        return null;
    }

    /** Every module's {@code src/main/java} under the root, however deep the module. */
    private static List<Path> sources(Path root) {
        var out = new ArrayList<Path>();
        try {
            Files.walkFileTree(root, new SimpleFileVisitor<>() {
                @Override public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    String name = dir.getFileName() == null ? "" : dir.getFileName().toString();
                    if (!dir.equals(root) && (name.startsWith(".") || NOT_SOURCES.contains(name))) return FileVisitResult.SKIP_SUBTREE;
                    if (dir.endsWith(Path.of("src", "main", "java"))) { out.add(dir); return FileVisitResult.SKIP_SUBTREE; }
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(root.toString(), e);
        }
        out.sort(null);
        return out;
    }

    /** {@code homing-server}; {@code homing-theme-design/homing-semantic-class}, two deep. */
    private static String module(Path root, Path sources) { return slashed(root.relativize(sources.getParent().getParent().getParent())); }

    private static List<Path> javaFiles(Path sources) {
        try (Stream<Path> walk = Files.walk(sources)) {
            return walk.filter(p -> p.toString().endsWith(".java")).sorted().toList();
        } catch (IOException e) {
            throw new UncheckedIOException(sources.toString(), e);
        }
    }

    private static List<String> lines(Path file) {
        try {
            return Files.readAllLines(file);
        } catch (IOException e) {
            throw new UncheckedIOException(file.toString(), e);
        }
    }

    /** A line of a comment, which says what code does and does none of it. */
    private static boolean comment(String line) {
        String s = line.strip();
        return s.startsWith("*") || s.startsWith("//") || s.startsWith("/*");
    }

    private static String slashed(Path p) { return p.toString().replace('\\', '/'); }
}
