package hue.captains.singapura.js.homing.component.keyboard;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * {@code KeyboardChords}: a chord that got past the native world, offered up
 * the chain from where it was pressed. The sibling of {@link
 * KeyboardShortcutsModule.KeyboardShortcuts} — that one is the page's own
 * keys, tried before anyone; this one is a container's, tried only when the
 * key came out of something natively focused.
 *
 * <p>The DOM says where to start — {@code memberAt} walks out from the
 * focused element to the first enrolled root — and the focus party says who
 * is above, because that is the tree that means ownership rather than layout:
 * a floating dock is drawn inside the desk and belongs to the branch it was
 * given. Nothing is remembered; the chain is worked out from the event when
 * the key arrives, so there is no state to set, to clear, or to be wrong.</p>
 */
public record KeyboardChordsModule() implements EsModule<KeyboardChordsModule> {

    /** The class of statics: {@code took}, {@code offer}. */
    public record KeyboardChords() implements Exportable._Constant<KeyboardChordsModule> {}

    public static final KeyboardChordsModule INSTANCE = new KeyboardChordsModule();

    @Override
    public ImportsFor<KeyboardChordsModule> imports() {
        return ImportsFor.<KeyboardChordsModule>builder()
                .add(new hue.captains.singapura.js.homing.core.ModuleImports<>(
                        java.util.List.of(new KeyboardShortcutsModule.KeyboardShortcuts()), KeyboardShortcutsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<KeyboardChordsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new KeyboardChords()));
    }
}
