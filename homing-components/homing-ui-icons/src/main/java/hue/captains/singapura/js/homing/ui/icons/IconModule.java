package hue.captains.singapura.js.homing.ui.icons;

import hue.captains.singapura.js.homing.component.ElementComponent;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * The {@code Icon} element component: {@code new Icon(el, { name? })} dresses
 * an element the caller minted — a span, {@code Icon.TAG} — as a mark that
 * means the word named; {@code set(name)} changes the word, {@code clear()}
 * leaves the mark blank at its width, {@code name()} says which it wears.
 * A name not in {@code ICONS} is refused. The picture is the design's.
 */
public record IconModule() implements DomModule<IconModule> {

    /** The class. */
    public record Icon() implements ElementComponent<IconModule> {
        @Override public String tag() { return "span"; }
        @Override public String summary() { return "A mark that means a word of the design's Icon vocabulary; the design draws it."; }
    }

    public static final IconModule INSTANCE = new IconModule();

    @Override
    public ImportsFor<IconModule> imports() {
        return ImportsFor.<IconModule>builder()
                .add(new ModuleImports<>(List.of(new IconStyles.ic_base()), IconStyles.INSTANCE))
                .add(new ModuleImports<>(List.of(new IconsModule.ICONS()), IconsModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<IconModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Icon()));
    }
}
