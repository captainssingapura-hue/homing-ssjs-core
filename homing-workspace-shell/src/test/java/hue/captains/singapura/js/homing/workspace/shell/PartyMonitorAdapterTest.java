package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0063 — the adapter is pure, and its one shared fact is pinned.
 *
 * <p>{@code partySnapshotToTree} puts a branch's elements before its
 * sub-branches; {@code pathToBranch} offsets by the element count for the same
 * reason. If either side of that changes alone, the monitor selects the wrong
 * row as itself — quietly. The path test exists to make that loud.</p>
 */
class PartyMonitorAdapterTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/PartyMonitorAdapterModule.js";

    /** root → nav (2 el, alive) ; widgets → w-me (1 el) , w-dead (collected), never (unactivated). */
    private static final String FIXTURE = """
        const deepFreeze = o => { if (o && typeof o === "object") { Object.freeze(o); Object.values(o).forEach(deepFreeze); } return o; };
        globalThis.FIXTURE = deepFreeze({
            name: 'root', depth: 0, path: ['root'], owner: 'partyChief', ownerAlive: true,
            elements: [], branches: [
                { name: 'nav', depth: 1, path: ['root','nav'], owner: 'shell:nav', ownerAlive: true,
                  elements: [{ name: 'bar', tagName: 'nav' }, { name: 'logo', tagName: 'img' }], branches: [] },
                { name: 'widgets', depth: 1, path: ['root','widgets'], owner: 'shell:widgets', ownerAlive: true,
                  elements: [{ name: 'slot', tagName: 'div' }], branches: [
                    { name: 'w-me', depth: 2, path: ['root','widgets','w-me'], owner: 'widget:me', ownerAlive: true,
                      elements: [{ name: 'host', tagName: 'div' }], branches: [] },
                    { name: 'w-dead', depth: 2, path: ['root','widgets','w-dead'], owner: 'widget:dead', ownerAlive: false,
                      elements: [], branches: [] },
                    { name: 'never', depth: 2, path: ['root','widgets','never'], owner: null, ownerAlive: null,
                      elements: [], branches: [] }
                ] }
            ]
        });
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
        js.eval(Source.newBuilder("js", FIXTURE, "fixture.js").buildLiteral());
    }

    @Test
    void aBranchIsARowWithItsElementsThenItsSubBranchesAsChildren() {
        Value t = global("partySnapshotToTree").execute(global("FIXTURE"));
        assertEquals("root", t.getMember("display").getMember("label").asString());
        assertEquals(0, t.getMember("level").asInt());
        // root has no elements, two sub-branches
        assertEquals(2, t.getMember("children").getArraySize());

        Value nav = t.getMember("children").getArrayElement(0);
        assertEquals("nav", nav.getMember("display").getMember("label").asString());
        assertEquals(2, nav.getMember("children").getArraySize(), "two elements, no sub-branches");
        assertEquals("bar <nav>", nav.getMember("children").getArrayElement(0).getMember("display").getMember("label").asString());
        assertEquals("element", nav.getMember("children").getArrayElement(0).getMember("display").getMember("kind").asString());

        // widgets: 1 element FIRST, then 3 sub-branches
        Value widgets = t.getMember("children").getArrayElement(1);
        assertEquals(4, widgets.getMember("children").getArraySize());
        assertEquals("slot <div>", widgets.getMember("children").getArrayElement(0).getMember("display").getMember("label").asString());
        assertEquals("w-me",       widgets.getMember("children").getArrayElement(1).getMember("display").getMember("label").asString());
    }

    @Test
    void badgeCarriesDepthLivenessAndElementCountAndNoteCarriesTheOwner() {
        Value t = global("partySnapshotToTree").execute(global("FIXTURE"));
        Value nav = t.getMember("children").getArrayElement(0);
        assertEquals("L1 ● alive · 2 el", nav.getMember("display").getMember("badge").asString());
        assertEquals("shell:nav", nav.getMember("display").getMember("note").asString());
        assertEquals("branch", nav.getMember("display").getMember("kind").asString());

        Value widgets = t.getMember("children").getArrayElement(1);
        Value dead  = widgets.getMember("children").getArrayElement(2);
        Value never = widgets.getMember("children").getArrayElement(3);
        assertEquals("L2 ⚠ collected", dead.getMember("display").getMember("badge").asString());
        assertEquals("leaked", dead.getMember("display").getMember("kind").asString());
        assertEquals("L2", never.getMember("display").getMember("badge").asString(), "never activated: no liveness mark");
        assertTrue(never.getMember("display").getMember("note").isNull());
    }

    @Test
    void statsCountBranchesElementsAndCollectedOwners() {
        Value s = global("partySnapshotStats").execute(global("FIXTURE"));
        assertEquals(6, s.getMember("branches").asInt());
        assertEquals(4, s.getMember("elements").asInt());
        assertEquals(1, s.getMember("collected").asInt());
    }

    /** The shared fact: a sub-branch's index is offset by its parent's element count. */
    @Test
    void pathToBranchAgreesWithTheTreeOrdering() {
        Value f = global("FIXTURE");
        assertEquals("[]",      path(f, "root"));
        assertEquals("[0]",     path(f, "nav"));
        assertEquals("[1]",     path(f, "widgets"));
        // widgets has 1 element, so its first sub-branch is at index 1, not 0.
        assertEquals("[1,1]",   path(f, "w-me"));
        assertEquals("[1,2]",   path(f, "w-dead"));
        assertTrue(global("pathToBranch").execute(f, "nope").isNull());

        // And the path really lands on that row in the tree the adapter built.
        Value t = global("partySnapshotToTree").execute(f);
        Value row = t.getMember("children").getArrayElement(1).getMember("children").getArrayElement(1);
        assertEquals("w-me", row.getMember("display").getMember("label").asString());
    }

    /** A child-index path as "[1,1]" — Value.toString() would say "(2)[1,1]". */
    private String path(Value fixture, String name) {
        Value p = global("pathToBranch").execute(fixture, name);
        if (p.isNull()) return "null";
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < p.getArraySize(); i++) { if (i > 0) sb.append(','); sb.append(p.getArrayElement(i).asInt()); }
        return sb.append(']').toString();
    }

    @Test
    void theAdapterDoesNotTouchItsInput() {
        Value f = global("FIXTURE");
        global("partySnapshotToTree").execute(f);
        global("partySnapshotStats").execute(f);
        global("pathToBranch").execute(f, "w-me");
        assertTrue(js.eval("js", "Object.isFrozen(FIXTURE) && Object.isFrozen(FIXTURE.branches[1])").asBoolean());
    }
}
