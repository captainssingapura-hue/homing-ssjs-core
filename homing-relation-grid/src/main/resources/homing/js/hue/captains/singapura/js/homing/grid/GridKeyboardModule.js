// =============================================================================
// GridKeyboardModule — RFC 0050's shallow keyboard: keydown → selection
// intent, nothing else. It never touches DOM state or the maps; it only
// translates keys into GridSelection operations (the RFC 0049 ShallowKeyboard
// pattern one level down). Deep-mode keys (Enter/F2/Escape editing) arrive
// with Phase 5's edit controller, which will own the keyboard while editing.
//
// Bindings (Excel-shaped):
//   Arrows            move the cursor (edge-confined; ranges collapse)
//   Shift+Arrows      extend the active range's focus (cursor stays)
//   Tab / Shift+Tab   move right / left
//   Home / End        first / last column of the cursor's row
//   Ctrl+A            select the whole view
//   Ctrl+C            fire the onCopy callback (bound only when provided)
//
//   new GridKeyboard({ selection, onCopy? }).attach(el);
// =============================================================================

class GridKeyboard {

    constructor(opts) {
        opts = opts || {};
        if (!opts.selection) throw new Error("[GridKeyboard] opts.selection is required");
        this._selection = opts.selection;
        this._onCopy = opts.onCopy || null;
        this._el = null;
        var self = this;
        this._handler = function (e) { self.handleKey(e); };
    }

    attach(el) {
        this._el = el;
        el.addEventListener("keydown", this._handler);
        return this;
    }

    detach() {
        if (this._el) this._el.removeEventListener("keydown", this._handler);
        this._el = null;
        return this;
    }

    /** Returns true when the key was consumed (and preventDefault called). */
    handleKey(e) {
        var s = this._selection, ext = !!e.shiftKey;
        var consumed = true;
        switch (e.key) {
            case "ArrowUp":    s.move(-1,  0, ext); break;
            case "ArrowDown":  s.move( 1,  0, ext); break;
            case "ArrowLeft":  s.move( 0, -1, ext); break;
            case "ArrowRight": s.move( 0,  1, ext); break;
            case "Tab":        s.move( 0, e.shiftKey ? -1 : 1, false); break;
            case "Home":       s.move( 0, -Infinity, ext); break;
            case "End":        s.move( 0,  Infinity, ext); break;
            case "a": case "A":
                if (e.ctrlKey || e.metaKey) s.selectAll(); else consumed = false;
                break;
            case "c": case "C":
                if ((e.ctrlKey || e.metaKey) && this._onCopy) this._onCopy();
                else consumed = false;
                break;
            default:
                consumed = false;
        }
        if (consumed && e.preventDefault) e.preventDefault();
        return consumed;
    }
}
