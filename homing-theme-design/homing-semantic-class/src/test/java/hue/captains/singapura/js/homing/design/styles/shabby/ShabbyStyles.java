package hue.captains.singapura.js.homing.design.styles.shabby;

import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.js.homing.design.styling.StyledStyles;
import java.util.List;

/** Shabby's jar: every mistake the check refuses - and a CSS file it leaves out. */
public record ShabbyStyles() implements StyleDeclaration {
    public static final ShabbyStyles INSTANCE = new ShabbyStyles();

    @Override public List<Provided<?, ?>> provided() {
        return List.of(new Provided<>(StyledStyles.Shabby.INSTANCE, List.of(new Tab(), new TabAgain(), new Strip(), new Meter(), new TabLead(), new Badge())));
    }
}
