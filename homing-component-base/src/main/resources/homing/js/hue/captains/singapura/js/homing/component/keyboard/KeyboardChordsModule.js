// =============================================================================
// KeyboardChords — a chord that got past the native world, offered up the
// chain from where it was pressed. The sibling of KeyboardShortcuts: that one
// is the PAGE's own keys, tried before anyone; this one is a container's,
// tried only when the key came out of something natively focused.
//
//   KeyboardChords.took(steward, ev) → the member that took it, or null. It
//     decides what counts as a chord, walks the chain, and stops the event
//     itself when one is taken — so the steward's own line stays a line.
//
// WHY THIS EXISTS AT ALL. While something is natively focused the keys are
// its own, and that is the rule a text field is promised. But a browser moves
// you between its tabs on Ctrl+Tab while you are typing in a page, and a
// container that means to behave that way cannot be told about the key by
// anybody else: it is not the holder, and it may not listen on the document,
// because only the steward does.
//
// THE DOM SAYS WHERE TO START, THE PARTY SAYS WHO IS ABOVE. memberAt walks
// out from the focused element to the first enrolled root — that is the
// innermost thing that owns where you are. From there the chain is the focus
// party's, not the DOM's, because that is the tree that means ownership: a
// floating dock is drawn inside the desk and belongs to the branch it was
// given, and a chord should reach its owner rather than whatever it happens
// to be drawn inside.
//
// Nothing is remembered. The chain is worked out from the event when the key
// arrives, so there is no state to set on the way in, none to clear on the
// way out, and none to be wrong. A member that takes nothing simply has no
// chord reactor.
// =============================================================================

class KeyboardChords {
    /**
     * Offered from the innermost member outward until one takes it. The id
     * that took it, or null — and null means nothing was taken, so the native
     * world keeps the key exactly as it would have.
     *
     * ONLY A CHORD IS OFFERED, and that is a floor rather than a courtesy:
     * without it the first container to answer a bare arrow would break every
     * text field beneath it. KeyboardShortcuts.claimable is the same test the
     * page's own keys pass — a modifier, or a function key, never something
     * somebody could be typing.
     */
    static took(s, ev) {
        if (!KeyboardShortcuts.claimable(ev)) return null;
        var id = s.memberAt(ev.target);
        if (!id) return null;
        var m = s._focus ? s._focus.find(id) : null;
        if (!m) return KeyboardChords.offer(s, id, ev) ? KeyboardChords.stop(ev, id) : null;   // no focus tree: the member at the element, nobody above it
        for (; m; m = m.parent()) if (KeyboardChords.offer(s, m.id, ev)) return KeyboardChords.stop(ev, m.id);
        return null;
    }

    /** Taken: the key goes no further, and neither does the browser's own meaning for it. */
    static stop(ev, id) { ev.preventDefault(); ev.stopPropagation(); return id; }

    /** One member asked; true when it took the key. A member without a chord reactor is not asked at all. */
    static offer(s, id, ev) {
        var h = s._members[id];
        if (!h || typeof h.chord !== "function") return false;
        try { return h.chord(ev) === true; } catch (e) { console.error("[KeyboardChords] a chord threw:", e); return false; }
    }
}
