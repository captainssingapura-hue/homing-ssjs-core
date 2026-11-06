package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.design.semantic.BaseColours;
import hue.captains.singapura.js.homing.design.styles.PaletteDeclaration;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Ink;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What is made from a styling, handed the styling by a renderer built with it: an empty module and an empty sheet ask
 * nothing of anything else, so two stylings live side by side in one process.
 */
class StylingRendererTest {

    private static final Styling PLAIN_FIRST = styling(List.of(Plain.INSTANCE, Ink.INSTANCE));
    private static final Styling INK_FIRST = styling(List.of(Ink.INSTANCE, Plain.INSTANCE));

    private static Styling styling(List<hue.captains.singapura.js.homing.design.semantic.DesignStyle<?>> styles) {
        return new Styling(StyledManagers.INSTANCE.plainAndInk(), styles,
                new PaletteRegistry(List.<PaletteDeclaration>of(() -> List.of(BaseColours.Paper.INSTANCE, BaseColours.Night.INSTANCE)), BaseColours.SCHEMES),
                Map.of(Plain.INSTANCE, BaseColours.Paper.INSTANCE, Ink.INSTANCE, BaseColours.Night.INSTANCE));
    }

    /** Says the styling's default style. */
    record Tokens() implements EsModule<Tokens>, StyledModule {
        static final Tokens INSTANCE = new Tokens();
        @Override public ImportsFor<Tokens> imports() { return new ImportsFor<>(); }
        @Override public ExportsOf<Tokens> exports() { return new ExportsOf<>(INSTANCE, List.of()); }
        @Override public List<String> selfContent(Styling styling) { return List.of(styling.defaultStyle().token()); }
    }

    record Plainly() implements EsModule<Plainly> {
        @Override public ImportsFor<Plainly> imports() { return new ImportsFor<>(); }
        @Override public ExportsOf<Plainly> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record Sheet() implements DerivedCss<Sheet>, StyledSheet {
        static final Sheet INSTANCE = new Sheet();
        @Override public VariesWith variesWith() { return VariesWith.STYLE; }
        @Override public String css(Styling styling, String style) { return styling.styleSheet(style); }
        @Override public String fallback(Styling styling) { return styling.defaultStyle().token(); }
    }

    record Other() implements DerivedCss<Other> {}

    @Test
    void aModule_isHandedTheStyling() {
        assertEquals(Optional.of(List.of(PLAIN_FIRST.defaultStyle().token())), new StylingRenderer(PLAIN_FIRST).script(Tokens.INSTANCE));
    }

    @Test
    void aModuleMadeOfNoStyling_isNotItsToMake() {
        assertEquals(Optional.empty(), new StylingRenderer(PLAIN_FIRST).script(new Plainly()));
    }

    @Test
    void aSheet_isHandedTheStyling_andOwned() {
        var renderer = new StylingRenderer(PLAIN_FIRST);
        String ink = Ink.INSTANCE.token();
        assertTrue(renderer.owns(Sheet.INSTANCE));
        assertEquals(Optional.of(PLAIN_FIRST.styleSheet(ink)), renderer.render(Sheet.INSTANCE, ink));
        assertEquals(Optional.of(PLAIN_FIRST.defaultStyle().token()), renderer.fallback(Sheet.INSTANCE));
    }

    @Test
    void aSheetOfNoStyling_isNotItsToRender() {
        var renderer = new StylingRenderer(PLAIN_FIRST);
        assertFalse(renderer.owns(new Other()));
        assertEquals(Optional.empty(), renderer.render(new Other(), "any"));
        assertEquals(Optional.empty(), renderer.fallback(new Other()));
    }

    @Test
    void aSheet_isMadeOnceUnderEachSlug() {
        var renderer = new StylingRenderer(PLAIN_FIRST);
        String token = Plain.INSTANCE.token();
        assertSame(renderer.render(Sheet.INSTANCE, token).orElseThrow(), renderer.render(Sheet.INSTANCE, token).orElseThrow());
    }

    @Test
    void aSlugTheStylingDoesNotServe_isRefused() {
        var renderer = new StylingRenderer(PLAIN_FIRST);
        assertThrows(IllegalArgumentException.class, () -> renderer.render(Sheet.INSTANCE, "baroque"));
    }

    @Test
    void twoStylings_liveSideBySide() {
        var plainFirst = new StylingRenderer(PLAIN_FIRST);
        var inkFirst = new StylingRenderer(INK_FIRST);
        assertEquals(Optional.of(Plain.INSTANCE.token()), plainFirst.fallback(Sheet.INSTANCE));
        assertEquals(Optional.of(Ink.INSTANCE.token()), inkFirst.fallback(Sheet.INSTANCE));
        assertEquals(Optional.of(List.of(Plain.INSTANCE.token())), plainFirst.script(Tokens.INSTANCE));
        assertEquals(Optional.of(List.of(Ink.INSTANCE.token())), inkFirst.script(Tokens.INSTANCE));
    }
}
