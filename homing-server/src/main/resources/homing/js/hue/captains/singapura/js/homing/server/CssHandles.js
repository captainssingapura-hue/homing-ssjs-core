// =============================================================================
// CssHandles — the CSS manager's affiliate: the class handles (RFC 0002-ext1).
//
// Per-class JS handles use ES6 classes for prototype sharing and clean variant
// exposition. Each pseudo-state has a dedicated subclass mirroring the Java
// side (HoverVariantOf / FocusVariantOf / ActiveVariantOf): one uniform value
// type for plain classes, one subclass per variant state.
//
// A handle is IDENTITY, not behaviour: `st_btn` is the string "st-btn", and
// the rules behind it are bound late by the cascade. That is what lets the
// manager swap every sheet on the page while every holder of a handle keeps
// holding the same object (RFC 0064).
//
// Split out of CssClassManager when the manager took on the dependency graph
// and the theme switch; nothing here changed in the move.
// =============================================================================

class CssClass {
    constructor(name) { this.name = name; }
    toString() { return this.name; }
}

// Dedicated variant classes — mirror Java's HoverVariantOf / FocusVariantOf /
// ActiveVariantOf. Each carries the kebab-name of the state-restricted CSS
// rule. Distinct types so consumers can introspect via `instanceof`.
class HoverVariant  extends CssClass { static pseudoState = ":hover";  }
class FocusVariant  extends CssClass { static pseudoState = ":focus";  }
class ActiveVariant extends CssClass { static pseudoState = ":active"; }

// Lookup: variant state name (as emitted by CssGroupContentProvider) →
// dedicated subclass. Unknown states fall back to plain CssClass — keeps the
// JS forward-compatible if a new VariantOf subtype is added on the Java side
// before the JS is updated.
const VARIANT_CLASSES = {
    hover:  HoverVariant,
    focus:  FocusVariant,
    active: ActiveVariant,
};

class CssUtility extends CssClass {
    /**
     * @param {string} name           kebab-case base class name
     * @param {Object<string,string>} variants  state → variant kebab-name (e.g. { hover: "hover-bg-accent" })
     *
     * Each variant is precomputed once as the appropriate subclass instance
     * (HoverVariant / FocusVariant / etc.) and exposed as a PROPERTY (no
     * parens, no string return). This keeps every class handle in the system
     * the same uniform value type — `cls instanceof CssClass` for all of them
     * — while preserving distinguishability via the dedicated subclasses.
     *
     * Use site: `cn(bg_accent, bg_accent.hover)` — property access, no parens.
     */
    constructor(name, variants) {
        super(name);
        const states = Object.keys(variants || {});
        for (const state of states) {
            const VariantClass = VARIANT_CLASSES[state] || CssClass;
            this[state] = new VariantClass(variants[state]);
        }
    }
}
