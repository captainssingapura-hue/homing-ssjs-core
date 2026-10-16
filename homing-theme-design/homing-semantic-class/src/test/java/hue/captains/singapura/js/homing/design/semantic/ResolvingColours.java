package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.CurrentColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.Interpolated;
import hue.captains.singapura.js.homing.design.color.L1_ColorBranch;
import hue.captains.singapura.js.homing.design.color.Palettable;

import hue.captains.singapura.js.homing.design.styles.PaletteDeclaration;

import java.util.List;
import java.util.Optional;

/**
 * The colour tree the resolution's cases are painted in: surfaces, words and signals in the base - a leaf of every kind,
 * a scale from safe through clear to danger, a warning half way along it - and {@link Loud}, a scheme on top of the base
 * with a glare of its own, read by {@link LoudColours} over this layer; and the mines, a flag and a mine, in the base too.
 * Three palettes: {@link Paper} and {@link Night} of the base, {@link Neon} of the loud scheme, sitting on paper.
 */
public record ResolvingColours() implements SchemeTreeReader<BaseScheme> {

    public static final ResolvingColours INSTANCE = new ResolvingColours();

    /** A scheme on top of the base: what it sees is the base's, and its glare, read by {@link LoudColours}. */
    public abstract static class Loud extends BaseScheme {}

    public record Surfaces() implements L1_ColorBranch<BaseScheme> {}
    public record Words() implements L1_ColorBranch<BaseScheme> {}
    public record Signals() implements L1_ColorBranch<BaseScheme> {}
    public record Mines() implements L1_ColorBranch<BaseScheme> {}

    public record Page() implements Palettable<BaseScheme, Surfaces> {}
    public record Raised() implements Palettable<BaseScheme, Surfaces> {}
    public record Clear() implements Fixed<BaseScheme, Surfaces> {
        @Override public ConcreteColor color() { return new ConcreteColor.Srgb("transparent"); }
    }
    public record Copy() implements Palettable<BaseScheme, Words> {}
    public record Danger() implements Palettable<BaseScheme, Signals> {}
    public record Safe() implements Palettable<BaseScheme, Signals> {}
    public record Alarm() implements Fixed<BaseScheme, Signals> {
        @Override public ConcreteColor color() { return new ConcreteColor.Srgb("#c62828"); }
    }
    /** Safe at -1, clear at 0, danger at 1. */
    public record Risk() implements ColorScale<BaseScheme, Signals> {
        @Override public List<Knot<BaseScheme>> knots() {
            return List.of(new Knot<>(new Safe(), -1), new Knot<>(new Clear(), 0), new Knot<>(new Danger(), 1));
        }
    }
    /** Half way to danger. */
    /** A line between rows: the colour it is given, at a share. */
    public record Hairline() implements CurrentColor<BaseScheme, Words> {
        @Override public double share() { return 0.3; }
    }

    public record Warning() implements Interpolated<BaseScheme, Signals> {
        @Override public ColorScale<? super BaseScheme, ?> scale() { return new Risk(); }
        @Override public double degree() { return 0.5; }
    }
    /** A mine's flag: each palette's. */
    public record Flag() implements Palettable<BaseScheme, Mines> {}
    /** A mine: black, whatever the palette. */
    public record Mine() implements Fixed<BaseScheme, Mines> {
        @Override public ConcreteColor color() { return new ConcreteColor.Srgb("#000000"); }
    }

    /** The loud scheme's own. */
    public record Glare() implements Palettable<Loud, Signals> {}

    /** The base's layer: every colour of the base, the mines among them. */
    @Override public List<? extends ColorNode<BaseScheme, ?, ?>> layer() {
        return List.of(new Surfaces(), new Words(), new Signals(), new Mines(), new Page(), new Raised(), new Clear(), new Copy(), new Danger(), new Safe(), new Alarm(), new Risk(), new Warning(), new Hairline(), new Flag(), new Mine());
    }

    /** The loud scheme's reader: its glare, over the base. */
    public record LoudColours() implements SchemeTreeReader<Loud> {
        public static final LoudColours INSTANCE = new LoudColours();
        @Override public List<? extends ColorNode<Loud, ?, ?>> layer() { return List.of(new Glare()); }
        @Override public Optional<SchemeTreeReader<? super Loud>> below() { return Optional.of(ResolvingColours.INSTANCE); }
    }

    /** The resolution's jar of palettes, declared: the night first, the default; paper; neon on paper. */
    public record Palettes() implements PaletteDeclaration {
        public static final Palettes INSTANCE = new Palettes();
        @Override public List<? extends ColorPalette<?>> palettes() { return List.of(Night.INSTANCE, Paper.INSTANCE, Neon.INSTANCE); }
    }

    /** White paper, dark copy: the base's every palettable colour. */
    public record Paper() implements ColorPalette<BaseScheme> {
        public static final Paper INSTANCE = new Paper();
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) {
            return Optional.ofNullable(switch (color) {
                case Page p -> new ConcreteColor.Srgb("#ffffff");
                case Raised r -> new ConcreteColor.Srgb("#f4f4f4");
                case Copy c -> new ConcreteColor.Srgb("#1f2937");
                case Danger d -> new ConcreteColor.Srgb("#dc2626");
                case Safe s -> new ConcreteColor.Srgb("#16a34a");
                case Flag f -> new ConcreteColor.Srgb("#e53935");
                default -> null;
            });
        }
    }

    /** The night: the base's colours in the dark, and a mine's flag of its own. */
    public record Night() implements ColorPalette<BaseScheme> {
        public static final Night INSTANCE = new Night();
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) {
            return Optional.ofNullable(switch (color) {
                case Page p -> new ConcreteColor.Oklch("oklch(0.2 0.02 260)");
                case Raised r -> new ConcreteColor.Oklch("oklch(0.27 0.02 260)");
                case Copy c -> new ConcreteColor.Oklch("oklch(0.93 0.01 260)");
                case Danger d -> new ConcreteColor.Oklch("oklch(0.65 0.2 25)");
                case Safe s -> new ConcreteColor.Oklch("oklch(0.7 0.17 150)");
                case Flag f -> new ConcreteColor.Srgb("#ff6f00");
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
    public record Unsafe() implements ColorPalette<BaseScheme> {
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) {
            return color instanceof Safe ? Optional.empty() : Paper.INSTANCE.color(color);
        }
    }

    /** What a scheme never is: the marker itself. */
    public record Unmarked() implements ColorPalette<ColorScheme> {
        @Override public Optional<ConcreteColor> color(Palettable<?, ?> color) { return Optional.empty(); }
    }
}
