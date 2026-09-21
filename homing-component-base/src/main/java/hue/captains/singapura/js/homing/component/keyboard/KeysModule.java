package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The claiming convention as one line for a component: {@code Keys.claimOn(
 * root, steward, id)} — a press or the focus arriving in the root claims, in
 * the capture phase so the innermost of nested components claims last and
 * holds; the focus leaving releases. Listens on the component's own element.
 */
public record KeysModule() implements DomModule<KeysModule> {

    /** The statics: {@code claimOn(root, steward, id, opts?) → off()}. */
    public record Keys() implements Exportable._Constant<KeysModule> {}

    public static final KeysModule INSTANCE = new KeysModule();

    @Override public ImportsFor<KeysModule> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<KeysModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Keys()));
    }
}
