package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A rule the central manager holds an instance's registrations to, across jars (RFC 0067, appendix The Style Jars):
 * what no jar's own check can see. The set is extensible - an instance is made with whichever rules it is given,
 * {@link #DEFAULT} unless it says.
 */
public interface StyleRule extends StatelessFunctionalObject {

    /** What refuses the instance, each said whole; none when it holds. */
    List<String> check(StyleManager manager);

    /**
     * The rules an instance is held to unless it says: one implementation per casting, Base's for every one, every style
     * provided for registered, every casting within the components' domain. (Every style's scheme read is no rule: the
     * manager reads its styles' schemes, and only those - D47.)
     */
    List<StyleRule> DEFAULT = List.of(new OnePerCasting(), new StylesProvidedRegistered(), new BaseForEveryCasting(), new WithinDomain());

    /**
     * A style is a function of the components, and the components registered are its domain: every casting a registered
     * jar implements is a casting of the taxonomy the instance registers. One outside it - a component whose module the
     * instance leaves out, or one no taxonomy can make - is refused, each said whole. Checked apart from the taxonomy,
     * which knows nothing of styles.
     */
    record WithinDomain() implements StyleRule {
        @Override public List<String> check(StyleManager manager) {
            var problems = new ArrayList<String>();
            var domain = new HashSet<>(manager.taxonomy().castings());
            for (StyleRegistration r : manager.registrations())
                for (StyleDeclaration jar : r.jars())
                    for (StyleDeclaration.Provided<?, ?> p : jar.provided()) {
                        if (p.style().getClass() != r.style().getClass()) continue;
                        for (CastingStyle<?, ?, ?, ?> i : p.castings()) {
                            var casting = StyleManager.casting(i);
                            if (casting.isEmpty())
                                problems.add(r.style().token() + ": " + i.getClass().getName() + " implements no casting the taxonomy can make");
                            else if (!domain.contains(casting.get()))
                                problems.add(r.style().token() + ": " + jar.getClass().getSimpleName() + " implements " + casting.get().token()
                                        + ", which no component registered has: a style is a function of the components, their registry its domain");
                        }
                    }
            return problems;
        }
    }

    /** No two implementations of one (style, casting), across every jar registered for the style. */
    record OnePerCasting() implements StyleRule {
        @Override public List<String> check(StyleManager manager) {
            var problems = new ArrayList<String>();
            for (StyleRegistration r : manager.registrations())
                for (Map.Entry<Casting, List<CastingStyle<?, ?, ?, ?>>> e : manager.implementations(r.style()).entrySet())
                    if (e.getValue().size() > 1)
                        problems.add(r.style().token() + " has " + e.getValue().size() + " implementations of " + e.getKey().token() + ": "
                                + String.join(", ", e.getValue().stream().map(i -> i.getClass().getName()).toList())
                                + " - one implementation of a casting in a style");
            return problems;
        }
    }

    /**
     * Every style registered once; each jar registered for a style provides for it; and every style a registered jar
     * provides for is registered - a jar's implementations of a style the instance leaves out would be served by no one.
     */
    record StylesProvidedRegistered() implements StyleRule {
        @Override public List<String> check(StyleManager manager) {
            var problems = new ArrayList<String>();
            var registered = new HashSet<Class<?>>();
            for (StyleRegistration r : manager.registrations())
                if (!registered.add(r.style().getClass())) problems.add(r.style().token() + " is registered twice: a style is registered once, with all its jars");
            for (StyleRegistration r : manager.registrations()) {
                Set<StyleDeclaration> jars = new HashSet<>();
                for (StyleDeclaration jar : r.jars()) {
                    if (!jars.add(jar)) problems.add(jar.getClass().getName() + " is registered for " + r.style().token() + " twice");
                    boolean provides = false;
                    for (StyleDeclaration.Provided<?, ?> p : jar.provided()) {
                        if (p.style().getClass() == r.style().getClass()) provides = true;
                        else if (!registered.contains(p.style().getClass()))
                            problems.add(jar.getClass().getName() + " provides for " + p.style().token() + ", which the instance does not register: "
                                    + "every style a registered jar provides for is registered");
                    }
                    if (!provides) problems.add(jar.getClass().getName() + " is registered for " + r.style().token() + ", and provides nothing for it");
                }
            }
            return problems;
        }
    }

    /**
     * Every style's base layer registered, and implementing every casting of the taxonomy (D26): what a style leaves
     * absent is the base layer's, so the base layer leaves nothing.
     */
    record BaseForEveryCasting() implements StyleRule {
        @Override public List<String> check(StyleManager manager) {
            var problems = new ArrayList<String>();
            var bases = new ArrayList<DesignStyle<?>>();
            for (StyleRegistration r : manager.registrations()) {
                DesignStyle<?> base = StyleManager.baseLayer(r.style()).orElse(null);
                if (base == null) {
                    problems.add(r.style().token() + " derives, in the end, from itself");
                    continue;
                }
                if (manager.registrations().stream().noneMatch(o -> o.style().getClass() == base.getClass()))
                    problems.add(r.style().token() + "'s base layer, " + base.token() + ", is not registered: what it leaves absent is its base layer's");
                else if (bases.stream().noneMatch(b -> b.getClass() == base.getClass())) bases.add(base);
            }
            for (DesignStyle<?> base : bases) {
                Map<Casting, List<CastingStyle<?, ?, ?, ?>>> own = manager.implementations(base);
                var missing = new ArrayList<String>();
                for (Casting c : manager.taxonomy().castings()) if (!own.containsKey(c)) missing.add(c.token());
                if (!missing.isEmpty())
                    problems.add(base.token() + ", a base layer, implements none of " + String.join(", ", missing)
                            + ": a base layer implements every casting, a component's jar its components' (D26)");
            }
            return problems;
        }
    }
}
