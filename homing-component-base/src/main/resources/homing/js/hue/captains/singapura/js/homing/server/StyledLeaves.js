// =============================================================================
// StyledLeaves — how an element wears its leaf's style-aware classes (RFC
// 0067, appendix Two Classes per Leaf). A leaf is a component, or a part at its
// role; its names are generated from Java, handed here as a leaf's
// description:
//
//   { token: "tab", styled: "styled-tab",
//     states: { selected: { false: null, true: "styled-tab--selected" }, ... } }
//
// each application state with every value it has - the rest's class none -
// and no pseudo state, which the browser draws by its pseudo-class.
//
//   StyledLeaf.wear(el, leaf)                 the leaf's class; refused on an
//                                             element wearing another leaf's -
//                                             a part's element wears its
//                                             part's classes alone (D10)
//   StyledLeaf.hold(el, leaf, state, value)   the state's class for the value,
//                                             the state's others taken off
//                                             (D12); refused for a state the
//                                             leaf lists not, or a value it
//                                             has not
//
// Nothing is guessed: a wrong name throws where it is written, never later as
// a look that silently does not apply.
// =============================================================================

class StyledLeaf {

    /** The leaf's class, on an element that wears no other leaf's. */
    static wear(el, leaf) {
        StyledLeaf._described(leaf);
        for (const c of Array.from(el.classList)) {
            if (c.startsWith("styled-") && c !== leaf.styled && !c.startsWith(leaf.styled + "--")) {
                throw new Error("styled: the element wears " + c + ", and " + leaf.token
                    + " is another leaf - an element wears one leaf's classes, a part's its part's alone");
            }
        }
        CssClassManagerInstance.addClass(el, CssClassManagerInstance.cls(leaf.styled));
        return el;
    }

    /** The state's class for a value, every other class of the state taken off; none for the state's rest. */
    static hold(el, leaf, state, value) {
        StyledLeaf._described(leaf);
        const values = leaf.states[state];
        if (!values) {
            throw new Error("styled: " + leaf.token + " lists no state " + state + "; its states are "
                + (Object.keys(leaf.states).join(", ") || "none") + " (a pseudo state is the browser's)");
        }
        const at = String(value);
        if (!Object.prototype.hasOwnProperty.call(values, at)) {
            throw new Error("styled: " + state + " has no value " + at + "; its values are " + Object.keys(values).join(", "));
        }
        const css = CssClassManagerInstance;
        if (!css.hasClass(el, css.cls(leaf.styled))) {
            throw new Error("styled: the element does not wear " + leaf.styled + ": wear the leaf before holding its states");
        }
        for (const c of Object.values(values)) if (c) css.removeClass(el, css.cls(c));
        if (values[at]) css.addClass(el, css.cls(values[at]));
        return el;
    }

    static _described(leaf) {
        if (!leaf || typeof leaf.styled !== "string" || typeof leaf.token !== "string" || !leaf.states) {
            throw new TypeError("styled: a leaf is described by its token, its class and its states, as generated from Java");
        }
    }
}
