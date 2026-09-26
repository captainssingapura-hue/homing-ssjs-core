package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * {@code KeyboardMark}: where the focus is — the steward's marker, one per
 * page (RFC 0066 E3, keyboard §17.5). The holder, {@code held} with nothing
 * natively focused or {@code lent} with a native control of its own focused,
 * or {@code away} with the browser's focus outside every member. The rules
 * the steward holds after every focus event and before every key: the
 * browser's focus arriving inside a member makes it the holder; arriving on
 * a member's own root, or on something focusable only because it scrolls, is
 * refused; a claim blurs the browser's focus outside the claimer; a release
 * blurs it inside the one that let go. The marks ({@code data-keys} on the
 * enrolled roots) are written here and nowhere else, and the members above
 * the marker are told {@code within(on, at)} on every move of it. Statics on
 * a steward, as {@link KeyboardWalkModule}'s are; it reads the browser's
 * focus and writes the marks, so it is a DOM module.
 */
public record KeyboardMarkModule() implements DomModule<KeyboardMarkModule> {

    /** The class of statics: {@code sync}, {@code claimed}, {@code released}, {@code paint}, {@code tell}, {@code marker}, {@code check}, {@code asked}, {@code focused}. */
    public record KeyboardMark() implements Exportable._Constant<KeyboardMarkModule> {}

    public static final KeyboardMarkModule INSTANCE = new KeyboardMarkModule();

    @Override public ImportsFor<KeyboardMarkModule> imports() {
        return ImportsFor.<KeyboardMarkModule>builder()
                .add(new ModuleImports<>(List.of(new KeyboardEventsModule.KeyboardEvents()), KeyboardEventsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<KeyboardMarkModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new KeyboardMark()));
    }
}
