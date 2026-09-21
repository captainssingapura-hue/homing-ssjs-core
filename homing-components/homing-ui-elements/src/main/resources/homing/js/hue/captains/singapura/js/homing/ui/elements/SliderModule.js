// =============================================================================
// Slider — a number set by a knob on a track, as a BRANCH component made
// through its builder: the caller makes a sub-branch for it and hands it in.
// A label before the rail, a readout after; the track is sunk, the fill runs
// from the detent to the value, the knob is raised and takes the focus, and
// carries a mark — an Icon: a grip, or the word for what the slider sets.
// The knob's look is its face's: the design's corner, or a clip that cuts it
// to a diamond, a hexagon, a pointer — or no face at all, the mark alone on
// the track, at the knob's own size; the box beneath keeps the ring.
// Every part is a real element wearing a design word, so a design draws the
// whole of it: no vendor pseudo-element, nothing of the browser's own slider.
//
//   var s = new SliderBuilder().label("The tabs' size").axis()          // −1…1 by 0.1, the detent at 0
//               .format(function (v) { return v.toFixed(1); })
//               .onInput(function (v) { dock.size(v); })                 // live
//               .onChange(function (v) { store.set(v); })                // on release, or a key
//               .build(branch.createBranch("size"));
//     .range(min, max, step)   any range; .axis() is range(−1, 1, 0.1) with a detent at 0
//     .value(v)  .detent(v…)   where the knob rests: the fill runs from the first detent, else from min
//     .icon(name)              the mark on the knob: an Icon word — "size", "aspect", "extent", "level" —
//                              for what the slider sets; "grip" unless said; null for a bare knob
//     .vertical()              stood up, as a fader: the value rises upward, the knob is the
//                              design's cap — wide across the track, low along it — the
//                              label above, the readout below
//     .ticks([{ at, label }…]) a scale beside the track: a tick at each value, its caption
//     .size(s)                 −1 … 1: every length the design gives the slider grows by its ratio
//     .labelWidth(css)         one width for the labels of sliders that stack, so their rails align
//     .keyboard(steward, id?)  the page's KeyboardSteward: the slider joins the keyboard party as
//                              `id` (its branch's name, unless said) and takes its keys through it,
//                              claiming on a press or the focus arriving in it; without, it has no
//                              keys of its own — a slider in a group is built so, and the group
//                              hands it the keys it holds
//   Slider:
//     .root                    the row: label, rail, readout
//     .value(v?)               read, or set — clamped, on the step, snapped to a detent within its bite
//     .setOn(on)               off is inert and says so
//     .size(s)                 live, on the slider and its parts
//     .label(text?)            read, or set
//     .icon(name?)             read, or set: the knob's mark
//     .labelWidth(css?)        the label's width: a length, or null for its own
//     .key(ev)                 a keydown, from whoever holds the keys for it: true when taken
//     .current(on)             the one of a group: the knob's ring drawn now
//     .focusKnob()             physical focus to the knob: a group's, on Tab
//     .dispose()
//   The rail takes the hand anywhere on it: a press jumps and grabs, the
//   pointer captured at the press; the knob is the focusable part (role
//   slider, the value in aria). The keys, through the party: arrows by a
//   step, with Shift by ten, Home and End, PageUp and PageDown by ten; no
//   keydown listener of the slider's own, ever. A detent has a bite of six
//   tenths of a step: the hand within it rests there. `css` is injected
//   with the styles import.
// =============================================================================

const _sliderOwner = Object.freeze({ toString: () => "slider" });
var _BITE = 0.6;   // of a step, either side of a detent

class Slider {
    /** The builder's; a caller makes a slider through SliderBuilder. */
    constructor(branch, props) {
        if (!branch) throw new Error("[Slider] a branch of its own is required");
        var self = this;
        var p = props || {};
        branch.activate(_sliderOwner);
        this.branch = branch;
        this._min = p.min == null ? 0 : Number(p.min);
        this._max = p.max == null ? 100 : Number(p.max);
        if (!(this._max > this._min)) throw new Error("[Slider] max must be above min");
        this._step = p.step == null ? 1 : Math.abs(Number(p.step)) || 1;
        this._detents = (p.detents || []).map(Number).filter(function (d) { return d >= self._min && d <= self._max; });
        this._format = typeof p.format === "function" ? p.format : function (v) { return String(v); };
        this._onInput = typeof p.onInput === "function" ? p.onInput : null;
        this._onChange = typeof p.onChange === "function" ? p.onChange : null;
        this._on = true;
        this._size = 0;
        this._pressed = null;    // the value at the press, while a hand is on the rail
        this._vertical = !!p.vertical;
        var vertical = this._vertical;

        var root = branch.createElement("slider", "div");
        css.addClass(root, el_slider);
        if (vertical) css.addClass(root, el_slider_vertical);
        this.root = root;
        this._label = branch.createElement("label", "span");
        css.addClass(this._label, el_slider_label);
        this._label.textContent = p.label == null ? "" : String(p.label);
        root.appendChild(this._label);

        var rail = branch.createElement("rail", "div");
        css.addClass(rail, el_slider_rail);
        if (vertical) css.addClass(rail, el_slider_rail_vertical);
        this._rail = rail;
        var track = branch.createElement("track", "div");
        css.addClass(track, el_slider_track);
        if (vertical) css.addClass(track, el_slider_track_vertical);
        this._fill = branch.createElement("fill", "div");
        css.addClass(this._fill, el_slider_fill);
        if (vertical) css.addClass(this._fill, el_slider_fill_vertical);
        track.appendChild(this._fill);
        rail.appendChild(track);
        this._detent = null;
        if (this._detents.length) {
            this._detent = branch.createElement("detent", "div");
            css.addClass(this._detent, el_slider_detent);
            if (vertical) css.addClass(this._detent, el_slider_detent_vertical);
            rail.appendChild(this._detent);
        }
        // the scale: a tick at each value named, its caption beside it
        this._ticks = [];
        var ticks = Array.isArray(p.ticks) ? p.ticks : [];
        for (var t = 0; t < ticks.length; t++) {
            var at = Number(ticks[t] && ticks[t].at != null ? ticks[t].at : ticks[t]);
            if (!(at >= this._min && at <= this._max)) continue;
            var tick = branch.createElement("tick-" + t, "div");
            css.addClass(tick, el_slider_tick);
            if (vertical) css.addClass(tick, el_slider_tick_vertical);
            tick.style.setProperty("--sl-tick", (this._fraction(at) * 100).toFixed(3) + "%");
            var line = branch.createElement("tick-" + t + "-line", "div");
            css.addClass(line, el_slider_tick_line);
            if (vertical) css.addClass(line, el_slider_tick_line_vertical);
            tick.appendChild(line);
            var caption = branch.createElement("tick-" + t + "-label", "span");
            css.addClass(caption, el_slider_tick_label);
            caption.textContent = ticks[t] && ticks[t].label != null ? String(ticks[t].label) : this._format(at);
            tick.appendChild(caption);
            rail.appendChild(tick);
            this._ticks.push(tick, line, caption);
        }
        if (this._ticks.length) css.addClass(rail, el_slider_rail_ticked);
        // the knob — the cap, when stood up
        var knob = branch.createElement("knob", "div");
        css.addClass(knob, vertical ? el_slider_cap : el_slider_knob);
        knob.setAttribute("role", "slider");
        knob.setAttribute("tabindex", "0");
        knob.setAttribute("aria-valuemin", String(this._min));
        knob.setAttribute("aria-valuemax", String(this._max));
        knob.setAttribute("aria-orientation", vertical ? "vertical" : "horizontal");
        if (p.label != null) knob.setAttribute("aria-label", String(p.label));
        this._knob = knob;
        this._face = branch.createElement("face", "div");
        css.addClass(this._face, vertical ? el_slider_cap_face : el_slider_face);
        knob.appendChild(this._face);
        this._mark = new Icon(branch.createElement("mark", Icon.TAG), null);
        css.addClass(this._mark.el, vertical ? el_slider_cap_mark : el_slider_mark);
        knob.appendChild(this._mark.el);
        this.icon(p.icon === undefined ? "grip" : p.icon);
        rail.appendChild(knob);
        root.appendChild(rail);

        this._readout = branch.createElement("readout", "span");
        css.addClass(this._readout, el_slider_readout);
        root.appendChild(this._readout);
        this._parts = [root, this._label, rail, track, this._fill, knob, this._face, this._mark.el, this._readout].concat(this._ticks);
        if (this._detent) this._parts.push(this._detent);

        this._value = this._snap(p.value == null ? this._rest() : Number(p.value), false);
        this._draw();
        this.size(p.size == null ? 0 : p.size);
        if (p.labelWidth != null) this.labelWidth(p.labelWidth);

        // the hand: a press anywhere on the rail jumps and grabs; captured at the press
        this._onMove = function (ev) { self._set(self._fromPointer(ev), true, false); };
        this._onUp = function () { self._release(); };
        rail.addEventListener("pointerdown", function (ev) {
            if (!self._on || (ev.button != null && ev.button !== 0)) return;
            ev.preventDefault();
            self._pressed = self._value;
            try { rail.setPointerCapture(ev.pointerId); } catch (e) {}
            css.addClass(knob, el_slider_held);
            css.addClass(self._face, el_slider_face_held);
            self._set(self._fromPointer(ev), true, false);
            try { knob.focus({ preventScroll: true }); } catch (e) {}
            rail.addEventListener("pointermove", self._onMove);
            rail.addEventListener("pointerup", self._onUp);
            rail.addEventListener("pointercancel", self._onUp);
            rail.addEventListener("lostpointercapture", self._onUp);
        });
        // the keys: through the party, when the slider is handed the page's steward
        this._kb = null; this._kbId = null; this._offKeys = null;
        if (p.keyboard) {
            this._kb = p.keyboard;
            this._kbId = this._kb.join(p.keyboardId != null ? String(p.keyboardId) : branch.name, { keyDown: function (ev) { return self.key(ev); } });
            this._offKeys = Keys.claimOn(this.root, this._kb, this._kbId);
        }
    }

    // ── the keys ──────────────────────────────────────────────────────────
    /** A keydown, from whoever holds the keys for this slider: a step, ten, an end; true when taken. */
    key(ev) {
        if (!this._on || !ev) return false;
        var by = ev.shiftKey ? 10 : 1, v = null;
        switch (ev.key) {
            case "ArrowRight": case "ArrowUp": v = this._value + this._step * by; break;
            case "ArrowLeft": case "ArrowDown": v = this._value - this._step * by; break;
            case "PageUp": v = this._value + this._step * 10; break;
            case "PageDown": v = this._value - this._step * 10; break;
            case "Home": v = this._min; break;
            case "End": v = this._max; break;
            default: return false;
        }
        this._set(v, false, true);
        return true;
    }
    /** The one of a group: the knob's ring drawn now, whether or not it has the focus. */
    current(on) {
        if (on) css.addClass(this._knob, el_slider_knob_current); else css.removeClass(this._knob, el_slider_knob_current);
        return this;
    }
    /** Physical focus to the knob, without scrolling: a group's, on Tab. */
    focusKnob() { try { this._knob.focus({ preventScroll: true }); } catch (e) {} return this; }

    // ── the number ────────────────────────────────────────────────────────
    _rest() { return this._detents.length ? this._detents[0] : this._min; }
    /** Clamped and on the step; a value within a detent's bite rests there (the hand's), a keyed value only where it lands exactly. */
    _snap(raw, bite) {
        var v = Math.max(this._min, Math.min(this._max, raw));
        if (bite) for (var i = 0; i < this._detents.length; i++) if (Math.abs(v - this._detents[i]) <= this._step * _BITE + 1e-9) return this._detents[i];
        var steps = Math.round((v - this._min) / this._step);
        v = this._min + steps * this._step;
        v = Number(v.toFixed(10));
        return Math.max(this._min, Math.min(this._max, v));
    }
    _fraction(v) { return (v - this._min) / (this._max - this._min); }
    /** The value under the pointer: along the rail, from the left — or, stood up, from the bottom. */
    _fromPointer(ev) {
        var r = this._rail.getBoundingClientRect();
        var f = this._vertical ? (r.height > 0 ? (r.bottom - ev.clientY) / r.height : 0) : (r.width > 0 ? (ev.clientX - r.left) / r.width : 0);
        return this._min + Math.max(0, Math.min(1, f)) * (this._max - this._min);
    }
    _set(raw, bite, commit) {
        var v = this._snap(raw, bite);
        var changed = v !== this._value;
        this._value = v;
        if (changed) { this._draw(); if (this._onInput) this._onInput(v); }
        if (commit && changed && this._onChange) this._onChange(v);
    }
    _release() {
        var rail = this._rail;
        rail.removeEventListener("pointermove", this._onMove);
        rail.removeEventListener("pointerup", this._onUp);
        rail.removeEventListener("pointercancel", this._onUp);
        rail.removeEventListener("lostpointercapture", this._onUp);
        css.removeClass(this._knob, el_slider_held);
        css.removeClass(this._face, el_slider_face_held);
        var was = this._pressed;
        this._pressed = null;
        if (was !== null && was !== this._value && this._onChange) this._onChange(this._value);
    }
    _draw() {
        var fv = this._fraction(this._value), fr = this._fraction(this._rest());
        var pct = function (f) { return (f * 100).toFixed(3) + "%"; };
        this._rail.style.setProperty("--sl-value", pct(fv));
        this._rail.style.setProperty("--sl-rest", pct(fr));
        this._rail.style.setProperty("--sl-from", pct(Math.min(fv, fr)));
        this._rail.style.setProperty("--sl-span", pct(Math.abs(fv - fr)));
        this._knob.setAttribute("aria-valuenow", String(this._value));
        this._knob.setAttribute("aria-valuetext", this._format(this._value));
        this._readout.textContent = this._format(this._value);
    }

    // ── the surface ───────────────────────────────────────────────────────
    value(v) {
        if (v !== undefined) { this._value = this._snap(Number(v), false); this._draw(); }
        return this._value;
    }
    setOn(on) {
        this._on = !!on;
        css.toggleClass(this.root, el_slider_off, !this._on);
        this._knob.setAttribute("aria-disabled", this._on ? "false" : "true");
        this._knob.setAttribute("tabindex", this._on ? "0" : "-1");
        if (!this._on && this._pressed !== null) this._release();
        return this;
    }
    size(s) {
        var n = Math.max(-1, Math.min(1, Number(s)));
        this._size = Number.isFinite(n) ? n : 0;
        var v = this._size === 0 ? null : this._size;
        this._parts.forEach(function (el) { css.size(el, v); });
        return this;
    }
    label(text) {
        if (text !== undefined) { this._label.textContent = String(text); this._knob.setAttribute("aria-label", String(text)); }
        return this._label.textContent;
    }
    icon(name) {
        if (name !== undefined) { if (name == null) this._mark.clear(); else this._mark.set(name); }
        return this._mark.name();
    }
    labelWidth(w) {
        if (w == null) this._label.style.removeProperty("--sl-label"); else this._label.style.setProperty("--sl-label", String(w));
        return this;
    }
    dispose() {
        if (this._pressed !== null) this._release();
        if (this._offKeys) { this._offKeys(); this._offKeys = null; }
        if (this._kb) { this._kb.leave(this._kbId); this._kb = null; }
        this.branch.dissolve();
    }
}

class SliderBuilder {
    constructor() { this._props = { min: 0, max: 100, step: 1, detents: [], size: 0 }; }
    label(text)           { this._props.label = text; return this; }
    range(min, max, step) { this._props.min = min; this._props.max = max; if (step != null) this._props.step = step; return this; }
    /** −1 … 1 by a tenth, resting at nought: the design's axes — size, aspect, extent. */
    axis()                { this._props.min = -1; this._props.max = 1; this._props.step = 0.1; this._props.detents = [0]; if (this._props.value == null) this._props.value = 0; return this; }
    value(v)              { this._props.value = v; return this; }
    detent()              { for (var i = 0; i < arguments.length; i++) this._props.detents.push(arguments[i]); return this; }
    format(fn)            { this._props.format = fn; return this; }
    onInput(fn)           { this._props.onInput = fn; return this; }
    onChange(fn)          { this._props.onChange = fn; return this; }
    size(s)               { this._props.size = s; return this; }
    labelWidth(w)         { this._props.labelWidth = w; return this; }
    icon(name)            { this._props.icon = name; return this; }
    vertical()            { this._props.vertical = true; return this; }
    ticks(list)           { this._props.ticks = list; return this; }
    keyboard(steward, id) { this._props.keyboard = steward; this._props.keyboardId = id; return this; }
    build(branch) {
        if (!branch) throw new Error("[SliderBuilder] build wants the sub-branch the caller made for the slider");
        return new Slider(branch, this._props);
    }
}
