package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModules;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.State;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * The resolution on the command line: a node, a state and a target walked in a theme - a style worn
 * in a palette - and how the walk went.
 *
 * <pre>
 * resolve --modules CLASS[,CLASS] --colors CLASS[,CLASS] --style CLASS --palette CLASS NODE STATE TARGET   the walk, step by step
 * resolve --modules CLASS[,CLASS] --colors CLASS[,CLASS] --style CLASS --palette CLASS NODE STATE|* TARGET|*   a table: every state, every target
 * resolve --modules CLASS[,CLASS] --colors CLASS[,CLASS] --style CLASS --palette CLASS --flatten [--out CSS]   the theme's sheet, flattened
 * resolve --modules CLASS[,CLASS] --list nodes|roles|states|targets   the tokens there are
 * </pre>
 *
 * Each {@code CLASS} names a type by its class, whose {@code INSTANCE} is read: a
 * {@link ComponentModule} whose components are read, each registered as an instance registers its own, the {@link SchemeDeclaration}s of the style's scheme and those below it, a {@link DesignStyle}, a {@link ColorPalette} of the style's scheme; {@code NODE} is a casting's token -
 * {@code danger-button}, {@code danger-button_go}. The exit is 0 when every look resolves, 1 when
 * one is unanswered, 2 when the command is wrong.
 */
public record ResolveCli() implements StatelessFunctionalObject {

    public static final ResolveCli INSTANCE = new ResolveCli();

    /** Runs the command line and exits with its status. */
    public static void main(String[] args) {
        System.exit(INSTANCE.run(List.of(args), System.out, System.err));
    }

    private static final String USAGE = String.join("\n",
            "usage: resolve --modules CLASS[,CLASS] --colors CLASS --style CLASS --palette CLASS NODE STATE TARGET",
            "       resolve --modules CLASS[,CLASS] --colors CLASS --style CLASS --palette CLASS NODE STATE|* TARGET|*",
            "       resolve --modules CLASS[,CLASS] --colors CLASS --style CLASS --palette CLASS --flatten [--out CSS]",
            "       resolve --modules CLASS[,CLASS] --list nodes|roles|states|targets");

    /** Runs a command; the exit code. */
    public int run(List<String> args, PrintStream out, PrintStream err) {
        Options o = options(args);
        if (o.modules() == null) return usage(err, "no --modules");
        try {
            Taxonomy taxonomy = taxonomy(o.modules());
            if (o.list() != null) return list(taxonomy, o.list(), out, err);
            if (o.colors() == null || o.style() == null || o.palette() == null) return usage(err, "no --colors, no --style or no --palette");
            Dressed theme = theme(o);
            if (o.flatten()) return flatten(taxonomy, theme, o.outFile(), out, err);
            if (o.rest().size() != 3) return usage(err, "a node, a state and a target");
            return resolved(taxonomy, theme, o.rest(), out);
        } catch (IllegalArgumentException | IllegalStateException e) {
            err.println("resolve: " + e.getMessage());
            return 2;
        }
    }

    /** What a command line says: a flag's value or null, whether to flatten, and the words left over. */
    private record Options(String modules, String colors, String style, String palette, String list, String outFile,
                           boolean flatten, List<String> rest) {}

    /** Reads a command line. */
    private Options options(List<String> args) {
        String modulesClasses = null, colorsClass = null, styleClass = null, paletteClass = null, list = null, outFile = null;
        boolean flatten = false;
        var rest = new ArrayList<String>();
        for (int i = 0; i < args.size(); i++) {
            String a = args.get(i);
            switch (a) {
                case "--modules" -> modulesClasses = i + 1 < args.size() ? args.get(++i) : null;
                case "--colors" -> colorsClass = i + 1 < args.size() ? args.get(++i) : null;
                case "--style" -> styleClass = i + 1 < args.size() ? args.get(++i) : null;
                case "--palette" -> paletteClass = i + 1 < args.size() ? args.get(++i) : null;
                case "--list" -> list = i + 1 < args.size() ? args.get(++i) : null;
                case "--out" -> outFile = i + 1 < args.size() ? args.get(++i) : null;
                case "--flatten" -> flatten = true;
                default -> rest.add(a);
            }
        }
        return new Options(modulesClasses, colorsClass, styleClass, paletteClass, list, outFile, flatten, rest);
    }

    /** The theme a command names: its style worn in its palette, over its scheme's colours. */
    private Dressed theme(Options o) {
        var declarations = new ArrayList<SchemeDeclaration<?>>();
        for (String c : o.colors().split(",")) declarations.add(instance(c.strip(), SchemeDeclaration.class));
        DesignStyle<?> style = instance(o.style(), DesignStyle.class);
        ColorTree colors = SchemeTrees.INSTANCE.tree(new SchemeRegistry(declarations), style.scheme());
        ColorPalette<?> palette = instance(o.palette(), ColorPalette.class);
        return Wear.INSTANCE.of(colors, style, palette).orElseThrow(() -> new IllegalArgumentException(
                style.token() + " and " + palette.token() + " are of different schemes: a style is worn only in a palette of its own"));
    }

    /** A node, a state and a target: the walk, step by step, or a table where a state or a target is every one. */
    private int resolved(Taxonomy taxonomy, Dressed theme, List<String> rest, PrintStream out) {
        var tokens = SemanticTokens.INSTANCE;
        Casting node = tokens.castingOf(taxonomy, rest.get(0));
        List<State> states = "*".equals(rest.get(1)) ? List.of(State.values()) : List.of(tokens.stateOf(rest.get(1)));
        List<Target> targets = "*".equals(rest.get(2)) ? tokens.leaves() : List.of(tokens.targetOf(rest.get(2)));
        if (states.size() == 1 && targets.size() == 1) {
            Resolution r = ResolveSemanticClass.INSTANCE.resolve(taxonomy, theme, node, states.get(0), targets.get(0));
            r.explain().forEach(out::println);
            return r.outcome() instanceof Resolution.Unanswered ? 1 : 0;
        }
        return table(taxonomy, theme, node, states, targets, out);
    }

    /** One row per state and target: the class worn, the node that answered, how far up. */
    private int table(Taxonomy taxonomy, Dressed theme, Casting node, List<State> states, List<Target> targets, PrintStream out) {
        var tokens = SemanticTokens.INSTANCE;
        int unanswered = 0;
        out.println("node     " + tokens.casting(node) + "  (" + node + ")");
        out.println("theme    " + theme + "  (" + String.join(" over ", theme.chain()) + ")");
        for (State s : states)
            for (Target t : targets) {
                Resolution r = ResolveSemanticClass.INSTANCE.resolve(taxonomy, theme, node, s, t);
                String what = switch (r.outcome()) {
                    case Resolution.Answered a -> a.className() + "   from " + a.layer();
                    case Resolution.Silent x -> "-";
                    case Resolution.Unanswered x -> "UNANSWERED";
                };
                if (r.outcome() instanceof Resolution.Unanswered) unanswered++;
                if (states.size() > 1 && r.outcome() instanceof Resolution.Silent) continue;   // a state nothing treats: nothing to say
                out.println(String.format("  %-12s %-18s %s", tokens.state(s), tokens.target(t), what));
            }
        if (unanswered > 0) out.println(unanswered + " unanswered: a theme answers every target at the root");
        return unanswered > 0 ? 1 : 0;
    }

    private int list(Taxonomy taxonomy, String what, PrintStream out, PrintStream err) {
        var tokens = SemanticTokens.INSTANCE;
        switch (what) {
            case "nodes" -> {
                out.println(Root.INSTANCE.token());
                for (ComponentNode n : taxonomy.nodes()) if (n instanceof Taxon t) out.println(t.token());
            }
            case "roles" -> {
                taxonomy.roleBranches().forEach(b -> out.println(b.name().value() + (b.functional() ? "   (functional)" : "")));
                taxonomy.roles().forEach(r -> out.println(r.name().value() + (new Casting(Root.INSTANCE, r).functional() ? "   (functional)" : "")));
            }
            case "states" -> tokens.states().forEach(out::println);
            case "targets" -> tokens.targets().forEach(out::println);
            default -> { return usage(err, "--list nodes, roles, states or targets"); }
        }
        return 0;
    }

    /** The theme's sheet, flattened, to the file named or the output; what is in it, and every look nothing answers. */
    private int flatten(Taxonomy taxonomy, Dressed theme, String outFile, PrintStream out, PrintStream err) {
        var flat = FlattenSheet.INSTANCE.flatten(taxonomy, theme, theme.toString());
        PrintStream said = outFile == null ? err : out;   // the sheet on the output, what is in it beside
        if (outFile == null) out.print(flat.css());
        else {
            try { Files.writeString(Path.of(outFile), flat.css(), StandardCharsets.UTF_8); }
            catch (IOException e) { throw new IllegalArgumentException("cannot write " + outFile + " (" + e.getClass().getSimpleName() + ")", e); }
            said.println("wrote    " + outFile);
        }
        said.println("looks    " + flat.looks() + " rules, treatments " + flat.treatments() + " rules; " + flat.copied() + " copied from the node that answers");
        if (!flat.unanswered().isEmpty()) {
            said.println("UNANSWERED " + flat.unanswered().size() + " - a theme answers every target at the root:");
            flat.unanswered().stream().limit(20).forEach(u -> said.println("         " + u));
            if (flat.unanswered().size() > 20) said.println("         …");
        }
        return flat.unanswered().isEmpty() ? 0 : 1;
    }

    /** The {@code INSTANCE} of the class named, as the kind asked for. */
    private <K> K instance(String className, Class<K> kind) {
        try {
            Object found = Class.forName(className).getField("INSTANCE").get(null);
            if (!kind.isInstance(found)) throw new IllegalArgumentException(className + " is no " + kind.getSimpleName());
            return kind.cast(found);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("no " + kind.getSimpleName() + " " + className + ": " + e.getMessage(), e);
        }
    }

    /** The taxonomy of the component modules named, each by its class: their components, their parts with them. */
    private Taxonomy taxonomy(String classNames) {
        var modules = new ArrayList<ComponentModule>();
        for (String c : classNames.split(",")) modules.add(instance(c.strip(), ComponentModule.class));
        return ComponentModules.INSTANCE.read(modules, List.of(), List.of());
    }

    private int usage(PrintStream err, String why) {
        err.println("resolve: " + why);
        err.println(USAGE);
        return 2;
    }
}
