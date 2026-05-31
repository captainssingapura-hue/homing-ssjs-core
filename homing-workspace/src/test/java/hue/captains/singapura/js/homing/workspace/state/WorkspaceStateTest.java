package hue.captains.singapura.js.homing.workspace.state;

import hue.captains.singapura.js.homing.core.Widget;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0029 cycle 1 — contract tests for the workspace state records.
 *
 * <p>Exercises the typed identifiers (Names Are Types doctrine), the
 * {@link WidgetLocation} sealed ADT, the {@link LayoutNode} sealed
 * hierarchy, and the cross-widget invariants in the
 * {@link WorkspaceState} envelope's compact constructor.</p>
 */
class WorkspaceStateTest {

    // Test-only Params record — every widget kind has one of these.
    private record TestParams(String animal) implements Widget._Param {}

    // ── helpers ─────────────────────────────────────────────────────────────

    private static WidgetInstance widget(WidgetInstanceId id, WidgetLocation loc) {
        return new WidgetInstance(id, WidgetKind.of("test-widget"),
                new TestParams("cat"), WidgetTitle.of("Test"), loc);
    }

    private static WorkspaceState minimalEmptyState() {
        return new WorkspaceState(
                WorkspaceState.CURRENT_SCHEMA_VERSION,
                WorkspaceKind.of("AnimalsPlayground"),
                Instant.parse("2026-05-26T10:00:00Z"),
                new LayoutNode.Leaf(PaneId.of("p1")),
                Map.of(),
                ChromeState.defaults());
    }

    // ── Typed identifiers (Names Are Types) ─────────────────────────────────

    @Test
    void paneIdValidatesGrammar() {
        assertThrows(NullPointerException.class, () -> new PaneId(null));
        assertThrows(IllegalArgumentException.class, () -> new PaneId(""));
        assertThrows(IllegalArgumentException.class, () -> new PaneId("p with spaces"));
        assertThrows(IllegalArgumentException.class, () -> new PaneId("p/slash"));
        assertDoesNotThrow(() -> new PaneId("p1"));
        assertDoesNotThrow(() -> new PaneId("left-side_inner"));
    }

    @Test
    void widgetInstanceIdNonNull() {
        assertThrows(NullPointerException.class, () -> new WidgetInstanceId(null));
        var id = WidgetInstanceId.fresh();
        assertNotNull(id.id());
        assertEquals(id, WidgetInstanceId.parse(id.id().toString()));
        assertThrows(IllegalArgumentException.class, () -> WidgetInstanceId.parse("not-a-uuid"));
    }

    @Test
    void widgetKindAcceptsIdentifierShapedStrings() {
        // Permissive grammar — accommodates the framework's actual widget
        // simpleName conventions (Java class PascalCase, kebab-case, snake_case).
        assertDoesNotThrow(() -> new WidgetKind("MovingAnimalWidget"));
        assertDoesNotThrow(() -> new WidgetKind("spinning-animals"));
        assertDoesNotThrow(() -> new WidgetKind("doc_view"));
        assertDoesNotThrow(() -> new WidgetKind("Widget123"));
        // Still rejects empty + non-identifier characters.
        assertThrows(IllegalArgumentException.class, () -> new WidgetKind(""));
        assertThrows(IllegalArgumentException.class, () -> new WidgetKind("with spaces"));
        assertThrows(IllegalArgumentException.class, () -> new WidgetKind("kind/with/slashes"));
        assertThrows(IllegalArgumentException.class, () -> new WidgetKind("kind.with.dots"));
    }

    @Test
    void workspaceKindRequiresPascalCase() {
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceKind("camelCase"));
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceKind("kebab-case"));
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceKind(""));
        assertDoesNotThrow(() -> new WorkspaceKind("AnimalsPlayground"));
        assertDoesNotThrow(() -> new WorkspaceKind("Workspace2"));
    }

    @Test
    void widgetTitleAcceptsAnyNonNullString() {
        assertThrows(NullPointerException.class, () -> new WidgetTitle(null));
        assertDoesNotThrow(() -> new WidgetTitle(""));         // empty is fine — display label
        assertDoesNotThrow(() -> new WidgetTitle("Cat tab"));
        assertDoesNotThrow(() -> new WidgetTitle("with / slashes & ☃ unicode"));
    }

    @Test
    void themeNameValidates() {
        assertThrows(IllegalArgumentException.class, () -> new ThemeName("Default"));
        assertThrows(IllegalArgumentException.class, () -> new ThemeName(""));
        assertDoesNotThrow(() -> new ThemeName("default"));
        assertEquals("default", ThemeName.DEFAULT.value());
    }

    // ── WidgetLocation sealed ADT ───────────────────────────────────────────

    @Test
    void inPaneValidates() {
        assertThrows(NullPointerException.class,
                () -> new WidgetLocation.InPane(null, 0, false));
        assertThrows(IllegalArgumentException.class,
                () -> new WidgetLocation.InPane(PaneId.of("p"), -1, false));
        assertDoesNotThrow(() -> new WidgetLocation.InPane(PaneId.of("p"), 0, true));
    }

    @Test
    void inModalIsSingleton() {
        assertSame(WidgetLocation.InModal.INSTANCE, WidgetLocation.InModal.INSTANCE);
        // Records with no components are equal across instances regardless.
        assertEquals(new WidgetLocation.InModal(), new WidgetLocation.InModal());
    }

    @Test
    void locationPatternMatchIsExhaustive() {
        WidgetLocation loc = new WidgetLocation.InPane(PaneId.of("p"), 0, true);
        String kind = switch (loc) {
            case WidgetLocation.InPane  p -> "pane:" + p.paneId();
            case WidgetLocation.InModal m -> "modal";
        };
        assertEquals("pane:p", kind);
    }

    // ── LayoutNode ──────────────────────────────────────────────────────────

    @Test
    void splitValidatesRatioAndChildren() {
        var a = new LayoutNode.Leaf(PaneId.of("a"));
        var b = new LayoutNode.Leaf(PaneId.of("b"));
        assertThrows(IllegalArgumentException.class,
                () -> new LayoutNode.Split(Orientation.HORIZONTAL, 0.0, a, b));
        assertThrows(IllegalArgumentException.class,
                () -> new LayoutNode.Split(Orientation.HORIZONTAL, 1.0, a, b));
        assertThrows(NullPointerException.class,
                () -> new LayoutNode.Split(null, 0.5, a, b));
        assertDoesNotThrow(() -> new LayoutNode.Split(Orientation.HORIZONTAL, 0.5, a, b));
    }

    // ── WorkspaceState envelope ─────────────────────────────────────────────

    @Test
    void minimalEmptyStateBuilds() {
        assertDoesNotThrow(WorkspaceStateTest::minimalEmptyState);
    }

    @Test
    void envelopeValidatesRequiredFields() {
        assertThrows(NullPointerException.class, () -> new WorkspaceState(
                1, null, Instant.now(), new LayoutNode.Leaf(PaneId.of("p")),
                Map.of(), ChromeState.defaults()));
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceState(
                0, WorkspaceKind.of("Ws"), Instant.now(), new LayoutNode.Leaf(PaneId.of("p")),
                Map.of(), ChromeState.defaults()));
        assertThrows(NullPointerException.class, () -> new WorkspaceState(
                1, WorkspaceKind.of("Ws"), Instant.now(), null,
                Map.of(), ChromeState.defaults()));
    }

    @Test
    void widgetInPaneMustReferenceExistingPane() {
        var id = WidgetInstanceId.fresh();
        var w = widget(id, new WidgetLocation.InPane(PaneId.of("ghost"), 0, true));
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceState(
                1, WorkspaceKind.of("Ws"), Instant.now(),
                new LayoutNode.Leaf(PaneId.of("p1")),    // no "ghost" pane in layout
                Map.of(id, w),
                ChromeState.defaults()));
    }

    @Test
    void atMostOneWidgetInModal() {
        var id1 = WidgetInstanceId.fresh();
        var id2 = WidgetInstanceId.fresh();
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceState(
                1, WorkspaceKind.of("Ws"), Instant.now(),
                new LayoutNode.Leaf(PaneId.of("p1")),
                Map.of(id1, widget(id1, WidgetLocation.InModal.INSTANCE),
                       id2, widget(id2, WidgetLocation.InModal.INSTANCE)),
                ChromeState.defaults()));
    }

    @Test
    void atMostOneActiveTabPerPane() {
        var p = PaneId.of("p1");
        var id1 = WidgetInstanceId.fresh();
        var id2 = WidgetInstanceId.fresh();
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceState(
                1, WorkspaceKind.of("Ws"), Instant.now(),
                new LayoutNode.Leaf(p),
                Map.of(id1, widget(id1, new WidgetLocation.InPane(p, 0, true)),
                       id2, widget(id2, new WidgetLocation.InPane(p, 1, true))),
                ChromeState.defaults()));
    }

    @Test
    void noTwoWidgetsAtSameTabSlot() {
        var p = PaneId.of("p1");
        var id1 = WidgetInstanceId.fresh();
        var id2 = WidgetInstanceId.fresh();
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceState(
                1, WorkspaceKind.of("Ws"), Instant.now(),
                new LayoutNode.Leaf(p),
                Map.of(id1, widget(id1, new WidgetLocation.InPane(p, 0, true)),
                       id2, widget(id2, new WidgetLocation.InPane(p, 0, false))),
                ChromeState.defaults()));
    }

    @Test
    void keyMustMatchWidgetInstanceId() {
        var idA = WidgetInstanceId.fresh();
        var idB = WidgetInstanceId.fresh();
        // Map key idA but value's id is idB — drift detected
        assertThrows(IllegalArgumentException.class, () -> new WorkspaceState(
                1, WorkspaceKind.of("Ws"), Instant.now(),
                new LayoutNode.Leaf(PaneId.of("p1")),
                Map.of(idA, widget(idB, new WidgetLocation.InPane(PaneId.of("p1"), 0, true))),
                ChromeState.defaults()));
    }

    @Test
    void singleModalSlotPlusPaneTabsAllowed() {
        var p = PaneId.of("p1");
        var id1 = WidgetInstanceId.fresh();
        var id2 = WidgetInstanceId.fresh();
        var id3 = WidgetInstanceId.fresh();
        assertDoesNotThrow(() -> new WorkspaceState(
                1, WorkspaceKind.of("Ws"), Instant.now(),
                new LayoutNode.Leaf(p),
                Map.of(
                    id1, widget(id1, new WidgetLocation.InPane(p, 0, true)),
                    id2, widget(id2, new WidgetLocation.InPane(p, 1, false)),
                    id3, widget(id3, WidgetLocation.InModal.INSTANCE)),
                ChromeState.defaults()));
    }

    @Test
    void widgetsByIdIsImmutable() {
        var s = minimalEmptyState();
        var id = WidgetInstanceId.fresh();
        assertThrows(UnsupportedOperationException.class,
                () -> s.widgetsById().put(id, widget(id, WidgetLocation.InModal.INSTANCE)));
    }

    @Test
    void recordEqualityHoldsAcrossAxes() {
        var s1 = minimalEmptyState();
        var s2 = minimalEmptyState();
        assertEquals(s1, s2);
        assertEquals(s1.hashCode(), s2.hashCode());
    }

    @Test
    void typedParamsRoundTripPreservesIdentity() {
        // The type carries the data structure: a typed Params record sits
        // directly in WidgetInstance.params, no JSON intermediate.
        var id = WidgetInstanceId.fresh();
        var w = widget(id, new WidgetLocation.InPane(PaneId.of("p1"), 0, true));
        assertInstanceOf(TestParams.class, w.params());
        assertEquals("cat", ((TestParams) w.params()).animal());
    }
}
