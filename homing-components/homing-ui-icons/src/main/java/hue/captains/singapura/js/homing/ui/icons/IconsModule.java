package hue.captains.singapura.js.homing.ui.icons;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.core.StampedParams;

import java.util.ArrayList;
import java.util.List;

/**
 * The table of icon words, generated from {@link IconStyles}: {@code ICONS},
 * a frozen object of word token → the class that wears the word, one entry
 * per worded class. The {@code Icon} component looks a name up here, so a
 * new word is a record in the vocabulary and a class in the wardrobe, and
 * nothing else is written by hand.
 */
public record IconsModule() implements EsModule<IconsModule>, SelfContent {

    /** Word token → class name: {@code { check: ic_check, … }}, frozen. */
    public record ICONS() implements Exportable._Constant<IconsModule> {}

    public static final IconsModule INSTANCE = new IconsModule();

    @Override
    public ImportsFor<IconsModule> imports() {
        return ImportsFor.<IconsModule>builder()
                .add(new ModuleImports<>(IconStyles.worded(), IconStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<IconsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new ICONS()));
    }

    /** The words a component may ask for, in the vocabulary's order. */
    public static List<String> names() { return IconStyles.worded().stream().map(IconStyles.Worded::name).toList(); }

    @Override
    public List<String> selfContent(ModuleNameResolver resolver) {
        var lines = new ArrayList<String>();
        lines.add("// Generated from IconStyles - do not hand-edit. The icon words a component may ask");
        lines.add("// for, each to the class that wears the word on the mark; the design draws the rest.");
        var sb = new StringBuilder("const ICONS = Object.freeze({");
        var worded = IconStyles.worded();
        for (int i = 0; i < worded.size(); i++) {
            if (i > 0) sb.append(',');
            sb.append(' ').append(StampedParams.jsString(worded.get(i).name())).append(": ").append(worded.get(i).getClass().getSimpleName());
        }
        lines.add(sb.append(" });").toString());
        return lines;
    }
}
