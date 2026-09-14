package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0058 — a <b>workspace group</b>: the unit of placement. A studio places one
 * as an ordinary catalogue leaf ({@code Navigable(GenericWorkspace, Params(group))});
 * the kinds it holds are never leaves themselves. Inside the page a kind is a
 * path in the group's own tree, named by the anchor {@code #ws/<section>/<kind>}.
 *
 * <p>The group's tree is two levels — {@link WorkspaceSpec#section()} then
 * {@link WorkspaceSpec#kind()} — and a group validates it at construction, in the
 * register of RFC 0051's boot laws:</p>
 * <ol>
 *   <li>every kind is registered in {@link WorkspaceSpecRegistry}, and every
 *       section's slug is unique among the group's sections;</li>
 *   <li>the default kind is a member;</li>
 *   <li>a kind's segment is its id — the registry already keeps ids unique, and
 *       an id is a {@link NodeName} by construction, which is checked here so a
 *       kind that could never be addressed is refused before it is served.</li>
 * </ol>
 * <p>The fourth law — every kind in exactly one group, every group placed exactly
 * once — spans groups and the catalogue tree, so it lives in
 * {@link WorkspaceGroupRegistry} and {@link WorkspaceGroups}.</p>
 *
 * <p>Sections keep the order of first appearance in {@code specs}, so a group
 * orders its own tree by ordering its specs. Section slugs are the server's to
 * derive ({@link NodeName#conciseSlug}) and are served with the group; the client
 * never re-derives one.</p>
 *
 * @param id          the group's stable identity — {@code ?ws_group=<id>}; a {@link NodeName}
 * @param title       the group's label — the leaf's name is the placement's, this is the page's
 * @param summary     one line for the leaf's tile
 * @param defaultKind the kind an anchorless address opens
 * @param specs       the kinds this group holds, in order
 */
public record WorkspaceGroup(String id, String title, String summary, String defaultKind,
                             List<WorkspaceSpec> specs) {

    /** The anchor prefix that marks a workspace path: {@code #ws/<section>/<kind>}. */
    public static final String ANCHOR_PREFIX = "ws";

    /** One section of the group's tree: its heading, its served slug, its kinds in order. */
    public record Section(String name, NodeName slug, List<WorkspaceSpec> kinds) {
        public Section {
            kinds = List.copyOf(kinds);
        }
    }

    public WorkspaceGroup {
        Objects.requireNonNull(id, "WorkspaceGroup.id");
        Objects.requireNonNull(title, "WorkspaceGroup.title");
        Objects.requireNonNull(specs, "WorkspaceGroup.specs");
        new NodeName(id);   // a group id is a URL-safe segment, like a kind
        if (title.isBlank()) throw new IllegalArgumentException("WorkspaceGroup '" + id + "': title must not be blank");
        if (summary == null) summary = "";
        specs = List.copyOf(specs);
        if (specs.isEmpty()) throw new IllegalArgumentException("WorkspaceGroup '" + id + "' holds no kinds");
        if (defaultKind == null) defaultKind = specs.get(0).kind();

        var seen = new LinkedHashMap<String, WorkspaceSpec>();
        for (WorkspaceSpec spec : specs) {
            String kind = spec.kind();
            // Law 3 — a kind's segment is its id.
            try {
                new NodeName(kind);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("WorkspaceGroup '" + id + "': kind '" + kind
                        + "' cannot be an anchor segment — " + e.getMessage());
            }
            // Law 1a — every kind is registered.
            var registered = WorkspaceSpecRegistry.INSTANCE.get(kind);
            if (registered.isEmpty()) {
                throw new IllegalArgumentException("WorkspaceGroup '" + id + "': kind '" + kind
                        + "' is not registered in WorkspaceSpecRegistry — register the spec before grouping it");
            }
            if (registered.get() != spec) {
                throw new IllegalArgumentException("WorkspaceGroup '" + id + "': kind '" + kind
                        + "' is held as " + spec.getClass().getName() + " but registered as "
                        + registered.get().getClass().getName());
            }
            if (seen.put(kind, spec) != null) {
                throw new IllegalArgumentException("WorkspaceGroup '" + id + "': kind '" + kind + "' listed twice");
            }
        }
        // Law 2 — the default kind is a member.
        if (!seen.containsKey(defaultKind)) {
            throw new IllegalArgumentException("WorkspaceGroup '" + id + "': default kind '" + defaultKind
                    + "' is not a member (members: " + String.join(", ", seen.keySet()) + ")");
        }
        // Law 1b — section slugs unique among the group's sections.
        var slugToName = new LinkedHashMap<String, String>();
        for (WorkspaceSpec spec : specs) {
            String name = spec.section();
            String slug = NodeName.conciseSlug(name).value();
            String prior = slugToName.putIfAbsent(slug, name);
            if (prior != null && !prior.equals(name)) {
                throw new IllegalArgumentException("WorkspaceGroup '" + id + "': sections '" + prior + "' and '"
                        + name + "' both slug to '" + slug + "' — rename one");
            }
        }
    }

    /** A group whose default is its first kind. */
    public static WorkspaceGroup of(String id, String title, String summary, List<WorkspaceSpec> specs) {
        return new WorkspaceGroup(id, title, summary, null, specs);
    }

    /** The group's tree: sections in order of first appearance, each with its kinds in order. */
    public List<Section> sections() {
        var byName = new LinkedHashMap<String, List<WorkspaceSpec>>();
        for (WorkspaceSpec spec : specs) byName.computeIfAbsent(spec.section(), k -> new ArrayList<>()).add(spec);
        var out = new ArrayList<Section>(byName.size());
        byName.forEach((name, kinds) -> out.add(new Section(name, NodeName.conciseSlug(name), kinds)));
        return List.copyOf(out);
    }

    /** The spec for {@code kind}, when this group holds it. */
    public Optional<WorkspaceSpec> spec(String kind) {
        for (WorkspaceSpec spec : specs) if (spec.kind().equals(kind)) return Optional.of(spec);
        return Optional.empty();
    }

    public boolean holds(String kind) { return spec(kind).isPresent(); }

    /** The kinds this group holds, in order. */
    public List<String> kinds() {
        return specs.stream().map(WorkspaceSpec::kind).toList();
    }

    /**
     * A kind's anchor inside this group — {@code ws/<section-slug>/<kind>}, without
     * the {@code #}. The path the RFC names, minted from the same slug the group
     * serves, so the server and the client agree by construction.
     */
    public Optional<String> anchorFor(String kind) {
        return spec(kind).map(spec ->
                ANCHOR_PREFIX + "/" + NodeName.conciseSlug(spec.section()).value() + "/" + kind);
    }

    /** The section a kind sits in, when this group holds it. */
    public Optional<Section> sectionOf(String kind) {
        for (Section s : sections()) for (WorkspaceSpec k : s.kinds()) if (k.kind().equals(kind)) return Optional.of(s);
        return Optional.empty();
    }

    /** Kinds by id — the shape the chrome serialises. */
    public Map<String, WorkspaceSpec> specsByKind() {
        var out = new LinkedHashMap<String, WorkspaceSpec>();
        for (WorkspaceSpec spec : specs) out.put(spec.kind(), spec);
        return out;
    }
}
