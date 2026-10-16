package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A rule an instance's registry of schemes is held to (RFC 0067, D49): a function of the declarations registered,
 * saying what it finds. The registry is made only when every rule finds nothing ({@link SchemeRegistry}); the rules are
 * applied apart, every problem of every one kept ({@link Validation}).
 */
public interface SchemeRule extends StatelessFunctionalObject {

    /** What the rule finds of the declarations registered. */
    Validation check(List<SchemeDeclaration<?>> declarations);

    /** The rules a registry of schemes is held to, applied each apart. */
    List<SchemeRule> RULES = List.of(new RegistersSome(), new EachSaysItsScheme(), new OnePerScheme(), new SchemesBelowRegistered(),
            new EveryTreeReads());

    /** What a list of problems comes to: valid when none. */
    private static Validation of(List<String> problems) {
        return problems.isEmpty() ? Validation.Valid.INSTANCE : new Validation.Invalid(problems);
    }

    /** It registers a scheme. */
    record RegistersSome() implements SchemeRule {
        @Override public Validation check(List<SchemeDeclaration<?>> declarations) {
            return of(declarations.isEmpty() ? List.of("it registers no scheme") : List.of());
        }
    }

    /** Each declaration says its scheme. */
    record EachSaysItsScheme() implements SchemeRule {
        @Override public Validation check(List<SchemeDeclaration<?>> declarations) {
            var problems = new ArrayList<String>();
            for (SchemeDeclaration<?> d : declarations) if (d.scheme() == null) problems.add(d.getClass().getName() + " declares no scheme");
            return of(problems);
        }
    }

    /** A scheme has one declaration. */
    record OnePerScheme() implements SchemeRule {
        @Override public Validation check(List<SchemeDeclaration<?>> declarations) {
            var problems = new ArrayList<String>();
            var byScheme = new LinkedHashMap<Class<?>, SchemeDeclaration<?>>();
            for (SchemeDeclaration<?> d : declarations) {
                if (d.scheme() == null) continue;
                SchemeDeclaration<?> before = byScheme.putIfAbsent(d.scheme().getClass(), d);
                if (before != null) problems.add(d.scheme().name() + " is declared twice, by " + before.getClass().getName() + " and "
                        + d.getClass().getName() + ": a scheme has one declaration");
            }
            return of(problems);
        }
    }

    /** Every scheme below a registered one registered with it, and none coming back. */
    record SchemesBelowRegistered() implements SchemeRule {
        @Override public Validation check(List<SchemeDeclaration<?>> declarations) {
            var problems = new ArrayList<String>();
            var trees = SchemeTrees.INSTANCE;
            Map<Class<?>, SchemeDeclaration<?>> byScheme = trees.byScheme(declarations);
            var missing = new HashSet<Class<?>>();
            for (SchemeDeclaration<?> d : byScheme.values()) {
                switch (trees.layers(d.scheme(), byScheme)) {
                    case SchemeTrees.Layers.Found found -> { }
                    case SchemeTrees.Layers.Missing m -> {
                        if (missing.add(m.missing().getClass()))
                            problems.add(m.above().map(a -> a.name() + " is on top of ").orElse("") + m.missing().name()
                                    + ", which no registered declaration declares: a scheme's schemes below are registered with it");
                    }
                    case SchemeTrees.Layers.ComesBack back ->
                            problems.add(d.scheme().name() + "'s schemes below come back to " + back.at().name() + ": a scheme is on top of one below it");
                }
            }
            return of(problems);
        }
    }

    /** Every scheme whose layers are all registered reads: its full tree refused nothing. */
    record EveryTreeReads() implements SchemeRule {
        @Override public Validation check(List<SchemeDeclaration<?>> declarations) {
            var problems = new ArrayList<String>();
            var trees = SchemeTrees.INSTANCE;
            Map<Class<?>, SchemeDeclaration<?>> byScheme = trees.byScheme(declarations);
            for (SchemeDeclaration<?> d : byScheme.values()) {
                ColorScheme scheme = d.scheme();
                if (!(trees.layers(scheme, byScheme) instanceof SchemeTrees.Layers.Found found)) continue;   // another rule's to say
                try { ReadColors.INSTANCE.read(found.declarations()); }
                catch (RefusedColors e) { problems.add(scheme.name() + "'s tree is refused: " + e.getMessage()); }
            }
            return of(problems);
        }
    }
}
