package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0058 — the anchor is a kind's path inside its group, on the client:
 * {@code WorkspaceGroupPathModule} (parse, resolve, crumbs) and the switcher
 * model's tree and target URL over the group the server served.
 */
class WorkspaceGroupPathTest extends JsModuleTestBase {

    private static final String PATH  = "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WorkspaceGroupPathModule.js";
    private static final String MODEL = "/homing/js/hue/captains/singapura/js/homing/workspace/shell/WorkspaceSwitcherModel.js";

    /** A group as the server serves it, plus the browser API the model leans on. */
    private static final String FIXTURE = """
        globalThis.GROUP = Object.freeze({
            id: 'fx-desk', title: 'FX Options Desk', summary: '', defaultKind: 'trader',
            kinds: [
                { kind: 'trader',  title: 'Trader Desk',  section: 'Trading',              sectionSlug: 'trading' },
                { kind: 'sales',   title: 'Sales Desk',   section: 'Trading',              sectionSlug: 'trading' },
                { kind: 'ipv',     title: 'IPV',          section: 'Product Control & IPV', sectionSlug: 'product-control-ipv' }
            ]
        });
        // Enough of URLSearchParams for targetUrl: parse, get/set/delete/has, toString.
        globalThis.URLSearchParams = class {
            constructor(s) { this._m = new Map(); String(s || '').split('&').filter(Boolean).forEach(p => {
                const i = p.indexOf('='); this._m.set(decodeURIComponent(i < 0 ? p : p.slice(0, i)), decodeURIComponent(i < 0 ? '' : p.slice(i + 1))); }); }
            get(k) { return this._m.has(k) ? this._m.get(k) : null; }
            set(k, v) { this._m.set(k, String(v)); }
            delete(k) { this._m.delete(k); }
            has(k) { return this._m.has(k); }
            toString() { return [...this._m].map(([k, v]) => encodeURIComponent(k) + '=' + encodeURIComponent(v)).join('&'); }
        };
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(PATH);
        loadModule(MODEL);
        js.eval(Source.newBuilder("js", FIXTURE, "fixture.js").buildLiteral());
    }

    // ------------------------------------------------------------- the anchor

    @Test
    void parseAnchorReadsOnlyWorkspacePaths() {
        Value full = global("parseAnchor").execute("#ws/trading/trader");
        assertEquals("trading", full.getMember("section").asString());
        assertEquals("trader",  full.getMember("kind").asString());
        Value bare = global("parseAnchor").execute("ws/trader");
        assertTrue(bare.getMember("section").isNull());
        assertEquals("trader", bare.getMember("kind").asString());
        assertTrue(global("parseAnchor").execute("#the-heading").isNull(), "a doc heading anchor is not a workspace path");
        assertTrue(global("parseAnchor").execute("").isNull());
        assertTrue(global("parseAnchor").execute("#ws").isNull());
    }

    @Test
    void anchorOfMintsFromTheServedSlug() {
        assertEquals("ws/product-control-ipv/ipv", global("anchorOf").execute(global("GROUP").getMember("kinds"), "ipv").asString());
        assertTrue(global("anchorOf").execute(global("GROUP").getMember("kinds"), "nope").isNull());
    }

    @Test
    void resolveKindIsTheAnchorsKindElseTheDefault() {
        Value g = global("GROUP");
        // The anchor's kind, canonical.
        Value r = global("resolveKind").execute(g, "#ws/trading/sales");
        assertEquals("sales", r.getMember("kind").asString());
        assertEquals("ws/trading/sales", r.getMember("anchor").asString());
        assertFalse(r.getMember("canonicalised").asBoolean());
        assertTrue(r.getMember("notice").isNull());
        // A known kind under the wrong section, or alone, is canonicalised.
        r = global("resolveKind").execute(g, "#ws/wrong/ipv");
        assertEquals("ipv", r.getMember("kind").asString());
        assertEquals("ws/product-control-ipv/ipv", r.getMember("anchor").asString());
        assertTrue(r.getMember("canonicalised").asBoolean());
        r = global("resolveKind").execute(g, "#ws/ipv");
        assertEquals("ipv", r.getMember("kind").asString());
        assertTrue(r.getMember("canonicalised").asBoolean());
        // The default, silently, with no anchor or a heading anchor.
        r = global("resolveKind").execute(g, "");
        assertEquals("trader", r.getMember("kind").asString());
        assertEquals("ws/trading/trader", r.getMember("anchor").asString());
        assertFalse(r.getMember("canonicalised").asBoolean());
        assertTrue(r.getMember("notice").isNull());
        r = global("resolveKind").execute(g, "#some-heading");
        assertEquals("trader", r.getMember("kind").asString());
        assertTrue(r.getMember("notice").isNull(), "a heading anchor was never a workspace path — no notice");
        // The default WITH a notice when a ws/ anchor named a kind the group does not hold.
        r = global("resolveKind").execute(g, "#ws/trading/etrading");
        assertEquals("trader", r.getMember("kind").asString());
        assertTrue(r.getMember("canonicalised").asBoolean());
        assertTrue(r.getMember("notice").asString().contains("#ws/trading/etrading"), r.getMember("notice").asString());
        assertTrue(r.getMember("notice").asString().contains("Trader Desk"));
    }

    @Test
    void innerCrumbsAreTheSectionThenTheKind() {
        Value c = global("innerCrumbs").execute(global("GROUP"), "ipv");
        assertEquals(2, c.getArraySize());
        assertEquals("Product Control & IPV", c.getArrayElement(0).getMember("text").asString());
        assertEquals("IPV", c.getArrayElement(1).getMember("text").asString());
        assertFalse(c.getArrayElement(0).hasMember("href"), "neither crumb is a server position");
        assertEquals(0, global("innerCrumbs").execute(global("GROUP"), "nope").getArraySize());
    }

    // ----------------------------------------------------------- the switcher

    @Test
    void theSwitcherTreeIsTheGroupsTreeWithServedSlugs() {
        Value t = global("kindTreeData").execute(global("GROUP").getMember("kinds"), "sales");
        assertEquals(2, t.getMember("children").getArraySize());
        Value trading = t.getMember("children").getArrayElement(0);
        assertEquals("trading", trading.getMember("segment").asString());
        assertEquals("section", trading.getMember("display").getMember("kind").asString());
        assertEquals(2, trading.getMember("children").getArraySize());
        assertEquals("current", trading.getMember("children").getArrayElement(1).getMember("display").getMember("badge").asString());
        assertEquals("product-control-ipv", t.getMember("children").getArrayElement(1).getMember("segment").asString());
        // Positional path for selectPath.
        Value p = global("pathOfKind").execute(global("GROUP").getMember("kinds"), "ipv");
        assertEquals(1, p.getArrayElement(0).asInt());
        assertEquals(0, p.getArrayElement(1).asInt());
    }

    @Test
    void anchorMode_targetUrlIsTheSameAddressWithTheKindsAnchor() {
        Value kinds = global("GROUP").getMember("kinds");
        // A path address: nothing but the anchor changes; scoped params are cleared.
        String u = global("targetUrl").execute("/cat/fx-options-desk/workspaces?workspace=old&slowmo=500",
                obj("kinds", kinds, "currentKind", "trader", "kind", "ipv")).asString();
        assertEquals("/cat/fx-options-desk/workspaces#ws/product-control-ipv/ipv", u);
        // An instance rides in the query, before the anchor; a same-kind change keeps the anchor.
        u = global("targetUrl").execute("/cat/fx-options-desk/workspaces",
                obj("kinds", kinds, "currentKind", "sales", "kind", "sales", "instanceId", "abc")).asString();
        assertEquals("/cat/fx-options-desk/workspaces?workspace=abc#ws/trading/sales", u);
        // A new name likewise.
        u = global("targetUrl").execute("/cat/fx-options-desk/workspaces",
                obj("kinds", kinds, "currentKind", "sales", "kind", "trader", "name", "EOD checks")).asString();
        assertEquals("/cat/fx-options-desk/workspaces?name=EOD%20checks#ws/trading/trader", u);
        // The flat form of the same app behaves the same: the anchor names the kind.
        u = global("targetUrl").execute("/app?app=workspaceGroup&ws_group=fx-desk",
                obj("kinds", kinds, "currentKind", "trader", "kind", "ipv")).asString();
        assertEquals("/app?app=workspaceGroup&ws_group=fx-desk#ws/product-control-ipv/ipv", u);
    }

    @Test
    void gotoMode_theLegacyAppIsUnchanged() {
        // The legacy chrome serves kinds without slugs and hands a goto base.
        Value kinds = js.eval("js", "([{ kind: 'studio', title: 'Studio', section: 'Workspaces' }, { kind: 'zoo', title: 'Zoo', section: 'Demos' }])");
        String base = "/goto?app=genericWorkspace";
        // A kind change goes to the goto base with ws_kind — no anchor.
        String u = global("targetUrl").execute("/cat/studio-ws?workspace=old",
                obj("kinds", kinds, "base", base, "currentKind", "studio", "kind", "zoo")).asString();
        assertEquals("/goto?app=genericWorkspace&ws_kind=zoo", u);
        // A same-kind change edits the current URL; every other parameter survives.
        u = global("targetUrl").execute("/app?app=genericWorkspace&ws_kind=studio&theme=x&slowmo=1",
                obj("kinds", kinds, "base", base, "currentKind", "studio", "kind", "studio", "instanceId", "abc")).asString();
        assertEquals("/app?app=genericWorkspace&ws_kind=studio&theme=x&workspace=abc", u);
        // And its tree derives a slug client-side, since the server minted none.
        Value t = global("kindTreeData").execute(kinds, "zoo");
        assertEquals("workspaces", t.getMember("children").getArrayElement(0).getMember("segment").asString());
        assertEquals("demos", t.getMember("children").getArrayElement(1).getMember("segment").asString());
    }

    private Value obj(Object... kv) {
        Value o = js.eval("js", "({})");
        for (int i = 0; i < kv.length; i += 2) o.putMember((String) kv[i], kv[i + 1]);
        return o;
    }
}
