package hue.captains.singapura.js.homing.preferences;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.tree.TreeNodeJsonWriter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A site's preferences, stamped into one JS module: the rigid tree as its
 * canonical JSON, and for the master and every node the widget that shows
 * it — the address its module is served at, the export to call, the params
 * to call it with.
 *
 * <pre>
 *   const PREFERENCES = Object.freeze({
 *     tree:   { level, segment, dimensions, children … },
 *     master: { module, export, params: { tree, … } },
 *     nodes:  { "<path>": { label, summary, widget: { module, export, params } } }
 *   });
 * </pre>
 *
 * <p>Abstract: a site extends it with a class of its own, constructed with
 * the site's tree, and declares that class's instance in the site's crate —
 * a module whose content is the site's, served under the site's name. The
 * page that mounts the view imports {@code PREFERENCES} from that class.
 * The module addresses are resolved when the module is
 * served, through the resolver every served module is written with, so a
 * widget's module is named here and loaded by the view only when a node is
 * chosen. The master's params are the provider's plus the tree itself and
 * the nodes' labels, so a tree view can be built from them alone.</p>
 */
public abstract class PreferencesRegistry implements EsModule<PreferencesRegistry>, SelfContent {

    public record PREFERENCES() implements Exportable._Constant<PreferencesRegistry> {}

    private final PreferenceTree tree;
    private final WidgetProvider master;

    protected PreferencesRegistry(PreferenceTree tree, WidgetProvider master) {
        this.tree   = Objects.requireNonNull(tree, "PreferencesRegistry.tree");
        this.master = Objects.requireNonNull(master, "PreferencesRegistry.master");
    }

    public PreferenceTree tree()   { return tree; }
    public WidgetProvider master() { return master; }

    @Override public ImportsFor<PreferencesRegistry> imports() { return ImportsFor.noImports(); }

    @Override
    public ExportsOf<PreferencesRegistry> exports() {
        return new ExportsOf<>(this, List.of(new PREFERENCES()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver resolver) {
        var lines = new ArrayList<String>();
        lines.add("// Generated from the site's PreferenceTree - do not hand-edit. A widget's module is");
        lines.add("// named here and imported by the view only when its node is chosen.");
        lines.add("const PREFERENCES = Object.freeze(" + json(resolver) + ");");
        return lines;
    }

    /** The registry as JSON; what the module holds, for a test or another writer. */
    public String json(ModuleNameResolver resolver) {
        String treeJson = new TreeNodeJsonWriter().write(tree.root());

        var nodeLabels = new LinkedHashMap<String, Object>();
        for (String path : tree.paths()) nodeLabels.put(path, tree.at(path).label());
        var masterParams = new LinkedHashMap<String, Object>(master.params());
        masterParams.put("tree", new Json.Raw(treeJson));
        masterParams.put("labels", nodeLabels);

        var nodes = new LinkedHashMap<String, Object>();
        for (String path : tree.paths()) {
            PreferenceNode n = tree.at(path);
            var entry = new LinkedHashMap<String, Object>();
            entry.put("label", n.label());
            entry.put("summary", n.summary());
            entry.put("widget", provider(resolver, n.provider(), n.provider().params()));
            nodes.put(path, entry);
        }

        var out = new LinkedHashMap<String, Object>();
        out.put("tree", new Json.Raw(treeJson));
        out.put("master", provider(resolver, master, masterParams));
        out.put("nodes", nodes);
        return Json.write(out);
    }

    private static Map<String, Object> provider(ModuleNameResolver resolver, WidgetProvider p, Map<String, Object> params) {
        var out = new LinkedHashMap<String, Object>();
        out.put("module", resolver.resolve(p.widget()).basePath());
        out.put("export", p.widget().exports().exports().get(0).getClass().getSimpleName());   // the widget's class
        out.put("params", params);
        return out;
    }
}
