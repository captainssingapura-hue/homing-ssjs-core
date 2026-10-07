package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Badge;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Caption;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Container;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Entry;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Heading;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Naming;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Notch;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Tag;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Telling;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Text;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Tick;
import hue.captains.singapura.js.homing.component.taxonomy.Sketch.Title;

import java.util.List;

/**
 * Hypothetical declarations that compile and that reading refuses - each breaks one rule, on the
 * {@link Sketch} house.
 */
final class Broken {

    private Broken() {}

    // ── parents ────────────────────────────────────────────────────────────

    /** Names no parent. */
    record Orphan() implements Component<Text> {
        static final Orphan INSTANCE = new Orphan();
        @Override public Text parent() { return null; }
    }

    /** A role that names no parent. */
    record Stray() implements Role<Naming> {
        static final Stray INSTANCE = new Stray();
        @Override public Naming parent() { return null; }
    }

    record Strays() implements Component<Container> {
        static final Strays INSTANCE = new Strays();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Caption.INSTANCE).as(Stray.INSTANCE).one()); }
    }

    // ── state ──────────────────────────────────────────────────────────────

    /** A component that holds state: a label of its own. */
    record Stateful(String label) implements Component<Container> {
        static final Stateful INSTANCE = new Stateful("held");
        @Override public Container parent() { return Container.INSTANCE; }
    }

    /** A role with a static helper - which jOntology refuses a stateless functional object. */
    record Helped() implements Role<Naming> {
        static final Helped INSTANCE = new Helped();
        @Override public Naming parent() { return Naming.INSTANCE; }
        static String help() { return "help"; }
    }

    // ── slots ──────────────────────────────────────────────────────────────

    /** A title played by nothing. */
    record Vacant() implements Component<Container> {
        static final Vacant INSTANCE = new Vacant();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.<Caption>part(null).as(Title.INSTANCE).one()); }
    }

    /** A part played by a caption, in no role. */
    record Nameless() implements Component<Container> {
        static final Nameless INSTANCE = new Nameless();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Caption.INSTANCE).as(null).one()); }
    }

    /** One role, named twice. */
    record Stutter() implements Component<Container> {
        static final Stutter INSTANCE = new Stutter();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(Caption.INSTANCE).as(Title.INSTANCE).one(), DSL.part(Heading.INSTANCE).as(Title.INSTANCE).optional());
        }
    }

    // ── composition ────────────────────────────────────────────────────────

    /** Lists itself - optionally, and still a part of itself: a cycle is refused on the types. */
    record Mirror() implements Component<Container> {
        static final Mirror INSTANCE = new Mirror();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Mirror.INSTANCE).as(Entry.INSTANCE).optional()); }
    }

    /** Each lists the other, any number of times, none included. */
    record Ping() implements Component<Container> {
        static final Ping INSTANCE = new Ping();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Pong.INSTANCE).as(Entry.INSTANCE).any()); }
    }

    record Pong() implements Component<Container> {
        static final Pong INSTANCE = new Pong();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Ping.INSTANCE).as(Entry.INSTANCE).any()); }
    }

    // ── names ──────────────────────────────────────────────────────────────

    /** Two components that print one token. */
    static final class Here {
        private Here() {}
        record Badge() implements Component<Text> {
            static final Badge INSTANCE = new Badge();
            @Override public Text parent() { return Text.INSTANCE; }
        }
    }

    static final class There {
        private There() {}
        record Badge() implements Component<Text> {
            static final Badge INSTANCE = new Badge();
            @Override public Text parent() { return Text.INSTANCE; }
        }
    }

    /** A second role that answers to "title". */
    static final class Elsewhere {
        private Elsewhere() {}
        record Title() implements Role<Naming> {
            static final Title INSTANCE = new Title();
            @Override public Naming parent() { return Naming.INSTANCE; }
        }
    }

    /** Names the second title; read beside anything that names the house's, two roles answer to one name. */
    record Doubled() implements Component<Container> {
        static final Doubled INSTANCE = new Doubled();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Caption.INSTANCE).as(Elsewhere.Title.INSTANCE).one()); }
    }

    /** A second "naming", a branch under Doing - beside the house's under Saying: a branch clash, under different parents. */
    static final class Another {
        private Another() {}
        record Naming() implements L2_RoleBranch<Sketch.Doing> {
            static final Naming INSTANCE = new Naming();
            @Override public Sketch.Doing parent() { return Sketch.Doing.INSTANCE; }
        }
        record Moniker() implements Role<Naming> {
            static final Moniker INSTANCE = new Moniker();
            @Override public Naming parent() { return Naming.INSTANCE; }
        }
    }

    /** A branch called "title", like the house's role: a branch and a role, one name. */
    static final class Misnamed {
        private Misnamed() {}
        record Title() implements L2_RoleBranch<Sketch.Saying> {
            static final Title INSTANCE = new Title();
            @Override public Sketch.Saying parent() { return Sketch.Saying.INSTANCE; }
        }
        record Subhead() implements Role<Title> {
            static final Subhead INSTANCE = new Subhead();
            @Override public Title parent() { return Title.INSTANCE; }
        }
    }

    /** A role named for what plays it: "caption", like the component. */
    static final class Objective {
        private Objective() {}
        record Caption() implements Role<Telling> {
            static final Caption INSTANCE = new Caption();
            @Override public Telling parent() { return Telling.INSTANCE; }
        }
    }

    record Labelled() implements Component<Container> {
        static final Labelled INSTANCE = new Labelled();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Sketch.Caption.INSTANCE).as(Objective.Caption.INSTANCE).one()); }
    }

    // ── counts ─────────────────────────────────────────────────────────────

    /** Exactly none. */
    record Nothing() implements Component<Container> {
        static final Nothing INSTANCE = new Nothing();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Badge.INSTANCE).as(Tag.INSTANCE).exactly(0)); }
    }

    /** Three up to one. */
    record Lopsided() implements Component<Container> {
        static final Lopsided INSTANCE = new Lopsided();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Tick.INSTANCE).as(Notch.INSTANCE).between(3, 1)); }
    }

    /** Two up to two: that is exactly two. */
    record Flat() implements Component<Container> {
        static final Flat INSTANCE = new Flat();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Tick.INSTANCE).as(Notch.INSTANCE).between(2, 2)); }
    }

    // ── extents ────────────────────────────────────────────────────────────

    /** A size declared again under the button branch, which declares it already. */
    record Redeclared() implements Component<Sketch.Button> {
        static final Redeclared INSTANCE = new Redeclared();
        @Override public Sketch.Button parent() { return Sketch.Button.INSTANCE; }
        @Override public List<ExtentAxis> extents() { return List.of(ExtentAxis.SIZE); }
    }

    /** One axis, declared twice. */
    record Doubly() implements Component<Sketch.Button> {
        static final Doubly INSTANCE = new Doubly();
        @Override public Sketch.Button parent() { return Sketch.Button.INSTANCE; }
        @Override public List<ExtentAxis> extents() { return List.of(ExtentAxis.COLOUR, ExtentAxis.COLOUR); }
    }
}
