package hue.captains.singapura.js.homing.ui.split;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Structure.Divider;
import static hue.captains.singapura.js.homing.design.Structure.Spine;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;

/**
 * The splitter's classes. A split is a flex row or column; each child takes
 * its share through {@code --sp-ratio}, set at runtime and read here; a leaf
 * is a box its holder fills. The divider between two side-by-side panes is
 * a spine — the design's line along the leading edge of a column — and
 * between two stacked panes a divider, its rule between things; the handle
 * around the line is layout, and shows the primary surface by extent as it
 * is hovered and held. The bodies hold nothing that could be a value.
 */
public record SplitStyles() implements CssGroup<SplitStyles> {

    public static final SplitStyles INSTANCE = new SplitStyles();

    /**
     * The root: fills its host by growing and stretching, so the host is a
     * flex box — a column or a row — and the splitter is its item. Never a
     * percentage height, which a flex host cannot resolve and which would
     * keep the root from stretching. {@code --sp-min} is the smallest a pane
     * may be, set once here.
     */
    public record sp_root() implements CssClass<SplitStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sp-min")); }
        @Override public String body() { return """
            display: flex;
            width: 100%;
            flex: 1 1 auto;
            align-self: stretch;
            min-width: 0;
            min-height: 0;
            """;
        }
    }

    /** A split: its children in a row or a column, sharing its space. */
    public record sp_split() implements CssClass<SplitStyles> {
        @Override public String body() { return """
            display: flex;
            flex: 1 1 0%;
            min-width: 0;
            min-height: 0;
            """;
        }
    }

    public record sp_split_h() implements CssClass<SplitStyles> {
        @Override public String body() { return "flex-direction: row;"; }
    }

    public record sp_split_v() implements CssClass<SplitStyles> {
        @Override public String body() { return "flex-direction: column;"; }
    }

    /** A child of a split: its share of the space is its ratio, and no less than the minimum. */
    public record sp_child() implements CssClass<SplitStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sp-ratio"), new CssVar("--sp-min")); }
        @Override public String body() { return """
            display: flex;
            flex: var(--sp-ratio, 1) 1 0%;
            min-width: 0;
            min-height: 0;
            overflow: hidden;
            """;
        }
    }

    public record sp_child_h() implements CssClass<SplitStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sp-min")); }
        @Override public String body() { return "min-width: var(--sp-min, 40px);"; }
    }

    public record sp_child_v() implements CssClass<SplitStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sp-min")); }
        @Override public String body() { return "min-height: var(--sp-min, 40px);"; }
    }

    /** A leaf: the box the holder fills. It scrolls nothing itself; what is put in it decides. */
    public record sp_leaf() implements CssClass<SplitStyles> {
        @Override public String body() { return """
            flex: 1 1 auto;
            min-width: 0;
            min-height: 0;
            position: relative;
            display: flex;
            flex-direction: column;
            """;
        }
    }

    /** The handle between two neighbours: a few pixels to grab, the design's line along one edge; nothing of its own at rest. */
    public record sp_divider() implements CssClass<SplitStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Interactive.class, Motion.Ease.class)); }
        @Override public String body() { return """
            flex: 0 0 auto;
            box-sizing: border-box;
            touch-action: none;
            user-select: none;
            """;
        }
    }

    /** Between side-by-side panes: a spine, the line along the leading edge of the pane that follows. */
    public record sp_divider_h() implements CssClass<SplitStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Spine.class, Color.Edge.class), of(Spine.class, Shape.Rule.class)); }
        @Override public String body() { return """
            width: 7px;
            cursor: col-resize;
            """;
        }
    }

    /**
     * The handle lit: the primary surface by extent, worn while the pointer
     * is over it or holds it — part of the way from the design's neutral
     * on hover, at full while held. The splitter sets the number and takes
     * the class off at rest; the design owns the anchors.
     */
    public record sp_divider_lit() implements CssClass<SplitStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class)); }
        @Override public List<? extends Wearable> extents() { return List.of(of(Primary.class, Color.Surface.class)); }
        @Override public String body() { return ""; }
    }

    /** Between stacked panes: a divider, the rule between things. */
    public record sp_divider_v() implements CssClass<SplitStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Divider.class, Color.Edge.class), of(Divider.class, Shape.Rule.class)); }
        @Override public String body() { return """
            height: 7px;
            cursor: row-resize;
            """;
        }
    }

    @Override
    public List<CssClass<SplitStyles>> cssClasses() {
        return List.of(new sp_root(), new sp_split(), new sp_split_h(), new sp_split_v(), new sp_child(), new sp_child_h(), new sp_child_v(),
                       new sp_leaf(), new sp_divider(), new sp_divider_h(), new sp_divider_v(), new sp_divider_lit());
    }
}
