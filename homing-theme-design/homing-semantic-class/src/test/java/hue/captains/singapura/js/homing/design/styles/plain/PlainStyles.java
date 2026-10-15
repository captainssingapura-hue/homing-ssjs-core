package hue.captains.singapura.js.homing.design.styles.plain;

import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.js.homing.design.styling.StyledStyles;
import java.util.List;

/** Plain's jar: every casting of the fixtures' taxonomy, the base layer's. */
public record PlainStyles() implements StyleDeclaration {
    public static final PlainStyles INSTANCE = new PlainStyles();

    @Override public List<Provided<?, ?>> provided() {
        return List.of(new Provided<>(StyledStyles.Plain.INSTANCE, List.of(new Tab(), new TabLead(), new Strip(), new Meter(), new Badge())));
    }
}
