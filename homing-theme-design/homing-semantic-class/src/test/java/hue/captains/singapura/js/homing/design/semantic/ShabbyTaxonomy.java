package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.L1_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.LocalStyleBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ReadTaxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.Owns;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.local.ConcreteComponent;
import hue.captains.singapura.js.homing.design.local.LocalStyle;

import java.util.List;

/**
 * A local-style branch whose leaves own their targets badly - one owns nothing, one owns a target twice, one
 * owns a sound no sheet carries - and a local style of it that is complete all the same.
 */
public record ShabbyTaxonomy() {

    public static final ShabbyTaxonomy INSTANCE = new ShabbyTaxonomy();

    public record Shabby() implements L1_ComponentBranch<Root>, LocalStyleBranch {
        public static final Shabby INSTANCE = new Shabby();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    public record Bare() implements VirtualComponent<Shabby> {
        public static final Bare INSTANCE = new Bare();
        @Override public Shabby parent() { return Shabby.INSTANCE; }
    }

    public record Doubled() implements VirtualComponent<Shabby>, Owns {
        public static final Doubled INSTANCE = new Doubled();
        @Override public Shabby parent() { return Shabby.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Shape.Border.INSTANCE, Target.Shape.Border.INSTANCE); }
    }

    public record Sounding() implements VirtualComponent<Shabby>, Owns {
        public static final Sounding INSTANCE = new Sounding();
        @Override public Shabby parent() { return Shabby.INSTANCE; }
        @Override public List<Target> owns() { return List.of(Target.Sound.Cue.INSTANCE); }
    }

    public record Scruffy() implements LocalStyle<BaseScheme, Shabby> {
        public static final Scruffy INSTANCE = new Scruffy();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public List<ConcreteComponent<?, Shabby, ?>> concretes() { return List.of(new ScruffyBare(), new ScruffyDoubled(), new ScruffySounding()); }
    }

    public record ScruffyBare() implements ConcreteComponent<Bare, Shabby, Scruffy>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(); }
    }

    public record ScruffyDoubled() implements ConcreteComponent<Doubled, Shabby, Scruffy>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(Target.Shape.Border.INSTANCE)); }
    }

    public record ScruffySounding() implements ConcreteComponent<Sounding, Shabby, Scruffy>, Drawn {
        @Override public List<Drawing<?>> drawings(DesignStyle<?> style) { return List.of(new Drawing.None<>(Target.Sound.Cue.INSTANCE)); }
    }

    public Taxonomy read() { return new ReadTaxonomy().read(List.of(Bare.INSTANCE, Doubled.INSTANCE, Sounding.INSTANCE)); }
}
