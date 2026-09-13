package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0044 — a crate entry's declared type is a <em>domain</em> role. The
 * mechanical types are the classifier's to infer from the module's form; an
 * author declaring one would be a first-party module handing itself the bundled
 * external's empty rule set, so the entry refuses it at construction.
 */
class CrateEntryTest {

    record Plain() implements DomModule<Plain>, SelfContent {
        static final Plain INSTANCE = new Plain();
        @Override public ImportsFor<Plain> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<Plain> exports() { return new ExportsOf<>(INSTANCE, List.of()); }
        @Override public List<String> selfContent(ModuleNameResolver r) { return List.of("export const x = 1;"); }
    }

    /** A downstream's own type — outside the sealed enum, never mechanical. */
    enum LibType implements JsModuleType {
        GAME_LOOP;
        @Override public String label() { return "Game loop"; }
        @Override public String slug()  { return "game-loop"; }
    }

    @Test
    void undeclaredAndDomainRolesPack() {
        assertNull(CrateEntry.of(Plain.INSTANCE).declaredType());
        for (var role : List.of(StandardJsModuleType.PRIMITIVE, StandardJsModuleType.SECRETARY,
                StandardJsModuleType.PURE_LOGIC, StandardJsModuleType.CONSUMER)) {
            assertEquals(role, CrateEntry.of(Plain.INSTANCE, role).declaredType());
        }
        assertEquals(LibType.GAME_LOOP, CrateEntry.of(Plain.INSTANCE, LibType.GAME_LOOP).declaredType());
    }

    @Test
    void aMechanicalTypeCannotBeDeclared() {
        for (var mechanical : List.of(StandardJsModuleType.BUNDLED_EXTERNAL,
                StandardJsModuleType.MANAGER_INJECTOR, StandardJsModuleType.GENERATED_CSS)) {
            var ex = assertThrows(IllegalArgumentException.class,
                    () -> CrateEntry.of(Plain.INSTANCE, mechanical), mechanical.name());
            assertTrue(ex.getMessage().contains("mechanical type " + mechanical), ex.getMessage());
        }
    }
}
