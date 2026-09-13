package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Navigable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0058 law 4, the tree half — every group placed exactly once, no kind a
 * leaf, no unknown group placed. A static walk from the L0 root, so the studio
 * fails to boot with the names rather than the index shadowing a placement.
 */
class WorkspaceGroupsTest {

    private WorkspaceGroup desk, ops;

    @BeforeEach
    void groups() {
        var trader = WorkspaceGroupTest.reg(WorkspaceGroupTest.spec("trader", "Trader", "Trading"));
        var audit  = WorkspaceGroupTest.reg(WorkspaceGroupTest.spec("audit",  "Audit",  "Governance"));
        WorkspaceGroupTest.reg(WorkspaceGroupTest.spec("loose", "Loose", "Workspaces"));
        desk = WorkspaceGroupRegistry.INSTANCE.register(WorkspaceGroup.of("desk", "Desk", "", List.of(trader)));
        ops  = WorkspaceGroupRegistry.INSTANCE.register(WorkspaceGroup.of("ops",  "Ops",  "", List.of(audit)));
    }

    @AfterEach
    void reset() {
        WorkspaceGroupRegistry.INSTANCE.resetForTesting();
        WorkspaceSpecRegistry.INSTANCE.resetForTesting();
        Tree.LEAVES = List.of();
        Sub.LEAVES  = List.of();
    }

    // Fixtures: a root and one child whose leaves a test sets.
    record Tree() implements L0_Catalogue<Tree> {
        static final Tree INSTANCE = new Tree();
        static List<Entry<Tree>> LEAVES = List.of();
        @Override public String name() { return "Root"; }
        @Override public List<? extends L1_Catalogue<Tree, ?>> subCatalogues() { return List.of(Sub.INSTANCE); }
        @Override public List<Entry<Tree>> leaves() { return LEAVES; }
    }
    record Sub() implements L1_Catalogue<Tree, Sub> {
        static final Sub INSTANCE = new Sub();
        static List<Entry<Sub>> LEAVES = List.of();
        @Override public Tree parent() { return Tree.INSTANCE; }
        @Override public String name() { return "Sub"; }
        @Override public List<Entry<Sub>> leaves() { return LEAVES; }
    }

    private static Navigable<GenericWorkspace.Params, GenericWorkspace> nav(GenericWorkspace.Params p, String name) {
        return new Navigable<>(GenericWorkspace.INSTANCE, p, name, "");
    }

    @Test
    void everyGroupPlacedOnceAcrossTheTreePasses() {
        Tree.LEAVES = List.of(Entry.of(Tree.INSTANCE, nav(GenericWorkspace.ofGroup(desk), "Desk")));
        Sub.LEAVES  = List.of(Entry.of(Sub.INSTANCE,  nav(GenericWorkspace.ofGroup(ops),  "Ops")));
        assertDoesNotThrow(() -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
    }

    @Test
    void anUnplacedGroupIsNamed() {
        Tree.LEAVES = List.of(Entry.of(Tree.INSTANCE, nav(GenericWorkspace.ofGroup(desk), "Desk")));
        var e = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
        assertTrue(e.getMessage().contains("group 'ops'") && e.getMessage().contains("placed nowhere"), e.getMessage());
    }

    @Test
    void aGroupPlacedTwiceNamesBothPositions() {
        Tree.LEAVES = List.of(Entry.of(Tree.INSTANCE, nav(GenericWorkspace.ofGroup(desk), "Desk")));
        Sub.LEAVES  = List.of(Entry.of(Sub.INSTANCE,  nav(GenericWorkspace.ofGroup(desk), "Desk again")),
                              Entry.of(Sub.INSTANCE,  nav(GenericWorkspace.ofGroup(ops),  "Ops")));
        var e = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
        assertTrue(e.getMessage().contains("group 'desk' is placed 2 times"), e.getMessage());
        assertTrue(e.getMessage().contains(Tree.class.getName()) && e.getMessage().contains(Sub.class.getName()), e.getMessage());
    }

    @Test
    void aKindPlacedAsALeafIsRefused() {
        Tree.LEAVES = List.of(Entry.of(Tree.INSTANCE, nav(GenericWorkspace.ofGroup(desk), "Desk")),
                              Entry.of(Tree.INSTANCE, nav(new GenericWorkspace.Params(null, "loose"), "Loose")));
        Sub.LEAVES  = List.of(Entry.of(Sub.INSTANCE,  nav(GenericWorkspace.ofGroup(ops),  "Ops")));
        var e = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
        assertTrue(e.getMessage().contains("kind 'loose'") && e.getMessage().contains("never a leaf"), e.getMessage());
    }

    @Test
    void anUnregisteredGroupIsRefused() {
        Tree.LEAVES = List.of(Entry.of(Tree.INSTANCE, nav(GenericWorkspace.ofGroup(desk), "Desk")),
                              Entry.of(Tree.INSTANCE, nav(new GenericWorkspace.Params("ghost", null), "Ghost")));
        Sub.LEAVES  = List.of(Entry.of(Sub.INSTANCE,  nav(GenericWorkspace.ofGroup(ops),  "Ops")));
        var e = assertThrows(IllegalStateException.class,
                () -> WorkspaceGroups.assertPlacedOnce(Tree.INSTANCE, WorkspaceGroupRegistry.INSTANCE));
        assertTrue(e.getMessage().contains("unregistered group: 'ghost'"), e.getMessage());
    }
}
