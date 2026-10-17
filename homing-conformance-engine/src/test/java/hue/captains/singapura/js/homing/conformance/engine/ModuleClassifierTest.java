package hue.captains.singapura.js.homing.conformance.engine;

import hue.captains.singapura.js.homing.component.BranchComponent;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A widget is told by its export: a module that exports a component whose shape is a widget's is a
 * {@code WIDGET} when its entry declares nothing. A declared type is honoured - a downstream's own
 * too - but no widget is declared out of its discipline: the engine holds it to the widget rule
 * whatever its type. A module that exports a composed component is not one.
 */
class ModuleClassifierTest {

    /** A widget's module. */
    record BoardModule() implements DomModule<BoardModule>, SelfContent {
        static final BoardModule INSTANCE = new BoardModule();
        record Board() implements BranchComponent<BoardModule> {
            @Override public Shape shape() { return Shape.WIDGET; }
        }
        @Override public ImportsFor<BoardModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<BoardModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Board())); }
        @Override public List<String> selfContent(ModuleNameResolver resolver) {
            return List.of("class Board { constructor(branch) { this.root = branch.createElement('root', 'div'); } }");
        }
    }

    /** A composed component's module. */
    record TrayModule() implements DomModule<TrayModule>, SelfContent {
        static final TrayModule INSTANCE = new TrayModule();
        record Tray() implements BranchComponent<TrayModule> {}
        @Override public ImportsFor<TrayModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<TrayModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Tray())); }
        @Override public List<String> selfContent(ModuleNameResolver resolver) { return List.of("class Tray {}"); }
    }

    /** A crate of one entry. */
    record OneEntry(CrateEntry entry) implements Crate {
        @Override public String name() { return "one"; }
        @Override public List<CrateEntry> entries() { return List.of(entry); }
        @Override public List<Crate> requires() { return List.of(); }
    }

    @Test
    void aWidget_undeclared_isAWidget() {
        assertEquals(StandardJsModuleType.WIDGET, ModuleClassifier.classify(CrateEntry.of(BoardModule.INSTANCE)));
    }

    @Test
    void aWidgetDeclaredOtherwise_keepsItsType_butIsHeldToTheWidgetRule() {
        var declared = CrateEntry.of(BoardModule.INSTANCE, StandardJsModuleType.PRIMITIVE);
        assertEquals(StandardJsModuleType.PRIMITIVE, ModuleClassifier.classify(declared), "the declaration is honoured");
        var rules = new ConformanceEngine().checkCrates(List.of(new OneEntry(declared))).stream().map(f -> f.rule().value()).toList();
        assertTrue(rules.contains("widget-draws-only-components"), "and still the widget rule: " + rules);
        var tray = new ConformanceEngine().checkCrates(List.of(new OneEntry(CrateEntry.of(TrayModule.INSTANCE)))).stream().map(f -> f.rule().value()).toList();
        assertFalse(tray.contains("widget-draws-only-components"), "a composed component is not held to it");
    }

    @Test
    void aComposedComponent_isNoWidget() {
        assertEquals(StandardJsModuleType.CONSUMER, ModuleClassifier.classify(CrateEntry.of(TrayModule.INSTANCE)));
        assertEquals(StandardJsModuleType.PRIMITIVE, ModuleClassifier.classify(CrateEntry.of(TrayModule.INSTANCE, StandardJsModuleType.PRIMITIVE)));
    }

    @Test
    void theWidgetTypeCannotBeDeclared() {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> CrateEntry.of(TrayModule.INSTANCE, StandardJsModuleType.WIDGET), "inferred from the export, never declared");
    }
}
