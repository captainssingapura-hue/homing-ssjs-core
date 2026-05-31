package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.graalvm.polyglot.PolyglotException;

/**
 * Unit test for {@code TabRegistry}. Pure state container — no
 * collaborators, no async. Tests cover register/unregister/lookup/
 * updateSlot/inspect, plus the required-field validation in register.
 */
class TabRegistryTest extends JsModuleTestBase {

    private static final String MODULE =
            "/homing/js/hue/captains/singapura/js/homing/workspace/shell/TabRegistryModule.js";

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(MODULE);
    }

    private Value freshRegistry() {
        return global("TabRegistry").newInstance();
    }

    private Value entry(String uuid, String tabId, String slot, String kind) {
        Value e = js.eval("js", "({})");
        e.putMember("widgetInstanceUuid", uuid);
        e.putMember("tab",                js.eval("js", "({ id: '" + tabId + "' })"));
        e.putMember("slotId",             slot);
        e.putMember("widgetKind",         kind);
        return e;
    }

    @Test
    void registerAndLookupReturnsTabSlotKind() {
        Value r = freshRegistry();
        r.invokeMember("register", entry("uuid-1", "t-1", "tl", "DocViewWidget"));

        Value e = r.invokeMember("lookup", "uuid-1");
        assertNotNull(e);
        assertEquals("tl",            e.getMember("slotId").asString());
        assertEquals("DocViewWidget", e.getMember("widgetKind").asString());
        assertEquals("t-1",           e.getMember("tab").getMember("id").asString());
    }

    @Test
    void lookupUnknownReturnsNull() {
        Value e = freshRegistry().invokeMember("lookup", "ghost");
        assertTrue(e == null || e.isNull());
    }

    @Test
    void tabIdOfReturnsTabIdOrNull() {
        Value r = freshRegistry();
        r.invokeMember("register", entry("u", "tx", "tl", "K"));
        assertEquals("tx", r.invokeMember("tabIdOf", "u").asString());
        Value none = r.invokeMember("tabIdOf", "ghost");
        assertTrue(none == null || none.isNull());
    }

    @Test
    void unregisterRemovesEntry() {
        Value r = freshRegistry();
        r.invokeMember("register", entry("u", "t", "tl", "K"));
        assertEquals(1, r.invokeMember("size").asInt());
        r.invokeMember("unregister", "u");
        assertEquals(0, r.invokeMember("size").asInt());
        assertNull(r.invokeMember("tabIdOf", "u").as(String.class));
    }

    @Test
    void unregisterUnknownIsNoOp() {
        Value r = freshRegistry();
        r.invokeMember("unregister", "ghost");
        assertEquals(0, r.invokeMember("size").asInt());
    }

    @Test
    void updateSlotUpdatesOnlyTheSlotField() {
        Value r = freshRegistry();
        r.invokeMember("register", entry("u", "t", "tl", "K"));
        r.invokeMember("updateSlot", "u", "br");
        Value e = r.invokeMember("lookup", "u");
        assertEquals("br", e.getMember("slotId").asString());
        assertEquals("K",  e.getMember("widgetKind").asString(),
                "kind not touched by updateSlot");
    }

    @Test
    void updateSlotForUnknownUuidIsNoOp() {
        Value r = freshRegistry();
        r.invokeMember("updateSlot", "ghost", "tr");
        assertEquals(0, r.invokeMember("size").asInt());
    }

    @Test
    void registerRejectsMissingRequiredFields() {
        Value r = freshRegistry();
        assertThrows(PolyglotException.class, () ->
                r.invokeMember("register",
                        js.eval("js", "({ tab: {id:'t'}, slotId: 'tl', widgetKind: 'K' })")),
                "missing widgetInstanceUuid → throws");
        assertThrows(PolyglotException.class, () ->
                r.invokeMember("register",
                        js.eval("js", "({ widgetInstanceUuid: 'u', slotId: 'tl', widgetKind: 'K' })")),
                "missing tab → throws");
        assertThrows(PolyglotException.class, () ->
                r.invokeMember("register",
                        js.eval("js", "({ widgetInstanceUuid: 'u', tab: {id:'t'}, widgetKind: 'K' })")),
                "missing slotId → throws");
        assertThrows(PolyglotException.class, () ->
                r.invokeMember("register",
                        js.eval("js", "({ widgetInstanceUuid: 'u', tab: {id:'t'}, slotId: 'tl' })")),
                "missing widgetKind → throws");
    }

    @Test
    void findByTabIdReturnsMatchingEntry() {
        Value r = freshRegistry();
        r.invokeMember("register", entry("u1", "picker:1", "tl", "A"));
        r.invokeMember("register", entry("u2", "DocView:pinned", "tl", "DocView"));

        Value hit = r.invokeMember("findByTabId", "picker:1");
        assertNotNull(hit);
        assertEquals("u1", hit.getMember("widgetInstanceUuid").asString());
        assertEquals("A",  hit.getMember("entry").getMember("widgetKind").asString());

        Value miss = r.invokeMember("findByTabId", "ghost");
        assertTrue(miss == null || miss.isNull());

        Value nullArg = r.invokeMember("findByTabId", (Object) null);
        assertTrue(nullArg == null || nullArg.isNull());
    }

    @Test
    void inspectListsAllEntries() {
        Value r = freshRegistry();
        r.invokeMember("register", entry("u1", "t1", "tl", "A"));
        r.invokeMember("register", entry("u2", "t2", "br", "B"));

        Value snap = r.invokeMember("inspect");
        assertEquals(2, snap.getMember("size").asInt());
        assertEquals(2, snap.getMember("entries").getArraySize());
    }
}
