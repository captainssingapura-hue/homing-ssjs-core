package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * What a component talks to about the keys, never the steward: {@code
 * Keys.claimOn(root, m)} — a press or the focus arriving in the root claims
 * for the membership, in the capture phase so the innermost of nested
 * components claims last and holds; the focus leaving releases — and
 * {@code Keys.claim(m)} by call. Every call goes to the page's steward
 * directly; none touches the party. Listens on the component's own element.
 */
public record KeysModule() implements DomModule<KeysModule> {

    /** The statics: {@code claimOn(root, m, opts?) → off()}, {@code claim(m)}, {@code release(m)}; the older {@code (steward, id)} pair accepted. */
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
