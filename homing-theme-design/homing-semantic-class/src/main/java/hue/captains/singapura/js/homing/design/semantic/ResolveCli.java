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
 * The resolution on the command line: a node, a state and a target walked under a theme's answers,
 * and how the walk went.
 *
 * <pre>
 * resolve --taxonomy CLASS --answers FILE NODE STATE TARGET   the walk, step by step
 * resolve --taxonomy CLASS --answers FILE NODE STATE|* TARGET|*   a table: every state, every target
 * resolve --taxonomy CLASS --list nodes|roles|states|targets   the tokens there are
 * </pre>
 *
 * {@code CLASS} names a {@link TaxonomySource} by its class, whose {@code INSTANCE} is read;
 * {@code FILE} is a theme's answers ({@link ReadAnswers}); {@code NODE} is a casting's token -
 * {@code danger-button}, {@code danger-button_go}. The exit is 0 when every look resolves, 1 when
 * one is unanswered, 2 when the command is wrong.
 */
public record ResolveCli() implements StatelessFunctionalObject {

    public static final ResolveCli INSTANCE = new ResolveCli();

    public static void main(String[] args) {
        System.exit(INSTANCE.run(List.of(args), System.out, System.err));
    }

    private static final String USAGE = String.join("\n",
            "usage: resolve --taxonomy CLASS --answers FILE NODE STATE TARGET",
            "       resolve --taxonomy CLASS --answers FILE NODE STATE|* TARGET|*",
            "       resolve --taxonomy CLASS --list nodes|roles|states|targets");

    /** Runs a command; the exit code. */
    public int run(List<String> args, PrintStream out, PrintStream err) {
        String taxonomyClass = null, answersFile = null, list = null;
        var rest = new ArrayList<String>();
        for (int i = 0; i < args.size(); i++) {
            String a = args.get(i);
            switch (a) {
                case "--taxonomy" -> taxonomyClass = i + 1 < args.size() ? args.get(++i) : null;
                case "--answers" -> answersFile = i + 1 < args.size() ? args.get(++i) : null;
                case "--list" -> list = i + 1 < args.size() ? args.get(++i) : null;
                default -> rest.add(a);
            }
        }
        if (taxonomyClass == null) return usage(err, "no --taxonomy");
        try {
            Taxonomy taxonomy = taxonomy(taxonomyClass);
            if (list != null) return list(taxonomy, list, out, err);
            if (answersFile == null) return usage(err, "no --answers");
            if (rest.size() != 3) return usage(err, "a node, a state and a target");
            AnswerSet answers = ReadAnswers.INSTANCE.read(taxonomy, lines(answersFile));
            var tokens = SemanticTokens.INSTANCE;
            Casting node = tokens.castingOf(taxonomy, rest.get(0));
            List<State> states = "*".equals(rest.get(1)) ? List.of(State.values()) : List.of(tokens.stateOf(rest.get(1)));
            List<Target> targets = "*".equals(rest.get(2)) ? Target.leaves() : List.of(tokens.targetOf(rest.get(2)));
            var resolver = ResolveSemanticClass.INSTANCE;
            if (states.size() == 1 && targets.size() == 1) {
                Resolution r = resolver.resolve(taxonomy, answers, node, states.get(0), targets.get(0));
                r.explain().forEach(out::println);
                return r.outcome() instanceof Resolution.Unanswered ? 1 : 0;
            }
            return table(taxonomy, answers, node, states, targets, out);
        } catch (IllegalArgumentException | IllegalStateException e) {
            err.println("resolve: " + e.getMessage());
            return 2;
        }
    }

    /** One row per state and target: the class worn, the node that answered, how far up. */
    private static int table(Taxonomy taxonomy, Answers answers, Casting node, List<State> states, List<Target> targets, PrintStream out) {
        var tokens = SemanticTokens.INSTANCE;
        int unanswered = 0;
        out.println("node     " + tokens.casting(node) + "  (" + node + ")");
        for (State s : states)
            for (Target t : targets) {
                Resolution r = ResolveSemanticClass.INSTANCE.resolve(taxonomy, answers, node, s, t);
                String what = switch (r.outcome()) {
                    case Resolution.Answered a -> a.className() + "   (" + (r.steps().size() - 1) + " up)";
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

    private static List<String> lines(String file) {
        try {
            return Files.readAllLines(Path.of(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalArgumentException("cannot read the answers " + file + " (" + e.getClass().getSimpleName() + ")", e);
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
