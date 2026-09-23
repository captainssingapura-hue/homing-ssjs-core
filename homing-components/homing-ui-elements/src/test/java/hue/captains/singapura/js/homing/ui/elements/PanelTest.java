package hue.captains.singapura.js.homing.ui.elements;

import hue.captains.singapura.js.homing.ssjs.test.JsModuleTestBase;
import org.graalvm.polyglot.Value;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The panel: a head that names it with a slot for what acts on it, a body the
 * caller mounts into, and nothing else. A panel that is filled by what is
 * mounted in it keeps no air of its own and does not scroll; one that holds
 * content has both. It listens to nothing and takes no keys, and its branch
 * goes with it.
 */
class PanelTest extends JsModuleTestBase {

    private static final String DIR = "/homing/js/hue/captains/singapura/js/homing/";

    private static final String SHIM = """
        var log = [], sized = [];
        function el(tag) {
            var classes = new Set(), attrs = {};
            var node = { tag: tag, tagName: tag.toUpperCase(), nodeType: 1, children: [], parentNode: null, listeners: {}, textContent: "",
                classList: { add: function () { for (var i = 0; i < arguments.length; i++) classes.add(arguments[i]); }, remove: function () { for (var i = 0; i < arguments.length; i++) classes.delete(arguments[i]); }, contains: function (c) { return classes.has(c); } },
                appendChild: function (c) { this.children.push(c); c.parentNode = this; return c; },
                removeChild: function (c) { var i = this.children.indexOf(c); if (i >= 0) this.children.splice(i, 1); c.parentNode = null; return c; },
                removeAttribute: function (k) { delete attrs[k]; },
                contains: function (o) { for (var x = o; x; x = x.parentNode) if (x === this) return true; return false; },
                setAttribute: function (k, v) { attrs[k] = String(v); }, getAttribute: function (k) { return attrs[k] == null ? null : attrs[k]; },
                addEventListener: function (t, fn) { (this.listeners[t] = this.listeners[t] || []).push(fn); },
                classes: function () { return Array.from(classes).join(" "); },
                listening: function (t) { return (this.listeners[t] || []).length; } };
            return node;
        }
        function fakeBranch(name) { return { name: name, createElement: function (n, tag) { var e = el(tag); e.name = n; return e; }, createBranch: function (n) { return fakeBranch(n); }, dissolve: function () { log.push("dissolved:" + name); }, activate: function () {} }; }
        var css = { addClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.add(arguments[i]); }, removeClass: function (e) { for (var i = 1; i < arguments.length; i++) e.classList.remove(arguments[i]); },
                    size: function (e, v) { sized.push(e.name + ":" + v); }, extent: function () {}, aspect: function () {},
                    elevation: function (e, v) { lifted.push(e.name + ":" + v); } };
        var lifted = [];
        ["el_panel", "el_panel_head", "el_panel_title", "el_panel_slot", "el_panel_body", "el_panel_body_air", "el_panel_current", "el_panel_framed"].forEach(function (c) { globalThis[c] = c; });
        var observers = [];
        function MutationObserver(fn) { this.fn = fn; this.el = null; observers.push(this); }
        MutationObserver.prototype.observe = function (el, opts) { this.el = el; this.opts = opts; el._observer = this; };
        MutationObserver.prototype.saw = function (target) { this.fn([{ target: target }]); };
        MutationObserver.prototype.disconnect = function () { if (this.el) delete this.el._observer; this.el = null; };
        function keys(el, v) { el.setAttribute("data-keys", v); for (var x = el; x; x = x.parentNode) if (x._observer) { x._observer.saw(el); return; } }
        function unkeys(el) { el.removeAttribute("data-keys"); for (var x = el; x; x = x.parentNode) if (x._observer) { x._observer.saw(el); return; } }
        var page = fakeBranch("page");
        var host = el("div");
        var panel = new PanelBuilder().title("Dock A").fills().host(host).build(page.createBranch("dock-a"));
        var reading = new PanelBuilder().title("Notes").build(page.createBranch("notes"));
        var bare = new PanelBuilder().build(page.createBranch("bare"));
        """;

    @BeforeEach
    void load() {
        js = buildContext();
        loadModule(DIR + "ui/elements/PanelModule.js");
        js.eval("js", SHIM);
    }

    private Value eval(String src) { return js.eval("js", src); }

    /** The parts, and where they are: a head that names it, a slot at its end, a body under it; appended to the host it was given. */
    @Test
    void aHeadThatNamesIt_aSlotForWhatActsOnIt_andABodyToMountInto() {
        assertEquals("section", eval("panel.root.tag").asString());
        assertEquals("Dock A", eval("panel.title()").asString());
        assertEquals("Dock A", eval("panel.root.getAttribute('aria-label')").asString(), "the region is named for whoever cannot see it");
        assertEquals("header,div", eval("panel.root.children.map(function (c) { return c.tag; }).join()").asString(), "the head over the body");
        assertEquals("h2,div", eval("panel.head.children.map(function (c) { return c.tag; }).join()").asString(), "the name, then the slot");
        assertTrue(eval("panel.controls === panel.head.children[1]").asBoolean(), "the slot is the caller's to fill");
        assertTrue(eval("host.children[0] === panel.root").asBoolean(), "appended to the host it was given");
        assertEquals("Dock B", eval("panel.title('Dock B'); panel.title()").asString());
        assertEquals("Dock B", eval("panel.root.getAttribute('aria-label')").asString(), "renamed, and said again");
    }

    /** A panel with nothing to say has no head at all: a body alone, and nothing to name. */
    @Test
    void aPanelWithoutATitle_hasNoHead() {
        assertTrue(eval("bare.head === null && bare.controls === null").asBoolean());
        assertEquals("div", eval("bare.root.children.map(function (c) { return c.tag; }).join()").asString(), "the body alone");
        assertTrue(eval("bare.title() === null").asBoolean(), "there is no name to read");
        assertEquals("null", eval("String(bare.root.getAttribute('aria-label'))").asString());
    }

    /** What is mounted fills the body, or the body holds content: the difference is the air and the scroll, and it is a class, not a style. */
    @Test
    void aFilledBodyKeepsNoAirOfItsOwn_aReadingOneDoes() {
        assertEquals("el-panel-body", eval("panel.body.classes()").asString().replace("el_panel_body", "el-panel-body"), "filled: the layout alone");
        assertTrue(eval("reading.body.classes().indexOf('el_panel_body_air') >= 0").asBoolean(), "content: the design's air and the scroll");
        assertFalse(eval("panel.body.classes().indexOf('el_panel_body_air') >= 0").asBoolean());
    }

    /** The size is an element's, not inherited, so the panel carries it to every part it minted - and not to a body that wears no length. */
    @Test
    void theSizeReachesEveryPartThePanelMinted() {
        eval("sized = []; reading.size(0.5)");
        assertEquals("panel:0.5 head:0.5 title:0.5 controls:0.5 body:0.5", eval("sized.join(' ')").asString());
        eval("sized = []; panel.size(0)");
        assertEquals("panel:null head:null title:null controls:null", eval("sized.join(' ')").asString(), "a filled body wears no length to grow");
    }

/**
     * Two axes, and neither of them means anything by itself: the depth the
     * design answers in its own plane, the colour that says current. What a
     * state maps to is the app's, said in the app's code — the panel links
     * nothing.
     */
    @Test
    void theTwoRegistersAndTheColourAreSeparate_andThePanelSaysWhatItIsTold() {
        assertTrue(eval("panel.elevationOf() === null").asBoolean(), "flat, and no default was ever chosen for it");
        eval("lifted = []; panel.elevation(\"elevated\")");
        assertEquals("panel:elevated", eval("lifted.join(' ')").asString());
        eval("lifted = []; panel.elevation(\"sunken\")");
        assertEquals("panel:sunken", eval("lifted.join(' ')").asString(), "the other register");
        eval("lifted = []; panel.elevation(\"lofty\")");
        assertEquals("panel:null", eval("lifted.join(' ')").asString(), "two registers and flat; anything else is flat");
        eval("lifted = []; panel.elevation(null)");
        assertEquals("panel:null", eval("lifted.join(' ')").asString(), "flat says nothing at all: the design rests it where it rests it");
        assertFalse(eval("panel.isHighlighted()").asBoolean());
        assertFalse(eval("panel.root.classes().indexOf(\"el_panel_current\") >= 0").asBoolean());
        eval("panel.highlight(true)");
        assertTrue(eval("panel.isHighlighted() && panel.root.classes().indexOf(\"el_panel_current\") >= 0").asBoolean());
        eval("lifted = []; panel.elevation(\"elevated\")");
        assertTrue(eval("panel.isHighlighted() && panel.root.classes().indexOf(\"el_panel_current\") >= 0").asBoolean(), "orthogonal: the depth does not touch the colour");
        eval("panel.highlight(false)");
        assertFalse(eval("panel.root.classes().indexOf(\"el_panel_current\") >= 0").asBoolean());
        assertEquals("elevated", eval("panel.elevationOf()").asString(), "nor the colour the depth");
    }

    /**
     * Watching what is mounted: the panel is active while the keys are in it —
     * held, or lent to a control of its own — and not while they are merely
     * offered to it. It reads one attribute and tells the mounted thing
     * nothing; unwatch and dispose stop it.
     */
    @Test
    void itFollowsTheKeysOfWhatIsMountedInIt() {
        eval("var said = []; var dock = { root: el(\"div\") }; panel.body.appendChild(dock.root); panel.watch(dock, function (on) { said.push(on); })");
        assertFalse(eval("panel.isWithin()").asBoolean(), "nothing said yet: not active");
        eval("keys(dock.root, \"held\")");
        assertTrue(eval("panel.isWithin()").asBoolean(), "the keys are in it");
        eval("keys(dock.root, \"lent\")");
        assertTrue(eval("panel.isWithin()").asBoolean(), "lent to a control of its own: still the region being worked in");
        eval("keys(dock.root, \"candidate\")");
        assertFalse(eval("panel.isWithin()").asBoolean(), "offered is not held");
        eval("unkeys(dock.root); var inner = el(\"div\"); dock.root.appendChild(inner); keys(inner, \"held\")");
        assertTrue(eval("panel.isWithin()").asBoolean(), "the dock handed the keys to a tab's widget: the region is still the one being worked in");
        eval("unkeys(inner)");
        assertFalse(eval("panel.isWithin()").asBoolean(), "and quiet when the keys leave it altogether");
        eval("keys(inner, \"held\"); dock.root.removeChild(inner); keys(dock.root, \"held\"); unkeys(dock.root)");
        assertFalse(eval("panel.isWithin()").asBoolean(), "what was taken out of the panel holds nothing");
        eval("keys(dock.root, \"held\"); panel.unwatch(); keys(dock.root, \"candidate\")");
        assertTrue(eval("panel.isWithin()").asBoolean(), "unwatched: it keeps what it says");
        assertEquals(0, eval("dock.root.listening(\"keydown\") + dock.root.listening(\"pointerdown\")").asInt(), "the mounted thing is told nothing and given nothing");
        assertEquals("true false true false true false true", eval("said.join(' ')").asString(), "it said each turn once, and only on a change");
        assertFalse(eval("panel.root.classes().indexOf(\"el_panel_current\") >= 0 || panel.elevationOf() !== null").asBoolean(),
                    "and decided nothing by it: no colour, no depth, until an app says what it means");
        assertTrue(eval("panel.watch(dock.root) === panel").asBoolean(), "an element does as well as a component");
        assertFalse(eval("panel.isWithin()").asBoolean(), "read afresh on watching: candidate is not held");
    }

    /** Furniture: no listener anywhere, and nothing of the keyboard party. */
    @Test
    void itListensToNothing() {
        assertEquals(0, eval("panel.root.listening('keydown') + panel.root.listening('pointerdown') + panel.root.listening('click') + panel.head.listening('pointerdown')").asInt());
        assertTrue(eval("panel.focus === undefined && panel.keyDown === undefined").asBoolean(), "a panel is not a member of the party");
    }

    /** The panel and its branch go together; what was mounted in the body is the mounter's. */
    @Test
    void disposeTakesTheRootOutAndDissolvesTheBranch() {
        eval("log = []; var mounted = el('div'); panel.body.appendChild(mounted); panel.dispose()");
        assertEquals("0", eval("String(host.children.length)").asString(), "out of the host");
        assertEquals("dissolved:dock-a", eval("log.join(' ')").asString());
        assertTrue(eval("mounted.parentNode === panel.body").asBoolean(), "what was mounted is left as it was: the mounter disposes it");
    }
}
