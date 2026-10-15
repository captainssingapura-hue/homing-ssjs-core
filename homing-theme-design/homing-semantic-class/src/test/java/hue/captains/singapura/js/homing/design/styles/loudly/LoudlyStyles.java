package hue.captains.singapura.js.homing.design.styles.loudly;

import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.js.homing.design.styling.StyledStyles;
import java.util.List;

/** Loudly's jar: its meter. */
public record LoudlyStyles() implements StyleDeclaration {
    public static final LoudlyStyles INSTANCE = new LoudlyStyles();

    @Override public List<Provided<?, ?>> provided() {
        return List.of(new Provided<>(StyledStyles.Loudly.INSTANCE, List.of(new Meter())));
    }
}
