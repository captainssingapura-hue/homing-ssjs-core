package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.WidgetEntriesJson;
import hue.captains.singapura.js.homing.workspace.WorkspaceLayoutJson;

import java.util.Collection;
import java.util.Map;

/**
 * Serializer for {@link WorkspaceSpec} → JS object literal. The output
 * is interpolated into {@link GenericWorkspaceChrome}'s body JS and
 * consumed by {@code mountWorkspaceShell(branch, parent, spec)}.
 *
 * <p>Lives in its own class so the wire contract is unit-testable.
 * Wire shape (one spec):</p>
 *
 * <pre>{@code
 * {
 *   "kind":           "animalPlayground",
 *   "title":          "Animals Playground",
 *   "entries":        [ ... WidgetEntriesJson output ... ],
 *   "ribbonItems":    [ ... WorkspaceLayoutJson.ribbonItems output ... ],
 *   "footerItems":    [ ... WorkspaceLayoutJson.footerItems output ... ],
 *   "parties":        [ {name, secretaryModuleUrl, secretaryExportName,
 *                        actors: [{id, parentSecretary}], exposedAs }, ... ],
 *   "actionDispatch": { "animal-selected": {kind:"tellParty", ...}, ... },
 *   "widgetCodecs":   [ {widgetKind, moduleUrl, exportName}, ... ]
 * }
 * }</pre>
 *
 * @since post-RFC-0034 workspace chrome decomposition
 */
public final class WorkspaceSpecJson {

    private WorkspaceSpecJson() {}

    /** Serialize every registered spec as a JS object keyed by {@code kind}. */
    public static String allAsObject(Collection<WorkspaceSpec> specs) {
        var sb = new StringBuilder("{");
        boolean first = true;
        for (WorkspaceSpec spec : specs) {
            if (!first) sb.append(',');
            first = false;
            sb.append(WorkspaceLayoutJson.quoteString(spec.kind()))
              .append(':')
              .append(one(spec));
        }
        return sb.append('}').toString();
    }

    /** Serialize one spec as a JS object literal. */
    public static String one(WorkspaceSpec spec) {
        var sb = new StringBuilder(256);
        sb.append('{');
        sb.append("\"kind\":").append(WorkspaceLayoutJson.quoteString(spec.kind()));
        sb.append(",\"title\":").append(WorkspaceLayoutJson.quoteString(spec.title()));
        sb.append(",\"entries\":").append(WidgetEntriesJson.of(spec.widgetEntries()));
        sb.append(",\"ribbonItems\":").append(WorkspaceLayoutJson.ribbonItems(spec.ribbonItems()));
        sb.append(",\"footerItems\":").append(WorkspaceLayoutJson.footerItems(spec.footerItems()));
        sb.append(",\"parties\":").append(parties(spec.parties()));
        sb.append(",\"actionDispatch\":").append(actionDispatch(spec.actionDispatch()));
        sb.append(",\"widgetCodecs\":").append(widgetCodecs(spec.widgetCodecs()));
        sb.append(",\"pinnedSpawns\":").append(pinnedSpawns(spec.pinnedSpawns()));
        sb.append('}');
        return sb.toString();
    }

    static String pinnedSpawns(Iterable<String> kinds) {
        var sb = new StringBuilder("[");
        boolean first = true;
        for (String kind : kinds) {
            if (!first) sb.append(',');
            first = false;
            sb.append(WorkspaceLayoutJson.quoteString(kind));
        }
        return sb.append(']').toString();
    }

    // ── parties ──────────────────────────────────────────────────────────

    static String parties(Iterable<PartyDecl> parties) {
        var sb = new StringBuilder("[");
        boolean first = true;
        for (PartyDecl p : parties) {
            if (!first) sb.append(',');
            first = false;
            sb.append('{');
            sb.append("\"name\":").append(WorkspaceLayoutJson.quoteString(p.name()));
            sb.append(",\"secretaryModuleUrl\":").append(WorkspaceLayoutJson.quoteString(
                    moduleUrlFor(p.secretaryModule().getClass())));
            sb.append(",\"secretaryExportName\":").append(WorkspaceLayoutJson.quoteString(p.secretaryExportName()));
            sb.append(",\"actors\":[");
            boolean firstA = true;
            for (PartyActor a : p.actors()) {
                if (!firstA) sb.append(',');
                firstA = false;
                sb.append('{');
                sb.append("\"id\":").append(WorkspaceLayoutJson.quoteString(a.id()));
                sb.append(",\"parentSecretary\":").append(WorkspaceLayoutJson.quoteString(a.parentSecretary()));
                sb.append('}');
            }
            sb.append(']');
            if (p.exposedAs() != null) {
                sb.append(",\"exposedAs\":").append(WorkspaceLayoutJson.quoteString(p.exposedAs()));
            } else {
                sb.append(",\"exposedAs\":null");
            }
            sb.append('}');
        }
        return sb.append(']').toString();
    }

    // ── actionDispatch ───────────────────────────────────────────────────

    static String actionDispatch(Map<String, ActionDispatch> map) {
        var sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, ActionDispatch> e : map.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            sb.append(WorkspaceLayoutJson.quoteString(e.getKey()))
              .append(':')
              .append(actionDispatchValue(e.getValue()));
        }
        return sb.append('}').toString();
    }

    private static String actionDispatchValue(ActionDispatch d) {
        return switch (d) {
            case ActionDispatch.TellParty tp -> "{"
                    + "\"kind\":\"tellParty\""
                    + ",\"party\":"       + WorkspaceLayoutJson.quoteString(tp.partyName())
                    + ",\"actor\":"       + WorkspaceLayoutJson.quoteString(tp.actorId())
                    + ",\"messageKind\":" + WorkspaceLayoutJson.quoteString(tp.messageKind())
                    + ",\"valueKey\":"    + WorkspaceLayoutJson.quoteString(tp.valueKey())
                    + "}";
            case ActionDispatch.LogOnly l -> "{\"kind\":\"logOnly\"}";
        };
    }

    // ── widgetCodecs ─────────────────────────────────────────────────────

    static String widgetCodecs(Iterable<WidgetCodecRef> codecs) {
        var sb = new StringBuilder("[");
        boolean first = true;
        for (WidgetCodecRef c : codecs) {
            if (!first) sb.append(',');
            first = false;
            sb.append('{');
            sb.append("\"widgetKind\":").append(WorkspaceLayoutJson.quoteString(c.widgetKind()));
            sb.append(",\"moduleUrl\":").append(WorkspaceLayoutJson.quoteString(
                    moduleUrlFor(c.module().getClass())));
            sb.append(",\"exportName\":").append(WorkspaceLayoutJson.quoteString(c.exportName()));
            sb.append('}');
        }
        return sb.append(']').toString();
    }

    // ── helpers ──────────────────────────────────────────────────────────

    /**
     * Standard module URL the framework serves at: {@code /module?class=<fqcn>}.
     * Matches the URL the {@code EsModuleWriter} generates for inter-module
     * imports.
     */
    static String moduleUrlFor(Class<?> moduleClass) {
        return "/module?class=" + moduleClass.getName();
    }
}
