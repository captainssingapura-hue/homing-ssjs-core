package hue.captains.singapura.js.homing.ui.icons;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Icon;
import hue.captains.singapura.js.homing.design.Trees;

import java.util.List;
import java.util.Optional;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.Type;

/**
 * The icons' styles: the mark's box, and one class per {@link Icon} word,
 * wearing that word on {@code Type.Glyph} — the design writes the picture
 * as {@code content} on the mark's {@code ::before}, and nothing here is a
 * picture. A word without a class here, or a class without a word, fails
 * the crate's test: the vocabulary and the wardrobe are the same list.
 */
public record IconStyles() implements CssGroup<IconStyles> {

    public static final IconStyles INSTANCE = new IconStyles();

    /**
     * The mark: an inline box of one em, a quarter wider than tall so the
     * widest glyph sits in it, the glyph centred; it takes the ink and the
     * size of the text it sits in, and it is never in the way of the hand.
     */
    public record ic_base() implements CssClass<IconStyles> {
        @Override public String body() { return """
            display: inline-flex;
            align-items: center;
            justify-content: center;
            flex: none;
            inline-size: 1.25em;
            block-size: 1em;
            line-height: 1;
            font-style: normal;
            font-weight: normal;
            vertical-align: -0.125em;
            pointer-events: none;
            user-select: none;
            """;
        }
    }

    /** One word, one class: the word on the glyph target. */
    public sealed interface Worded extends CssClass<IconStyles> permits ic_check, ic_disclose, ic_close, ic_detach, ic_rotate, ic_flip, ic_add, ic_remove, ic_reset, ic_pin, ic_settings,
                                                                          ic_grip, ic_size, ic_aspect, ic_extent, ic_level, ic_column, ic_row, ic_within {
        Class<? extends Icon> word();
        @Override default List<? extends Wearable> wears() { return List.of(of(word(), Type.Glyph.class)); }
        @Override default String body() { return ""; }
        /** The word's token, the name a component asks the icon by: {@code check}, {@code disclose} … */
        default String name() { return Trees.semanticToken(word()); }
    }

    public record ic_check()    implements Worded { @Override public Class<? extends Icon> word() { return Icon.Check.class; } }
    public record ic_disclose() implements Worded { @Override public Class<? extends Icon> word() { return Icon.Disclose.class; } }
    public record ic_close()    implements Worded { @Override public Class<? extends Icon> word() { return Icon.Close.class; } }
    public record ic_detach()   implements Worded { @Override public Class<? extends Icon> word() { return Icon.Detach.class; } }
    public record ic_rotate()   implements Worded { @Override public Class<? extends Icon> word() { return Icon.Rotate.class; } }
    public record ic_flip()     implements Worded { @Override public Class<? extends Icon> word() { return Icon.Flip.class; } }
    public record ic_add()      implements Worded { @Override public Class<? extends Icon> word() { return Icon.Add.class; } }
    public record ic_remove()   implements Worded { @Override public Class<? extends Icon> word() { return Icon.Remove.class; } }
    public record ic_reset()    implements Worded { @Override public Class<? extends Icon> word() { return Icon.Reset.class; } }
    public record ic_pin()      implements Worded { @Override public Class<? extends Icon> word() { return Icon.Pin.class; } }
    public record ic_settings() implements Worded { @Override public Class<? extends Icon> word() { return Icon.Settings.class; } }
    public record ic_grip()     implements Worded { @Override public Class<? extends Icon> word() { return Icon.Grip.class; } }
    public record ic_size()     implements Worded { @Override public Class<? extends Icon> word() { return Icon.Size.class; } }
    public record ic_aspect()   implements Worded { @Override public Class<? extends Icon> word() { return Icon.Aspect.class; } }
    public record ic_extent()   implements Worded { @Override public Class<? extends Icon> word() { return Icon.Extent.class; } }
    public record ic_level()    implements Worded { @Override public Class<? extends Icon> word() { return Icon.Level.class; } }
    public record ic_column()   implements Worded { @Override public Class<? extends Icon> word() { return Icon.Column.class; } }
    public record ic_row()      implements Worded { @Override public Class<? extends Icon> word() { return Icon.Row.class; } }
    public record ic_within()   implements Worded { @Override public Class<? extends Icon> word() { return Icon.Within.class; } }

    /** The worded classes, in the vocabulary's order. */
    public static List<Worded> worded() {
        return List.of(new ic_check(), new ic_disclose(), new ic_close(), new ic_detach(), new ic_rotate(), new ic_flip(),
                       new ic_add(), new ic_remove(), new ic_reset(), new ic_pin(), new ic_settings(),
                       new ic_grip(), new ic_size(), new ic_aspect(), new ic_extent(), new ic_level(),
                       new ic_column(), new ic_row(), new ic_within());
    }

    /** The class for a word, if the wardrobe has it. */
    public static Optional<Worded> forWord(Class<? extends Icon> word) {
        return worded().stream().filter(w -> w.word() == word).findFirst();
    }

    /** The design pair a word is worn as: for a test, or a component that wears a word directly. */
    public static DesignClass<Type.Glyph> glyph(Class<? extends Icon> word) { return of(word, Type.Glyph.class); }

    @Override
    public List<CssClass<IconStyles>> cssClasses() {
        var all = new java.util.ArrayList<CssClass<IconStyles>>();
        all.add(new ic_base());
        all.addAll(worded());
        return List.copyOf(all);
    }
}
