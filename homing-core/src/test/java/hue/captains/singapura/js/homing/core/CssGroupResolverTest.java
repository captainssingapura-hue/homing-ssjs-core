package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CssGroupResolverTest {

    // --- Test CssGroups with a diamond dependency graph, declared the only
    // way there is: per class, on the class that leans on another ---
    // Base (no deps) <- Left <- Root
    //                <- Right <-/

    record Base() implements CssGroup<Base> {
        static final Base INSTANCE = new Base();
        record base_thing() implements CssClass<Base> {}
        @Override public List<CssClass<Base>> cssClasses() { return List.of(new base_thing()); }
    }

    record Left() implements CssGroup<Left> {
        static final Left INSTANCE = new Left();
        record left_thing() implements CssClass<Left> {
            @Override public List<CssClass<?>> dependsOn() { return List.of(new Base.base_thing()); }
        }
        @Override public List<CssClass<Left>> cssClasses() { return List.of(new left_thing()); }
    }

    record Right() implements CssGroup<Right> {
        static final Right INSTANCE = new Right();
        record right_thing() implements CssClass<Right> {
            @Override public List<CssClass<?>> dependsOn() { return List.of(new Base.base_thing()); }
        }
        @Override public List<CssClass<Right>> cssClasses() { return List.of(new right_thing()); }
    }

    record Root() implements CssGroup<Root> {
        static final Root INSTANCE = new Root();
        record root_thing() implements CssClass<Root> {
            @Override public List<CssClass<?>> dependsOn() {
                return List.of(new Left.left_thing(), new Right.right_thing(), new Base.base_thing());
            }
        }
        @Override public List<CssClass<Root>> cssClasses() { return List.of(new root_thing()); }
    }

    // A cycle: CycA <- CycB <- CycA.
    record CycA() implements CssGroup<CycA> {
        static final CycA INSTANCE = new CycA();
        record a() implements CssClass<CycA> {
            @Override public List<CssClass<?>> dependsOn() { return List.of(new CycB.b()); }
        }
        @Override public List<CssClass<CycA>> cssClasses() { return List.of(new a()); }
    }
    record CycB() implements CssGroup<CycB> {
        static final CycB INSTANCE = new CycB();
        record b() implements CssClass<CycB> {
            @Override public List<CssClass<?>> dependsOn() { return List.of(new CycA.a()); }
        }
        @Override public List<CssClass<CycB>> cssClasses() { return List.of(new b()); }
    }

    // A prior that leans on something — refused.
    record BadPrior() implements CssGroup<BadPrior> {
        static final BadPrior INSTANCE = new BadPrior();
        @Override public boolean prior() { return true; }
        record p() implements CssClass<BadPrior> {
            @Override public List<CssClass<?>> dependsOn() { return List.of(new Base.base_thing()); }
        }
        @Override public List<CssClass<BadPrior>> cssClasses() { return List.of(new p()); }
    }

    @Test
    void groupDependencies_areDerivedFromClasses_selfExcluded_firstMentionOrder() {
        assertEquals(List.of(), CssImportsFor.of(Base.INSTANCE).imports());
        assertEquals(List.of(Base.INSTANCE), CssImportsFor.of(Left.INSTANCE).imports());
        assertEquals(List.of(Left.INSTANCE, Right.INSTANCE, Base.INSTANCE), CssImportsFor.of(Root.INSTANCE).imports());
    }

    @Test
    void aCycle_isRefusedWithItsNames() {
        var e = assertThrows(IllegalStateException.class, () -> CssGroupResolver.resolve(List.of(CycA.INSTANCE)));
        assertTrue(e.getMessage().contains("CycA -> CycB -> CycA"), e.getMessage());
    }

    @Test
    void aPriorWithDependencies_isRefused() {
        var e = assertThrows(IllegalStateException.class, () -> CssGroupResolver.resolve(List.of(BadPrior.INSTANCE)));
        assertTrue(e.getMessage().contains("prior"), e.getMessage());
    }
    @Test
    void resolve_emptyList() {
        var result = CssGroupResolver.resolve(List.of());
        assertTrue(result.isEmpty());
    }

    @Test
    void resolve_singleNoDeps() {
        var result = CssGroupResolver.resolve(List.of(Base.INSTANCE));
        assertEquals(List.of(Base.INSTANCE), result);
    }

    @Test
    void resolve_transitiveDeps_dependenciesFirst() {
        var result = CssGroupResolver.resolve(List.of(Root.INSTANCE));

        assertEquals(4, result.size());
        // Base must come before Left and Right; Root must be last
        assertTrue(result.indexOf(Base.INSTANCE) < result.indexOf(Left.INSTANCE));
        assertTrue(result.indexOf(Base.INSTANCE) < result.indexOf(Right.INSTANCE));
        assertTrue(result.indexOf(Left.INSTANCE) < result.indexOf(Root.INSTANCE));
        assertTrue(result.indexOf(Right.INSTANCE) < result.indexOf(Root.INSTANCE));
        assertEquals(Root.INSTANCE, result.getLast());
    }

    @Test
    void resolve_diamondDedup() {
        // Base appears in both Left and Right's imports — should only appear once
        var result = CssGroupResolver.resolve(List.of(Root.INSTANCE));
        long baseCount = result.stream().filter(c -> c instanceof Base).count();
        assertEquals(1, baseCount);
    }

    @Test
    void resolve_multipleRoots_dedup() {
        var result = CssGroupResolver.resolve(List.of(Left.INSTANCE, Right.INSTANCE));

        // Base, Left, Right — Base shared but only appears once
        assertEquals(3, result.size());
        assertTrue(result.indexOf(Base.INSTANCE) < result.indexOf(Left.INSTANCE));
        assertTrue(result.indexOf(Base.INSTANCE) < result.indexOf(Right.INSTANCE));
    }
}
