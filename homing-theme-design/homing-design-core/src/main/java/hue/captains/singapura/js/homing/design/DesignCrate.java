package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;

import java.util.ArrayList;
import java.util.List;

/**
 * The crate of the design substrate: the 29 target groups it serves, and —
 * because a crate is where a deployment says what it ships — the
 * pre-registration of the framework's nine semantic branches with the
 * {@link Vocabulary}. A product crate that adds leaves registers them the
 * same way, in its own static initialiser, and requires this one.
 */
public final class DesignCrate implements Crate {

    public static final DesignCrate INSTANCE = new DesignCrate();

    static {
        Vocabulary.register(
                Feedback.Danger.class, Feedback.Warning.class, Feedback.Success.class, Feedback.Info.class,
                Emphasis.Primary.class, Emphasis.Secondary.class, Emphasis.Tertiary.class, Emphasis.Muted.class,
                Layer.Base.class, Layer.Raised.class, Layer.Recessed.class, Layer.Inverted.class, Layer.Overlay.class,
                Interaction.Interactive.class, Interaction.Selected.class, Interaction.Current.class,
                Interaction.Focus.class, Interaction.Dragging.class, Interaction.DropTarget.class, Interaction.Inert.class,
                Text.Body.class, Text.Heading.class, Text.Display.class, Text.Caption.class, Text.Label.class, Text.Code.class, Text.Link.class,
                Text.Kicker.class, Text.Prose.class, Text.Lede.class, Text.Numeral.class,
                Pairing.OnPrimary.class, Pairing.OnSecondary.class, Pairing.OnDanger.class, Pairing.OnWarning.class,
                Pairing.OnSuccess.class, Pairing.OnInfo.class, Pairing.OnInverted.class, Pairing.OnInvertedMuted.class, Pairing.OnOverlay.class,
                Box.Control.class, Box.Inline.class, Box.Container.class, Box.Section.class,
                Brand.Mark.class, Brand.House.class,
                Structure.Divider.class, Structure.Hairline.class, Structure.Bar.class, Structure.Spine.class, Structure.Marker.class, Structure.Cap.class, Structure.Rail.class, Structure.Backdrop.class);
    }

    private DesignCrate() {}

    @Override public String name() { return "homing-design-core"; }

    @Override
    public List<CrateEntry> entries() {
        var out = new ArrayList<CrateEntry>();
        for (Target t : Target.leaves()) out.add(CrateEntry.of((EsModule<?>) t));
        return List.copyOf(out);
    }
}
