package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.SelfRenderedCss;
import hue.captains.singapura.js.homing.core.Theme;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0067, appendix Two Classes per Leaf - a style's sheet and a palette's, each served by its own name and swapped
 * on its own: a style's sheet read under {@code ?style=}, a palette's under {@code ?palette=}, each else its own
 * fallback; the others on the address ignored; a slug the sheet does not know not found; and the module each loads by
 * handing the theme's owner what it varies with.
 */
class StyleAndPaletteSheetsTest {

    /** A deployment's style-aware sheet: per style, the plain one by default. */
    record Styled() implements DerivedCss<Styled>, SelfRenderedCss {
        static final Styled INSTANCE = new Styled();
        record style_sheet() implements Exportable._Constant<Styled> {}
        @Override public ExportsOf<Styled> exports() { return new ExportsOf<>(this, List.of(new style_sheet())); }
        @Override public VariesWith variesWith() { return VariesWith.STYLE; }
        @Override public Optional<String> fallback() { return Optional.of("plain"); }
        @Override public String css(String style) {
            if (!Set.of("plain", "ink").contains(style)) throw new IllegalArgumentException("no style " + style);
            return "/* " + style + "'s */";
        }
    }

    /** A deployment's palette sheet: per palette, paper by default. */
    record Painted() implements DerivedCss<Painted>, SelfRenderedCss {
        static final Painted INSTANCE = new Painted();
        @Override public VariesWith variesWith() { return VariesWith.PALETTE; }
        @Override public Optional<String> fallback() { return Optional.of("paper"); }
        @Override public String css(String palette) { return ":root { /* " + palette + " */ }"; }
    }

    record Fixture() implements Crate {
        @Override public String name() { return "fixture"; }
        @Override public List<CrateEntry> entries() { return List.of(CrateEntry.of(Styled.INSTANCE), CrateEntry.of(Painted.INSTANCE)); }
    }

    record Default() implements Theme {
        @Override public String slug() { return "default"; }
    }

    private final CssContentGetAction action = new CssContentGetAction(new Default(), ServedModules.of(List.of(new Fixture())),
            List.of(SelfRenderedCssRenderer.INSTANCE));

    private String served(Class<?> sheet, String theme, String style, String palette) throws Exception {
        return action.execute(new CssQuery(sheet.getCanonicalName(), theme, style, palette), null).get().body();
    }

    @Test
    void eachSheet_readsItsOwnName_andIgnoresTheOthers() throws Exception {
        assertEquals("/* ink's */", served(Styled.class, "forest", "ink", "night"));
        assertEquals(":root { /* night */ }", served(Painted.class, "forest", "ink", "night"));
    }

    @Test
    void namingNothing_eachSheetIsServedUnderItsOwnFallback_neverTheDefaultTheme() throws Exception {
        assertEquals("/* plain's */", served(Styled.class, null, null, null));
        assertEquals(":root { /* paper */ }", served(Painted.class, "default", null, null));
    }

    @Test
    void aSlugTheSheetDoesNotKnow_isNotFound() {
        var failed = assertThrows(ExecutionException.class, () -> served(Styled.class, null, "baroque", null));
        assertTrue(failed.getCause() instanceof ResourceNotFound, String.valueOf(failed.getCause()));
    }

    @Test
    void eachSheetsModule_handsTheOwnerWhatItVariesWith_andItsFallback() {
        var resolver = new QueryParamResolver("/module");
        assertEquals("await _themes.load([\"" + Styled.class.getCanonicalName() + "\"], \"plain\", \"style\");",
                new DerivedCssContentProvider<>(Styled.INSTANCE, "default", resolver).content().get(1));
        assertEquals("await _themes.load([\"" + Painted.class.getCanonicalName() + "\"], \"paper\", \"palette\");",
                new DerivedCssContentProvider<>(Painted.INSTANCE, "default", resolver).content().get(1));
    }
}
