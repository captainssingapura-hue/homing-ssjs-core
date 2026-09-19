package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.conformance.rules.CrateDependencyRule;
import hue.captains.singapura.js.homing.conformance.rules.OrphanCheck;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.PartialModulePath;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The tree levels and addresses its nodes; the registry stamps modules, exports and params; the crate holds. */
class PreferencesRegistryTest {

    static final ModuleNameResolver RESOLVER = (EsModule<?> m) ->
            new PartialModulePath("/module?class=" + m.getClass().getCanonicalName(), true);

    static PreferenceTree tree() {
        return PreferenceTree.of(PreferenceNode.of("preferences", "Preferences", "All of them",
                WidgetProvider.of(OverviewWidget.INSTANCE, Map.of("label", "Preferences", "settings", List.of())),
                PreferenceNode.of("theme", "Theme", "", WidgetProvider.of(ChoiceWidget.INSTANCE,
                        Map.of("name", "theme", "options", List.of(Map.of("value", "a", "label", "A"))))),
                PreferenceNode.of("editor", "Editor", "How text is shown", WidgetProvider.of(OverviewWidget.INSTANCE),
                        PreferenceNode.of("font-size", "Font size", "", WidgetProvider.of(ScaleWidget.INSTANCE,
                                Map.of("name", "editor/font-size", "min", 12, "max", 24, "default", 16))),
                        PreferenceNode.of("wrap", "Wrap lines", "", WidgetProvider.of(ToggleWidget.INSTANCE,
                                Map.of("name", "editor/wrap", "default", true))))));
    }

    @Test
    void levelsByDepthAndAddressesByPath() {
        var t = tree();
        assertEquals(List.of("preferences", "preferences/theme", "preferences/editor",
                             "preferences/editor/font-size", "preferences/editor/wrap"), t.paths());
        assertEquals(TreeLevel.L0.INSTANCE, t.root().level());
        assertEquals(TreeLevel.L2.INSTANCE, t.at("preferences/editor/wrap").level());
        assertEquals(List.of("preferences/editor/font-size", "preferences/editor/wrap"), t.below("preferences/editor"));
        assertEquals("Wrap lines", t.at("preferences/editor/wrap").label());
    }

    @Test
    void refusesTwoSiblingsWithOneSegment() {
        var ex = assertThrows(IllegalArgumentException.class, () -> PreferenceTree.of(
                PreferenceNode.of("p", "P", "", WidgetProvider.of(OverviewWidget.INSTANCE),
                        PreferenceNode.of("x", "X1", "", WidgetProvider.of(ToggleWidget.INSTANCE)),
                        PreferenceNode.of("x", "X2", "", WidgetProvider.of(ToggleWidget.INSTANCE)))));
        assertTrue(ex.getMessage().contains("share the segment 'x'"), ex.getMessage());
    }

    @Test
    void stampsEveryNodeWithItsWidgetModuleExportAndParams() {
        var reg = new PreferencesRegistry(tree(), WidgetProvider.of(ChoiceWidget.INSTANCE, Map.of("folder", true))) {};
        String json = reg.json(RESOLVER);
        // the rigid tree, canonical
        assertTrue(json.startsWith("{\"tree\":{\"level\":\"L0\",\"segment\":\"preferences\""), json.substring(0, 80));
        assertTrue(json.contains("{\"key\":\"displayLabel\",\"valueTag\":\"name\",\"text\":\"Font size\"}"), json);
        // the master: the provider's params plus the tree and the labels
        assertTrue(json.contains("\"master\":{\"module\":\"/module?class=hue.captains.singapura.js.homing.preferences.ChoiceWidget\",\"export\":\"construct\",\"params\":{\"folder\":true,\"tree\":{"), json);
        assertTrue(json.contains("\"labels\":{\"preferences\":\"Preferences\",\"preferences/theme\":\"Theme\""), json);
        // a node: label, summary, widget with typed params
        assertTrue(json.contains("\"preferences/editor/font-size\":{\"label\":\"Font size\",\"summary\":\"\",\"widget\":{\"module\":\"/module?class=hue.captains.singapura.js.homing.preferences.ScaleWidget\",\"export\":\"construct\",\"params\":{"), json);
        assertTrue(json.contains("\"min\":12") && json.contains("\"default\":true"), json);
        // the module itself defines the constant
        var lines = reg.selfContent(RESOLVER);
        assertTrue(lines.get(lines.size() - 1).startsWith("const PREFERENCES = Object.freeze({"), lines.toString());
    }

    @Test
    void theCrateHoldsAndImportsOnlyWhatItRequires() {
        assertEquals(List.of(), OrphanCheck.check(UiPreferencesCrate.INSTANCE));
        assertEquals(List.of(), CrateDependencyRule.check(UiPreferencesCrate.INSTANCE));
    }

    @Test
    void jsonEscapesWhatWouldCloseAScript() {
        assertEquals("\"a\\u003c/script>\\\"b\\\"\"", Json.write("a</script>\"b\""));
        var ordered = new java.util.LinkedHashMap<String, Object>();
        ordered.put("n", 1); ordered.put("t", true); ordered.put("l", java.util.Arrays.asList("x", null));
        assertEquals("{\"n\":1,\"t\":true,\"l\":[\"x\",null]}", Json.write(ordered));
    }
}
