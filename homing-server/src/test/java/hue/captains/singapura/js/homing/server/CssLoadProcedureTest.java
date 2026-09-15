package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0064 — the load procedure against a fake link factory: links are plain
 * objects appended to a list, and each fetch is settled by the test. Pins the
 * waves (a wave is appended only when the previous has landed), the deferred
 * flip (nothing applied until everything has arrived, then all in one pass),
 * sharing of a node in flight, abort with nothing applied, and retirement.
 */
class CssLoadProcedureTest extends JsModuleTestBase {

    private static final String GRAPH =
            "/homing/js/hue/captains/singapura/js/homing/server/CssDependencyGraph.js";
    private static final String PROCEDURE =
            "/homing/js/hue/captains/singapura/js/homing/server/CssLoadProcedure.js";

    private static final String SHIMS = """
            globalThis.links = [];                       // every link ever appended, in order
            globalThis.settle = {};                      // href → { ok(), fail() }
            globalThis.appendLink = function (href) {
                const link = { href: href, media: "", removed: false, remove: function () { this.removed = true; } };
                globalThis.links.push(link);
                const loaded = new Promise(function (res, rej) {
                    globalThis.settle[href] = { ok: function () { res(link); }, fail: function () { rej(new Error("failed " + href)); } };
                });
                return { link: link, loaded: loaded };
            };
            globalThis.hrefFor = function (id, theme) { return id + "@" + theme; };
            globalThis.graph = createCssDependencyGraph();
            graph.merge({ Base: { deps: [], prior: true }, Left: { deps: ["Base"] }, Right: { deps: ["Base"] }, Root: { deps: ["Left", "Right"] } });
            globalThis.proc = createCssLoadProcedure(graph, appendLink, hrefFor);
            globalThis.outcome = null;
            globalThis.start = function (ids, theme) {
                globalThis.outcome = null;
                proc.load(ids, theme).then(function () { globalThis.outcome = "ok"; }, function (e) { globalThis.outcome = "fail:" + e.message; });
            };
            globalThis.hrefs = function () { return links.filter(function (l) { return !l.removed; }).map(function (l) { return l.href; }); };
            globalThis.medias = function () { return links.filter(function (l) { return !l.removed; }).map(function (l) { return l.media; }); };
            """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(GRAPH);
        loadModule(PROCEDURE);
        js.eval(Source.newBuilder("js", SHIMS, "shims.js").buildLiteral());
    }

    private void ok(String href)   { js.eval("js", "settle['" + href + "'].ok(); undefined"); tick(); }
    private void fail(String href) { js.eval("js", "settle['" + href + "'].fail(); undefined"); tick(); }
    /** Promise jobs run when an evaluation returns; an empty one drains them. */
    private void tick() { js.eval("js", "undefined"); js.eval("js", "undefined"); }
    @SuppressWarnings("unchecked")
    private List<String> hrefs()  { return js.eval("js", "hrefs()").as(List.class); }
    @SuppressWarnings("unchecked")
    private List<String> medias() { return js.eval("js", "medias()").as(List.class); }
    private String outcome()      { Value v = js.eval("js", "outcome"); return v.isNull() ? null : v.asString(); }

    // ── Waves ─────────────────────────────────────────────────────────────────

    @Test
    void wavesAppendInOrder_andTheNextWaitsForThePrevious() {
        js.eval("js", "start(['Root', 'Left', 'Right', 'Base'], 'a')");
        tick();
        assertEquals(List.of("__theme-vars@a"), hrefs(), "wave 0 alone until it lands");
        ok("__theme-vars@a");
        assertEquals(List.of("__theme-vars@a", "__theme-globals@a"), hrefs());
        ok("__theme-globals@a");
        assertEquals(List.of("__theme-vars@a", "__theme-globals@a", "Base@a"), hrefs(), "the prior");
        ok("Base@a");
        assertEquals(List.of("__theme-vars@a", "__theme-globals@a", "Base@a", "Left@a", "Right@a"), hrefs(),
                "Left and Right together: nothing pending for either");
        ok("Left@a");
        assertEquals(5, hrefs().size(), "Root waits for the whole wave");
        ok("Right@a");
        assertEquals(6, hrefs().size());
        assertEquals("Root@a", hrefs().get(5));
        assertTrue(medias().stream().allMatch("not all"::equals), "nothing applied before the last has landed");
        assertEquals(null, outcome());
        ok("Root@a");
        assertTrue(medias().stream().allMatch("all"::equals), "then everything, in one pass");
        assertEquals("ok", outcome());
    }

    @Test
    void aDependencyOutsideTheSet_isNotAppended() {
        js.eval("js", "start(['Left'], 'a')");
        tick(); ok("__theme-vars@a"); ok("__theme-globals@a");
        assertEquals(List.of("__theme-vars@a", "__theme-globals@a", "Left@a"), hrefs());
    }

    // ── Sharing ───────────────────────────────────────────────────────────────

    @Test
    void aNodeInFlight_isSharedNotAppendedTwice() {
        js.eval("js", "start(['Base'], 'a')");
        tick(); ok("__theme-vars@a"); ok("__theme-globals@a");
        js.eval("js", "proc.load(['Left', 'Base'], 'a')");     // second caller wants Base too
        tick();
        assertEquals(1, hrefs().stream().filter("Base@a"::equals).count());
        ok("Base@a");
        assertEquals(List.of("__theme-vars@a", "__theme-globals@a", "Base@a", "Left@a"), hrefs());
        ok("Left@a");
        assertTrue(medias().stream().allMatch("all"::equals));
    }

    @Test
    void anAppliedNode_isNotAppendedAgain() {
        js.eval("js", "start(['Base'], 'a')");
        tick(); ok("__theme-vars@a"); ok("__theme-globals@a"); ok("Base@a");
        assertEquals("ok", outcome());
        js.eval("js", "start(['Base', 'Left'], 'a')");
        tick();
        assertEquals(List.of("__theme-vars@a", "__theme-globals@a", "Base@a", "Left@a"), hrefs());
    }

    // ── Abort ─────────────────────────────────────────────────────────────────

    @Test
    void aFailedFetch_abortsWithNothingApplied_andCanBeRetried() {
        js.eval("js", "start(['Left', 'Base'], 'a')");
        tick(); ok("__theme-vars@a"); ok("__theme-globals@a");
        fail("Base@a");
        assertEquals("fail:failed Base@a", outcome());
        assertEquals(List.of(), hrefs(), "every link this call appended is gone");
        assertEquals(List.of(), js.eval("js", "proc.loadedUnder('a')").as(List.class));

        js.eval("js", "start(['Left', 'Base'], 'a')");         // retry appends afresh
        tick(); ok("__theme-vars@a"); ok("__theme-globals@a"); ok("Base@a"); ok("Left@a");
        assertEquals("ok", outcome());
        assertEquals(4, hrefs().size());
    }

    // ── Switching: load under b, retire a ────────────────────────────────────

    @Test
    void aSecondTheme_arrivesAfterTheFirst_andRetiringTheFirst_leavesOnlyTheSecond() {
        js.eval("js", "start(['Left', 'Base'], 'a')");
        tick(); ok("__theme-vars@a"); ok("__theme-globals@a"); ok("Base@a"); ok("Left@a");
        assertEquals(List.of("__theme-vars", "__theme-globals", "Base", "Left").size(),
                js.eval("js", "proc.loadedUnder('a')").as(List.class).size());

        js.eval("js", "start(proc.loadedUnder('a').filter(id => !id.startsWith('__')), 'b')");
        tick(); ok("__theme-vars@b"); ok("__theme-globals@b"); ok("Base@b");
        assertTrue(medias().subList(4, medias().size()).stream().allMatch("not all"::equals), "b not applied yet");
        assertTrue(medias().subList(0, 4).stream().allMatch("all"::equals), "a still authoritative");
        ok("Left@b");
        assertEquals("ok", outcome());
        assertTrue(medias().stream().allMatch("all"::equals));

        js.eval("js", "proc.retire('a')");
        assertEquals(List.of("__theme-vars@b", "__theme-globals@b", "Base@b", "Left@b"), hrefs());
        assertEquals(List.of(), js.eval("js", "proc.loadedUnder('a')").as(List.class));

        // and back: A -> B -> A loads a again, after b
        js.eval("js", "start(['Left', 'Base'], 'a')");
        tick(); ok("__theme-vars@a"); ok("__theme-globals@a"); ok("Base@a"); ok("Left@a");
        assertEquals("ok", outcome());
        assertEquals("Left@a", hrefs().get(hrefs().size() - 1));
    }

    @Test
    void theSnapshot_isFrozenData() {
        js.eval("js", "start(['Base'], 'a')");
        tick(); ok("__theme-vars@a"); ok("__theme-globals@a"); ok("Base@a");
        assertTrue(js.eval("js", "(() => { const s = proc.snapshot(); return Object.isFrozen(s) && Object.isFrozen(s[0]) && s.some(e => e.id === 'Base' && e.theme === 'a' && e.applied); })()").asBoolean());
        assertFalse(js.eval("js", "proc.snapshot().some(e => !e.applied)").asBoolean());
    }
}
