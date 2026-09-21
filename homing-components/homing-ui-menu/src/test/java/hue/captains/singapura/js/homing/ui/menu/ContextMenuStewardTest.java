package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The steward over a fake DOM whose document dispatches in two phases —
 * capture listeners on the document, then the target's — so the press that
 * closes a menu can be shown to reach nothing. The invariants of the
 * appendix, one method each where a fake can hold it; the tree three levels
 * deep, the marks as icons, the specimen.
 */
class ContextMenuStewardTest extends JsModuleTestBase {

    private static final String P = "/homing/js/hue/captains/singapura/js/homing/ui/menu/";

    private static final String SHIM = """
        var log = [], minted = 0;
        var captureListeners = {};   // the document's, capture phase
        function el(tag) {
            var classes = new Set(), attrs = {}, props = {};
            var node = { tag: tag, children: [], parentNode: null, listeners: {}, textContent: "", isConnected: true,
                rect: { left: 0, top: 0, width: 160, height: 32, right: 160, bottom: 32 },
                style: { setProperty: function (k, v) { props[k] = v; }, removeProperty: function (k) { delete props[k]; }, getPropertyValue: function (k) { return props[k] == null ? "" : props[k]; } },
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); },
                             remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); },
                             contains: function (c) { return classes.has(c); }, toggle: function (c, f) { if (f) classes.add(c); else classes.delete(c); } },
                appendChild: function (c) { if (c.parentNode) c.parentNode.removeChild(c); this.children.push(c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                removeAttribute: function (k) { delete attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                removeEventListener: function (t, fn) { var l = this.listeners[t] || []; var i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); },
                contains: function (c) { for (var n = c; n; n = n.parentNode) if (n === this) return true; return false; },
                getBoundingClientRect: function () { return this.rect; },
                focus: function () { document.activeElement = this; log.push("focus:" + (this.name || this.tag)); },
                has: function (c) { return classes.has(c); }, prop: function (k) { return props[k]; }, attr: function (k) { return attrs[k]; } };
            return node;
        }
        function fakeBranch(name) { return { name: name, createElement: function (n, tag) { minted++; var e = el(tag); e.name = n; return e; }, createBranch: function (n) { return fakeBranch(n); }, dissolve: function () { log.push("dissolved:" + name); }, activate: function () {} }; }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); },
                    removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    toggleClass: function (e, c, f) { e.classList.toggle(c, f); } };
        var cm_layer = "cm_layer", cm_frame = "cm_frame", cm_frame_static = "cm_frame_static", cm_specimen = "cm_specimen", cm_item = "cm_item", cm_item_label = "cm_item_label", cm_item_hint = "cm_item_hint",
            cm_item_disclose = "cm_item_disclose", cm_item_disabled = "cm_item_disabled", cm_item_hidden = "cm_item_hidden", cm_separator = "cm_separator";
        var console = { error: function (m, e) { log.push("error:" + m); } };
        var body = el("body");
        var document = { body: body, activeElement: body, listeners: {},
            addEventListener: function (t, fn, cap) { var key = t + (cap ? ":c" : ""); (captureListeners[key] = captureListeners[key] || []).push(fn); },
            removeEventListener: function (t, fn, cap) { var key = t + (cap ? ":c" : ""); var l = captureListeners[key] || []; var i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); } };
        var windowListeners = {};
        var window = { innerWidth: 1000, innerHeight: 600,
            addEventListener: function (t, fn) { (windowListeners[t] = windowListeners[t] || []).push(fn); },
            removeEventListener: function (t, fn) { var l = windowListeners[t] || []; var i = l.indexOf(fn); if (i >= 0) l.splice(i, 1); } };
        var timers = [];
        var setTimeout = function (fn, ms) { timers.push(fn); return timers.length; }, clearTimeout = function () {};
        function runTimers() { var t = timers.slice(); timers = []; t.forEach(function (f) { f(); }); }
        /** An event dispatched as a document does: capture listeners on the document first, then the target's own, unless stopped. */
        function dispatch(target, type, init) {
            var e = init || {}; e.type = type; e.target = target; e.stopped = false; e.defaulted = false;
            e.stopPropagation = function () { e.stopped = true; }; e.preventDefault = function () { e.defaulted = true; };
            (captureListeners[type + ":c"] || []).slice().forEach(function (fn) { if (!e.stopped) fn(e); });
            if (!e.stopped) (target.listeners[type] || []).slice().forEach(function (fn) { fn(e); });
            return e;
        }
        function listening() { var n = 0; for (var k in captureListeners) n += captureListeners[k].length; for (var w in windowListeners) n += windowListeners[w].length; return n; }
        var page = fakeBranch("page");
        var sink = function (ev) { log.push(ev.kind === "Opened" ? "opened:" + ev.menuKind + "@" + ev.x + "," + ev.y : ev.kind === "Picked" ? "picked:" + ev.menuKind + "/" + ev.itemId : "closed:" + ev.menuKind + "/" + ev.reason); };
        // the record a Java registry stamps: three levels, an icon, a section change, a hint
        var MENUS = { animal: { kind: "animal", nodes: [ { id: "rotate", label: "Rotate", icon: "rotate", hint: "a quarter turn" },
                                                          { id: "animal", label: "Animal", section: 1, nodes: [ { id: "cat", label: "Cat" }, { id: "dog", label: "Dog", nodes: [ { id: "big", label: "Big" }, { id: "small", label: "Small" } ] } ] } ] } };
        var steward = new ContextMenuSteward(page.createBranch("menus"), { types: MENUS, onEvent: sink });
        // a chip on the page that selects on a press, as the strip's do
        var chip = el("div"); chip.name = "chip"; body.appendChild(chip);
        chip.addEventListener("pointerdown", function () { log.push("chip:pressed"); });
        chip.addEventListener("click", function () { log.push("chip:clicked"); });
        chip.addEventListener("contextmenu", function () { log.push("chip:contextmenu"); });
        function frame() { return steward._menus.animal.el; }
        function rows(f) { return (f || frame()).children.filter(function (c) { return c.has("cm_item"); }); }
        function row(id, f) { return rows(f).find(function (r) { return r.attr("data-id") === id; }); }
        function mark(r) { return r.children[0]; }
        function sub(i) { return steward._menus.animal._path[i].sub.el; }
        function dividers(f) { return (f || frame()).children.filter(function (c) { return c.has("cm_separator"); }).length; }
        function cursor(f) { var r = rows(f).find(function (r) { return r.attr("data-highlighted") === "true"; }); return r ? r.attr("data-id") : "-"; }
        function menusInDom() { return body.children.filter(function (c) { return c.has("cm_layer"); }).reduce(function (n, l) { return n + l.children.filter(function (c) { return c.has("cm_frame"); }).length; }, 0); }
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(P + "MenuEventsModule.js");
        loadModule(P + "MenuGeometryModule.js");
        loadModule(P + "MenuTreeModule.js");
        // the icon component reads ICONS as it loads: the wardrobe's names first, as the served module would import them
        js.eval("js", "var ic_base = 'ic_base', ICONS = { check: 'ic_check', disclose: 'ic_disclose', rotate: 'ic_rotate', flip: 'ic_flip', reset: 'ic_reset' };");
        loadModule("/homing/js/hue/captains/singapura/js/homing/ui/icons/IconModule.js");
        loadModule(P + "ContextMenuModule.js");
        loadModule(P + "ContextMenuStewardModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }
    private String log() { return eval("log.join(' ')").asString(); }

    @Test
    void oneMenuGlobally_aSecondOpenReplacesTheFirst_andThereIsNeverMoreThanOneFrameInTheDocument() {
        eval("steward.define('card').row('open', 'Open').divider().row('remove', 'Remove').done();");
        assertTrue(eval("steward.open('animal', { id: 1 }, { x: 100, y: 100 })").asBoolean());
        assertEquals("1", eval("String(menusInDom())").asString());
        assertEquals("animal", eval("steward.active()").asString());
        eval("log.length = 0;");
        assertTrue(eval("steward.open('card', { id: 2 }, { x: 200, y: 200 })").asBoolean());
        assertEquals("focus:body closed:animal/replaced focus:frame opened:card@200,200", log(), "the first is told replaced before the second opens");
        assertEquals("1", eval("String(menusInDom())").asString(), "never two frames in the document");
        assertEquals("2", eval("String(steward.bound().id)").asString());
        assertEquals("1", eval("String(dividers(steward._menus.card.el))").asString(), "a divider where the builder's section changed");
    }

    @Test
    void lazy_nothingMintedAtConstruction_aKindAtItsFirstOpenOnly_andNothingListenedToWhileClosed() {
        assertEquals("0", eval("String(minted)").asString(), "a steward mints nothing");
        assertEquals("0", eval("String(listening())").asString());
        eval("steward.open('animal', {}, { x: 10, y: 10 });");
        int after = eval("minted").asInt();
        assertTrue(after > 0, "the kind's instance and the layer, at the first open");
        assertTrue(eval("listening() > 0").asBoolean(), "listening while open");
        eval("steward.close(); for (var i = 0; i < 100; i++) { steward.open('animal', { i: i }, { x: 10, y: 10 }); steward.close(); }");
        assertEquals(String.valueOf(after), eval("String(minted)").asString(), "a hundred opens mint nothing");
        assertEquals("0", eval("String(listening())").asString(), "nothing listened to while closed");
        assertEquals("0", eval("String(menusInDom())").asString());
        assertTrue(eval("steward.bound() === null && steward.active() === null").asBoolean());
    }

    @Test
    void thePressThatClosesIsSwallowed_theClickToo_butTheRightPressesContextmenuGoesThrough() {
        eval("steward.open('animal', {}, { x: 10, y: 10 }); log.length = 0;");
        eval("var down = dispatch(chip, 'pointerdown', { button: 0, pointerId: 1 });");
        assertEquals("focus:body closed:animal/outside", log(), "closed on the press outside; the chip never heard the press");
        assertTrue(eval("down.stopped && down.defaulted").asBoolean());
        eval("log.length = 0; dispatch(chip, 'pointerup', { pointerId: 1 }); dispatch(chip, 'mouseup', {}); dispatch(chip, 'click', {}); runTimers();");
        assertEquals("", log(), "the release and the click of that gesture reached nothing");
        eval("dispatch(chip, 'click', {});");
        assertEquals("chip:clicked", log(), "the next gesture is the chip's again");
        assertEquals("1", eval("String(dividers())").asString(), "one divider, where the section changes");
        // a right-press outside: closed and swallowed as a press, and its contextmenu goes through
        eval("steward.open('animal', {}, { x: 10, y: 10 }); log.length = 0; dispatch(chip, 'pointerdown', { button: 2, pointerId: 1 }); dispatch(chip, 'pointerup', { pointerId: 1 }); dispatch(chip, 'contextmenu', {});");
        assertEquals("focus:body closed:animal/outside chip:contextmenu", log());
        // a press inside is the menu's
        eval("steward.open('animal', {}, { x: 10, y: 10 }); log.length = 0; var inside = dispatch(row('rotate'), 'pointerdown', { button: 0, pointerId: 1 });");
        assertEquals("", log(), "not closed");
        assertFalse(eval("inside.stopped").asBoolean());
    }

    @Test
    void aPickIsPickedThenClosed_theHandlerActingLast_andAnUnhandledKindOnlyReports() {
        eval("steward.handle('animal', { pick: function (id, o) { log.push('act:' + id + ':' + o.name); }, close: function (r, o) { log.push('handler-close:' + r + ':' + o.name); } });");
        eval("steward.open('animal', { name: 'fox' }, { x: 10, y: 10 }); log.length = 0; dispatch(row('rotate'), 'click', {});");
        assertEquals("picked:animal/rotate focus:body handler-close:pick:fox closed:animal/pick act:rotate:fox", log());
        assertTrue(eval("steward.bound() === null").asBoolean(), "unbound on the pick");
        eval("steward.handle('animal', null); steward.open('animal', {}, { x: 10, y: 10 }); log.length = 0; dispatch(row('rotate'), 'click', {});");
        assertEquals("picked:animal/rotate focus:body closed:animal/pick", log(), "no handler: reported and nothing else");
    }

    @Test
    void theStateOfEachRowIsTheHandlers_askedAtBind_theMarksAreIcons_andASubmenuOpensBesideItsRow() {
        eval("steward.handle('animal', { state: function (id, o) { return id === 'rotate' ? { disabled: o.fixed } : id === o.animal ? { checked: true } : id === 'dog' ? { hidden: o.noDogs } : null; } });");
        eval("steward.open('animal', { fixed: true, animal: 'cat', noDogs: true }, { x: 10, y: 10 });");
        assertTrue(eval("row('rotate').has('cm_item_disabled') && row('rotate').attr('aria-disabled') === 'true'").asBoolean());
        eval("var sub0 = row('animal'); dispatch(sub0, 'pointerenter', {});");
        assertEquals("true", eval("sub0.attr('aria-expanded')").asString(), "hovered: the submenu is open");
        assertEquals("2", eval("String(menusInDom())").asString(), "the submenu is a second frame in the layer, part of the one menu");
        assertTrue(eval("mark(row('cat', sub(0))).has('ic_check') && mark(row('cat', sub(0))).has('ic_base')").asBoolean(), "the checked one's mark is the check icon");
        assertTrue(eval("mark(row('rotate')).has('ic_rotate')").asBoolean(), "a row's own icon on its mark");
        assertTrue(eval("!mark(row('animal')).has('ic_rotate') && row('animal').children[row('animal').children.length - 1].has('ic_disclose')").asBoolean(), "a row with rows ends in the disclose mark");
        assertTrue(eval("row('dog', sub(0)).has('cm_item_hidden')").asBoolean());
        eval("steward.close(); steward.open('animal', { fixed: false, animal: 'dog', noDogs: false }, { x: 10, y: 10 });");
        assertFalse(eval("row('rotate').has('cm_item_disabled')").asBoolean(), "bound afresh: the state is this object's");
        assertEquals("false", eval("row('animal').attr('aria-expanded')").asString(), "no submenu open after a rebind");
        assertTrue(eval("!mark(row('cat', steward._menus.animal._root.rows[1].sub.el)).has('ic_check')").asBoolean(), "the check off a row no longer checked");
    }

    @Test
    void keys_areCapturedWhileOpen_moveTheCursorSkippingDisabled_walkThreeLevels_pickAndEscape() {
        eval("steward.handle('animal', { state: function (id) { return id === 'rotate' ? { disabled: true } : null; } });");
        eval("steward.open('animal', {}, { x: 10, y: 10 }, { keyboard: true }); log.length = 0;");
        assertEquals("animal", eval("cursor()").asString(), "from the keyboard: the first enabled row — Rotate is disabled, the divider is not a row");
        var down = eval("dispatch(document.body, 'keydown', { key: 'ArrowDown' })");
        assertTrue(down.getMember("stopped").asBoolean() && down.getMember("defaulted").asBoolean(), "captured: the page behind sees nothing");
        assertEquals("animal", eval("cursor()").asString(), "the only enabled row: itself");
        eval("dispatch(document.body, 'keydown', { key: 'ArrowRight' });");
        assertEquals("true", eval("row('animal').attr('aria-expanded')").asString());
        assertEquals("cat", eval("cursor(sub(0))").asString(), "into the submenu, on its first");
        eval("dispatch(document.body, 'keydown', { key: 'ArrowDown' });");
        assertEquals("dog", eval("cursor(sub(0))").asString());
        eval("dispatch(document.body, 'keydown', { key: 'ArrowRight' });");
        assertEquals("3", eval("String(menusInDom())").asString(), "a third level: three frames on the path");
        assertEquals("big", eval("cursor(sub(1))").asString(), "into the third level, on its first");
        eval("dispatch(document.body, 'keydown', { key: 'ArrowLeft' });");
        assertEquals("dog", eval("cursor(sub(0))").asString(), "back to the second, on the row that opened the third");
        assertEquals("2", eval("String(menusInDom())").asString());
        eval("dispatch(row('cat', sub(0)), 'pointerenter', {});");
        assertEquals("cat", eval("cursor(sub(0))").asString(), "hovering a row of the second level leaves the second open");
        eval("dispatch(document.body, 'keydown', { key: 'ArrowUp' });");
        assertEquals("dog", eval("cursor(sub(0))").asString(), "wrapped");
        eval("dispatch(document.body, 'keydown', { key: 'ArrowUp' });");
        eval("dispatch(document.body, 'keydown', { key: 'ArrowLeft' });");
        assertEquals("false", eval("row('animal').attr('aria-expanded')").asString(), "back out");
        assertEquals("animal", eval("cursor()").asString());
        eval("dispatch(document.body, 'keydown', { key: 'Enter' }); dispatch(document.body, 'keydown', { key: 'Enter' });");
        assertEquals("picked:animal/cat focus:body closed:animal/pick", log(), "Enter opens the submenu, Enter picks its first");
        eval("steward.open('animal', {}, { x: 10, y: 10 }); log.length = 0; var esc = dispatch(document.body, 'keydown', { key: 'Escape' });");
        assertEquals("focus:body closed:animal/escape", log());
        assertTrue(eval("esc.stopped").asBoolean(), "Escape captured: a dialog behind does not also close");
    }

    @Test
    void scrollResizeAndBlurClose_theOwnerCloses_andAKindIsDeclaredOnce_heldToTheTreesRules() {
        eval("steward.open('animal', {}, { x: 10, y: 10 }); log.length = 0; dispatch(chip, 'scroll', {});");
        assertEquals("", log(), "a scroll of something unrelated — a log filling behind the menu — is nothing");
        eval("dispatch(document, 'scroll', {});");
        assertEquals("focus:body closed:animal/scroll", log(), "a scroll of the page closes");
        eval("steward.open('animal', {}, { x: 10, y: 10 }, { anchor: chip }); log.length = 0; dispatch(body, 'scroll', {});");
        assertEquals("focus:body closed:animal/scroll", log(), "a scroll of a box that holds the anchor closes");
        eval("steward.open('animal', {}, { x: 10, y: 10 }); log.length = 0; windowListeners.resize[0]();");
        assertEquals("focus:body closed:animal/scroll", log());
        eval("steward.open('animal', {}, { x: 10, y: 10 }); log.length = 0; windowListeners.blur[0]();");
        assertEquals("focus:body closed:animal/blur", log());
        eval("steward.open('animal', {}, { x: 10, y: 10 }); log.length = 0; steward.close();");
        assertEquals("focus:body closed:animal/owner", log());
        assertFalse(eval("steward.open('nope', {}, { x: 1, y: 1 })").asBoolean(), "an unknown kind is not taken: the browser's menu stays");
        var ex = assertThrows(PolyglotException.class, () -> eval("steward.define('animal').row('x', 'X').done()"));
        assertTrue(ex.getMessage().contains("declared twice"), ex.getMessage());
        var dup = assertThrows(PolyglotException.class, () -> eval("steward.define('two').row('x', 'X').sub('s', 'S', function (sub) { sub.row('x', 'again'); }).done()"));
        assertTrue(dup.getMessage().contains("row id repeated: x"), dup.getMessage());
        var deep = assertThrows(PolyglotException.class, () -> eval("steward.define('deep').sub('a', 'A', function (a) { a.sub('b', 'B', function (b) { b.sub('c', 'C', function (c) { c.sub('d', 'D', function (d) { d.row('e', 'E'); }); }); }); }).done()"));
        assertTrue(deep.getMessage().contains("last level"), deep.getMessage());
        assertEquals("animal,card", eval("steward.define('card').row('open', 'Open', { icon: 'reset', hint: 'again' }).done().kinds().sort().join(',')").asString());
        assertEquals("open,Open,reset,again,0", eval("var n = steward._types.card.nodes[0]; [n.id, n.label, n.icon, n.hint, n.section].join(',')").asString());
    }

    @Test
    void placedByTheGeometry_withinTheViewport_andFocusReturnsWhereItWas() {
        eval("chip.focus(); log.length = 0; steward.open('animal', {}, { x: 990, y: 590 });");
        assertEquals("focus:frame opened:animal@830,558", log(), "off both edges at 1000×600 with a 160×32 frame: flipped to the left and above");
        assertEquals("830px,558px", eval("frame().prop('--cm-x') + ',' + frame().prop('--cm-y')").asString());
        eval("log.length = 0; steward.close();");
        assertEquals("focus:chip closed:animal/owner", log(), "the focus goes back to the chip that had it");
    }

    @Test
    void aSpecimen_isTheWholeTreeOpenInAHost_boundThroughTheHandler_andPicksNothing() {
        eval("steward.handle('animal', { pick: function (id) { log.push('act:' + id); }, state: function (id, o) { return id === o.animal ? { checked: true } : id === 'rotate' ? { disabled: true } : null; } });");
        eval("var host = el('div'); body.appendChild(host); var s = steward.specimen('animal', host, { animal: 'cat' }); log.length = 0;");
        assertEquals("3", eval("String(host.children.length)").asString(), "the root and both submenus, all in the host");
        assertTrue(eval("host.children.every(function (f) { return f.has('cm_frame') && f.has('cm_frame_static'); })").asBoolean(), "each frame static: in the host's flow");
        assertTrue(eval("row('animal', host.children[0]).attr('aria-expanded') === 'true' && row('animal', host.children[0]).attr('data-highlighted') === 'true'").asBoolean(), "a row with rows shown open");
        assertTrue(eval("mark(row('cat', host.children[1])).has('ic_check') && row('rotate', host.children[0]).has('cm_item_disabled')").asBoolean(), "bound through the kind's handler");
        eval("dispatch(row('cat', host.children[1]), 'click', {}); dispatch(row('small', host.children[2]), 'pointerenter', {});");
        assertEquals("", log(), "nothing picked, nothing closed");
        assertEquals("small", eval("cursor(host.children[2])").asString(), "hover still moves the cursor: the design's highlighted row on view");
        assertEquals("0", eval("String(menusInDom())").asString(), "no live menu");
        assertTrue(eval("steward.open('animal', {}, { x: 10, y: 10 })").asBoolean(), "the live instance is another; the specimen stays");
        assertEquals("3", eval("String(host.children.length)").asString());
        eval("steward.close(); s.dispose();");
        assertTrue(log().contains("dissolved:specimen-animal-1"), log());
    }

    @Test
    void onePerPage_andDisposeDissolvesEverything() {
        var ex = assertThrows(PolyglotException.class, () -> eval("new ContextMenuSteward(page.createBranch('another'), {})"));
        assertTrue(ex.getMessage().contains("one per page"), ex.getMessage());
        eval("steward.open('animal', {}, { x: 10, y: 10 }); log.length = 0; steward.dispose();");
        assertTrue(log().contains("closed:animal/owner") && log().contains("dissolved:type-animal") && log().endsWith("dissolved:menus"), log());
        eval("new ContextMenuSteward(page.createBranch('again'), {})");
    }
}
