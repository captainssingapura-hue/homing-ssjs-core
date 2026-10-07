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

    /** Two kinds, each the other's parent. */
    record Chick() implements Kind<Egg> {
        static final Chick INSTANCE = new Chick();
        @Override public Egg parent() { return Egg.INSTANCE; }
    }

    record Egg() implements Kind<Chick> {
        static final Egg INSTANCE = new Egg();
        @Override public Chick parent() { return Chick.INSTANCE; }
    }

    record Hatchling() implements Component<Chick> {
        static final Hatchling INSTANCE = new Hatchling();
        @Override public Chick parent() { return Chick.INSTANCE; }
    }

    /** A role that names no family. */
    record Stray() implements Role<Naming> {
        static final Stray INSTANCE = new Stray();
        @Override public Naming family() { return null; }
    }

    record Strays() implements Component<Container> {
        static final Strays INSTANCE = new Strays();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.part(Caption.INSTANCE).as(Stray.INSTANCE).one()); }
    }

    /** Two families, each the other's parent. */
    record Ouroboros() implements RoleFamily<Tail> {
        static final Ouroboros INSTANCE = new Ouroboros();
        @Override public Tail parent() { return Tail.INSTANCE; }
    }

    record Tail() implements RoleFamily<Ouroboros> {
        static final Tail INSTANCE = new Tail();
        @Override public Ouroboros parent() { return Ouroboros.INSTANCE; }
    }

    record Looped() implements Role<Ouroboros> {
        static final Looped INSTANCE = new Looped();
        @Override public Ouroboros family() { return Ouroboros.INSTANCE; }
    }

    // ── slots ──────────────────────────────────────────────────────────────

    /** A title played by nothing. */
    record Vacant() implements Component<Container> {
        static final Vacant INSTANCE = new Vacant();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.<Caption>part(null).as(Title.INSTANCE).one()); }
    }

    /** A part played by a caption, in no role. */
    record Nameless() implements Component<Container> {
        static final Nameless INSTANCE = new Nameless();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.part(Caption.INSTANCE).as(null).one()); }
    }

    /** One role, named twice. */
    record Stutter() implements Component<Container> {
        static final Stutter INSTANCE = new Stutter();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE;
            return List.of(dsl.part(Caption.INSTANCE).as(Title.INSTANCE).one(), dsl.part(Heading.INSTANCE).as(Title.INSTANCE).optional());
        }
    }

    // ── composition ────────────────────────────────────────────────────────

    /** Lists itself - optionally, and still a part of itself: a cycle is refused on the types. */
    record Mirror() implements Component<Container> {
        static final Mirror INSTANCE = new Mirror();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.part(Mirror.INSTANCE).as(Entry.INSTANCE).optional()); }
    }

    /** Each lists the other, any number of times, none included. */
    record Ping() implements Component<Container> {
        static final Ping INSTANCE = new Ping();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.part(Pong.INSTANCE).as(Entry.INSTANCE).any()); }
    }

    record Pong() implements Component<Container> {
        static final Pong INSTANCE = new Pong();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.part(Ping.INSTANCE).as(Entry.INSTANCE).any()); }
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
            @Override public Naming family() { return Naming.INSTANCE; }
        }
    }

    /** Names the second title; read beside anything that names the house's, two roles answer to one name. */
    record Doubled() implements Component<Container> {
        static final Doubled INSTANCE = new Doubled();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.part(Caption.INSTANCE).as(Elsewhere.Title.INSTANCE).one()); }
    }

    /** A role named for what plays it: "caption", like the component. */
    static final class Objective {
        private Objective() {}
        record Caption() implements Role<Telling> {
            static final Caption INSTANCE = new Caption();
            @Override public Telling family() { return Telling.INSTANCE; }
        }
    }

    record Labelled() implements Component<Container> {
        static final Labelled INSTANCE = new Labelled();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.part(Sketch.Caption.INSTANCE).as(Objective.Caption.INSTANCE).one()); }
    }

    // ── counts ─────────────────────────────────────────────────────────────

    /** Exactly none. */
    record Nothing() implements Component<Container> {
        static final Nothing INSTANCE = new Nothing();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.part(Badge.INSTANCE).as(Tag.INSTANCE).exactly(0)); }
    }

    /** Three up to one. */
    record Lopsided() implements Component<Container> {
        static final Lopsided INSTANCE = new Lopsided();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.part(Tick.INSTANCE).as(Notch.INSTANCE).between(3, 1)); }
    }

    /** Two up to two: that is exactly two. */
    record Flat() implements Component<Container> {
        static final Flat INSTANCE = new Flat();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE; return List.of(dsl.part(Tick.INSTANCE).as(Notch.INSTANCE).between(2, 2)); }
    }
}
