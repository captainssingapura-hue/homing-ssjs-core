package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Paper;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Bold;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Broken;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Half;
import hue.captains.singapura.js.homing.design.semantic.FramingStyles.Shouted;
import hue.captains.singapura.js.homing.design.semantic.FramingTaxonomy.Edge;
import hue.captains.singapura.js.homing.design.semantic.FramingTaxonomy.Mark;
import hue.captains.singapura.js.homing.design.semantic.LocalStyleProblem.Rule;
import hue.captains.singapura.js.homing.design.styling.BaseModule;
import hue.captains.singapura.js.homing.design.styling.PaletteRegistry;
import hue.captains.singapura.js.homing.design.styling.StyleBundle;
import hue.captains.singapura.js.homing.design.styling.StyleManager;
import hue.captains.singapura.js.homing.design.styling.StyleRegistryBuilder;
import hue.captains.singapura.js.homing.design.styling.StyledManagers;
import hue.captains.singapura.js.homing.design.styling.StyledStyles;
import hue.captains.singapura.js.homing.design.styling.Styling;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Local styles registered and served (RFC 0067, appendix Local Styles): a base module brings a local-style branch's
 * leaves and registers its local styles with them; the manager holds them, refusing one that cannot be read over the
 * components; the styling draws each in every style it serves, refusing one that cannot be, and serves for each its
 * sheet in a style - the same in every palette - and its object. Nothing is found on a classpath.
 */
class LocalStylesServedTest {

    /** The frame's leaves, as a jar lists them. */
    record FramingComponents() implements ComponentModule {
        static final FramingComponents INSTANCE = new FramingComponents();
        @Override public List<? extends Component<?>> components() { return List.of(Edge.INSTANCE, Mark.INSTANCE); }
    }

    /** The frame in the base, with its local styles: its leaves are never cast, so it bundles no look. */
    record FramingBase() implements BaseModule<BaseScheme, StyledStyles.Plain> {
        static final FramingBase INSTANCE = new FramingBase();
        @Override public StyledStyles.Plain style() { return StyledStyles.Plain.INSTANCE; }
        @Override public List<StyleBundle<BaseScheme, StyledStyles.Plain>> bundles() { return List.of(); }
        @Override public ComponentModule components() { return FramingComponents.INSTANCE; }
        @Override public List<? extends LocalStyle<?, ?>> localStyles() { return List.of(FramingStyles.Plain.INSTANCE, Bold.INSTANCE); }
    }

    /** The frame, with a local style that misses a leaf. */
    record HalfBase() implements BaseModule<BaseScheme, StyledStyles.Plain> {
        @Override public StyledStyles.Plain style() { return StyledStyles.Plain.INSTANCE; }
        @Override public List<StyleBundle<BaseScheme, StyledStyles.Plain>> bundles() { return List.of(); }
        @Override public ComponentModule components() { return FramingComponents.INSTANCE; }
        @Override public List<? extends LocalStyle<?, ?>> localStyles() { return List.of(Half.INSTANCE); }
    }

    /** The frame, with a local style whose drawings cannot be made. */
    record BrokenBase() implements BaseModule<BaseScheme, StyledStyles.Plain> {
        @Override public StyledStyles.Plain style() { return StyledStyles.Plain.INSTANCE; }
        @Override public List<StyleBundle<BaseScheme, StyledStyles.Plain>> bundles() { return List.of(); }
        @Override public ComponentModule components() { return FramingComponents.INSTANCE; }
        @Override public List<? extends LocalStyle<?, ?>> localStyles() { return List.of(Broken.INSTANCE); }
    }

    private static final PaletteRegistry REGISTRY = new PaletteRegistry(List.of(BaseColours.Palettes.INSTANCE), BaseColours.SCHEMES);

    @Test
    void aBaseModule_registersItsLocalStyles_andTheStylingServesEachSheetAndObject() {
        StyleManager manager = manager(FramingBase.INSTANCE);
        assertEquals(List.of(FramingStyles.Plain.INSTANCE, Bold.INSTANCE), manager.localStyles(), "in the order registered");
        Styling styling = styling(manager);
        assertEquals(List.of("plain", "bold"), styling.localStyles().stream().map(LocalStyleRead::token).toList());
        String sheet = styling.localStyleSheet(Bold.INSTANCE, "plain");
        assertTrue(sheet.contains(".bold___edge__color-border {\n    border-color: var(--fp-copy);\n}\n"),
                "its palettable colours the palette's variables, defined by the palette's own sheet:\n" + sheet);
        assertTrue(styling.paletteSheet("paper").contains("--fp-copy:"), "which the palette's sheet defines");
        assertEquals("no style served is ink", assertThrows(IllegalArgumentException.class, () -> styling.localStyleSheet(Bold.INSTANCE, "ink")).getMessage());
        assertTrue(assertThrows(IllegalArgumentException.class, () -> styling.localStyle(Shouted.INSTANCE)).getMessage()
                .contains("shouted is registered by no base module"));
        var object = styling.localStyleObject(FramingStyles.Plain.INSTANCE, LocalStylesTest.PlainSheet.INSTANCE);
        assertTrue(object.get(1).startsWith("void plain_sheet;"), object.get(1));
    }

    @Test
    void aLocalStyleThatCannotBeReadOverTheComponents_refusesTheStyles() {
        String refused = assertThrows(IllegalArgumentException.class, () -> manager(new HalfBase())).getMessage();
        assertTrue(refused.contains("LEAF_MISSING: half draws no mark, a leaf of frame"), refused);
    }

    @Test
    void aLocalStyleNotDrawnInAStyleServed_refusesTheStyling() {
        StyleManager manager = manager(new BrokenBase());
        var e = assertThrows(RefusedLocalStyles.class, () -> styling(manager));
        assertEquals(List.of(Rule.DRAWING_REFUSED), e.problems().stream().map(LocalStyleProblem::rule).toList(), e.getMessage());
    }

    /** The tests' base modules and this one: plain the base, nothing over it. */
    private static StyleManager manager(BaseModule<BaseScheme, StyledStyles.Plain> framing) {
        var base = new ArrayList<>(StyledManagers.INSTANCE.base());
        base.add(framing);
        return new StyleRegistryBuilder<>(base).build();
    }

    private static Styling styling(StyleManager manager) {
        return new Styling(manager, List.of(StyledStyles.Plain.INSTANCE), REGISTRY, Map.of(StyledStyles.Plain.INSTANCE, Paper.INSTANCE));
    }
}
