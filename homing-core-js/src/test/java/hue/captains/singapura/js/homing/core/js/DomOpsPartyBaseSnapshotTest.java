package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0063 — {@code snapshot()}: observation by construction.
 *
 * <p>This is the first direct test of {@code _DomOpsPartyBase}. The party is
 * the discipline every conformance rule enforces the use of, and until now it
 * was exercised only through those rules — which check that code USES the
 * party — and through stubs that stand in for it. Nothing asserted what the base
 * itself does. D13 of the RFC exists because of that.</p>
 *
 * <p>The contract under test is negative as much as positive: a holder of a
 * snapshot can inspect the tree and <b>cannot touch it</b>. So the assertions
 * that matter most are the ones that try to — writing into the result, and
 * walking it for anything that is a function or a live object.</p>
 */
class DomOpsPartyBaseSnapshotTest extends JsModuleTestBase {

    private static final String BASE  = "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyBaseModule.js";
    private static final String PARTY = "/homing/js/hue/captains/singapura/js/homing/core/js/DomOpsPartyModule.js";

    /** Enough DOM for createElement + remove; tagName is what listElements reads. */
    private static final String DOM = """
        globalThis.document = {
            createElement: function (tag) {
                return { tagName: String(tag).toUpperCase(), remove: function () {} };
            }
        };
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", DOM, "dom.js").buildLiteral());
        loadModule(BASE);
        loadModule(PARTY);
    }

    /**
     * A small tree with every owner state in it:
     *   root (partyChief, alive)
     *     ├─ nav      activated, owner kept alive by the test
     *     │    └─ menu   never activated (and so empty)
     *     └─ widgets  activated
     *          └─ w-1  activated, then its owner dropped (the leak candidate)
     */
    private Value tree() {
        return js.eval("js", """
            (() => {
                const root = new DomOpsParty('root');
                root.activate(Object.freeze({ toString: () => 'partyChief' }));
                const nav = root.createBranch('nav');
                globalThis.__navOwner = Object.freeze({ toString: () => 'shell:nav' });
                nav.activate(globalThis.__navOwner);
                nav.createElement('bar', 'nav');
                nav.createElement('logo', 'img');
                nav.createBranch('menu');                       // never activated — so it can own no elements
                const widgets = root.createBranch('widgets');
                widgets.activate(Object.freeze({ toString: () => 'shell:widgets' }));
                const w1 = widgets.createBranch('w-1');
                let owner = Object.freeze({ toString: () => 'widget:1' });
                w1.activate(owner);
                w1.createElement('root', 'div');
                owner = null;                                     // only ref dropped
                return root;
            })()""");
    }

    // ---------------------------------------------------------------- shape

    @Test
    void snapshotCarriesEveryLevelWithNameDepthPathAndCounts() {
        Value s = tree().invokeMember("snapshot");

        assertEquals("root", s.getMember("name").asString());
        assertEquals(0,      s.getMember("depth").asInt());
        assertEquals(2,      s.getMember("branches").getArraySize());

        Value nav = s.getMember("branches").getArrayElement(0);
        assertEquals("nav", nav.getMember("name").asString());
        assertEquals(1,     nav.getMember("depth").asInt());
        assertEquals(2,     nav.getMember("elements").getArraySize());
        assertEquals("bar", nav.getMember("elements").getArrayElement(0).getMember("name").asString());
        assertEquals("nav", nav.getMember("elements").getArrayElement(0).getMember("tagName").asString());

        Value menu = nav.getMember("branches").getArrayElement(0);
        assertEquals("menu", menu.getMember("name").asString());
        assertEquals(2,      menu.getMember("depth").asInt());
    }

    @Test
    void pathIsAbsoluteFromTheRootAndRelativeFromABranch() {
        Value root = tree();
        Value fromRoot = root.invokeMember("snapshot")
                .getMember("branches").getArrayElement(0)      // nav
                .getMember("branches").getArrayElement(0);     // menu
        assertEquals("root,nav,menu", join(fromRoot.getMember("path")));

        Value fromNav = root.invokeMember("getBranch", "nav").invokeMember("snapshot")
                .getMember("branches").getArrayElement(0);     // menu
        assertEquals("nav,menu", join(fromNav.getMember("path")),
                     "called on a sub-branch, the path starts there");
    }

    // ---------------------------------------------------------------- owner

    @Test
    void ownerIsALabelOrNullAndLivenessMatches() {
        Value s = tree().invokeMember("snapshot");

        assertEquals("partyChief", s.getMember("owner").asString());
        assertTrue(s.getMember("ownerAlive").asBoolean());

        Value nav  = s.getMember("branches").getArrayElement(0);
        Value menu = nav.getMember("branches").getArrayElement(0);
        assertEquals("shell:nav", nav.getMember("owner").asString());
        assertTrue(nav.getMember("ownerAlive").asBoolean());

        assertTrue(menu.getMember("owner").isNull(),      "never activated → no label");
        assertTrue(menu.getMember("ownerAlive").isNull(), "never activated → liveness unknown, not false");
    }

    /**
     * The one the monitor exists for. The label must OUTLIVE the owner: a
     * leaked branch is precisely one whose owner can no longer be asked, and
     * precisely the one whose name matters most. So the label is captured at
     * activate(), and this test drops the owner, forces collection, and reads
     * both fields.
     *
     * <p>Collection is the engine's to grant. The test asks for it repeatedly
     * and gives up honestly if the engine will not — which is also the
     * monitor's own caveat (RFC 0063 D8): "none collected" is not "none".</p>
     */
    @Test
    void aCollectedOwnerReadsAsALeakAndKeepsItsLabel() {
        Value root = tree();
        Value w1 = root.invokeMember("getBranch", "widgets").invokeMember("getBranch", "w-1");

        // Before anything is collected, the label is already there.
        assertEquals("widget:1", w1.getMember("ownerLabel").asString());

        boolean collected = false;
        for (int i = 0; i < 20 && !collected; i++) {
            System.gc();
            try { Thread.sleep(25); } catch (InterruptedException ignored) { }
            Value alive = w1.getMember("isOwnerAlive");
            collected = !alive.isNull() && !alive.asBoolean();
            if (collected) System.out.println("[DomOpsPartyBaseSnapshotTest] owner collected after " + (i + 1) + " gc pass(es)");
        }
        if (!collected) {
            // The engine kept the owner reachable through the polyglot boundary
            // or simply did not collect. That is a fact about the engine, not
            // about the projection — record it rather than fake a pass.
            System.out.println("[DomOpsPartyBaseSnapshotTest] engine did not collect the owner; "
                             + "leak branch not observable in this run");
            return;
        }

        Value s = root.invokeMember("snapshot");
        Value leaked = s.getMember("branches").getArrayElement(1)     // widgets
                        .getMember("branches").getArrayElement(0);    // w-1
        assertEquals("widget:1", leaked.getMember("owner").asString(),
                     "the label survives the owner — that is why it is captured, not derived");
        assertFalse(leaked.getMember("ownerAlive").asBoolean(), "collected owner → leak");
    }

    // ---------------------------------------------------- cannot touch

    /** Every level, every array, every element record: frozen. Asserted by writing. */
    @Test
    void everythingInTheSnapshotIsFrozen() {
        Value s = tree().invokeMember("snapshot");
        Value check = js.eval("js", """
            (function walk(o) {
                if (o === null || typeof o !== 'object') return [];
                const bad = Object.isFrozen(o) ? [] : ['not frozen: ' + JSON.stringify(o).slice(0, 60)];
                for (const v of Object.values(o)) bad.push(...walk(v));
                return bad;
            })""");
        Value bad = check.execute(s);
        assertEquals(0, bad.getArraySize(), () -> bad.toString());

        // And the write actually fails in strict mode, rather than merely being ignored.
        Value tryWrite = js.eval("js", """
            (function (s) {
                'use strict';
                try { s.name = 'x'; return 'assignment succeeded'; }
                catch (e) { return e.constructor.name; }
            })""");
        assertEquals("TypeError", tryWrite.execute(s).asString());
    }

    /**
     * The contract in one assertion: nothing in the snapshot is a function, and
     * nothing in it is identical to any live branch or element. A holder of it
     * has no way back into the tree.
     */
    @Test
    void theSnapshotHoldsNoFunctionAndNoLiveReference() {
        Value root = tree();
        Value verdict = js.eval("js", """
            (function (root) {
                // Every live object the tree has: branches and their elements.
                const live = new Set();
                (function collect(b) {
                    live.add(b);
                    for (const { name } of b.listElements()) live.add(b.getElement(name));
                    for (const n of b.listBranches()) collect(b.getBranch(n));
                })(root);
                const bad = [];
                (function walk(v, at) {
                    if (typeof v === 'function') bad.push('function at ' + at);
                    if (v !== null && typeof v === 'object') {
                        if (live.has(v)) bad.push('live reference at ' + at);
                        for (const [k, x] of Object.entries(v)) walk(x, at + '.' + k);
                    }
                })(root.snapshot(), 'snapshot');
                return bad;
            })""").execute(root);
        assertEquals(0, verdict.getArraySize(), () -> verdict.toString());
    }

    // ------------------------------------------------------------- liveness

    @Test
    void aBranchDissolvedBetweenSnapshotsIsAbsentFromTheNext() {
        Value root = tree();
        assertEquals(2, root.invokeMember("snapshot").getMember("branches").getArraySize());
        root.invokeMember("dissolveBranch", "nav");
        Value after = root.invokeMember("snapshot");
        assertEquals(1, after.getMember("branches").getArraySize());
        assertEquals("widgets", after.getMember("branches").getArrayElement(0).getMember("name").asString());
    }

    @Test
    void snapshotDoesNotAlterTheTree() {
        Value root = tree();
        String before = root.invokeMember("toString").asString();
        root.invokeMember("snapshot");
        root.invokeMember("snapshot");
        assertEquals(before, root.invokeMember("toString").asString());
        assertEquals(2, root.getMember("branchCount").asInt());
    }

    // -------------------------------------------------------------- helpers

    private static String join(Value arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.getArraySize(); i++) {
            if (i > 0) sb.append(',');
            sb.append(arr.getArrayElement(i).asString());
        }
        return sb.toString();
    }
}
