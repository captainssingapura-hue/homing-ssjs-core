package hue.captains.singapura.js.homing.design.color;

import org.junit.jupiter.api.Test;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What the compiler refuses in a colour tree, proved by compiling: each case is a hypothetical declaration, written as
 * a repository would write it, handed to javac against this module - one that must compile, and one for each refusal
 * the colour tree promises, which must fail on the line it is marked on and nowhere else.
 */
class ColorTreeRefusesTest {

    /** The base with two branches, a scheme on it with one of its own, and a leaf of each kind. */
    private static final String PREAMBLE = """
            package hypothetical;
            import hue.captains.singapura.js.homing.design.color.*;
            import hue.captains.singapura.js.homing.design.Target.Color;
            import java.util.List;
            public class Hypothetical {
                abstract static class Base implements ColorScheme {}
                abstract static class Neo extends Base {}
                record Attention() implements L1_ColorBranch<Base> {}
                record Feedback() implements L1_ColorBranch<Base> {}
                record Glow() implements L1_ColorBranch<Neo> {}
                record Accent() implements Palettable<Base, Attention> {}
                record Calm() implements Palettable<Base, Feedback> {}
                record Danger() implements Palettable<Base, Feedback> {}
                record MineRed() implements Fixed<Base, Feedback> { public ConcreteColor color() { return new ConcreteColor.Srgb("#ff0000"); } }
                record Risk() implements ColorScale<Base, Feedback> { public List<ColorScale.Knot<Base>> knots() { return List.of(new ColorScale.Knot<>(new Calm(), -1), new ColorScale.Knot<>(new Accent(), 0), new ColorScale.Knot<>(new Danger(), 1)); } }
                record Warning() implements Interpolated<Base, Feedback> { public ColorScale<? super Base, ?> scale() { return new Risk(); } public double degree() { return 0.5; } }
                record Spark() implements Palettable<Neo, Glow> {}
                record Glare() implements ColorScale<Neo, Glow> { public List<ColorScale.Knot<Neo>> knots() { return List.of(new ColorScale.Knot<>(new Calm(), -1), new ColorScale.Knot<>(new Spark(), 0), new ColorScale.Knot<>(new MineRed(), 1)); } }
            """;

    private static final String MARK = "// refused here";

    @Test
    void aWellFormedTree_compiles() {
        var errors = compile("""
                record Halo() implements L2_ColorBranch<Neo, Attention> {}
                record Rim() implements L3_ColorBranch<Neo, Halo> {}
                record Edge() implements L2_ColorBranch<Base, Feedback> {}
                record Flare() implements Palettable<Neo, Rim> {}
                record Ember() implements Fixed<Neo, Edge> { public ConcreteColor color() { return new ConcreteColor.Oklch("oklch(0.6 0.2 30)"); } }
                record Hazard() implements Interpolated<Neo, Glow> { public ColorScale<? super Neo, ?> scale() { return new Risk(); } public double degree() { return -0.25; } }
                record Flash() implements Interpolated<Neo, Glow> { public ColorScale<? super Neo, ?> scale() { return new Glare(); } public double degree() { return 1; } }
                static SemanticColor<? super Neo> neoPicks() { return new Accent(); }
                static SemanticColor<? super Neo> neoPicksItsOwn() { return new Flare(); }
                static SemanticColor<? super Base> basePicks() { return new Warning(); }
                """);
        assertTrue(errors.isEmpty(), "a scheme's branches and leaves under the base's, every kind, a scheme's style naming the base's colours: " + describe(errors));
    }

    @Test
    void theBasesBranchUnderASchemesBranch_doesNotCompile() {
        refusedOnItsLine("""
                record Lost() implements L2_ColorBranch<Base, Glow> {} // refused here
                """);
    }

    @Test
    void theBasesLeafUnderASchemesBranch_doesNotCompile() {
        refusedOnItsLine("""
                record Stray() implements Palettable<Base, Glow> {} // refused here
                """);
    }

    @Test
    void aLeafAtTheTopOfTheTree_doesNotCompile() {
        refusedOnItsLine("""
                record Orphan() implements Palettable<Base, ColorBranch.Top> {} // refused here
                """);
    }

    @Test
    void aBranchAtTheWrongLevel_doesNotCompile() {
        refusedOnItsLine("""
                record Skip() implements L3_ColorBranch<Base, Attention> {} // refused here
                """);
    }

    @Test
    void aBranchAtTwoLevels_doesNotCompile() {
        refusedOnItsLine("""
                record Twice() implements L1_ColorBranch<Base>, L2_ColorBranch<Base, Feedback> {} // refused here
                """);
    }

    @Test
    void aLeafOfTwoKinds_doesNotCompile() {
        refusedOnItsLine("""
                record Both() implements Palettable<Base, Feedback>, Fixed<Base, Feedback> { public ConcreteColor color() { return null; } } // refused here
                """);
    }

    @Test
    void aLeafUnderTwoBranches_doesNotCompile() {
        refusedOnItsLine("""
                record Torn() implements Palettable<Base, Feedback>, ColorScale<Base, Attention> { public List<ColorScale.Knot<Base>> knots() { return List.of(); } } // refused here
                """);
    }

    @Test
    void aLeafInTwoSchemes_doesNotCompile() {
        refusedOnItsLine("""
                record Torn() implements Palettable<Base, Feedback>, Interpolated<Neo, Feedback> { public ColorScale<? super Neo, ?> scale() { return null; } public double degree() { return 0; } } // refused here
                """);
    }

    @Test
    void aBranchThatIsAlsoALeaf_doesNotCompile() {
        refusedOnItsLine("""
                record Hybrid() implements L1_ColorBranch<Base>, Palettable<Base, Feedback> {} // refused here
                """);
    }

    @Test
    void aFourthKindOfLeaf_doesNotCompile() {
        refusedOnItsLine("""
                record Odd() implements ColorLeaf<Base, Feedback, LeafKind.PalettableKind> {} // refused here
                """);
    }

    @Test
    void aColourFromAScaleOfASchemeAbove_doesNotCompile() {
        refusedOnItsLine("""
                record Up() implements Interpolated<Base, Feedback> { public ColorScale<? super Base, ?> scale() { return new Glare(); } public double degree() { return 0; } } // refused here
                """);
    }

    @Test
    void aKnotOfAColourOfASchemeAbove_doesNotCompile() {
        refusedOnItsLine("""
                record Peek() implements ColorScale<Base, Feedback> { public List<ColorScale.Knot<Base>> knots() { return List.of(new ColorScale.Knot<>(new Calm(), -1), new ColorScale.Knot<>(new Spark(), 0), new ColorScale.Knot<>(new Danger(), 1)); } } // refused here
                """);
    }

    @Test
    void aKnotOfAnInterpolatedColour_doesNotCompile() {
        refusedOnItsLine("""
                record Nested() implements ColorScale<Base, Feedback> { public List<ColorScale.Knot<Base>> knots() { return List.of(new ColorScale.Knot<>(new Calm(), -1), new ColorScale.Knot<>(new Warning(), 0), new ColorScale.Knot<>(new Danger(), 1)); } } // refused here
                """);
    }

    @Test
    void aKnotOfAScale_doesNotCompile() {
        refusedOnItsLine("""
                record Nested() implements ColorScale<Base, Feedback> { public List<ColorScale.Knot<Base>> knots() { return List.of(new ColorScale.Knot<>(new Calm(), -1), new ColorScale.Knot<>(new Risk(), 0), new ColorScale.Knot<>(new Danger(), 1)); } } // refused here
                """);
    }

    @Test
    void theBasesStyleNamingASchemesColour_doesNotCompile() {
        refusedOnItsLine("""
                static SemanticColor<? super Base> picks() { return new Spark(); } // refused here
                """);
    }

    @Test
    void aStyleNamingAScaleForAColour_doesNotCompile() {
        refusedOnItsLine("""
                static SemanticColor<? super Base> picks() { return new Risk(); } // refused here
                """);
    }

    // ── what a colour target is painted with ──────────────────────────────

    @Test
    void everyCaseOnATargetThatOffersIt_compiles() {
        var errors = compile("""
                static final List<Paint<Base, Color.Text>> INK = List.of(new Paint.With<>(new Accent()), new Paint.Inherited<>(), new Paint.Along<>(new Risk()));
                static final List<Paint<Base, Color.Fill>> FILL = List.of(new Paint.With<>(new MineRed()), new Paint.Inherited<>(), new Paint.Nothing<>(), new Paint.Typography<>());
                static final List<Paint<Base, Color.Stroke>> STROKE = List.of(new Paint.With<>(new Warning()), new Paint.Inherited<>(), new Paint.Nothing<>());
                static final List<Paint<Base, Color.Scrollbar>> SCROLLBAR = List.of(new Paint.ThumbAndTrack<>(new Calm(), new Danger()), new Paint.Inherited<>());
                static final List<Paint<Base, Color.Background>> BACKGROUND = List.of(new Paint.Ground<>(new Calm()), new Paint.Along<>(new Risk()),
                        new Paint.Drawn<>(new Calm(), new Accent(), new Danger(), new Calm(), new MineRed()));
                static final List<Paint<Base, Color.Border>> BORDER = List.of(new Paint.With<>(new Accent()), new Paint.Typography<>(),
                        new Paint.Sides<>(new Accent(), new Calm(), new Calm(), new Accent()));
                static final List<Paint<Base, Color.Outline>> OUTLINE = List.of(new Paint.With<>(new Accent()), new Paint.Typography<>());
                static final List<Paint<Base, Color.ColumnRule>> RULE = List.of(new Paint.With<>(new Accent()), new Paint.Typography<>());
                static final List<Paint<Base, Color.BoxShadow>> SHADOW = List.of(new Paint.Shadow<>(new Danger(), new Calm(), new Accent()), new Paint.Cast<>(new Danger()));
                static final List<Paint<Neo, Color.Text>> NEO = List.of(new Paint.With<>(new Accent()), new Paint.With<>(new Spark()), new Paint.Along<>(new Glare()));
                """);
        assertTrue(errors.isEmpty(), "every case on a target that offers it, a scheme's paint naming the base's colours and its own: " + describe(errors));
    }

    @Test
    void oneColourOnABackground_doesNotCompile() {
        refusedOnItsLine("""
                static final Paint<Base, Color.Background> P = new Paint.With<>(new Calm()); // refused here
                """);
    }

    @Test
    void anInheritedBorder_doesNotCompile() {
        refusedOnItsLine("""
                static final Paint<Base, Color.Border> P = new Paint.Inherited<>(); // refused here
                """);
    }

    @Test
    void anInkOfNothing_doesNotCompile() {
        refusedOnItsLine("""
                static final Paint<Base, Color.Text> P = new Paint.Nothing<>(); // refused here
                """);
    }

    @Test
    void aStrokeInTheTypographyColour_doesNotCompile() {
        refusedOnItsLine("""
                static final Paint<Base, Color.Stroke> P = new Paint.Typography<>(); // refused here
                """);
    }

    @Test
    void aScaleInAShadow_doesNotCompile() {
        refusedOnItsLine("""
                static final Paint<Base, Color.BoxShadow> P = new Paint.Along<>(new Risk()); // refused here
                """);
    }

    @Test
    void theBasesPaintNamingASchemesColour_doesNotCompile() {
        refusedOnItsLine("""
                static final Paint<Base, Color.Text> P = new Paint.With<>(new Spark()); // refused here
                """);
    }

    @Test
    void theBasesPaintAlongASchemesScale_doesNotCompile() {
        refusedOnItsLine("""
                static final Paint<Base, Color.Text> P = new Paint.Along<>(new Glare()); // refused here
                """);
    }

    @Test
    void aPaintNamingAScale_doesNotCompile() {
        refusedOnItsLine("""
                static final Paint<Base, Color.Text> P = new Paint.With<>(new Risk()); // refused here
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

    /** This module's classes. */
    private static String classpath() {
        try { return Path.of(ColorNode.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString(); }
        catch (URISyntaxException e) { throw new IllegalStateException(e); }
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
