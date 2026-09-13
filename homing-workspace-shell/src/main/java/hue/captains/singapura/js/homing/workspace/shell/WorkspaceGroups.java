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
 * placed exactly once, and no kind is a leaf</b>. The registry already refuses
 * a kind in two groups; together the two give the invariant that matters —
 * every kind is positioned exactly once, as a path inside its group's page.
 *
 * <p>A static walk from a studio's L0 root through {@code subCatalogues()} and
 * {@code leaves()} — the same tree the {@code CatalogueRegistry} indexes (RFC
 * 0051 Law 3 guarantees the two agree), walked here because the registry is
 * built inside {@code Bootstrap}, below this crate. A studio's fixtures call it
 * once its groups are registered and its catalogues declared, so the boot
 * fails with the names rather than the index shadowing a placement silently.</p>
 *
 * <p>Hosted studios are followed through their {@link Entry.OfStudio} proxies,
 * so an umbrella's walk covers every studio it composes — which is where two
 * placements of one group would come from.</p>
 */
public final class WorkspaceGroups {

    private WorkspaceGroups() {}

    /**
     * Fails unless every group in {@code registry} is placed exactly once under
     * {@code root}, and no leaf places a kind ({@code Params} carrying
     * {@code ws_kind}) or a group the registry does not know.
     *
     * @throws IllegalStateException naming the group and both positions, the
     *         unplaced group, the kind leaf, or the unknown group
     */
    public static void assertPlacedOnce(L0_Catalogue<?> root, WorkspaceGroupRegistry registry) {
        Objects.requireNonNull(root, "root");
        Objects.requireNonNull(registry, "registry");
        var placements = new LinkedHashMap<String, List<String>>();   // group id → catalogue classes placing it
        var kindLeaves = new ArrayList<String>();
        var unknown    = new ArrayList<String>();
        walk(root, new HashSet<>(), placements, kindLeaves, unknown, registry);

        var failures = new ArrayList<String>();
        for (WorkspaceGroup g : registry.all()) {
            List<String> at = placements.getOrDefault(g.id(), List.of());
            if (at.isEmpty()) {
                failures.add("group '" + g.id() + "' (" + g.kinds() + ") is placed nowhere under "
                        + root.getClass().getName() + " — place it as a leaf: Entry.of(host, new Navigable<>("
                        + "GenericWorkspace.INSTANCE, GenericWorkspace.ofGroup(group), …))");
            } else if (at.size() > 1) {
                failures.add("group '" + g.id() + "' is placed " + at.size() + " times — at " + at
                        + "; a group is placed exactly once");
            }
        }
        for (String k : kindLeaves) {
            failures.add("a kind is placed as a leaf: " + k + " — a kind is a path inside its group "
                    + "(#ws/<section>/<kind>), never a leaf; place the group instead");
        }
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
                             Map<String, List<String>> placements, List<String> kindLeaves,
                             List<String> unknown, WorkspaceGroupRegistry registry) {
        if (!seen.add(node.getClass())) return;   // the registry dedups by class; so do we
        for (Entry<?> e : node.leaves()) {
            switch (e) {
                case Entry.OfLeaf<?, ?, ?> leaf -> {
                    Navigable<?, ?> nav = leaf.nav();
                    if (!(nav.params() instanceof GenericWorkspace.Params p)) continue;
                    String where = node.getClass().getName() + " → " + leaf.slug().value();
                    if (p.ws_kind() != null) {
                        kindLeaves.add("kind '" + p.ws_kind() + "' at " + where);
                        continue;
                    }
                    if (registry.get(p.ws_group()).isEmpty()) {
                        unknown.add("'" + p.ws_group() + "' at " + where);
                        continue;
                    }
                    placements.computeIfAbsent(p.ws_group(), k -> new ArrayList<>()).add(where);
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
