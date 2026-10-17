package hue.captains.singapura.js.homing.design.styles.ink;

import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.js.homing.design.styling.StyledStyles;
import java.util.List;

/** Ink's jar: its tab. */
public record InkStyles() implements StyleDeclaration {
    public static final InkStyles INSTANCE = new InkStyles();

    @Override public List<Provided<?, ?>> provided() {
        return List.of(new Provided<>(StyledStyles.Ink.INSTANCE, List.of(new Tab())));
    }
}
