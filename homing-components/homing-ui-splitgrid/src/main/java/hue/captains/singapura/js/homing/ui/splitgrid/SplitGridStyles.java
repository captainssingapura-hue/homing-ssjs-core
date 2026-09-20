package hue.captains.singapura.js.homing.ui.splitgrid;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;
import java.util.Set;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Box.Control;
import static hue.captains.singapura.js.homing.design.Emphasis.Primary;
import static hue.captains.singapura.js.homing.design.Interaction.Current;
import static hue.captains.singapura.js.homing.design.Interaction.Interactive;
import static hue.captains.singapura.js.homing.design.Layer.Raised;
import static hue.captains.singapura.js.homing.design.Layer.Recessed;
import static hue.captains.singapura.js.homing.design.Structure.Divider;
import static hue.captains.singapura.js.homing.design.Structure.Spine;
import static hue.captains.singapura.js.homing.design.Target.Affordance;
import static hue.captains.singapura.js.homing.design.Target.Color;
import static hue.captains.singapura.js.homing.design.Target.Motion;
import static hue.captains.singapura.js.homing.design.Target.Shape;

/**
 * The split grid's classes. A split is a flex row or column; each child takes
 * its share — its track — through {@code --sg-ratio}, set at runtime and read
 * here; a cell is a box its owner fills, and the grid never knows with what.
 * The divider between two side-by-side cells is a spine — the design's line
 * along the leading edge of a column — and between two stacked cells a
 * divider, its rule between things; the handle around the line is layout,
 * and shows the primary surface by extent as it is hovered and held. The
 * bodies hold nothing that could be a value.
 */
public record SplitGridStyles() implements CssGroup<SplitGridStyles> {

    public static final SplitGridStyles INSTANCE = new SplitGridStyles();

    /**
     * The root: fills its host by growing and stretching, so the host is a
     * flex box — a column or a row — and the splitter is its item. Never a
     * percentage height, which a flex host cannot resolve and which would
     * keep the root from stretching. {@code --sg-min} is the smallest a cell
     * may be, set once here.
     */
    public record sg_root() implements CssClass<SplitGridStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sg-min")); }
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
    public record sg_split() implements CssClass<SplitGridStyles> {
        @Override public String body() { return """
            display: flex;
            flex: 1 1 0%;
            min-width: 0;
            min-height: 0;
            """;
        }
    }

    public record sg_split_h() implements CssClass<SplitGridStyles> {
        @Override public String body() { return "flex-direction: row;"; }
    }

    public record sg_split_v() implements CssClass<SplitGridStyles> {
        @Override public String body() { return "flex-direction: column;"; }
    }

    /** A child of a split: its share of the space is its ratio, and no less than the minimum. */
    public record sg_child() implements CssClass<SplitGridStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sg-ratio"), new CssVar("--sg-min")); }
        @Override public String body() { return """
            display: flex;
            flex: var(--sg-ratio, 1) 1 0%;
            min-width: 0;
            min-height: 0;
            overflow: hidden;
            """;
        }
    }

    public record sg_child_h() implements CssClass<SplitGridStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sg-min")); }
        @Override public String body() { return "min-width: var(--sg-min, 40px);"; }
    }

    public record sg_child_v() implements CssClass<SplitGridStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sg-min")); }
        @Override public String body() { return "min-height: var(--sg-min, 40px);"; }
    }

    /** A cell: the box the owner fills. It scrolls nothing itself; what is put in it decides. */
    public record sg_cell() implements CssClass<SplitGridStyles> {
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
    public record sg_divider() implements CssClass<SplitGridStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Interactive.class, Motion.Ease.class)); }
        @Override public String body() { return """
            flex: 0 0 auto;
            box-sizing: border-box;
            touch-action: none;
            user-select: none;
            """;
        }
    }

    /** Between side-by-side cells: a spine, the line along the leading edge of the pane that follows. */
    public record sg_divider_h() implements CssClass<SplitGridStyles> {
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
    public record sg_divider_lit() implements CssClass<SplitGridStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Primary.class, Color.Surface.class)); }
        @Override public List<? extends Wearable> extents() { return List.of(of(Primary.class, Color.Surface.class)); }
        @Override public String body() { return ""; }
    }

    /** Between stacked cells: a divider, the rule between things. */
    public record sg_divider_v() implements CssClass<SplitGridStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Divider.class, Color.Edge.class), of(Divider.class, Shape.Rule.class)); }
        @Override public String body() { return """
            height: 7px;
            cursor: row-resize;
            """;
        }
    }

    // ── The mirror ────────────────────────────────────────────────────────

    /**
     * The mirror's box: a recessed floor the grid's size at scale ({@code --sgm-w},
     * {@code --sgm-h}), a control to the keyboard — its rule carries the ring on
     * focus — with the cells drawn on it.
     */
    public record sgm_root() implements CssClass<SplitGridStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sgm-w"), new CssVar("--sgm-h")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Recessed.class, Color.Surface.class), of(Control.class, Shape.Rule.class), of(Control.class, Shape.Corner.class), of(Control.class, Color.Edge.class), of(Raised.class, Color.Edge.class)); }
        @Override public String body() { return """
            position: relative;
            box-sizing: content-box;
            width: var(--sgm-w, 0px);
            height: var(--sgm-h, 0px);
            overflow: hidden;
            """;
        }
    }

    /** A cell in the mirror: a raised box at the cell's place and measure, scaled ({@code --sgm-x}, {@code --sgm-y}, {@code --sgm-w}, {@code --sgm-h}). */
    public record sgm_cell() implements CssClass<SplitGridStyles> {
        @Override public Set<CssVar> runtimeVars() { return Set.of(new CssVar("--sgm-x"), new CssVar("--sgm-y"), new CssVar("--sgm-w"), new CssVar("--sgm-h")); }
        @Override public List<? extends Wearable> wears() { return List.of(of(Raised.class, Color.Surface.class), of(Raised.class, Color.Edge.class), of(Raised.class, Shape.Rule.class), of(Interactive.class, Affordance.Cursor.class), of(Interactive.class, Motion.Ease.class)); }
        @Override public String body() { return """
            position: absolute;
            box-sizing: border-box;
            left: var(--sgm-x);
            top: var(--sgm-y);
            width: var(--sgm-w);
            height: var(--sgm-h);
            """;
        }
    }

    /** The cell the cursor is at: the current one. */
    public record sgm_cell_current() implements CssClass<SplitGridStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Current.class, Color.Surface.class), of(Current.class, Color.Edge.class)); }
        @Override public String body() { return ""; }
    }

    @Override
    public List<CssClass<SplitGridStyles>> cssClasses() {
        return List.of(new sg_root(), new sg_split(), new sg_split_h(), new sg_split_v(), new sg_child(), new sg_child_h(), new sg_child_v(),
                       new sg_cell(), new sg_divider(), new sg_divider_h(), new sg_divider_v(), new sg_divider_lit(),
                       new sgm_root(), new sgm_cell(), new sgm_cell_current());
    }
}
