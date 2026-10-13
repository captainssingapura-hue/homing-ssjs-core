package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.ColorScheme.Base;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.L1_ColorBranch;
import hue.captains.singapura.js.homing.design.color.Palettable;

import java.util.List;
import java.util.Optional;

/**
 * The colour tree the resolution's cases are painted in: surfaces, words and signals in the base - a leaf of every kind,
 * a scale from safe through clear to danger, a warning half way along it - and {@link Loud}, a scheme on top of the base
 * with a glare of its own. Three palettes: {@link Paper} and {@link Night} of the base, {@link Neon} of the loud scheme,
 * sitting on paper. {@link MineColours}, found on the classpath, grafts a branch of its own with its sub-palettes.
 */
public record ResolvingColours() implements ColorSource {

    public static final ResolvingColours INSTANCE = new ResolvingColours();

    /** A scheme on top of the base: what it sees is the base's, and its glare. */
    public abstract static class Loud extends Base {}

    public record Surfaces() implements L1_ColorBranch<Base> {}
    public record Words() implements L1_ColorBranch<Base> {}
    public record Signals() implements L1_ColorBranch<Base> {}

    public record Page() implements Palettable<Base, Surfaces> {}
    public record Raised() implements Palettable<Base, Surfaces> {}
    public record Clear() implements Fixed<Base, Surfaces> {
        @Override public ConcreteColor color() { return new ConcreteColor.Srgb("transparent"); }
    }
    public record Copy() implements Palettable<Base, Words> {}
    public record Danger() implements Palettable<Base, Signals> {}
    public record Safe() implements Palettable<Base, Signals> {}
    public record Alarm() implements Fixed<Base, Signals> {
        @Override public ConcreteColor color() { return new ConcreteColor.Srgb("#c62828"); }
    }
    /** Safe at -1, clear at 0, danger at 1. */
    public record Risk() implements ColorScale<Base, Signals> {
        @Override public List<Knot<Base>> knots() {
            return List.of(new Knot<>(new Safe(), -1), new Knot<>(new Clear(), 0), new Knot<>(new Danger(), 1));
        }
    }
    /** Half way to danger. */
    public record Warning() implements Interpolated<Base, Signals> {
        @Override public ColorScale<? super Base, ?> scale() { return new Risk(); }
        @Override public double degree() { return 0.5; }
    }
    /** The loud scheme's own. */
    public record Glare() implements Palettable<Loud, Signals> {}

    @Override public ColorTree read() {
        return ReadColors.INSTANCE.read(ResolvingColours.class, List.of(new Surfaces(), new Words(), new Signals(), new Page(), new Raised(),
                new Clear(), new Copy(), new Danger(), new Safe(), new Alarm(), new Risk(), new Warning(), new Glare()));
    }

    /** White paper, dark copy: the base's every palettable colour. */
    public record Paper() implements ColorPalette<Base> {
        public static final Paper INSTANCE = new Paper();
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) {
            return Optional.ofNullable(switch (color) {
                case Page p -> new ConcreteColor.Srgb("#ffffff");
                case Raised r -> new ConcreteColor.Srgb("#f4f4f4");
                case Copy c -> new ConcreteColor.Srgb("#1f2937");
                case Danger d -> new ConcreteColor.Srgb("#dc2626");
                case Safe s -> new ConcreteColor.Srgb("#16a34a");
                default -> null;
            });
        }
    }

    /** The night: the base's colours in the dark, and a mine's flag of its own. */
    public record Night() implements ColorPalette<Base> {
        public static final Night INSTANCE = new Night();
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) {
            return Optional.ofNullable(switch (color) {
                case Page p -> new ConcreteColor.Oklch("oklch(0.2 0.02 260)");
                case Raised r -> new ConcreteColor.Oklch("oklch(0.27 0.02 260)");
                case Copy c -> new ConcreteColor.Oklch("oklch(0.93 0.01 260)");
                case Danger d -> new ConcreteColor.Oklch("oklch(0.65 0.2 25)");
                case Safe s -> new ConcreteColor.Oklch("oklch(0.7 0.17 150)");
                case MineColours.Flag f -> new ConcreteColor.Srgb("#ff6f00");
                default -> null;
            });
        }
    }

    /** Neon, on paper: the glare the loud scheme adds, and its own danger. */
    public record Neon() implements ColorPalette<Loud> {
        public static final Neon INSTANCE = new Neon();
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) {
            return Optional.ofNullable(switch (color) {
                case Glare g -> new ConcreteColor.Srgb("#39ff14");
                case Danger d -> new ConcreteColor.Srgb("#ff0055");
                default -> null;
            });
        }
        @Override public Optional<ColorPalette<? super Loud>> below() { return Optional.of(Paper.INSTANCE); }
    }

    /** A loud style's palette that forgets what it sits on. */
    public record Stranded() implements ColorPalette<Loud> {
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) { return Optional.empty(); }
    }

    /** One that sits on a palette of its own scheme. */
    public record Stacked() implements ColorPalette<Loud> {
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) { return Optional.empty(); }
        @Override public Optional<ColorPalette<? super Loud>> below() { return Optional.of(Neon.INSTANCE); }
    }

    /** A base palette that answers nothing of the warning's scale's safe end. */
    public record Unsafe() implements ColorPalette<Base> {
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) {
            return color instanceof Safe ? Optional.empty() : Paper.INSTANCE.color(color);
        }
    }

    /** What a scheme never is: the marker itself. */
    public record Unmarked() implements ColorPalette<ColorScheme> {
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) { return Optional.empty(); }
    }
}
