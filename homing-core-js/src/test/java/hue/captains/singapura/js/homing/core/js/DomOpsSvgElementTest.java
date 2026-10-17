package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An SVG element minted on a branch: the twin of createElement in SVG's namespace - owned, named and refused as an
 * HTML element is, so a component that draws a line of its own still makes nothing outside the party.
 */
class DomOpsSvgElementTest extends JsModuleTestBase {

    private static final String BASE  = "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyBaseModule.js";
    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyModule.js";

    private static final String DOM = """
        globalThis.document = {
            createElement: function (tag) { return { tagName: String(tag).toUpperCase(), namespaceURI: 'html', remove: function () { this.removed = true; } }; },
            createElementNS: function (ns, tag) { return { tagName: String(tag), namespaceURI: ns, remove: function () { this.removed = true; } }; }
        };
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", DOM, "dom.js").buildLiteral());
        loadModule(BASE);
        loadModule(PARTY);
        js.eval("js", """
                var owner = { toString: function () { return 'owner'; } };
                var branch = domOpsParty.createBranch('drawing');
                branch.activate(owner);
                function fails(f) { try { f(); return null; } catch (e) { return e.message; } }
                """);
    }

    private String str(String expr) { return js.eval("js", expr).asString(); }
    private boolean yes(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void anSvgElement_isMintedInSvgsNamespace_andIsTheBranchs() {
        js.eval("js", "var line = branch.createSvgElement('line', 'rect');");
        assertEquals("http://www.w3.org/2000/svg", str("line.namespaceURI"));
        assertEquals("rect", str("line.tagName"));
        assertTrue(yes("branch.getElement('line') === line"), "registered under its name, as an HTML element is");
        js.eval("js", "branch.dissolve();");
        assertTrue(yes("line.removed === true"), "and released with the branch");
    }

    @Test
    void aNameInUse_orBadlyFormed_orAnIdleBranch_isRefused_asForAnHtmlElement() {
        js.eval("js", "branch.createElement('taken', 'div');");
        assertTrue(str("fails(() => branch.createSvgElement('taken', 'svg'))").contains("createSvgElement: name \"taken\" is already in use"));
        assertTrue(str("fails(() => branch.createSvgElement('a b', 'svg'))").contains("invalid characters"));
        assertTrue(str("fails(() => branch.createSvgElement('ok', ''))").contains("tagName must be a non-empty string"));
        assertTrue(str("fails(() => domOpsParty.createBranch('idle').createSvgElement('x', 'svg'))").contains("activated"));
        assertTrue(str("fails(() => branch.createElement('taken', 'div'))").contains("createElement: name \"taken\" is already in use"),
                "createElement says its own name, as before");
    }
}
