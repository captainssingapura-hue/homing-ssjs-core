package hue.captains.singapura.js.homing.ui.panes;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.component.keyboard.Key;
import hue.captains.singapura.js.homing.component.keyboard.KeyBinding;
import hue.captains.singapura.js.homing.component.keyboard.KeysModule;
import hue.captains.singapura.js.homing.component.keyboard.NeedKeyboard;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * {@code TabOpener}, what a new tab holds until you say what it should hold.
 * A strip's plus cannot know which of a page's kinds was meant, and guessing
 * one is worse than asking: so the plus opens a tab with this in it, and
 * picking from it turns that tab into the thing picked. The chip you made is
 * the chip you keep, in the place you made it.
 *
 * <p>It REPLACES rather than contains. A widget's root is appended to its
 * tab's panel once and never detached, so an opener that mounted the chosen
 * thing inside itself would be a tab that is always an opener wearing somebody
 * else's name. Giving the tab up and asking the {@link
 * TabSourceModule.TabSource} for a fresh one costs three calls and leaves
 * nothing of the opener behind — and it is the same minting rule {@link
 * AddTabModule.AddTab} uses, so a tab opened here and a tab added there are
 * the same kind of thing.</p>
 *
 * <p>Its choices are native buttons inside a logical member: the widget holds
 * the keys for the dock, the focused button holds them for the widget — said
 * as {@code lent} — and an Escape the button did not want comes back to the
 * widget rather than out of the room. That seam is wired here once, so a page
 * need not wire it.</p>
 */
public record TabOpenerModule() implements DomModule<TabOpenerModule> {

    /** The class. */
    public record TabOpener() implements BranchComponent<TabOpenerModule>, NeedKeyboard {
        @Override public String summary() { return "A new tab's chooser: the kinds a source can make, and the one picked becomes this very tab."; }
        /**
         * Two Escapes and nothing else. The choices are native buttons, so the
         * browser's own keys walk them and press them and this declares none
         * of that; what IS its own is the seam — one Escape brings the keys
         * back from a button to the widget, the next gives them to the dock —
         * and a seam that is not declared is a key nobody can find.
         */
        @Override public List<KeyBinding> keys() {
            return List.of(
                    KeyBinding.of(Key.ESCAPE, "from a choice: the keys come back to the opener, and the room is not left"),
                    KeyBinding.of(Key.ESCAPE, "from the opener: the keys go up to the dock"));
        }
    }

    public static final TabOpenerModule INSTANCE = new TabOpenerModule();

    @Override
    public ImportsFor<TabOpenerModule> imports() {
        return ImportsFor.<TabOpenerModule>builder()
                .add(new ModuleImports<>(List.of(
                        new PaneStyles.mtp_opener(),
                        new PaneStyles.mtp_opener_note(),
                        new PaneStyles.mtp_opener_grid(),
                        new PaneStyles.mtp_opener_pick()
                ), PaneStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new KeysModule.Keys()), KeysModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TabOpenerModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TabOpener()));
    }
}
