package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentNode;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Taxon;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomySource;
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
 * resolve --taxonomy CLASS --style CLASS --palette CLASS NODE STATE TARGET   the walk, step by step
 * resolve --taxonomy CLASS --style CLASS --palette CLASS NODE STATE|* TARGET|*   a table: every state, every target
 * resolve --taxonomy CLASS --style CLASS --palette CLASS --flatten [--out CSS]   the theme's sheet, flattened
 * resolve --taxonomy CLASS --list nodes|roles|states|targets   the tokens there are
 * </pre>
 *
 * Each {@code CLASS} names a type by its class, whose {@code INSTANCE} is read: a
 * {@link TaxonomySource}, a {@link DesignStyle}, a {@link ColorPalette}; {@code NODE} is a casting's token -
 * {@code danger-button}, {@code danger-button_go}. The exit is 0 when every look resolves, 1 when
 * one is unanswered, 2 when the command is wrong.
 */
public record ResolveCli() implements StatelessFunctionalObject {

    public static final ResolveCli INSTANCE = new ResolveCli();

    public static void main(String[] args) {
        System.exit(INSTANCE.run(List.of(args), System.out, System.err));
    }

    private static final String USAGE = String.join("\n",
            "usage: resolve --taxonomy CLASS --style CLASS --palette CLASS NODE STATE TARGET",
            "       resolve --taxonomy CLASS --style CLASS --palette CLASS NODE STATE|* TARGET|*",
            "       resolve --taxonomy CLASS --style CLASS --palette CLASS --flatten [--out CSS]",
            "       resolve --taxonomy CLASS --list nodes|roles|states|targets");

    /** Runs a command; the exit code. */
    public int run(List<String> args, PrintStream out, PrintStream err) {
        String taxonomyClass = null, styleClass = null, paletteClass = null, list = null, outFile = null;
        boolean flatten = false;
        var rest = new ArrayList<String>();
        for (int i = 0; i < args.size(); i++) {
            String a = args.get(i);
            switch (a) {
                case "--taxonomy" -> taxonomyClass = i + 1 < args.size() ? args.get(++i) : null;
                case "--style" -> styleClass = i + 1 < args.size() ? args.get(++i) : null;
                case "--palette" -> paletteClass = i + 1 < args.size() ? args.get(++i) : null;
                case "--list" -> list = i + 1 < args.size() ? args.get(++i) : null;
                case "--out" -> outFile = i + 1 < args.size() ? args.get(++i) : null;
                case "--flatten" -> flatten = true;
                default -> rest.add(a);
            }
        }
        if (taxonomyClass == null) return usage(err, "no --taxonomy");
        try {
            Taxonomy taxonomy = taxonomy(taxonomyClass);
            if (list != null) return list(taxonomy, list, out, err);
            if (styleClass == null || paletteClass == null) return usage(err, "no --style or no --palette");
            Dressed theme = new Dressed(instance(styleClass, DesignStyle.class), instance(paletteClass, ColorPalette.class));
            if (flatten) return flatten(taxonomy, theme, outFile, out, err);
            if (rest.size() != 3) return usage(err, "a node, a state and a target");
            var tokens = SemanticTokens.INSTANCE;
            Casting node = tokens.castingOf(taxonomy, rest.get(0));
            List<State> states = "*".equals(rest.get(1)) ? List.of(State.values()) : List.of(tokens.stateOf(rest.get(1)));
            List<Target> targets = "*".equals(rest.get(2)) ? Target.leaves() : List.of(tokens.targetOf(rest.get(2)));
            var resolver = ResolveSemanticClass.INSTANCE;
            if (states.size() == 1 && targets.size() == 1) {
                Resolution r = resolver.resolve(taxonomy, theme, node, states.get(0), targets.get(0));
                r.explain().forEach(out::println);
                return r.outcome() instanceof Resolution.Unanswered ? 1 : 0;
            }
            return table(taxonomy, theme, node, states, targets, out);
        } catch (IllegalArgumentException | IllegalStateException e) {
            err.println("resolve: " + e.getMessage());
            return 2;
        }
    }

    /** One row per state and target: the class worn, the node that answered, how far up. */
    private static int table(Taxonomy taxonomy, Dressed theme, Casting node, List<State> states, List<Target> targets, PrintStream out) {
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

    private static int list(Taxonomy taxonomy, String what, PrintStream out, PrintStream err) {
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
    private static int flatten(Taxonomy taxonomy, Dressed theme, String outFile, PrintStream out, PrintStream err) {
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
    private static <K> K instance(String className, Class<K> kind) {
        try {
            Object found = Class.forName(className).getField("INSTANCE").get(null);
            if (!kind.isInstance(found)) throw new IllegalArgumentException(className + " is no " + kind.getSimpleName());
            return kind.cast(found);
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("no " + kind.getSimpleName() + " " + className + ": " + e.getMessage(), e);
        }
    }

    private static Taxonomy taxonomy(String className) {
        try {
            Object source = Class.forName(className).getField("INSTANCE").get(null);
            if (!(source instanceof TaxonomySource s)) throw new IllegalArgumentException(className + " is no TaxonomySource");
            return s.read();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("no taxonomy " + className + ": " + e.getMessage(), e);
        }
    }

    private static int usage(PrintStream err, String why) {
        err.println("resolve: " + why);
        err.println(USAGE);
        return 2;
    }
}
