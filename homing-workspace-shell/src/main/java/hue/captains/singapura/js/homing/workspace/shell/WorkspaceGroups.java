package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.studio.base.app.Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L0_Catalogue;
import hue.captains.singapura.js.homing.studio.base.app.Navigable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * RFC 0058 — law 4, the half that needs the catalogue tree: <b>every group is
 * placed exactly once, and every kind is positioned exactly once</b>. The
 * registry already refuses a kind in two groups; this adds what only the tree
 * can show — a group placed twice or nowhere, an unregistered group placed, and
 * a kind that is <em>both</em> held by a group and placed as a legacy
 * {@link GenericWorkspace} leaf, which is one kind at two positions.
 *
 * <p>A legacy kind leaf on its own is not refused: {@code GenericWorkspace} is
 * the flat address every pre-0058 link carries, and a studio that has not
 * placed groups is unpositioned, not wrong. The law bites only when the two
 * addresses overlap.</p>
 *
 * <p>A static walk from a studio's L0 root through {@code subCatalogues()} and
 * {@code leaves()} — the same tree the {@code CatalogueRegistry} indexes (RFC
 * 0051 Law 3 guarantees the two agree), walked here because the registry is
 * built inside {@code Bootstrap}, below this crate. A studio's fixtures call it
 * once its groups are registered and its catalogues declared, so the boot
 * fails with the names rather than the index shadowing a placement silently.
 * Hosted studios are followed through their {@link Entry.OfStudio} proxies, so
 * an umbrella's walk covers every studio it composes.</p>
 */
public final class WorkspaceGroups {

    private WorkspaceGroups() {}

    /**
     * @throws IllegalStateException naming the group and both positions, the
     *         unplaced group, the unknown group, or the kind positioned twice
     */
    public static void assertPlacedOnce(L0_Catalogue<?> root, WorkspaceGroupRegistry registry) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(registry, "registry");
        var placements = new LinkedHashMap<String, List<String>>();   // group id → where placed
        var kindLeaves = new LinkedHashMap<String, List<String>>();   // kind → where placed as a legacy leaf
        var unknown    = new ArrayList<String>();
        walk(root, new HashSet<>(), placements, kindLeaves, unknown, registry);

        var failures = new ArrayList<String>();
        for (WorkspaceGroup g : registry.all()) {
            List<String> at = placements.getOrDefault(g.id(), List.of());
            if (at.isEmpty()) {
                failures.add("group '" + g.id() + "' (" + g.kinds() + ") is placed nowhere under "
                        + root.getClass().getName() + " — place it as a leaf: Entry.of(host, new Navigable<>("
                        + "WorkspaceGroupApp.INSTANCE, WorkspaceGroupApp.of(group), …))");
            } else if (at.size() > 1) {
                failures.add("group '" + g.id() + "' is placed " + at.size() + " times — at " + at
                        + "; a group is placed exactly once");
            }
        }
        kindLeaves.forEach((kind, at) -> registry.groupOf(kind).ifPresent(g ->
                failures.add("kind '" + kind + "' is positioned twice: held by group '" + g.id()
                        + "' (#ws/…/" + kind + ") and placed as a legacy leaf at " + at
                        + " — a kind a group holds is a path inside it, not a leaf; retire the leaf")));
        for (String u : unknown) {
            failures.add("leaf places an unregistered group: " + u
                    + " — register it in WorkspaceGroupRegistry before the catalogue places it");
        }
        if (!failures.isEmpty()) {
            throw new IllegalStateException("RFC 0058 law 4 — workspace groups are not positioned exactly once ("
                    + failures.size() + "):\n  " + String.join("\n  ", failures));
        }
    }

    private static void walk(Catalogue<?> node, Set<Class<?>> seen,
                             Map<String, List<String>> placements, Map<String, List<String>> kindLeaves,
                             List<String> unknown, WorkspaceGroupRegistry registry) {
        if (!seen.add(node.getClass())) return;   // the registry dedups by class; so do we
        for (Entry<?> e : node.leaves()) {
            switch (e) {
                case Entry.OfLeaf<?, ?, ?> leaf -> {
                    Navigable<?, ?> nav = leaf.nav();
                    String where = node.getClass().getName() + " → " + leaf.slug().value();
                    if (nav.params() instanceof WorkspaceGroupApp.Params p) {
                        if (registry.get(p.ws_group()).isEmpty()) unknown.add("'" + p.ws_group() + "' at " + where);
                        else placements.computeIfAbsent(p.ws_group(), k -> new ArrayList<>()).add(where);
                    } else if (nav.params() instanceof GenericWorkspace.Params p) {
                        kindLeaves.computeIfAbsent(p.ws_kind(), k -> new ArrayList<>()).add(where);
                    }
                }
                case Entry.OfStudio<?, ?> hosted ->
                        walk(hosted.proxy().source(), seen, placements, kindLeaves, unknown, registry);
                case Entry.OfIllustration<?> ignored -> { }
            }
        }
        for (Catalogue<?> child : node.subCatalogues()) {
            walk(child, seen, placements, kindLeaves, unknown, registry);
        }
    }
}
