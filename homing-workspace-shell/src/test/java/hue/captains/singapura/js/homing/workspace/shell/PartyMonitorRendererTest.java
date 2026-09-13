package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0063 — the monitor's chrome, and one TreeRenderer per snapshot.
 *
 * <p>The renderer is handed a fixture through {@code opts.view} and a stub
 * tree class through {@code opts.tree}, so neither the party nor the real
 * {@code TreeRenderer} is in these tests. The stub records what it was given;
 * the branch stub records every call. What is asserted is the contract: a
 * fresh sub-branch and a fresh renderer per refresh, the previous dissolved,
 * the self row selected by the adapter's path, the header honest.</p>
 */
class PartyMonitorRendererTest extends JsModuleTestBase {

    private static final String ADAPTER =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/PartyMonitorAdapterModule.js";
    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/PartyMonitorRendererModule.js";

    private static final String STUBS = """
        function el(tag) {
            const e = { tag, textContent: '', children: [], attrs: {}, listeners: {}, cls: new Set(),
                classList: { add: c => e.cls.add(c), remove: c => e.cls.delete(c), contains: c => e.cls.has(c) },
                appendChild(c) { e.children.push(c); return c; },
                setAttribute(k, v) { e.attrs[k] = v; },
                addEventListener(t, fn) { (e.listeners[t] = e.listeners[t] || []).push(fn); },
                click() { (e.listeners.click || []).forEach(fn => fn()); } };
            return e;
        }
        class StubBranch {
            constructor(name) { this.name = name; this.branches = {}; this.elements = {}; this.activated = null; this.log = []; }
            createElement(name, tag) {
                if (this.elements[name]) throw new RangeError('dup element ' + name);
                this.log.push('createElement:' + name); return (this.elements[name] = el(tag));
            }
            createBranch(name) {
                if (this.branches[name]) throw new RangeError('dup branch ' + name);
                this.log.push('createBranch:' + name); return (this.branches[name] = new StubBranch(name));
            }
            hasBranch(name) { return !!this.branches[name]; }
            dissolveBranch(name) { this.log.push('dissolveBranch:' + name); delete this.branches[name]; }
            activate(owner, label) { this.activated = { owner, label }; }
        }
        globalThis.StubBranch = StubBranch;
        globalThis.css = {
            addClass:    (e, ...cs) => cs.forEach(c => e.classList.add(c.name)),
            removeClass: (e, ...cs) => cs.forEach(c => e.classList.remove(c.name))
        };
        for (const n of ['pm_root','pm_head','pm_title','pm_count','pm_btn','pm_note','pm_note_leaked','pm_tree'])
            globalThis[n] = { name: n.replace(/_/g, '-') };
        globalThis.viewParty = () => { throw new Error('renderer reached the live party'); };
        globalThis.TreeRenderer = function () { throw new Error('renderer reached the real TreeRenderer'); };

        // A stub TreeRenderer that records its constructor options and selectPath calls.
        globalThis.MADE = [];
        class StubTree {
            constructor(opts) { this.opts = opts; this.selected = null; MADE.push(this); }
            selectPath(p, o) { this.selected = { path: p, opts: o }; return true; }
            handleKeydown(ev) { this.keys = (this.keys || []).concat([ev.key]); return ev.key === "ArrowDown"; }
        }
        globalThis.StubTree = StubTree;

        const deepFreeze = o => { if (o && typeof o === 'object') { Object.freeze(o); Object.values(o).forEach(deepFreeze); } return o; };
        globalThis.FIXTURE = deepFreeze({
            name: 'root', depth: 0, path: ['root'], owner: 'partyChief', ownerAlive: true, elements: [], branches: [
                { name: 'widgets', depth: 1, path: ['root','widgets'], owner: 'shell:widgets', ownerAlive: true,
                  elements: [{ name: 'slot', tagName: 'div' }], branches: [
                    { name: 'w-me',   depth: 2, path: ['root','widgets','w-me'],   owner: 'widget:me',   ownerAlive: true,  elements: [], branches: [] },
                    { name: 'w-dead', depth: 2, path: ['root','widgets','w-dead'], owner: 'widget:dead', ownerAlive: false, elements: [], branches: [] }
                ] } ] });
        globalThis.CLEAN = deepFreeze({ name: 'root', depth: 0, path: ['root'], owner: 'partyChief', ownerAlive: true, elements: [], branches: [] });
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(ADAPTER);
        loadModule(MODULE);
    }

    private Value mount(String selfName, String fixture) {
        return js.eval("js", """
            (() => {
                MADE.length = 0;
                const branch = new StubBranch('w-me');
                const host = { children: [], appendChild(c) { this.children.push(c); } };
                const ctl = renderPartyMonitor(branch, host, { selfName: %s, view: () => %s, tree: StubTree });
                return { branch, host, ctl };
            })()""".formatted(selfName == null ? "null" : "'" + selfName + "'", fixture));
    }

    private Value find(Value root, String cls) {
        return js.eval("js", """
            (function find(node, cls, acc) {
                acc = acc || [];
                if (node.cls && node.cls.has(cls)) acc.push(node);
                for (const c of (node.children || [])) find(c, cls, acc);
                return acc;
            })""").execute(root, cls);
    }

    @Test
    void oneTreeRendererPerSnapshotInItsOwnSubBranchWithTheAdaptedData() {
        Value m = mount("w-me", "FIXTURE");
        Value made = global("MADE");
        assertEquals(1, made.getArraySize());
        Value opts = made.getArrayElement(0).getMember("opts");
        assertEquals("tree", opts.getMember("branch").getMember("name").asString(), "drawn into the tree sub-branch");
        assertTrue(opts.getMember("container").getMember("cls").invokeMember("has", "pm-tree").asBoolean());
        assertEquals("root", opts.getMember("data").getMember("display").getMember("label").asString(), "adapter output, not the raw snapshot");
        assertTrue(opts.getMember("showBadge").asBoolean());
        assertTrue(opts.getMember("showNote").asBoolean());
        assertEquals(99, opts.getMember("expandDepth").asInt(), "fully expanded");

        Value tree = m.getMember("branch").getMember("branches").getMember("tree");
        assertEquals("partyMonitor:tree", tree.getMember("activated").getMember("label").asString());
        assertTrue(tree.getMember("activated").getMember("owner").getMember("cls").invokeMember("has", "pm-root").asBoolean(),
                   "owned by the monitor's root element");
    }

    @Test
    void theSelfRowIsSelectedByTheAdaptersPath() {
        Value m = mount("w-me", "FIXTURE");
        Value sel = global("MADE").getArrayElement(0).getMember("selected");
        // widgets has 1 element, so w-me is at [0, 1]
        assertEquals(2, sel.getMember("path").getArraySize());
        assertEquals(0, sel.getMember("path").getArrayElement(0).asInt());
        assertEquals(1, sel.getMember("path").getArrayElement(1).asInt());
        assertTrue(sel.getMember("opts").getMember("reveal").asBoolean());

        mount(null, "FIXTURE");
        assertTrue(global("MADE").getArrayElement(0).getMember("selected").isNull(), "no selfName → nothing selected");
    }

    @Test
    void refreshDissolvesThePreviousTreeAndBuildsANewRenderer() {
        Value m = mount("w-me", "FIXTURE");
        m.getMember("ctl").invokeMember("refresh");
        assertEquals(2, global("MADE").getArraySize(), "a fresh renderer per refresh — setData is never reused");
        String log = m.getMember("branch").getMember("log").toString();
        assertTrue(log.contains("dissolveBranch:tree"));
        assertTrue(m.getMember("branch").invokeMember("hasBranch", "tree").asBoolean(), "exactly one live tree sub-branch");
    }

    @Test
    void theRefreshButtonRefreshes() {
        Value m = mount("w-me", "FIXTURE");
        Value root = m.getMember("host").getMember("children").getArrayElement(0);
        Value btn = find(root, "pm-btn").getArrayElement(0);
        assertEquals("button", btn.getMember("attrs").getMember("type").asString());
        btn.invokeMember("click");
        assertEquals(2, global("MADE").getArraySize());
    }

    @Test
    void theHeaderCountsAndTheNoteNeverSaysNoLeaks() {
        Value m = mount("w-me", "FIXTURE");
        Value root = m.getMember("host").getMember("children").getArrayElement(0);
        assertEquals("4 branches · 1 element", find(root, "pm-count").getArrayElement(0).getMember("textContent").asString());
        Value note = find(root, "pm-note").getArrayElement(0);
        String text = note.getMember("textContent").asString();
        assertTrue(text.startsWith("1 owner collected"), text);
        assertFalse(text.toLowerCase().contains("no leaks"));
        assertTrue(note.getMember("cls").invokeMember("has", "pm-note-leaked").asBoolean());

        Value clean = mount("w-me", "CLEAN").getMember("host").getMember("children").getArrayElement(0);
        Value cnote = find(clean, "pm-note").getArrayElement(0);
        assertTrue(cnote.getMember("textContent").asString().startsWith("No owners collected"));
        assertTrue(cnote.getMember("textContent").asString().contains("not the same as no leaks"));
        assertFalse(cnote.getMember("cls").invokeMember("has", "pm-note-leaked").asBoolean());
    }

    /**
     * TreeRenderer owns the key semantics and the host owns when keys flow —
     * so the renderer exposes handleKeydown, and it must reach the renderer of
     * the LATEST snapshot, not the one that was current when the widget
     * attached its listener.
     */
    @Test
    void keysReachTheCurrentTreeRendererEvenAfterARefresh() {
        Value m = mount("w-me", "FIXTURE");
        Value ctl = m.getMember("ctl");
        assertTrue(ctl.invokeMember("handleKeydown", js.eval("js", "({ key: 'ArrowDown' })")).asBoolean(),
                   "consumed → the host will preventDefault");
        assertFalse(ctl.invokeMember("handleKeydown", js.eval("js", "({ key: 'x' })")).asBoolean());
        Value first = global("MADE").getArrayElement(0).getMember("keys");
        assertEquals(2, first.getArraySize());
        assertEquals("ArrowDown", first.getArrayElement(0).asString());
        assertEquals("x",         first.getArrayElement(1).asString());

        ctl.invokeMember("refresh");
        ctl.invokeMember("handleKeydown", js.eval("js", "({ key: 'ArrowUp' })"));
        Value second = global("MADE").getArrayElement(1);
        assertEquals(1, second.getMember("keys").getArraySize(), "the new renderer got the key");
        assertEquals("ArrowUp", second.getMember("keys").getArrayElement(0).asString());
        assertEquals(2, global("MADE").getArrayElement(0).getMember("keys").getArraySize(), "the old one got nothing more");
    }

    /** The default view and the default tree class both throw in these stubs; passing means neither was reached. */
    @Test
    void theRendererReachesNeitherThePartyNorTheRealTreeRenderer() {
        Value m = mount("w-me", "FIXTURE");
        m.getMember("ctl").invokeMember("refresh");
        assertTrue(js.eval("js", "Object.isFrozen(FIXTURE)").asBoolean());
    }
}
