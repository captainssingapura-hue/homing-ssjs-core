package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.TocSyncSecretaryModule;
import hue.captains.singapura.js.homing.studio.base.css.StudioStyles;

import java.util.List;

/**
 * The Actors around {@link TocSyncSecretaryModule TocSyncSecretary}, for a
 * <b>flat anchor</b> table of contents — the shape the markdown reader draws,
 * as opposed to the rigid-tree reader's {@code TreeRenderer}.
 *
 * <p>{@code attachTocSync({ navEl, links, headings })} takes a {@code <nav>},
 * its anchors and the headings they point at, and gives them what the rigid-tree
 * reader has had since RFC 0043: arrow through the TOC and the body follows,
 * scroll the body and the TOC follows. The <i>coordination</i> is not here — it
 * is the shared Secretary, so both readers obey one law rather than two
 * implementations of it. What lives here is the three Actors that law
 * coordinates: the keyboard, the scroll-spy, and the scroll.</p>
 *
 * <p><b>The key is the slug.</b> Each link carries {@code data-slug} and each
 * heading carries the same string as its {@code id} — the whole binding, so no
 * map crosses the seam.</p>
 *
 * <p><b>One tab stop.</b> The links are anchors, so untouched a forty-heading
 * TOC is forty tab stops in front of the document. A roving tabindex makes it
 * one, and the arrows move the cursor — the listbox pattern for the listbox
 * reason.</p>
 *
 * <p><b>The cursor follows the spy,</b> not only the keyboard. Scroll to section
 * five and press ArrowDown and you arrive at six, because a cursor left at one
 * while the highlight sat at five would be a second opinion about where the
 * reader is — the exact thing the Secretary exists to prevent.</p>
 *
 * <p>The anchors' inline {@code onclick} is left in place: it survives static
 * export, where an added listener would not. A click therefore scrolls twice to
 * the same place, invisibly, and the click handler here is what arms the guard
 * so the spy stops flickering through every heading on the way.</p>
 *
 * @since homing-studio-base — the markdown reader's half of RFC 0043's law
 */
public record TocSyncModule() implements DomModule<TocSyncModule> {

    /** The {@code attachTocSync(opts)} JS function. */
    public record attachTocSync() implements Exportable._Constant<TocSyncModule> {}

    public static final TocSyncModule INSTANCE = new TocSyncModule();

    @Override
    public ImportsFor<TocSyncModule> imports() {
        return ImportsFor.<TocSyncModule>builder()
                .add(new ModuleImports<>(List.of(new TocSyncSecretaryModule.TocSyncSecretary()),
                        TocSyncSecretaryModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new StudioStyles.st_toc_active()),
                        StudioStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<TocSyncModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new attachTocSync()));
    }
}
