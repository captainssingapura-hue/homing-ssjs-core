// =============================================================================
// LayoutCodec — pure converter between MultiTabPane's native layout shape and
// the typed LayoutNode record tree (RFC 0029). Both directions round-trip
// losslessly.
//
//   MultiTabPane native shape :  { kind: 'leaf', slotId }
//                              | { kind: 'split',
//                                  orientation: 'horizontal' | 'vertical',
//                                  children: [{ pane, ratio }, { pane, ratio }] }
//
//   Typed LayoutNode (RFC 0029):  LayoutNode.Leaf(PaneId)
//                              |  LayoutNode.Split(Orientation, ratio, first, second)
//
// Explicit Substrate doctrine: instance methods on a canonical INSTANCE
// singleton — never static. Mirrors Java Functional Objects faithfully:
// `LayoutCodec.INSTANCE.mtToTyped(node)`, not `LayoutCodec.mtToTyped(node)`.
//
// Stateless: zero instance fields. Behaviour is a pure function of input;
// testable in isolation without a DOM.
// =============================================================================

class LayoutCodec {

    /** MultiTabPane native layout → typed LayoutNode tree. */
    mtToTyped(node) {
        if (!node) return null;
        if (node.kind === 'leaf') {
            return new LayoutNode.Leaf(new PaneId(node.slotId));
        }
        // 'split'
        const orient = (node.orientation === 'vertical')
            ? Orientation.VERTICAL
            : Orientation.HORIZONTAL;
        // Typed Split carries the FIRST child's ratio only (second is 1 - r).
        let r = (node.children && node.children[0]
                 && typeof node.children[0].ratio === 'number')
              ? node.children[0].ratio
              : 0.5;
        // Clamp to (0, 1) strictly — typed record's compact ctor rejects 0/1.
        if (r <= 0.001) r = 0.001;
        if (r >= 0.999) r = 0.999;
        return new LayoutNode.Split(
            orient, r,
            this.mtToTyped(node.children[0].pane),
            this.mtToTyped(node.children[1].pane));
    }

    /** Typed LayoutNode tree → MultiTabPane native shape. */
    typedToMt(typed) {
        if (!typed) return null;
        if (typed instanceof LayoutNode.Leaf) {
            return { kind: 'leaf', slotId: typed.paneId.value };
        }
        // LayoutNode.Split
        const orientStr = (typed.orientation === Orientation.VERTICAL)
            ? 'vertical' : 'horizontal';
        const r = typed.ratio;
        return {
            kind: 'split',
            orientation: orientStr,
            children: [
                { pane: this.typedToMt(typed.first),  ratio: r       },
                { pane: this.typedToMt(typed.second), ratio: 1.0 - r }
            ]
        };
    }
}

LayoutCodec.INSTANCE = new LayoutCodec();
