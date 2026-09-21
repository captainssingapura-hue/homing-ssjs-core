// =============================================================================
// Modality — what it takes for one element to own the screen, and nothing of
// what it looks like.
//
//   new Modality(frame, { keep?, restoreTo? })
//     .release()
//
//   frame      the element that owns the screen while the hold lasts
//   keep       elements beside it under <body> that must stay live — a scrim
//              the caller drew, say; everything else under <body> goes inert
//   restoreTo  the element to focus on release; default: whatever had focus
//              when the hold began
//
// release() undoes the inert marks it made and only those, and gives the
// focus back if its holder is still on the page. Calling it twice is harmless.
//
// Inert, not a focus trap, is the point: an inert element cannot be focused,
// clicked or read by assistive technology, which is what "owns the screen"
// means, and the browser enforces it rather than a keydown handler. The keys
// are not captured here: they come to the dialog through the keyboard party,
// which it claims on open — and nothing inert can claim them back.
// =============================================================================

class Modality {
    constructor(frame, opts) {
        if (!frame) throw new Error("[Modality] frame is required");
        var o = opts || {};
        var keep = o.keep || [];
        this._restoreTo = o.restoreTo || document.activeElement;
        this._inerted = [];
        this._released = false;

        var kids = document.body.children;
        for (var k = 0; k < kids.length; k++) {
            var el = kids[k];
            if (el === frame || keep.indexOf(el) >= 0 || el.inert) continue;
            el.inert = true;
            this._inerted.push(el);
        }
    }

    release() {
        if (this._released) return;
        this._released = true;
        for (var i = 0; i < this._inerted.length; i++) this._inerted[i].inert = false;
        this._inerted = [];
        var to = this._restoreTo;
        if (to && to.focus && document.contains(to)) to.focus();
    }
}
