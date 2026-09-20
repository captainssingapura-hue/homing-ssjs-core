// =============================================================================
// TabDrag — the arithmetic of a chip in the hand, with no element in sight.
// A browser's tab bar: the tabs are one width, so the bar is a row of slots
// at one pitch; the chip pressed is lifted and goes where the hand goes,
// the press remembered as an offset within it, so the chip is placed and
// the hand never asked where on it the press was; the slot nearest the
// chip is where it will land, and the chips between its own slot and that
// one, step aside, live. Headless, so a strip draws what these say and they
// are tested without a browser.
//
//   TabDrag.pitch(slots)                  → the distance from one slot's left to the next; one slot, its width
//   TabDrag.clamp(left, slots, lo)        → the left kept within the slots from lo to the last
//   TabDrag.dest(left, slots, lo)         → the slot a chip at `left` is nearest, lo..n−1
//   TabDrag.shift(j, from, to)            → how many slots the chip at j steps when the one
//                                           at `from` is taken to `to`: −1, 0 or +1
//
// A slot is { left, top, width, height }: a chip's rectangle as the bar laid
// it, in order. lo is the count of pinned chips, which sit first and are
// never passed.
// =============================================================================

class TabDrag {
    static pitch(slots) {
        if (slots.length > 1) return slots[1].left - slots[0].left;
        return slots.length ? slots[0].width : 0;
    }
    static clamp(left, slots, lo) {
        var first = slots[Math.min(lo, slots.length - 1)].left, last = slots[slots.length - 1].left;
        return Math.max(first, Math.min(last, left));
    }
    static dest(left, slots, lo) {
        var p = TabDrag.pitch(slots);
        if (!(p > 0)) return lo;
        var k = Math.round((left - slots[0].left) / p);
        return Math.max(lo, Math.min(slots.length - 1, k));
    }
    static shift(j, from, to) {
        if (j === from) return to - from;
        if (from < to && j > from && j <= to) return -1;
        if (to < from && j >= to && j < from) return 1;
        return 0;
    }
}
