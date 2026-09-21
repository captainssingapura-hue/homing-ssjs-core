package hue.captains.singapura.js.homing.component.keyboard;

/** The four modifiers, by their {@code KeyboardEvent} flags. */
public enum Modifier {
    SHIFT("Shift"), CTRL("Ctrl"), ALT("Alt"), META("Meta");

    private final String label;
    Modifier(String label) { this.label = label; }
    public String label() { return label; }
}
