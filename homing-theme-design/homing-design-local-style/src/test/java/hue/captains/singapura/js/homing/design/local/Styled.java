package hue.captains.singapura.js.homing.design.local;

import hue.captains.singapura.js.homing.component.taxonomy.L1_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L2_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.LocalStyleBranch;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;

import java.util.List;

/** A local-style branch, two virtual leaves under it, two local styles of it and their concrete leaves. */
final class Styled {

    private Styled() {}

    record Region() implements L1_ComponentBranch<Root> {
        static final Region INSTANCE = new Region();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Frame() implements L2_ComponentBranch<Region>, LocalStyleBranch {
        static final Frame INSTANCE = new Frame();
        @Override public Region parent() { return Region.INSTANCE; }
    }

    record Edge() implements VirtualComponent<Frame> {
        static final Edge INSTANCE = new Edge();
        @Override public Frame parent() { return Frame.INSTANCE; }
        interface Concrete<D extends LocalStyle<?, Frame>> extends ConcreteComponent<Edge, Frame, D> {}
    }

    record Mark() implements VirtualComponent<Frame> {
        static final Mark INSTANCE = new Mark();
        @Override public Frame parent() { return Frame.INSTANCE; }
        interface Concrete<D extends LocalStyle<?, Frame>> extends ConcreteComponent<Mark, Frame, D> {}
    }

    record Plain() implements LocalStyle<BaseScheme, Frame> {
        static final Plain INSTANCE = new Plain();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new PlainEdge(), new DirectMark()); }
    }

    record Bold() implements LocalStyle<BaseScheme, Frame> {
        static final Bold INSTANCE = new Bold();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Frame, ?>> concretes() { return List.of(new BoldMark()); }
    }

    record PlainEdge() implements Edge.Concrete<Plain> {}

    record BoldMark() implements Mark.Concrete<Bold> {}

    /** A concrete leaf that names all three arguments itself, without its leaf's interface. */
    record DirectMark() implements ConcreteComponent<Mark, Frame, Plain> {}

    /** Its leaf's interface named raw: no local style given. */
    @SuppressWarnings("rawtypes")
    record RawEdge() implements Edge.Concrete {}
}
