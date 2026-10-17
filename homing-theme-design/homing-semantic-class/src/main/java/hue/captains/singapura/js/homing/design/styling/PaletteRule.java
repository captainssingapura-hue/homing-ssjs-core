package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.ColorPalette;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.SchemeRegistry;
import hue.captains.singapura.js.homing.design.semantic.SchemeTrees;
import hue.captains.singapura.js.homing.design.semantic.Validation;
import hue.captains.singapura.js.homing.design.semantic.Validations;
import hue.captains.singapura.js.homing.design.styles.PaletteDeclaration;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * A rule an instance's registry of palettes is held to (RFC 0067, D47, D49): a function of the jars registered and the
 * instance's schemes, saying what it finds. The registry is made only when every rule finds nothing
 * ({@link PaletteRegistry}); the rules are applied apart, every problem of every one kept.
 */
public interface PaletteRule extends StatelessFunctionalObject {

    /** What the rule finds of the jars registered, over the instance's schemes. */
    Validation check(List<PaletteDeclaration> jars, SchemeRegistry schemes);

    /** The rules a registry of palettes is held to, applied each apart. */
    List<PaletteRule> RULES = List.of(new RegistersSome(), new NoJarTwice(), new OneTokenEach(), new EachSaysItsScheme(),
            new OfARegisteredScheme(), new WholeOverItsScheme(), new SittingOnRegistered());

    /** It registers a palette. */
    record RegistersSome() implements PaletteRule {
        @Override public Validation check(List<PaletteDeclaration> jars, SchemeRegistry schemes) {
            return Validations.INSTANCE.of(RegisteredPalettes.INSTANCE.palettes(jars).isEmpty() ? List.of("it registers no palette") : List.of());
        }
    }

    /** No jar registered twice. */
    record NoJarTwice() implements PaletteRule {
        @Override public Validation check(List<PaletteDeclaration> jars, SchemeRegistry schemes) {
            var problems = new ArrayList<String>();
            var seen = new HashSet<Class<?>>();
            for (PaletteDeclaration d : jars) if (!seen.add(d.getClass())) problems.add(d.getClass().getName() + " is registered twice");
            return Validations.INSTANCE.of(problems);
        }
    }

    /** No two palettes of one token. */
    record OneTokenEach() implements PaletteRule {
        @Override public Validation check(List<PaletteDeclaration> jars, SchemeRegistry schemes) {
            var problems = new ArrayList<String>();
            var tokens = new HashSet<String>();
            for (ColorPalette<?> p : RegisteredPalettes.INSTANCE.palettes(jars)) if (!tokens.add(p.token())) problems.add("two palettes are " + p.token());
            return Validations.INSTANCE.of(problems);
        }
    }

    /** Each palette says its scheme. */
    record EachSaysItsScheme() implements PaletteRule {
        @Override public Validation check(List<PaletteDeclaration> jars, SchemeRegistry schemes) {
            var problems = new ArrayList<String>();
            for (ColorPalette<?> p : RegisteredPalettes.INSTANCE.palettes(jars))
                if (p.scheme() == null) problems.add(p.token() + " says no scheme: a palette says the scheme it implements");
            return Validations.INSTANCE.of(problems);
        }
    }

    /** Each palette of a scheme the instance registers. */
    record OfARegisteredScheme() implements PaletteRule {
        @Override public Validation check(List<PaletteDeclaration> jars, SchemeRegistry schemes) {
            var problems = new ArrayList<String>();
            for (ColorPalette<?> p : RegisteredPalettes.INSTANCE.palettes(jars)) {
                ColorScheme scheme = p.scheme();
                if (scheme != null && SchemeTrees.INSTANCE.find(schemes, scheme).isEmpty())
                    problems.add(p.token() + " is " + scheme.name() + "'s, which the instance does not register: a palette's scheme is registered with the instance");
            }
            return Validations.INSTANCE.of(problems);
        }
    }

    /** Each palette whole over its scheme's colours, and a chain down the schemes. */
    record WholeOverItsScheme() implements PaletteRule {
        @Override public Validation check(List<PaletteDeclaration> jars, SchemeRegistry schemes) {
            var problems = new ArrayList<String>();
            for (ColorPalette<?> p : RegisteredPalettes.INSTANCE.palettes(jars)) {
                Optional<ColorTree> tree = SchemeTrees.INSTANCE.find(schemes, p.scheme());
                tree.ifPresent(t -> problems.addAll(t.check(p)));   // a palette of no scheme, or of one not registered: another rule's to say
            }
            return Validations.INSTANCE.of(problems);
        }
    }

    /** Each palette sitting on palettes registered too. */
    record SittingOnRegistered() implements PaletteRule {
        @Override public Validation check(List<PaletteDeclaration> jars, SchemeRegistry schemes) {
            var problems = new ArrayList<String>();
            List<ColorPalette<?>> palettes = RegisteredPalettes.INSTANCE.palettes(jars);
            for (ColorPalette<?> p : palettes)
                for (Optional<? extends ColorPalette<?>> b = p.below(); b.isPresent(); b = b.get().below())
                    if (!palettes.contains(b.get())) { problems.add(p.token() + " sits on " + b.get().token() + ", which is not registered"); break; }
            return Validations.INSTANCE.of(problems);
        }
    }
}
