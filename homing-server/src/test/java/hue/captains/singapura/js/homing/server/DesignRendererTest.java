package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.DesignRenderer;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.MadeByDesign;
import hue.captains.singapura.js.homing.core.ModuleForm;
import hue.captains.singapura.js.homing.core.ModuleForms;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * What a site's design makes reaches the server as an object the site built: a module stays an empty value, its script
 * the design's; a sheet's slug and body are the design's; and two servers in one process, given two designs, each
 * serve their own.
 */
class DesignRendererTest {

    record Made() implements EsModule<Made>, MadeByDesign {
        static final Made INSTANCE = new Made();
        record MADE() implements Exportable._Constant<Made> {}
        @Override public ImportsFor<Made> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<Made> exports() { return new ExportsOf<>(INSTANCE, List.of(new MADE())); }
    }

    record Sheet() implements DerivedCss<Sheet> {
        static final Sheet INSTANCE = new Sheet();
        @Override public VariesWith variesWith() { return VariesWith.STYLE; }
    }

    /** A design of one word: the script, the sheet and the fallback all say it. */
    record Design(String word) implements DesignRenderer {
        @Override public Optional<List<String>> script(EsModule<?> module) {
            return module instanceof Made ? Optional.of(List.of("const MADE = \"" + word + "\";")) : Optional.empty();
        }
        @Override public Optional<String> render(DerivedCss<?> sheet, String slug) {
            return sheet instanceof Sheet ? Optional.of("/* " + word + " as " + slug + " */") : Optional.empty();
        }
        @Override public boolean owns(DerivedCss<?> sheet) { return sheet instanceof Sheet; }
        @Override public Optional<String> fallback(DerivedCss<?> sheet) { return sheet instanceof Sheet ? Optional.of(word) : Optional.empty(); }
    }

    record Served() implements Crate {
        @Override public String name() { return "served"; }
        @Override public List<CrateEntry> entries() { return List.of(new CrateEntry(Made.INSTANCE), new CrateEntry(Sheet.INSTANCE)); }
    }

    private static HomingActionRegistry site(String word) {
        return new HomingActionRegistry(new QueryParamResolver("/module"), null, ResourceReader.CLASSPATH, ThemeRegistry.EMPTY,
                AppMeta.DEFAULT, List.of(new Served()), new Design(word));
    }

    private static String module(HomingActionRegistry site) {
        return ((EsModuleGetAction) site.getActions().get("/module")).render(Made.INSTANCE);
    }

    private static String sheet(HomingActionRegistry site, String style) throws Exception {
        return ((CssContentGetAction) site.getActions().get("/css-content"))
                .execute(new CssQuery(Sheet.class.getCanonicalName(), null, style, null), null).get().body();
    }

    @Test
    void aModuleMadeByDesign_isWhatTheDesignMakes() {
        assertTrue(module(site("ink")).contains("const MADE = \"ink\";"), module(site("ink")));
    }

    @Test
    void aServerGivenNoDesign_refusesIt_namingWhatIsMissing() {
        var refused = assertThrows(IllegalStateException.class,
                () -> new EsModuleGetAction(new QueryParamResolver("/module")).render(Made.INSTANCE));
        assertTrue(refused.getMessage().contains(Made.class.getName()) && refused.getMessage().contains("given none"), refused.getMessage());
    }

    @Test
    void itIsMadeInJava_likeASelfContentModule() {
        assertEquals(ModuleForm.SELF_CONTENT, ModuleForms.INSTANCE.of(Made.INSTANCE));
    }

    @Test
    void aSlugNamed_isTheDesignsToRender() throws Exception {
        assertEquals("/* ink as bold */", sheet(site("ink"), "bold"));
    }

    @Test
    void namingNothing_theSheetIsServedUnderWhatTheDesignSays() throws Exception {
        assertEquals("/* ink as ink */", sheet(site("ink"), null));
    }

    @Test
    void theSheetsModule_handsTheOwnerWhatTheDesignSays() {
        String js = ((EsModuleGetAction) site("ink").getActions().get("/module")).render(Sheet.INSTANCE);
        assertTrue(js.contains("await _themes.load([\"" + Sheet.class.getCanonicalName() + "\"], \"ink\", \"style\");"), js);
    }

    @Test
    void twoSitesInAProcess_eachServeTheirOwn() throws Exception {
        var ink = site("ink");
        var plain = site("plain");
        assertTrue(module(ink).contains("\"ink\""));
        assertTrue(module(plain).contains("\"plain\""));
        assertEquals("/* ink as ink */", sheet(ink, null));
        assertEquals("/* plain as plain */", sheet(plain, null));
    }
}
