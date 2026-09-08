package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.WidgetEntry;
import hue.captains.singapura.js.homing.workspace.WidgetLabel;
import hue.captains.singapura.js.homing.workspace.WorkspaceWidget;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0060 — the guardrails on a spec's own declarations.
 *
 * <p>Each of these describes a failure that is <b>invisible at runtime</b>: the
 * seed is applied once, in a browser, to a workspace with no saved state. The
 * assertions are therefore as much about the <i>message</i> as the throw — a
 * check that fires without naming the spec and the widget has not helped
 * anyone.</p>
 */
class WorkspaceSpecGuardTest {

    private static final PaneArrangement TWO =
            PaneArrangement.named("two")
                    .root("left")
                    .splitEvenly("left", PaneDirection.RIGHT, "right")
                    .build();

    private static final ShapePane LEFT  = TWO.pane("left");
    private static final ShapePane RIGHT = TWO.pane("right");

    // ---------------------------------------------------------------- happy

    @Test
    void a_spec_whose_arrangement_matches_its_declarations_registers() {
        assertDoesNotThrow(() -> WorkspaceSpecGuard.check(
                spec().entries(TreeWidget.class, DocWidget.class)
                      .arrangement(TWO.allocate()
                              .place(LEFT,  TreeWidget.class)
                              .place(RIGHT, DocWidget.class)
                              .build())));
    }

    @Test
    void the_default_single_pane_arrangement_needs_no_declarations() {
        assertDoesNotThrow(() -> WorkspaceSpecGuard.check(
                spec().entries(TreeWidget.class)));
    }


    // ----------------------------------------------------------- the four

    @Test
    void two_widgets_sharing_a_simpleName_are_rejected_naming_both_packages() {
        var e = assertThrows(IllegalStateException.class, () -> WorkspaceSpecGuard.check(
                spec().entries(Alpha.DupWidget.class, Beta.DupWidget.class)));
        assertTrue(e.getMessage().contains("DupWidget"), e.getMessage());
        assertTrue(e.getMessage().contains(Alpha.DupWidget.class.getName()), e.getMessage());
        assertTrue(e.getMessage().contains(Beta.DupWidget.class.getName()), e.getMessage());
    }

    @Test
    void placing_an_undeclared_widget_is_rejected_naming_the_pane() {
        var e = assertThrows(IllegalStateException.class, () -> WorkspaceSpecGuard.check(
                spec().entries(TreeWidget.class)
                      .arrangement(TWO.allocate()
                              .place(LEFT,  TreeWidget.class)
                              .place(RIGHT, LogWidget.class)   // never declared
                              .build())));
        assertTrue(e.getMessage().contains("LogWidget"), e.getMessage());
        assertTrue(e.getMessage().contains("two.right"), e.getMessage());
        assertTrue(e.getMessage().contains("declared widgets are"), e.getMessage());
    }

    /**
     * The check compares CLASSES, not names — a same-named widget from another
     * package is the case a string comparison would wave through while the shell
     * mounted the other one.
     */
    @Test
    void placing_a_same_named_widget_from_another_package_is_still_rejected() {
        var e = assertThrows(IllegalStateException.class, () -> WorkspaceSpecGuard.check(
                spec().entries(Alpha.DupWidget.class)
                      .arrangement(TWO.allocate()
                              .place(LEFT, Beta.DupWidget.class)
                              .build())));
        assertTrue(e.getMessage().contains(Beta.DupWidget.class.getName()), e.getMessage());
    }

    /**
     * The one with no runtime diagnostic whatsoever: the model's spawn is
     * idempotent on the instance id, and the seeded id is the widget kind.
     */
    @Test
    void seeding_one_widget_into_two_panes_is_rejected_naming_both() {
        var e = assertThrows(IllegalStateException.class, () -> WorkspaceSpecGuard.check(
                spec().entries(TreeWidget.class)
                      .arrangement(TWO.allocate()
                              .place(LEFT,  TreeWidget.class)
                              .place(RIGHT, TreeWidget.class)
                              .build())));
        assertTrue(e.getMessage().contains("two.left"),  e.getMessage());
        assertTrue(e.getMessage().contains("two.right"), e.getMessage());
    }

    @Test
    void a_seed_larger_than_maxTabs_is_rejected_with_both_counts() {
        var e = assertThrows(IllegalStateException.class, () -> WorkspaceSpecGuard.check(
                spec().entries(TreeWidget.class, DocWidget.class)
                      .arrangement(TWO.allocate()
                              .place(LEFT,  TreeWidget.class)
                              .place(RIGHT, DocWidget.class)
                              .build())
                      .maxTabs(1)));
        assertTrue(e.getMessage().contains("seeds 2"),   e.getMessage());
        assertTrue(e.getMessage().contains("is 1"),      e.getMessage());
        assertTrue(e.getMessage().contains("two"),       e.getMessage());
    }

    /** Exactly at the budget is legal — the workspace opens full, not over. */
    @Test
    void a_seed_equal_to_maxTabs_is_allowed() {
        assertDoesNotThrow(() -> WorkspaceSpecGuard.check(
                spec().entries(TreeWidget.class, DocWidget.class)
                      .arrangement(TWO.allocate()
                              .place(LEFT,  TreeWidget.class)
                              .place(RIGHT, DocWidget.class)
                              .build())
                      .maxTabs(2)));
    }

    @Test
    void a_budget_of_zero_is_rejected() {
        var e = assertThrows(IllegalStateException.class, () -> WorkspaceSpecGuard.check(
                spec().entries(TreeWidget.class).maxTabs(0)));
        assertTrue(e.getMessage().contains("maxTabs()"), e.getMessage());
    }

    // ------------------------------------------------------- through the registry

    @Test
    void the_registry_applies_the_guard() {
        var bad = spec().entries(TreeWidget.class)
                         .arrangement(TWO.allocate().place(LEFT, LogWidget.class).build());
        var e = assertThrows(IllegalStateException.class,
                             () -> WorkspaceSpecRegistry.INSTANCE.register(bad));
        assertTrue(e.getMessage().contains("LogWidget"), e.getMessage());
        assertEquals(java.util.Optional.empty(), WorkspaceSpecRegistry.INSTANCE.get(bad.kind()),
                     "a spec that failed the guard must not be left half-registered");
    }

    // ------------------------------------------------------------- fixtures

    private static Fixture spec() { return new Fixture(); }

    /** A spec whose declarations are whatever a test says they are. */
    private static final class Fixture implements WorkspaceSpec {
        private static int seq = 0;
        private final String kind = "guard-fixture-" + (++seq);
        private List<WidgetEntry> entries = List.of();
        private Arrangement arrangement = PaneArrangements.SINGLE.empty();
        private int maxTabs = 16;

        @SafeVarargs
        final Fixture entries(Class<? extends WorkspaceWidget<?, ?>>... classes) {
            this.entries = java.util.Arrays.stream(classes)
                    .map(c -> WidgetEntry.of(c, WidgetLabel.of(c.getSimpleName())))
                    .toList();
            return this;
        }

        Fixture arrangement(Arrangement a) { this.arrangement = a;            return this; }
        Fixture maxTabs(int n)             { this.maxTabs = n;                return this; }

        @Override public String kind()                 { return kind; }
        @Override public String title()                { return "Guard Fixture"; }
        @Override public List<WidgetEntry> widgetEntries() { return entries; }
        @Override public Arrangement arrangement()     { return arrangement; }
        @Override public int maxTabs()                 { return maxTabs; }
    }

    abstract static class TreeWidget extends WorkspaceWidget<WorkspaceWidget._None, TreeWidget> {}
    abstract static class DocWidget  extends WorkspaceWidget<WorkspaceWidget._None, DocWidget> {}
    abstract static class LogWidget  extends WorkspaceWidget<WorkspaceWidget._None, LogWidget> {}

    /** Two widgets with one simpleName, which is what the wire key cannot tell apart. */
    static final class Alpha {
        abstract static class DupWidget
                extends WorkspaceWidget<WorkspaceWidget._None, Alpha.DupWidget> {}
    }

    static final class Beta {
        abstract static class DupWidget
                extends WorkspaceWidget<WorkspaceWidget._None, Beta.DupWidget> {}
    }
}
