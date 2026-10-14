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
import hue.captains.singapura.js.homing.design.Owns;
import hue.captains.singapura.js.homing.design.Target;

import java.util.List;

/**
 * The taxonomy the designs are written against: a design branch, {@code Frame}, with two virtual leaves -
 * its edge, owning a border's shape and colour, and its mark, owning a ring's - beside an ordinary box.
 */
public record DesigningTaxonomy() implements TaxonomySource {

    public static final DesigningTaxonomy INSTANCE = new DesigningTaxonomy();

    public record Region() implements L1_ComponentBranch<Root> {
        public static final Region INSTANCE = new Region();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    public record Frame() implements L2_ComponentBranch<Region>, DesignBranch {
        public static final Frame INSTANCE = new Frame();
        @Override public Region parent() { return Region.INSTANCE; }
    }

    public record Edge() implements VirtualComponent<Frame>, Owns {
        public static final Edge INSTANCE = new Edge();
        @Override public Frame parent() { return Frame.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Shape.Border.INSTANCE, Target.Color.Border.INSTANCE); }
        public interface Concrete<D extends Design<?, Frame>> extends ConcreteComponent<Edge, Frame, D> {}
    }

    public record Mark() implements VirtualComponent<Frame>, Owns {
        public static final Mark INSTANCE = new Mark();
        @Override public Frame parent() { return Frame.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Shape.Outline.INSTANCE, Target.Color.Outline.INSTANCE); }
        public interface Concrete<D extends Design<?, Frame>> extends ConcreteComponent<Mark, Frame, D> {}
    }

    public record Box() implements Component<Region> {
        public static final Box INSTANCE = new Box();
        @Override public Region parent() { return Region.INSTANCE; }
    }

    @Override public Taxonomy read() { return new ReadTaxonomy().read(List.of(Box.INSTANCE, Edge.INSTANCE, Mark.INSTANCE)); }
}
