package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * What a component talks to about the keys, never the steward: {@code
 * Keys.claimOn(root, m)} — a press in the root claims
 * for the membership — the innermost of nested roots alone: the roots are
 * enrolled with the steward, and an outer root stays silent for a press
 * inside an inner one, so a container is never granted for a press on its
 * child; native focus moves nothing, a member never having it — and
 * {@code Keys.claim(m)} by call. Every call goes to the page's steward
 * directly; none touches the party. Listens on the component's own element.
 */
public record KeysModule() implements DomModule<KeysModule> {

    /** The statics: {@code claimOn(root, m) → off()} — one {@code pointerdown} listener, the innermost member claims — {@code claim(m)}, {@code yield(m)}, {@code release(m)}; the older {@code (steward, id)} pair accepted. */
    public record Keys() implements Exportable._Constant<KeysModule> {}

    public static final KeysModule INSTANCE = new KeysModule();

    @Override public ImportsFor<KeysModule> imports() {
        return ImportsFor.<KeysModule>builder()
                .add(new ModuleImports<>(List.of(new KeyboardStewardModule.KeyboardStewardInstance()), KeyboardStewardModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<KeysModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Keys()));
    }
}
