package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.WidgetEntriesJson;
import hue.captains.singapura.js.homing.workspace.WorkspaceLayoutJson;
import hue.captains.singapura.js.homing.workspace.state.LayoutNode;
import hue.captains.singapura.js.homing.workspace.state.Orientation;
import hue.captains.singapura.js.homing.workspace.state.PaneId;
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
 *   "widgetCodecs":   [ {widgetKind, moduleUrl, exportName}, ... ],
 *   "maxTabs":        16    // RFC 0047 — global tab budget (shared pool)
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
        sb.append(",\"group\":").append(WorkspaceLayoutJson.quoteString(spec.group()));
        sb.append(",\"entries\":").append(WidgetEntriesJson.of(spec.widgetEntries()));
        sb.append(",\"ribbonItems\":").append(WorkspaceLayoutJson.ribbonItems(spec.ribbonItems()));
        sb.append(",\"footerItems\":").append(WorkspaceLayoutJson.footerItems(spec.footerItems()));
        sb.append(",\"parties\":").append(parties(spec.parties()));
        sb.append(",\"actionDispatch\":").append(actionDispatch(spec.actionDispatch()));
        sb.append(",\"widgetCodecs\":").append(widgetCodecs(spec.widgetCodecs()));
        sb.append(",\"pinnedSpawns\":").append(pinnedSpawns(spec.pinnedSpawns()));
        sb.append(",\"arrangement\":").append(arrangement(spec.arrangement()));  // RFC 0060
        sb.append(",\"maxTabs\":").append(spec.maxTabs());   // RFC 0047 — global tab budget
        sb.append('}');
        return sb.toString();
    }


    /**
     * RFC 0060 — the seed arrangement, as {@code {name, layout, widgets}}.
     *
     * <p>{@code layout} is emitted in <b>MultiTabPane's native shape</b>
     * ({@code kind/orientation/children[{pane,ratio}]}) rather than the typed
     * record's, because that is what the shell hands to {@code initialLayout} and
     * what {@code WorkspaceStateModel} seeds from. Converting here rather than in
     * the browser keeps the client free of a second layout dialect — and the two
     * children are emitted in first/second order, so the ratio stays the first
     * child's share exactly as {@link hue.captains.singapura.js.homing.workspace.state.LayoutNode.Split}
     * records it.</p>
     */
    static String arrangement(Arrangement a) {
        var sb = new StringBuilder(128);
        sb.append("{\"name\":").append(WorkspaceLayoutJson.quoteString(a.name()));
        sb.append(",\"layout\":");
        layoutNode(sb, a.layout());
        sb.append(",\"widgets\":{");
        boolean first = true;
        for (PaneId pane : a.panes()) {
            var widgets = a.widgetsIn(pane);
            if (widgets.isEmpty()) continue;
            if (!first) sb.append(',');
            first = false;
            sb.append(WorkspaceLayoutJson.quoteString(pane.value())).append(':')
              .append(pinnedSpawns(widgets));
        }
        return sb.append("}}").toString();
    }

    /** One node in MTP's native layout shape. Binary throughout (RFC 0060 D1). */
    private static void layoutNode(StringBuilder sb, LayoutNode node) {
        switch (node) {
            case LayoutNode.Leaf leaf ->
                    sb.append("{\"kind\":\"leaf\",\"slotId\":")
                      .append(WorkspaceLayoutJson.quoteString(leaf.paneId().value())).append('}');
            case LayoutNode.Split s -> {
                sb.append("{\"kind\":\"split\",\"orientation\":")
                  .append(WorkspaceLayoutJson.quoteString(
                          s.orientation() == Orientation.VERTICAL ? "vertical" : "horizontal"))
                  .append(",\"children\":[{\"pane\":");
                layoutNode(sb, s.first());
                sb.append(",\"ratio\":").append(s.ratio()).append("},{\"pane\":");
                layoutNode(sb, s.second());
                sb.append(",\"ratio\":").append(1.0 - s.ratio()).append("}]}");
            }
        }
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
