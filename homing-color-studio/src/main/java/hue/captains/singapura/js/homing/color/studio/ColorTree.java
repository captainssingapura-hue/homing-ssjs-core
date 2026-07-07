package hue.captains.singapura.js.homing.color.studio;

import com.hominglabs.rebar.core.desc.WithDesc;
import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.affective.Cheerful;
import hue.captains.singapura.js.homing.color.semantic.affective.Neutral;
import hue.captains.singapura.js.homing.color.semantic.affective.Serene;
import hue.captains.singapura.js.homing.color.semantic.affective.Somber;
import hue.captains.singapura.js.homing.color.semantic.affective.Tense;
import hue.captains.singapura.js.homing.color.semantic.group.Affective;
import hue.captains.singapura.js.homing.color.semantic.group.Categorical;
import hue.captains.singapura.js.homing.color.semantic.group.ColorGroup;
import hue.captains.singapura.js.homing.color.semantic.group.Colors;
import hue.captains.singapura.js.homing.color.semantic.group.Diverging;
import hue.captains.singapura.js.homing.color.semantic.group.Encoding;
import hue.captains.singapura.js.homing.color.semantic.group.Expressive;
import hue.captains.singapura.js.homing.color.semantic.group.Identity;
import hue.captains.singapura.js.homing.color.semantic.group.Sequential;
import hue.captains.singapura.js.homing.color.semantic.group.Structural;
import hue.captains.singapura.js.homing.color.semantic.group.Symbolic;
import hue.captains.singapura.js.homing.color.semantic.identity.Brand;
import hue.captains.singapura.js.homing.color.semantic.identity.BrandEmphasis;
import hue.captains.singapura.js.homing.color.semantic.structural.Border;
import hue.captains.singapura.js.homing.color.semantic.structural.BorderEmphasis;
import hue.captains.singapura.js.homing.color.semantic.structural.Surface;
import hue.captains.singapura.js.homing.color.semantic.structural.SurfaceInverted;
import hue.captains.singapura.js.homing.color.semantic.structural.SurfaceRaised;
import hue.captains.singapura.js.homing.color.semantic.structural.SurfaceRecessed;
import hue.captains.singapura.js.homing.color.semantic.structural.Text;
import hue.captains.singapura.js.homing.color.semantic.structural.TextMuted;
import hue.captains.singapura.js.homing.color.semantic.symbolic.Danger;
import hue.captains.singapura.js.homing.color.semantic.symbolic.Info;
import hue.captains.singapura.js.homing.color.semantic.symbolic.Success;
import hue.captains.singapura.js.homing.color.semantic.symbolic.Warning;
import hue.captains.singapura.js.homing.tree.DimensionKey;
import hue.captains.singapura.js.homing.tree.DimensionValue;
import hue.captains.singapura.js.homing.tree.DisplayLabel;
import hue.captains.singapura.js.homing.tree.Kind;
import hue.captains.singapura.js.homing.tree.LevelDepth;
import hue.captains.singapura.js.homing.tree.NormalizedNode;
import hue.captains.singapura.js.homing.tree.Summary;
import hue.captains.singapura.js.homing.tree.TreeLevel;
import hue.captains.singapura.js.homing.tree.dims.DepthValue;
import hue.captains.singapura.js.homing.tree.dims.NameValue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Adapts the {@code homing-semantic-color} ColorGroup taxonomy into the rigid
 * tree's {@link NormalizedNode} — so the universal JS {@code TreeRenderer} draws
 * it with zero bespoke JS.
 *
 * <p>The rebar ColorGroup nodes are single-parent (they expose {@code parent()},
 * not {@code children()}), so the tree shape is composed explicitly here — the
 * one place that knows the taxonomy's full structure. Groups become branches
 * ({@code kind=group}); colours become leaves ({@code kind=color}).</p>
 */
public final class ColorTree {

    private ColorTree() {}

    /** The whole ColorGroup taxonomy as a NormalizedNode tree rooted at L0. */
    public static NormalizedNode build() {
        return group("Colors", Colors.INSTANCE, 0, List.of(
                group("Expressive", Expressive.INSTANCE, 1, List.of(
                        group("Affective", Affective.INSTANCE, 2, List.of(
                                color(Serene.INSTANCE, 3), color(Cheerful.INSTANCE, 3),
                                color(Tense.INSTANCE, 3), color(Somber.INSTANCE, 3),
                                color(Neutral.INSTANCE, 3))),
                        group("Symbolic", Symbolic.INSTANCE, 2, List.of(
                                color(Danger.INSTANCE, 3), color(Success.INSTANCE, 3),
                                color(Warning.INSTANCE, 3), color(Info.INSTANCE, 3))))),
                group("Encoding", Encoding.INSTANCE, 1, List.of(
                        group("Categorical", Categorical.INSTANCE, 2, List.of()),
                        group("Sequential", Sequential.INSTANCE, 2, List.of()),
                        group("Diverging", Diverging.INSTANCE, 2, List.of()))),
                group("Identity", Identity.INSTANCE, 1, List.of(
                        color(Brand.INSTANCE, 2), color(BrandEmphasis.INSTANCE, 2))),
                group("Structural", Structural.INSTANCE, 1, List.of(
                        color(Surface.INSTANCE, 2), color(SurfaceRaised.INSTANCE, 2),
                        color(SurfaceRecessed.INSTANCE, 2), color(SurfaceInverted.INSTANCE, 2),
                        color(Text.INSTANCE, 2), color(TextMuted.INSTANCE, 2),
                        color(Border.INSTANCE, 2), color(BorderEmphasis.INSTANCE, 2)))));
    }

    private static NormalizedNode group(String label, ColorGroup node, int depth, List<NormalizedNode> children) {
        return new NormalizedNode(TreeLevel.atDepth(depth), dims(label, node.desc().summary().text(), "group", depth), children);
    }

    private static NormalizedNode color(SemanticColor color, int depth) {
        String summary = (color instanceof WithDesc wd) ? wd.desc().summary().text() : "";
        return new NormalizedNode(TreeLevel.atDepth(depth), dims(color.name(), summary, "color", depth), List.of());
    }

    private static Map<DimensionKey, DimensionValue> dims(String label, String summary, String kind, int depth) {
        var m = new LinkedHashMap<DimensionKey, DimensionValue>();
        m.put(DisplayLabel.INSTANCE, new NameValue(label));
        m.put(Summary.INSTANCE, new NameValue(summary));
        m.put(Kind.INSTANCE, new NameValue(kind));
        m.put(LevelDepth.INSTANCE, new DepthValue(depth));
        return m;
    }
}
