package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.design.Icon;
import hue.captains.singapura.js.homing.ui.menu.tree.ContextMenuKind;
import hue.captains.singapura.js.homing.ui.menu.tree.M1_Node;
import hue.captains.singapura.js.homing.ui.menu.tree.M2_Node;
import hue.captains.singapura.js.homing.ui.menu.tree.M3_Node;
import hue.captains.singapura.js.homing.ui.menu.tree.MenuTrees;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The registry over typed trees: a kind is a class, a row is a class, the
 * levels are typed and three deep at most; what the compiler cannot hold —
 * parent coherence, unique ids, labels, the last level empty — Java holds
 * at construction; and the data it stamps for the steward.
 */
class ContextMenuRegistryTest {

    // ── a kind, declared as the gallery would: a nest of records ──────────
    public record AnimalMenu() implements ContextMenuKind<AnimalMenu> {
        public static final AnimalMenu INSTANCE = new AnimalMenu();
        @Override public List<? extends M1_Node<AnimalMenu, ?>> children() { return List.of(Rotate.INSTANCE, Change.INSTANCE); }

        public record Rotate() implements M1_Node<AnimalMenu, Rotate> {
            public static final Rotate INSTANCE = new Rotate();
            @Override public AnimalMenu parent() { return AnimalMenu.INSTANCE; }
            @Override public String label() { return "Rotate"; }
            @Override public Class<? extends Icon> icon() { return Icon.Rotate.class; }
            @Override public String hint() { return "a quarter turn"; }
        }
        public record Change() implements M1_Node<AnimalMenu, Change> {
            public static final Change INSTANCE = new Change();
            @Override public AnimalMenu parent() { return AnimalMenu.INSTANCE; }
            @Override public String label() { return "Animal"; }
            @Override public int section() { return 1; }
            @Override public List<? extends M2_Node<Change, ?>> children() { return List.of(Cat.INSTANCE, Dog.INSTANCE); }

            public record Cat() implements M2_Node<Change, Cat> {
                public static final Cat INSTANCE = new Cat();
                @Override public Change parent() { return Change.INSTANCE; }
                @Override public String label() { return "Cat"; }
            }
            public record Dog() implements M2_Node<Change, Dog> {
                public static final Dog INSTANCE = new Dog();
                @Override public Change parent() { return Change.INSTANCE; }
                @Override public String label() { return "Dog"; }
                @Override public List<? extends M3_Node<Dog, ?>> children() { return List.of(Big.INSTANCE); }

                public record Big() implements M3_Node<Dog, Big> {
                    public static final Big INSTANCE = new Big();
                    @Override public Dog parent() { return Dog.INSTANCE; }
                    @Override public String label() { return "Big"; }
                }
            }
        }
    }

    public record CounterMenu() implements ContextMenuKind<CounterMenu> {
        public static final CounterMenu INSTANCE = new CounterMenu();
        @Override public List<? extends M1_Node<CounterMenu, ?>> children() { return List.of(Reset.INSTANCE); }
        public record Reset() implements M1_Node<CounterMenu, Reset> {
            public static final Reset INSTANCE = new Reset();
            @Override public CounterMenu parent() { return CounterMenu.INSTANCE; }
            @Override public String label() { return "Reset"; }
        }
    }

    private static final ContextMenuRegistry R = ContextMenuRegistry.of(AnimalMenu.INSTANCE, CounterMenu.INSTANCE);

    @Test
    void aNodeIsItsClass_theKindItsNameWithoutMenu_theRowItsNameInKebab() {
        assertEquals("animal", AnimalMenu.INSTANCE.kind());
        assertEquals("counter", CounterMenu.INSTANCE.kind());
        assertEquals("rotate", AnimalMenu.Rotate.INSTANCE.id());
        assertEquals("change-animal", MenuTrees.kebab("ChangeAnimal"));
        assertEquals("s10", MenuTrees.kebab("S10"));
        assertEquals("html-thing", MenuTrees.kebab("HTMLThing"));
        assertEquals(List.of("rotate", "change", "cat", "dog", "big"), MenuTrees.rows(AnimalMenu.INSTANCE).stream().map(r -> r.id()).toList(), "parents before children, in order");
        assertEquals("Big", MenuTrees.find(AnimalMenu.INSTANCE, "big").label());
        assertTrue(MenuTrees.find(AnimalMenu.INSTANCE, "owl") == null);
        assertEquals(List.of(), MenuTrees.validate(AnimalMenu.INSTANCE));
        assertEquals(AnimalMenu.INSTANCE, R.kind("animal"));
    }

    @Test
    void itStampsTheKindsAndTheirRowsAsData_iconsByToken_sectionsWhenSet() {
        assertEquals("{\"animal\":{\"kind\":\"animal\",\"nodes\":[{\"id\":\"rotate\",\"label\":\"Rotate\",\"icon\":\"rotate\",\"hint\":\"a quarter turn\"},"
                   + "{\"id\":\"change\",\"label\":\"Animal\",\"section\":1,\"nodes\":[{\"id\":\"cat\",\"label\":\"Cat\"},{\"id\":\"dog\",\"label\":\"Dog\",\"nodes\":[{\"id\":\"big\",\"label\":\"Big\"}]}]}]},"
                   + "\"counter\":{\"kind\":\"counter\",\"nodes\":[{\"id\":\"reset\",\"label\":\"Reset\"}]}}", R.json());
        assertTrue(R.js().get(2).startsWith("const MENUS = Object.freeze({"), R.js().toString());
    }

    // ── what Java refuses ─────────────────────────────────────────────────
    public record TwinMenu() implements ContextMenuKind<TwinMenu> {
        public static final TwinMenu INSTANCE = new TwinMenu();
        @Override public List<? extends M1_Node<TwinMenu, ?>> children() { return List.of(X.INSTANCE, S.INSTANCE); }
        public record X() implements M1_Node<TwinMenu, X> {
            public static final X INSTANCE = new X();
            @Override public TwinMenu parent() { return TwinMenu.INSTANCE; }
            @Override public String label() { return "X"; }
        }
        public record S() implements M1_Node<TwinMenu, S> {
            public static final S INSTANCE = new S();
            @Override public TwinMenu parent() { return TwinMenu.INSTANCE; }
            @Override public String label() { return "S"; }
            @Override public List<? extends M2_Node<S, ?>> children() { return List.of(Again.INSTANCE); }
            public record Again() implements M2_Node<S, Again> {
                public static final Again INSTANCE = new Again();
                @Override public S parent() { return S.INSTANCE; }
                @Override public String id() { return "x"; }            // the same id as the first-level X
                @Override public String label() { return ""; }          // and no label
            }
        }
    }
    public record StrayMenu() implements ContextMenuKind<StrayMenu> {
        public static final StrayMenu INSTANCE = new StrayMenu();
        @Override public List<? extends M1_Node<StrayMenu, ?>> children() { return List.of(Borrowed.INSTANCE); }
        public record Borrowed() implements M1_Node<StrayMenu, Borrowed> {
            public static final Borrowed INSTANCE = new Borrowed();
            @Override public StrayMenu parent() { return null; }
            @Override public String label() { return "Borrowed"; }
        }
    }
    public record EmptyMenu() implements ContextMenuKind<EmptyMenu> {
        public static final EmptyMenu INSTANCE = new EmptyMenu();
        @Override public List<? extends M1_Node<EmptyMenu, ?>> children() { return List.of(); }
    }

    @Test
    void whatTheCompilerCannotHold_javaRefuses() {
        var twin = MenuTrees.validate(TwinMenu.INSTANCE);
        assertEquals(List.of("twin: row id repeated: x", "twin: x has no label"), twin);
        var stray = MenuTrees.validate(StrayMenu.INSTANCE);
        assertEquals(List.of("stray: borrowed is listed under stray but names no parent"), stray);
        assertEquals(List.of("empty: a kind lists no rows"), MenuTrees.validate(EmptyMenu.INSTANCE));
        var ex = assertThrows(IllegalArgumentException.class, () -> ContextMenuRegistry.of(TwinMenu.INSTANCE));
        assertTrue(ex.getMessage().contains("row id repeated: x"), ex.getMessage());
        var twice = assertThrows(IllegalArgumentException.class, () -> ContextMenuRegistry.of(AnimalMenu.INSTANCE, AnimalMenu.INSTANCE));
        assertTrue(twice.getMessage().contains("kind declared twice: animal"), twice.getMessage());
    }
}
