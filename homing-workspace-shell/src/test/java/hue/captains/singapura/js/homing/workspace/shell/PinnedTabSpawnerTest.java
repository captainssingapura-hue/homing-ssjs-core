package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@code PinnedTabSpawner}. Exercises the synchronous core
 * — {@code _beginSpawn} plus the sync portion of {@code spawnAll} — so
 * every assertion lands without microtask draining.
 *
 * <p>Constructor-only DI: the mounter is fixed at construct time. Each
 * test builds a fresh spawner with the stub mounter injected; per-call
 * methods receive only data ({@code mtp, widgetsBranch, slotId, entry,
 * workspaceCtx}).</p>
 */
class PinnedTabSpawnerTest extends JsModuleTestBase {

    private static final String STUBS = """
            class StubMtp {
                constructor() {
                    this.addedTabs = [];
                    this.workspaceActiveTabId = null;
                }
                addTab(slotId, tab) {
                    const el = { children: [], appendChild(c) { this.children.push(c); },
                                 removeChild(c) { this.children = this.children.filter(x => x!==c); },
                                 get firstChild() { return this.children[0] || null; } };
                    tab.render(el);
                    tab._contentEl = el;
                    this.addedTabs.push({ slotId, tab });
                }
                getWorkspaceActiveTab() { return this.workspaceActiveTabId; }
                setWorkspaceActiveTab(id) { this.workspaceActiveTabId = id; }
            }
            class StubBranch {
                constructor(name) {
                    this.name = name;
                    this.children = [];
                }
                createBranch(name) { const c = new StubBranch(name); this.children.push(c); return c; }
                activate(owner) { this.owner = owner; }
                dissolve() { this.dissolved = true; }
            }
            globalThis.document = {
                createElement(tag) {
                    return { tag, style: { cssText: '' }, textContent: '',
                             children: [], appendChild(c) { this.children.push(c); } };
                }
            };
            // Stub mounter — sync resolveCalls capture, async mount/attach
            // (covered by integration). Also exposed as a global so a
            // PinnedTabSpawner.INSTANCE construction at module load won't
            // explode if WidgetMounter is missing in this stub env.
            globalThis.makeStubMounter = function () {
                return {
                    resolveCalls: [],
                    resolve: function (entry) {
                        this.resolveCalls.push(entry);
                        return Promise.resolve({
                            construct: function () {
                                return { root: { tag: 'div' }, setActive: () => {} };
                            }
                        });
                    },
                    mount:  function (mod, branch, entry) {
                        return mod.construct(branch, {}, {});
                    },
                    attach: function (controller, tab) { tab.controller = controller; }
                };
            };
            globalThis.WidgetMounter = { INSTANCE: makeStubMounter() };
            """;

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/PinnedTabSpawnerModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        js.eval(Source.newBuilder("js", STUBS, "stubs.js").buildLiteral());
        loadModule(MODULE);
    }

    /** Fresh spawner with the given stub mounter injected. */
    private Value freshSpawner(Value mounter) {
        Value deps = js.eval("js", "({})");
        deps.putMember("mounter", mounter);
        return global("PinnedTabSpawner").newInstance(deps);
    }

    @Test
    void beginSpawnAddsTabWithCorrectShape() {
        Value setup = js.eval("js", """
                (() => {
                    const mtp = new StubMtp();
                    const wB  = new StubBranch('widgets');
                    const entry = { simpleName: 'DocViewWidget', moduleUrl: '/dvw',
                                    label: 'Introduction', defaults: { title: 'Welcome' } };
                    const mounter = makeStubMounter();
                    return { mtp, wB, entry, mounter, ctx: { animalsParty: { _fake: true } } };
                })()""");

        Value result = freshSpawner(setup.getMember("mounter"))
                .invokeMember("_beginSpawn",
                        setup.getMember("mtp"),
                        setup.getMember("wB"),
                        "tl",
                        setup.getMember("entry"),
                        setup.getMember("ctx"));

        Value tab = result.getMember("tab");
        assertEquals("DocViewWidget:pinned", tab.getMember("id").asString());
        assertEquals("Introduction",         tab.getMember("title").asString());
        assertEquals(true,                   tab.getMember("pinned").asBoolean());

        Value addedTabs = setup.getMember("mtp").getMember("addedTabs");
        assertEquals(1, addedTabs.getArraySize());
        assertEquals("tl", addedTabs.getArrayElement(0).getMember("slotId").asString());

        Value resolveCalls = setup.getMember("mounter").getMember("resolveCalls");
        assertEquals(1, resolveCalls.getArraySize());
        assertEquals("DocViewWidget",
                     resolveCalls.getArrayElement(0).getMember("simpleName").asString());
    }

    @Test
    void emptyPinnedSpawnsReturnsImmediately() {
        Value mounter = js.eval("js", "makeStubMounter()");
        Value opts = js.eval("js", """
                ({ mtp: new StubMtp(), widgetsBranch: new StubBranch('w'),
                   spec: { entries: [], pinnedSpawns: [] }, workspaceCtx: {} })""");
        freshSpawner(mounter).invokeMember("spawnAll", opts);
        assertEquals(0, opts.getMember("mtp").getMember("addedTabs").getArraySize());
    }

    @Test
    void unknownPinnedKindIsSkipped() {
        Value setup = js.eval("js", """
                (() => {
                    const mtp = new StubMtp();
                    const wB  = new StubBranch('w');
                    const spec = {
                        entries:      [{ simpleName: 'OtherWidget', moduleUrl: '/x', label: 'Other' }],
                        pinnedSpawns: [ 'MissingWidget' ]
                    };
                    const mounter = makeStubMounter();
                    return { mtp, wB, spec, mounter };
                })()""");
        Value opts = js.eval("js", "({ workspaceCtx: {}, slotId: 'tl' })");
        opts.putMember("mtp",           setup.getMember("mtp"));
        opts.putMember("widgetsBranch", setup.getMember("wB"));
        opts.putMember("spec",          setup.getMember("spec"));

        freshSpawner(setup.getMember("mounter")).invokeMember("spawnAll", opts);
        assertEquals(0, setup.getMember("mtp").getMember("addedTabs").getArraySize());
        assertEquals(0, setup.getMember("mounter").getMember("resolveCalls").getArraySize());
    }

    @Test
    void firstSpawnedBecomesWorkspaceActiveSynchronously() {
        Value setup = js.eval("js", """
                (() => {
                    const mtp = new StubMtp();
                    const wB  = new StubBranch('w');
                    const spec = {
                        entries: [
                            { simpleName: 'A', moduleUrl: '/a', label: 'AWidget' },
                            { simpleName: 'B', moduleUrl: '/b', label: 'BWidget' }
                        ],
                        pinnedSpawns: [ 'A', 'B' ]
                    };
                    const mounter = makeStubMounter();
                    return { mtp, wB, spec, mounter };
                })()""");
        Value opts = js.eval("js", "({ workspaceCtx: {}, slotId: 'tl' })");
        opts.putMember("mtp",           setup.getMember("mtp"));
        opts.putMember("widgetsBranch", setup.getMember("wB"));
        opts.putMember("spec",          setup.getMember("spec"));

        freshSpawner(setup.getMember("mounter")).invokeMember("spawnAll", opts);

        Value addedTabs = setup.getMember("mtp").getMember("addedTabs");
        assertEquals(2, addedTabs.getArraySize());
        assertEquals("A:pinned",
                     setup.getMember("mtp").getMember("workspaceActiveTabId").asString());
    }

    @Test
    void widgetBranchNameMatchesSimpleNamePrefix() {
        Value setup = js.eval("js", """
                (() => {
                    const mtp = new StubMtp();
                    const wB  = new StubBranch('widgets');
                    const entry = { simpleName: 'MyWidget', moduleUrl: '/m', label: 'My' };
                    const mounter = makeStubMounter();
                    return { mtp, wB, entry, mounter };
                })()""");
        freshSpawner(setup.getMember("mounter")).invokeMember("_beginSpawn",
                setup.getMember("mtp"),
                setup.getMember("wB"),
                "tl",
                setup.getMember("entry"),
                js.eval("js", "({})"));

        Value children = setup.getMember("wB").getMember("children");
        assertEquals(1, children.getArraySize());
        assertEquals("p-MyWidget", children.getArrayElement(0).getMember("name").asString());
    }
}
