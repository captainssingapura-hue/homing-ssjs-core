package hue.captains.singapura.js.homing.component.keyboard;

/**
 * The keys a component may name in its declaration, by the browser's
 * {@code KeyboardEvent.key} value, which is what the component reads at
 * runtime. A typed vocabulary rather than a string, so a declaration cannot
 * name a key that does not exist and a studio can list who takes each.
 */
public enum Key {
    ARROW_UP("ArrowUp"), ARROW_DOWN("ArrowDown"), ARROW_LEFT("ArrowLeft"), ARROW_RIGHT("ArrowRight"),
    HOME("Home"), END("End"), PAGE_UP("PageUp"), PAGE_DOWN("PageDown"),
    ENTER("Enter"), SPACE(" "), ESCAPE("Escape"), TAB("Tab"), BACKSPACE("Backspace"), DELETE("Delete"),
    F1("F1"), F2("F2"), F3("F3"), F4("F4"), F5("F5"), F6("F6"), F7("F7"), F8("F8"), F9("F9"), F10("F10"), F11("F11"), F12("F12"),
    A("a"), B("b"), C("c"), D("d"), E("e"), F("f"), G("g"), H("h"), I("i"), J("j"), K("k"), L("l"), M("m"),
    N("n"), O("o"), P("p"), Q("q"), R("r"), S("s"), T("t"), U("u"), V("v"), W("w"), X("x"), Y("y"), Z("z"),
    DIGIT_0("0"), DIGIT_1("1"), DIGIT_2("2"), DIGIT_3("3"), DIGIT_4("4"), DIGIT_5("5"), DIGIT_6("6"), DIGIT_7("7"), DIGIT_8("8"), DIGIT_9("9"),
    PLUS("+"), MINUS("-"), SLASH("/"), CONTEXT_MENU("ContextMenu");

    private final String value;
    Key(String value) { this.value = value; }

    /** The {@code KeyboardEvent.key} value. */
    public String value() { return value; }

    /** What a person reads: {@code Space} for the space, else the value. */
    public String label() { return this == SPACE ? "Space" : value; }
}
