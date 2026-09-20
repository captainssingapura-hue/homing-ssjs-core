// =============================================================================
// Modality — what it takes for one element to own the screen, and nothing of
// what it looks like.
//
//   new Modality(frame, { keep?, onKeydown?, restoreTo? })
//     .release()
//
//   frame      the element that owns the screen while the hold lasts
//   keep       elements beside it under <body> that must stay live — a scrim
//              the caller drew, say; everything else under <body> goes inert
//   onKeydown  called on every keydown, CAPTURED on the document, before the
//              page behind can see it; return true to say the key was taken
//              (it is then stopped and its default prevented)
//   restoreTo  the element to focus on release; default: whatever had focus
//              when the hold began
//
// release() undoes the inert marks it made and only those, drops the
// keyboard capture, and gives the focus back if its holder is still on the
// page. Calling it twice is harmless.
//
// Capture, not bubble, is the point: the page behind may have its own tree
// listening on the document, and without capture one ArrowDown walks both.
// Inert, not a focus trap, is the other point: an inert element cannot be
// focused, clicked or read by assistive technology, which is what "owns the
// screen" means, and the browser enforces it rather than a keydown handler.
// =============================================================================

class Modality {
    constructor(frame, opts) {
        if (!frame) throw new Error("[Modality] frame is required");
        var o = opts || {};
        var keep = o.keep || [];
        this._restoreTo = o.restoreTo || document.activeElement;
        this._onKeydown = typeof o.onKeydown === "function" ? o.onKeydown : null;
        this._inerted = [];
        this._released = false;

        var kids = document.body.children;
        for (var k = 0; k < kids.length; k++) {
            var el = kids[k];
            if (el === frame || keep.indexOf(el) >= 0 || el.inert) continue;
            el.inert = true;
            this._inerted.push(el);
        }
        this._keys = this._keys.bind(this);
        document.addEventListener("keydown", this._keys, true);
    }

    _keys(ev) {
        if (this._onKeydown && this._onKeydown(ev)) {
            ev.preventDefault();
            ev.stopPropagation();
        }
    }

    release() {
        if (this._released) return;
        this._released = true;
        document.removeEventListener("keydown", this._keys, true);
        for (var i = 0; i < this._inerted.length; i++) this._inerted[i].inert = false;
        this._inerted = [];
        var to = this._restoreTo;
        if (to && to.focus && document.contains(to)) to.focus();
    }
}
