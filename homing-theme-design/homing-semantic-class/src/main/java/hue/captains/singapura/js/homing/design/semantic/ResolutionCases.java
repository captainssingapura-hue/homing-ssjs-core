package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The resolution's cases, run against a resolver - any resolver: the Java one, or the JS one the
 * pages run, so the two answer the same cases or a case says where they part. A cases file names
 * the theme it is in, then a query a line, and the outcome written:
 *
 * <pre>
 * theme sketch
 * danger-button_go   rest      color-surface   => danger-button_go__color-surface from sketch-colours
 * floating-pane      disabled  color-surface   => none
 * danger-button      rest      color-text       => unanswered
 * </pre>
 *
 * The outcome is the class worn and the style or palette that answered it, {@code none} when
 * nothing treats the state, or {@code unanswered} when nothing answers the look. Blank lines and
 * lines from a {@code #} are nothing.
 */
public record ResolutionCases() implements StatelessFunctionalObject {

    public static final ResolutionCases INSTANCE = new ResolutionCases();

    /** A resolver as the cases ask it: by tokens, in the theme named, the outcome's words. */
    public interface Resolver {
        String resolve(String theme, String node, String state, String target);
    }

    /** How many cases ran, and every one whose outcome was not the one written. */
    public record Report(int cases, List<String> failures) implements ValueObject {
        public Report { failures = List.copyOf(failures); }
    }

    public Report run(List<String> lines, Resolver resolver) {
        String theme = null;
        int cases = 0;
        var failures = new ArrayList<String>();
        for (int i = 0; i < lines.size(); i++) {
            String body = lines.get(i).strip();
            if (body.isEmpty() || body.startsWith("#")) continue;
            if (body.startsWith("theme ")) { theme = body.substring("theme ".length()).strip(); continue; }
            String[] sides = body.split("=>");
            String[] q = sides.length == 2 ? sides[0].strip().split("\\s+") : new String[0];
            if (q.length != 3) throw new IllegalArgumentException("line " + (i + 1) + ": a case is a node, a state, a target, '=>' and an outcome");
            if (theme == null) throw new IllegalArgumentException("line " + (i + 1) + ": a case before any 'theme'");
            String expected = sides[1].strip();
            String got;
            try { got = resolver.resolve(theme, q[0], q[1], q[2]); }
            catch (RuntimeException e) { got = "refused: " + e.getMessage(); }
            cases++;
            if (!expected.equals(got)) failures.add("line " + (i + 1) + ": " + sides[0].strip() + " => " + got + ", not " + expected);
        }
        return new Report(cases, failures);
    }

    /** The Java resolver, over a taxonomy, with the themes found by name. */
    public Resolver java(Taxonomy taxonomy, Function<String, Dressed> themes) {
        var tokens = SemanticTokens.INSTANCE;
        return (theme, node, state, target) -> {
            Resolution r = ResolveSemanticClass.INSTANCE.resolve(taxonomy, themes.apply(theme),
                    tokens.castingOf(taxonomy, node), tokens.stateOf(state), tokens.targetOf(target));
            return switch (r.outcome()) {
                case Resolution.Answered a -> a.className() + " from " + a.layer();
                case Resolution.Silent s -> "none";
                case Resolution.Unanswered u -> "unanswered";
            };
        };
    }
}
