// =============================================================================
// SliderGroup — sliders that share the keys: ONE member of the keyboard party
// for all of them, and one of them current. A BRANCH component made through
// its builder; the sliders are built inside it, on sub-branches of its own,
// without a steward of their own — the group holds the keys and hands them
// to the current slider. A header with the title, then the body.
//
//   var g = new SliderGroupBuilder().title("Mixer").keyboard(steward).across()
//               .build(branch.createBranch("mixer"));
//   var ch1 = g.add("ch1", new SliderBuilder().label("Ch 1").vertical()…);  // the BUILDER: built inside
//     .title(text)             the header's title
//     .keyboard(steward, id?)  the page's KeyboardSteward: the group joins the party as `id` (its
//                              branch's name, unless said); without, the group has no keys
//     .across()                the sliders side by side, as faders; stacked unless said
//   SliderGroup:
//     .root
//     .add(name, builder)      → Slider, built on a sub-branch of the body; the first is current
//     .sliders()               in order
//     .current(s?)             read, or set: a slider, or its index; the ring moves, nothing else
//     .key(ev)                 Tab and Shift+Tab: the next or previous slider is current, wrapping,
//                              and the physical focus goes to its knob (the group's own choice —
//                              the steward never moves it); Escape: the keys given back, and the
//                              key left to travel on; anything else: to the current slider
//     .dispose()
//   The claiming convention on the root: a press anywhere in it — the header,
//   a rail — or the focus arriving in it makes the group the holder; the
//   header's press also puts the physical focus on the current knob. The
//   focus arriving on a knob, by Tab from outside or a press on its rail,
//   makes that slider current. `css` is injected with the styles import.
// =============================================================================

const _groupOwner = Object.freeze({ toString: () => "slider-group" });

class SliderGroup {
    /** The builder's; a caller makes a group through SliderGroupBuilder. */
    constructor(branch, props) {
        if (!branch) throw new Error("[SliderGroup] a branch of its own is required");
        var self = this;
        var p = props || {};
        branch.activate(_groupOwner);
        this.branch = branch;
        this._sliders = [];
        this._current = null;
        this._held = false;

        var root = branch.createElement("group", "div");
        css.addClass(root, el_slider_group);
        root.setAttribute("role", "group");
        this.root = root;
        var header = branch.createElement("header", "div");
        css.addClass(header, el_slider_group_header);
        var title = branch.createElement("title", "h3");
        css.addClass(title, el_slider_group_title);
        title.textContent = p.title == null ? "" : String(p.title);
        if (p.title != null) root.setAttribute("aria-label", String(p.title));
        header.appendChild(title);
        root.appendChild(header);
        var body = branch.createElement("body", "div");
        css.addClass(body, el_slider_group_body);
        if (p.across) css.addClass(body, el_slider_group_body_across);
        root.appendChild(body);
        this._header = header; this._title = title; this._body = body;

        // the keys: through the party
        this._kb = null; this._kbId = null; this._offKeys = null;
        if (p.keyboard) {
            this._kb = p.keyboard;
            this._kbId = this._kb.join(p.keyboardId != null ? String(p.keyboardId) : branch.name, {
                keyDown: function (ev) { return self.key(ev); },
                granted: function () { self._held = true; css.addClass(root, el_slider_group_held); },
                taken: function () { self._held = false; css.removeClass(root, el_slider_group_held); }
            });
            this._offKeys = Keys.claimOn(root, this._kb, this._kbId);
        }
        // the header: a press makes the group the holder (the convention's, on the root) and
        // puts the focus on the current knob; the default kept off so the press does not blur it
        header.addEventListener("pointerdown", function (ev) {
            ev.preventDefault();
            if (self._current) self._current.focusKnob();
        });
        // the focus arriving on a knob makes its slider current
        root.addEventListener("focusin", function (ev) {
            var s = self._sliderOf(ev.target);
            if (s) self.current(s);
        });
    }

    // ── the sliders ───────────────────────────────────────────────────────
    /** A slider built inside, on a sub-branch of the body named `name`; the first one is current. */
    add(name, builder) {
        if (!builder || typeof builder.build !== "function") throw new Error("[SliderGroup] add wants a SliderBuilder");
        var s = builder.build(this.branch.createBranch(name));
        this._sliders.push(s);
        this._body.appendChild(s.root);
        if (this._current === null) this.current(s);
        return s;
    }
    sliders() { return this._sliders.slice(); }
    _sliderOf(el) {
        for (var i = 0; i < this._sliders.length; i++) if (this._sliders[i].root.contains(el)) return this._sliders[i];
        return null;
    }
    /** The current slider: read, or set by the slider or its index; the ring moves, nothing else. */
    current(s) {
        if (s === undefined) return this._current;
        var next = typeof s === "number" ? this._sliders[s] : s;
        if (!next || this._sliders.indexOf(next) < 0) throw new Error("[SliderGroup] current wants a slider of the group");
        if (next === this._current) return this;
        if (this._current) this._current.current(false);
        this._current = next;
        next.current(true);
        return this;
    }
    _step(by) {
        var n = this._sliders.length;
        if (!n) return;
        var i = this._sliders.indexOf(this._current);
        this.current(((i < 0 ? 0 : i) + by + n) % n);
        this._current.focusKnob();
    }

    // ── the keys ──────────────────────────────────────────────────────────
    /** A keydown while the group holds: Tab cycles, Escape gives the keys back, the rest is the current slider's. */
    key(ev) {
        if (!ev) return false;
        if (ev.key === "Tab") { this._step(ev.shiftKey ? -1 : 1); return true; }
        if (ev.key === "Escape") { if (this._kb) this._kb.release(this._kbId); return false; }
        return this._current ? this._current.key(ev) : false;
    }

    dispose() {
        if (this._offKeys) { this._offKeys(); this._offKeys = null; }
        if (this._kb) { this._kb.leave(this._kbId); this._kb = null; }
        for (var i = 0; i < this._sliders.length; i++) this._sliders[i].dispose();
        this._sliders = []; this._current = null;
        this.branch.dissolve();
    }
}

class SliderGroupBuilder {
    constructor() { this._props = {}; }
    title(text)           { this._props.title = text; return this; }
    keyboard(steward, id) { this._props.keyboard = steward; this._props.keyboardId = id; return this; }
    across()              { this._props.across = true; return this; }
    build(branch) {
        if (!branch) throw new Error("[SliderGroupBuilder] build wants the sub-branch the caller made for the group");
        return new SliderGroup(branch, this._props);
    }
}
