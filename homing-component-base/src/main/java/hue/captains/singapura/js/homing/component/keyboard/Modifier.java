package hue.captains.singapura.js.homing.component.keyboard;

/** The four modifiers, by their {@code KeyboardEvent} flags. */
public enum Modifier {
    SHIFT("Shift"), CTRL("Ctrl"), ALT("Alt"), META("Meta");

    private final String label;
    Modifier(String label) { this.label = label; }
    /** The modifier's name as a person reads it: {@code Shift}. */
    public String label() { return label; }
}
