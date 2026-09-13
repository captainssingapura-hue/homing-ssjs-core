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
 * RFC 0063 — the monitor draws a snapshot, and touches nothing.
 *
 * <p>The renderer is handed a fixture through {@code opts.view}, so no party
 * exists in these tests at all — which is itself the assertion that matters:
 * a renderer that needed the live tree to draw could not be tested this way.
 * What it draws is checked by walking the element stubs it built through the
 * branch stub; what it does NOT do is checked by the branch stub recording
 * every call.</p>
 */
class PartyMonitorRendererTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/PartyMonitorRendererModule.js";

    /**
     * Enough of the world for the renderer: a css manager over classList, the
     * typed class handles as name-bearing objects, a branch that mints element
     * stubs and records dissolutions, and no document — the renderer must not
     * need one.
     */
    private static final String STUBS = """
        function el(tag) {
            const e = { tag, textContent: '', children: [], attrs: {}, listeners: {},
                cls: new Set(),
                classList: { add: c => e.cls.add(c), remove: c => e.cls.delete(c),
                             contains: c => e.cls.has(c),
                             toggle: (c, f) => { const on = f === undefined ? !e.cls.has(c) : f;
                                                 on ? e.cls.add(c) : e.cls.delete(c); return on; } },
                appendChild(c) { e.children.push(c); return c; },
                setAttribute(k, v) { e.attrs[k] = v; },
                addEventListener(t, fn) { (e.listeners[t] = e.listeners[t] || []).push(fn); },
                click() { (e.listeners.click || []).forEach(fn => fn()); }
            };
            return e;
        }
        class StubBranch {
            constructor(name) { this.name = name; this.branches = {}; this.elements = {};
                                this.activated = null; this.log = []; }
            createElement(name, tag) {
                if (this.elements[name]) throw new RangeError('dup element ' + name);
                this.log.push('createElement:' + name);
                return (this.elements[name] = el(tag));
            }
            createBranch(name) {
                if (this.branches[name]) throw new RangeError('dup branch ' + name);
                this.log.push('createBranch:' + name);
                return (this.branches[name] = new StubBranch(name));
            }
            hasBranch(name) { return !!this.branches[name]; }
            dissolveBranch(name) { this.log.push('dissolveBranch:' + name); delete this.branches[name]; }
            activate(owner, label) { this.activated = { owner, label }; }
        }
        globalThis.StubBranch = StubBranch;
        globalThis.css = {
            addClass:    (e, ...cs) => cs.forEach(c => e.classList.add(c.name)),
            removeClass: (e, ...cs) => cs.forEach(c => e.classList.remove(c.name)),
            toggleClass: (e, c, f)  => e.classList.toggle(c.name, f),
            hasClass:    (e, c)     => e.classList.contains(c.name)
        };
        for (const n of ['pm_root','pm_head','pm_title','pm_count','pm_btn','pm_note','pm_note_leaked',
                         'pm_tree','pm_node','pm_node_header','pm_node_header_leaked','pm_node_header_self',
                         'pm_icon','pm_name','pm_depth_badge','pm_owner','pm_owner_dot','pm_owner_dot_alive',
                         'pm_owner_dot_leaked','pm_badge','pm_elements','pm_chip','pm_branches','pm_folded'])
            globalThis[n] = { name: n.replace(/_/g, '-') };
        // viewParty is what the module imports by default; tests inject opts.view instead,
        // and this one throws so a renderer that ignored opts.view would fail loudly.
        globalThis.viewParty = () => { throw new Error('renderer reached the live party'); };

        // A fixture in snapshot() shape: root → nav (alive), widgets → w-me (self), w-dead (collected).
        globalThis.FIXTURE = Object.freeze({
            name: 'root', depth: 0, path: ['root'], owner: 'partyChief', ownerAlive: true,
            elements: [], branches: [
                { name: 'nav', depth: 1, path: ['root','nav'], owner: 'shell:nav', ownerAlive: true,
                  elements: [{ name: 'bar', tagName: 'nav' }, { name: 'logo', tagName: 'img' }], branches: [] },
                { name: 'widgets', depth: 1, path: ['root','widgets'], owner: 'shell:widgets', ownerAlive: true,
                  elements: [], branches: [
                    { name: 'w-me', depth: 2, path: ['root','widgets','w-me'], owner: 'widget:me', ownerAlive: true,
                      elements: [{ name: 'host', tagName: 'div' }], branches: [] },
                    { name: 'w-dead', depth: 2, path: ['root','widgets','w-dead'], owner: 'widget:dead', ownerAlive: false,
                      elements: [{ name: 'root', tagName: 'div' }], branches: [] },
                    { name: 'never', depth: 2, path: ['root','widgets','never'], owner: null, ownerAlive: null,
                      elements: [], branches: [] }
                ] }
            ]
        });
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    /** Mount into a fresh branch + host, with the fixture as the view. */
    private Value mount(String selfName) {
        return js.eval("js", """
            (() => {
                const branch = new StubBranch('w-me');
                const host = { children: [], appendChild(c) { this.children.push(c); } };
                const ctl = renderPartyMonitor(branch, host, {
                    selfName: %s, view: () => FIXTURE });
                return { branch, host, ctl };
            })()""".formatted(selfName == null ? "null" : "'" + selfName + "'"));
    }

    /** Every element under `root` with the given class, depth-first. */
    private static Value withClass(Value root, String cls, Value js) {
        return js.execute(root, cls);
    }

    private Value finder() {
        return js.eval("js", """
            (function find(node, cls, acc) {
                acc = acc || [];
                if (node.cls && node.cls.has(cls)) acc.push(node);
                for (const c of (node.children || [])) find(c, cls, acc);
                return acc;
            })""");
    }

    // ---------------------------------------------------------------- draws

    @Test
    void everyBranchInTheSnapshotIsDrawnWithNameAndDepth() {
        Value m = mount("w-me");
        Value root = m.getMember("host").getMember("children").getArrayElement(0);
        Value names = finder().execute(root, "pm-name");
        assertEquals(6, names.getArraySize(), "root, nav, widgets, w-me, w-dead, never");
        StringBuilder seen = new StringBuilder();
        for (int i = 0; i < names.getArraySize(); i++) seen.append(names.getArrayElement(i).getMember("textContent").asString()).append(',');
        assertEquals("root,nav,widgets,w-me,w-dead,never,", seen.toString(), "tree order");

        Value depths = finder().execute(root, "pm-depth-badge");
        assertEquals("L0", depths.getArrayElement(0).getMember("textContent").asString());
        assertEquals("L2", depths.getArrayElement(3).getMember("textContent").asString());
    }

    @Test
    void elementsAppearAsChipsAndTheHeaderCountsThem() {
        Value m = mount("w-me");
        Value root = m.getMember("host").getMember("children").getArrayElement(0);
        Value chips = finder().execute(root, "pm-chip");
        assertEquals(4, chips.getArraySize(), "bar, logo, host, root");
        assertEquals("bar <nav>", chips.getArrayElement(0).getMember("textContent").asString());

        Value count = finder().execute(root, "pm-count").getArrayElement(0);
        assertEquals("6 branches · 4 elements", count.getMember("textContent").asString());
    }

    @Test
    void aCollectedOwnerIsDrawnRedAndCountedAndNeverCalledALeak() {
        Value m = mount("w-me");
        Value root = m.getMember("host").getMember("children").getArrayElement(0);

        Value leakedHeaders = finder().execute(root, "pm-node-header-leaked");
        assertEquals(1, leakedHeaders.getArraySize());
        Value nameInLeaked = finder().execute(leakedHeaders.getArrayElement(0), "pm-name").getArrayElement(0);
        assertEquals("w-dead", nameInLeaked.getMember("textContent").asString());

        assertEquals(1, finder().execute(root, "pm-owner-dot-leaked").getArraySize());
        assertEquals(4, finder().execute(root, "pm-owner-dot-alive").getArraySize(), "root, nav, widgets, w-me");
        // The never-activated node has no dot at all, and no owner span.
        assertEquals(5, finder().execute(root, "pm-owner").getArraySize());

        Value note = finder().execute(root, "pm-note").getArrayElement(0);
        String text = note.getMember("textContent").asString();
        assertTrue(text.startsWith("1 owner collected"), text);
        assertFalse(text.toLowerCase().contains("no leaks"), "D8: never claims no leaks");
        assertTrue(note.getMember("cls").invokeMember("has", "pm-note-leaked").asBoolean());
    }

    @Test
    void aCleanTreeSaysNoneCollectedNotNoLeaks() {
        Value m = js.eval("js", """
            (() => {
                const clean = Object.freeze({ name: 'root', depth: 0, path: ['root'], owner: 'partyChief',
                    ownerAlive: true, elements: [], branches: [] });
                const branch = new StubBranch('w-x');
                const host = { children: [], appendChild(c) { this.children.push(c); } };
                renderPartyMonitor(branch, host, { view: () => clean });
                return host.children[0];
            })()""");
        Value note = finder().execute(m, "pm-note").getArrayElement(0);
        String text = note.getMember("textContent").asString();
        assertTrue(text.startsWith("No owners collected"), text);
        assertTrue(text.contains("not the same as no leaks"), text);
        assertFalse(note.getMember("cls").invokeMember("has", "pm-note-leaked").asBoolean());
    }

    @Test
    void theMonitorOutlinesItsOwnBranchByName() {
        Value m = mount("w-me");
        Value root = m.getMember("host").getMember("children").getArrayElement(0);
        Value self = finder().execute(root, "pm-node-header-self");
        assertEquals(1, self.getArraySize());
        assertEquals("w-me", finder().execute(self.getArrayElement(0), "pm-name")
                .getArrayElement(0).getMember("textContent").asString());

        Value none = mount(null);
        assertEquals(0, finder().execute(none.getMember("host").getMember("children").getArrayElement(0),
                                         "pm-node-header-self").getArraySize());
    }

    // ------------------------------------------------------------- refresh

    @Test
    void refreshDissolvesTheOldTreeAndDrawsTheNewOne() {
        Value m = mount("w-me");
        Value branch = m.getMember("branch");
        Value log = branch.getMember("log");
        long before = log.getArraySize();
        assertTrue(log.toString().contains("createBranch:tree"));

        m.getMember("ctl").invokeMember("refresh");
        assertTrue(log.toString().contains("dissolveBranch:tree"), "the previous tree sub-branch is dissolved");
        assertTrue(log.getArraySize() > before);
        assertTrue(branch.invokeMember("hasBranch", "tree").asBoolean(), "exactly one tree sub-branch after refresh");
    }

    @Test
    void theRefreshButtonRefreshes() {
        Value m = mount("w-me");
        Value root = m.getMember("host").getMember("children").getArrayElement(0);
        Value btn = finder().execute(root, "pm-btn").getArrayElement(0);
        assertEquals("button", btn.getMember("attrs").getMember("type").asString());
        btn.invokeMember("click");
        assertTrue(m.getMember("branch").getMember("log").toString().contains("dissolveBranch:tree"));
    }

    @Test
    void clickingAHeaderFoldsItsChildren() {
        Value m = mount("w-me");
        Value root = m.getMember("host").getMember("children").getArrayElement(0);
        Value headers = finder().execute(root, "pm-node-header");
        Value widgetsHeader = headers.getArrayElement(2);
        assertEquals("widgets", finder().execute(widgetsHeader, "pm-name").getArrayElement(0).getMember("textContent").asString());
        assertEquals(0, finder().execute(root, "pm-folded").getArraySize());
        widgetsHeader.invokeMember("click");
        assertEquals(1, finder().execute(root, "pm-folded").getArraySize());
        widgetsHeader.invokeMember("click");
        assertEquals(0, finder().execute(root, "pm-folded").getArraySize());
    }

    // ----------------------------------------------------- cannot touch

    /**
     * The contract, stated as what the renderer never did: it made its own
     * elements and one sub-branch, and it never reached for the live party —
     * the default viewParty in these stubs throws, and the fixture is frozen.
     */
    @Test
    void theRendererTouchesOnlyItsOwnBranch() {
        Value m = mount("w-me");
        Value branch = m.getMember("branch");
        Value tree = branch.getMember("branches").getMember("tree");
        assertEquals("partyMonitor:tree", tree.getMember("activated").getMember("label").asString());
        // The sub-branch's owner is the monitor's own root element, not a party object.
        assertTrue(tree.getMember("activated").getMember("owner").getMember("cls")
                .invokeMember("has", "pm-root").asBoolean());
        m.getMember("ctl").invokeMember("refresh");
        assertTrue(js.eval("js", "Object.isFrozen(FIXTURE)").asBoolean(), "still frozen after two draws");
    }
}
