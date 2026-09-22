package hue.captains.singapura.js.homing.design;

/**
 * The states a physical target's template can show a value for. A state is
 * the element's, expressed natively (pseudo-class) or through the aria
 * attribute the component sets; a design binds a value per state it has an
 * opinion on, and the template falls back to {@link #REST} for the rest.
 *
 * <p>Custom states a component invents are not here and never will be: a
 * component in a state of its own wears a different design class. The slots
 * with no aria behind them are {@link #HIGHLIGHTED} — a thing lit from
 * elsewhere is a state every list, table and tree has, and none of aria's
 * words is it — and the three the keyboard party keeps: {@link #CANDIDATE},
 * {@link #LENT} and {@link #HELD}, which read one attribute,
 * {@code data-keys}, whose values are mutually exclusive. They say where the
 * keys are, and a design answers them on the word that says what the thing
 * is — so a card, a tab and a floating pane each say it in their own way.</p>
 *
 * <p>The order here is the cascade: a sheet emits a word's states in it, and
 * a later one wins where two apply at once. The hover yields to what the
 * thing is — selected, current, checked — and the press, {@link #ACTIVE},
 * comes last of all, so a press shows on a thing however else it is: a
 * selected tab pressed is seen pressed.</p>
 */
public enum State {
    REST(""),
    HOVER("&:hover"),
    FOCUS("&:focus-visible"),
    DISABLED("&:disabled, &[aria-disabled=\"true\"]"),
    SELECTED("&[aria-selected=\"true\"]"),
    CURRENT("&[aria-current]"),
    CHECKED("&:checked, &[aria-checked=\"true\"]"),
    INVALID("&:invalid, &[aria-invalid=\"true\"]"),
    EXPANDED("&[aria-expanded=\"true\"]"),
    /** Lit from elsewhere — a search hit, the rows a chart points at. No aria state says it, so the slot reads {@code data-highlighted}. */
    HIGHLIGHTED("&[data-highlighted]"),
    /** The keys would come here, if the walk were confirmed: the keyboard's cursor over the focus tree. Proposed, never in force. */
    CANDIDATE("&[data-keys=\"candidate\"]"),
    /** The keys are here, and lent to a native control inside: present, and not listening. */
    LENT("&[data-keys=\"lent\"]"),
    /** The keys are here: this is the holder. After the words that say what a thing is, so a held tab reads held over selected. */
    HELD("&[data-keys=\"held\"]"),
    /** Pressed: last, so it is seen whatever else the thing is. */
    ACTIVE("&:active");

    private final String selector;

    State(String selector) { this.selector = selector; }

    /** The nested selector the template wraps this state's declarations in; empty for {@link #REST}. */
    public String selector() { return selector; }

    /** The variable-name suffix; empty for {@link #REST}. */
    public String suffix() { return this == REST ? "" : "-" + name().toLowerCase(); }
}
