package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;
import hue.captains.singapura.js.homing.component.taxonomy.Design;
import hue.captains.singapura.js.homing.component.taxonomy.DesignBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L1_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L2_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ReadTaxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomySource;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.AnimationEffect;
import hue.captains.singapura.js.homing.design.Owns;
import hue.captains.singapura.js.homing.design.Target;

import java.util.List;

/**
 * A taxonomy whose design branch, {@code Overlay}, has leaves that move: a line, owning a stroke's colour and an
 * effect; an area, owning a ground and an effect; a plate that owns a ground alone; and a strip that owns a
 * background's image beside an effect - so an effect that takes the image clashes with it. Beside them an ordinary
 * pad, and the effects the designs wear.
 */
public record AnimatingTaxonomy() implements TaxonomySource {

    public static final AnimatingTaxonomy INSTANCE = new AnimatingTaxonomy();

    public record Stage() implements L1_ComponentBranch<Root> {
        public static final Stage INSTANCE = new Stage();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    public record Overlay() implements L2_ComponentBranch<Stage>, DesignBranch {
        public static final Overlay INSTANCE = new Overlay();
        @Override public Stage parent() { return Stage.INSTANCE; }
    }

    public record Line() implements VirtualComponent<Overlay>, Owns {
        public static final Line INSTANCE = new Line();
        @Override public Overlay parent() { return Overlay.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Color.Stroke.INSTANCE, Target.Animation.Effect.INSTANCE); }
        public interface Concrete<D extends Design<?, Overlay>> extends ConcreteComponent<Line, Overlay, D> {}
    }

    public record Area() implements VirtualComponent<Overlay>, Owns {
        public static final Area INSTANCE = new Area();
        @Override public Overlay parent() { return Overlay.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Color.Background.INSTANCE, Target.Animation.Effect.INSTANCE); }
        public interface Concrete<D extends Design<?, Overlay>> extends ConcreteComponent<Area, Overlay, D> {}
    }

    public record Plate() implements VirtualComponent<Overlay>, Owns {
        public static final Plate INSTANCE = new Plate();
        @Override public Overlay parent() { return Overlay.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Color.Background.INSTANCE); }
        public interface Concrete<D extends Design<?, Overlay>> extends ConcreteComponent<Plate, Overlay, D> {}
    }

    public record Strip() implements VirtualComponent<Overlay>, Owns {
        public static final Strip INSTANCE = new Strip();
        @Override public Overlay parent() { return Overlay.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Shape.Background.INSTANCE, Target.Animation.Effect.INSTANCE); }
        public interface Concrete<D extends Design<?, Overlay>> extends ConcreteComponent<Strip, Overlay, D> {}
    }

    public record Pad() implements Component<Stage> {
        public static final Pad INSTANCE = new Pad();
        @Override public Stage parent() { return Stage.INSTANCE; }
    }

    @Override public Taxonomy read() {
        return new ReadTaxonomy().read(List.of(Pad.INSTANCE, Line.INSTANCE, Area.INSTANCE, Plate.INSTANCE, Strip.INSTANCE));
    }

    // ── the effects ────────────────────────────────────────────────────────

    /** A dashed line whose dashes march along it. */
    public record Ants() implements AnimationEffect {
        public static final Ants INSTANCE = new Ants();
        @Override public String meaning() { return "a dashed line whose dashes travel along it"; }
        @Override public List<Target.Color> drawnIn() { return List.of(Target.Color.Stroke.INSTANCE); }
        @Override public String moving() { return "stroke-width: 1.5px; stroke-dasharray: 5 4;"; }
        @Override public String still() { return "stroke-width: 1.5px; stroke-dasharray: 5 4;"; }
        @Override public String frames() { return "to { stroke-dashoffset: -9; }"; }
        @Override public String timing() { return "0.6s linear infinite"; }
    }

    /** A band of tint sweeping across a box; still, no band. */
    public record Sweep() implements AnimationEffect {
        public static final Sweep INSTANCE = new Sweep();
        @Override public String meaning() { return "a band of tint sweeping across a box"; }
        @Override public List<Target.Color> drawnIn() { return List.of(Target.Color.Background.INSTANCE); }
        @Override public String moving() {
            return "background-image: linear-gradient(90deg, transparent, var(--background-ramp-via), transparent);"
                 + " background-size: 200% 100%; background-repeat: no-repeat;";
        }
        @Override public String still() { return ""; }
        @Override public String frames() { return "from { background-position: 150% 0; }\nto { background-position: -50% 0; }"; }
        @Override public String timing() { return "1.6s linear infinite"; }
    }
}
