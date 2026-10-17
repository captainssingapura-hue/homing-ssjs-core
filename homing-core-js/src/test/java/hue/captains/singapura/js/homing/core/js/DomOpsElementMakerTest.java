package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A branch's element maker: what an owner hands a simple component, so the component makes its own
 * element and the owner never holds a bare one. Single-use; the element the branch's, under the
 * owner's name; and told apart from any other function.
 */
class DomOpsElementMakerTest extends JsModuleTestBase {

    private static final String BASE  = "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyBaseModule.js";
    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyModule.js";

    private static final String DOM = """
        globalThis.document = {
            createElement: function (tag) { return { tagName: String(tag).toUpperCase(), remove: function () {} }; }
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
                var branch = domOpsParty.createBranch('page');
                branch.activate(owner);
                function fails(f) { try { f(); return null; } catch (e) { return e.message; } }
                """);
    }

    private String str(String expr) { return js.eval("js", expr).asString(); }
    private boolean yes(String expr) { return js.eval("js", expr).asBoolean(); }

    @Test
    void aMaker_makesItsElementOnceOnItsBranch_underTheOwnersName() {
        js.eval("js", "var make = branch.elementMaker('go'); var el = make('button');");
        assertEquals("BUTTON", str("el.tagName"), "the tag is the maker's caller's: the component's own");
        assertTrue(yes("branch.getElement('go') === el"), "the branch's, under the owner's name");
        assertTrue(str("fails(() => make('button'))").contains("spent"), "single-use");
    }

    @Test
    void aMaker_isToldApartFromAnyOtherFunction() {
        assertTrue(yes("DomOpsParty.isElementMaker(branch.elementMaker('a'))"));
        assertTrue(yes("StationedDomOpsParty.isElementMaker(branch.elementMaker('b'))"), "asked of any of the party's classes");
        assertFalse(yes("DomOpsParty.isElementMaker(function (tag) { return document.createElement(tag); })"),
                "a function made elsewhere is no maker, whatever it returns");
        assertFalse(yes("DomOpsParty.isElementMaker(null)"));
        assertTrue(yes("Object.isFrozen(branch.elementMaker('c'))"));
    }

    @Test
    void aNameInUse_orBadlyFormed_isRefused_whenTheMakerIsAskedFor() {
        js.eval("js", "branch.createElement('taken', 'div');");
        assertTrue(str("fails(() => branch.elementMaker('taken'))").contains("already in use"));
        assertTrue(str("fails(() => branch.elementMaker('a b'))").contains("letters, digits"));
        assertTrue(str("fails(() => domOpsParty.createBranch('idle').elementMaker('x'))").contains("activated"), "an idle branch issues none");
    }
}
