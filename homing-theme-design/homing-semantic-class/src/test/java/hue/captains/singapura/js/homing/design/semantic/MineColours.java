package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScheme.Base;
import hue.captains.singapura.js.homing.design.color.ConcreteColor;
import hue.captains.singapura.js.homing.design.color.Fixed;
import hue.captains.singapura.js.homing.design.color.L1_ColorBranch;
import hue.captains.singapura.js.homing.design.color.Palettable;

import java.util.List;
import java.util.Optional;

/**
 * An extension grafting a branch of its own onto {@link ResolvingColours}, found on the classpath: a mine's flag,
 * palettable, and the mine, fixed. Its word for paper, and its last word for every other palette.
 */
public record MineColours() implements ColorExtension {

    public record Mines() implements L1_ColorBranch<Base> {}
    public record Flag() implements Palettable<Base, Mines> {}
    public record Mine() implements Fixed<Base, Mines> {
        @Override public ConcreteColor color() { return new ConcreteColor.Srgb("#000000"); }
    }

    @Override public Class<? extends ColorSource> extending() { return ResolvingColours.class; }

    @Override public List<ColorNode<?, ?, ?>> nodes() { return List.of(new Mines(), new Flag(), new Mine()); }

    @Override public Optional<SubPalette> subPalette(ColorPalette<?> palette) {
        return palette instanceof ResolvingColours.Paper ? Optional.of(c -> c instanceof Flag ? Optional.of(new ConcreteColor.Srgb("#e53935")) : Optional.empty())
                                                         : Optional.empty();
    }

    @Override public Optional<SubPalette> baseSubPalette() {
        return Optional.of(c -> c instanceof Flag ? Optional.of(new ConcreteColor.Srgb("#d32f2f")) : Optional.empty());
    }
}
