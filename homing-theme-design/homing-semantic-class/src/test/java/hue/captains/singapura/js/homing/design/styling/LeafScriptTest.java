package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Every leaf described for the page, as the element wears it: generated from Java, a frozen object by token - each
 * leaf's class, and each application state it lists with every value, the rest's class null, in its precedence; no
 * pseudo state. Read back by the browser's own engine.
 */
class LeafScriptTest extends JsModuleTestBase {

    private static final Taxonomy TAXONOMY = StatedTaxonomy.INSTANCE.read();

    @Test
    void everyLeaf_itsClass_andItsApplicationStates_withEveryValue() {
        String script = LeafClasses.INSTANCE.script(TAXONOMY);
        assertTrue(script.contains("    \"tab\": Object.freeze({ token: \"tab\", styled: \"styled-tab\", states: Object.freeze({ "
                + "\"enabled\": Object.freeze({ \"true\": null, \"false\": \"styled-tab--disabled\" }), "
                + "\"selected\": Object.freeze({ \"false\": null, \"true\": \"styled-tab--selected\" }), "
                + "\"keys\": Object.freeze({ \"away\": null, \"candidate\": \"styled-tab--candidate\", \"held\": \"styled-tab--held\" }) }) })"),
                script);
        js = buildContext();
        Value leaves = js.eval("js", "(" + script + ")");
        assertEquals(List.of("tab", "strip", "strip-lead"), List.copyOf(leaves.getMemberKeys()), "components, then parts");
        assertEquals("styled-strip-lead--selected", leaves.getMember("strip-lead").getMember("states").getMember("selected").getMember("true").asString(),
                "a part's states its own");
        assertEquals(List.of("enabled", "selected", "keys"), List.copyOf(leaves.getMember("tab").getMember("states").getMemberKeys()),
                "the hover is the browser's, drawn by its pseudo-class; the rest in the tab's precedence");
        assertTrue(js.eval("js", "Object.isFrozen((" + script + ")['tab'].states.keys)").asBoolean());
    }
}
