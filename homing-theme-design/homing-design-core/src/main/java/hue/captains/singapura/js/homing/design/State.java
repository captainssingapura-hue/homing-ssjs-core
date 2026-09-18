package hue.captains.singapura.js.homing.design;

/**
 * The states a physical target's template can show a value for. A state is
 * the element's, expressed natively (pseudo-class) or through the aria
 * attribute the component sets; a design binds a value per state it has an
 * opinion on, and the template falls back to {@link #REST} for the rest.
 *
 * <p>Custom states a component invents are not here and never will be: a
 * component in a state of its own wears a different design class. The one
 * slot with no aria behind it is {@link #HIGHLIGHTED}: a thing lit from
 * elsewhere is a state every list, table and tree has, and none of aria's
 * words is it.</p>
 */
public enum State {
    REST(""),
    HOVER("&:hover"),
    ACTIVE("&:active"),
    FOCUS("&:focus-visible"),
    DISABLED("&:disabled, &[aria-disabled=\"true\"]"),
    SELECTED("&[aria-selected=\"true\"]"),
    CURRENT("&[aria-current]"),
    CHECKED("&:checked, &[aria-checked=\"true\"]"),
    INVALID("&:invalid, &[aria-invalid=\"true\"]"),
    EXPANDED("&[aria-expanded=\"true\"]"),
    /** Lit from elsewhere — a search hit, the rows a chart points at. No aria state says it, so the slot reads {@code data-highlighted}. */
    HIGHLIGHTED("&[data-highlighted]");

    private final String selector;

    State(String selector) { this.selector = selector; }

    /** The nested selector the template wraps this state's declarations in; empty for {@link #REST}. */
    public String selector() { return selector; }

    /** The variable-name suffix; empty for {@link #REST}. */
    public String suffix() { return this == REST ? "" : "-" + name().toLowerCase(); }
}
