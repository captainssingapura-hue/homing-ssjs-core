package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.WorkspaceLayoutJson;

import java.util.Collection;

/**
 * RFC 0058 — a {@link WorkspaceGroup} on the wire, the shape
 * {@code WorkspaceGroupPathModule} and the switcher read:
 * <pre>{@code
 * { "id": …, "title": …, "summary": …, "defaultKind": …,
 *   "kinds": [ { "kind": …, "title": …, "section": …, "sectionSlug": … }, … ] }
 * }</pre>
 * Kinds are listed section by section in the group's own order, each carrying
 * the slug the server derived — the client never derives one.
 */
public final class WorkspaceGroupJson {

    private WorkspaceGroupJson() {}

    public static String one(WorkspaceGroup group) {
        var sb = new StringBuilder(256);
        sb.append("{\"id\":").append(WorkspaceLayoutJson.quoteString(group.id()));
        sb.append(",\"title\":").append(WorkspaceLayoutJson.quoteString(group.title()));
        sb.append(",\"summary\":").append(WorkspaceLayoutJson.quoteString(group.summary()));
        sb.append(",\"defaultKind\":").append(WorkspaceLayoutJson.quoteString(group.defaultKind()));
        sb.append(",\"kinds\":[");
        boolean first = true;
        for (WorkspaceGroup.Section section : group.sections()) {
            for (WorkspaceSpec spec : section.kinds()) {
                if (!first) sb.append(',');
                first = false;
                sb.append("{\"kind\":").append(WorkspaceLayoutJson.quoteString(spec.kind()));
                sb.append(",\"title\":").append(WorkspaceLayoutJson.quoteString(spec.title()));
                sb.append(",\"section\":").append(WorkspaceLayoutJson.quoteString(section.name()));
                sb.append(",\"sectionSlug\":").append(WorkspaceLayoutJson.quoteString(section.slug().value()));
                sb.append('}');
            }
        }
        return sb.append("]}").toString();
    }

    /** Every group keyed by id — what the chrome inlines beside the specs. */
    public static String allAsObject(Collection<WorkspaceGroup> groups) {
        var sb = new StringBuilder("{");
        boolean first = true;
        for (WorkspaceGroup group : groups) {
            if (!first) sb.append(',');
            first = false;
            sb.append(WorkspaceLayoutJson.quoteString(group.id())).append(':').append(one(group));
        }
        return sb.append('}').toString();
    }
}
