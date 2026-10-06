package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Caption;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Container;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Dialog;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Text;

import java.util.List;

/** Taxonomies the reader must refuse, one problem each. */
final class Broken {

    private Broken() {}

    /** Lists a role declared by another component. */
    record Thief() implements Component<Container> {
        static final Thief INSTANCE = new Thief();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Role<?>> roles() { return List.of(Dialog.Ok.INSTANCE); }
    }

    /** Lists its one role twice. */
    record Stutter() implements Component<Container> {
        static final Stutter INSTANCE = new Stutter();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Role<?>> roles() { return List.of(Again.INSTANCE, Again.INSTANCE); }

        record Again() implements Role<Caption> {
            static final Again INSTANCE = new Again();
            @Override public Caption base() { return Caption.INSTANCE; }
        }
    }

    /** A role nobody plays. */
    record Vacant() implements Component<Container> {
        static final Vacant INSTANCE = new Vacant();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Role<?>> roles() { return List.of(Nobody.INSTANCE); }

        record Nobody() implements Role<Caption> {
            static final Nobody INSTANCE = new Nobody();
            @Override public Caption base() { return null; }
        }
    }

    /** A part of itself. */
    record Mirror() implements Component<Container> {
        static final Mirror INSTANCE = new Mirror();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Role<?>> roles() { return List.of(Self.INSTANCE); }

        record Self() implements Role<Mirror> {
            static final Self INSTANCE = new Self();
            @Override public Mirror base() { return Mirror.INSTANCE; }
        }
    }

    /** Ping holds a Pong, which holds a Ping. */
    record Ping() implements Component<Container> {
        static final Ping INSTANCE = new Ping();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Role<?>> roles() { return List.of(Other.INSTANCE); }

        record Other() implements Role<Pong> {
            static final Other INSTANCE = new Other();
            @Override public Pong base() { return Pong.INSTANCE; }
        }
    }

    record Pong() implements Component<Container> {
        static final Pong INSTANCE = new Pong();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Role<?>> roles() { return List.of(Back.INSTANCE); }

        record Back() implements Role<Ping> {
            static final Back INSTANCE = new Back();
            @Override public Ping base() { return Ping.INSTANCE; }
        }
    }

    /** Two kinds, each the other's parent. */
    record Hen() implements Kind<Egg> {
        static final Hen INSTANCE = new Hen();
        @Override public Egg parent() { return Egg.INSTANCE; }
    }

    record Egg() implements Kind<Hen> {
        static final Egg INSTANCE = new Egg();
        @Override public Hen parent() { return Hen.INSTANCE; }
    }

    record Chick() implements Component<Hen> {
        static final Chick INSTANCE = new Chick();
        @Override public Hen parent() { return Hen.INSTANCE; }
    }

    /** No parent at all. */
    record Orphan() implements Component<Container> {
        static final Orphan INSTANCE = new Orphan();
        @Override public Container parent() { return null; }
    }

    /** Two components named alike, under different kinds. */
    static final class Here {
        private Here() {}
        record Badge() implements Component<Container> {
            static final Badge INSTANCE = new Badge();
            @Override public Container parent() { return Container.INSTANCE; }
        }
    }

    static final class There {
        private There() {}
        record Badge() implements Component<Text> {
            static final Badge INSTANCE = new Badge();
            @Override public Text parent() { return Text.INSTANCE; }
        }
    }
}
