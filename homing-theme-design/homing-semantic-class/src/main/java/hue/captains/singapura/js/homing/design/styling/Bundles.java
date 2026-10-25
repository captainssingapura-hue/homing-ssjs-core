package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.Part;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.LayoutStyling;
import hue.captains.singapura.js.homing.component.taxonomy.VirtualComponent;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.Derives;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.TypeArguments;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * What can be worked out of looks and the components they style (RFC 0067, D50): a top-level component's design - the
 * castings that come with it - looks grouped into bundles by the component whose design each is of, the casting an
 * implementation implements, and the base layer a style's absence lands on. Used by the builder, by the registry it
 * makes, and by whoever collects modules from declarations made before modules were.
 */
public record Bundles() implements StatelessFunctionalObject {

    public static final Bundles INSTANCE = new Bundles();

    /**
     * A top-level component's design: its own casting, each part it casts in a role, and each part no module lists - with
     * that part's own parts, and so down - stopping at the next component a module lists, whose design is its own.
     *
     * @param topLevel the components the modules list
     */
    public Set<Casting> design(Taxonomy taxonomy, Component<?> component, Collection<? extends Component<?>> topLevel) {
        var out = new LinkedHashSet<Casting>();
        if (!(component instanceof VirtualComponent<?>) && !(component instanceof LayoutStyling<?>)) out.add(new Casting(component));   // the layouts' styling has no look of its own
        walk(taxonomy, component, topLevel, out, new HashSet<>());
        return out;
    }

    private static void walk(Taxonomy taxonomy, Component<?> at, Collection<? extends Component<?>> topLevel, Set<Casting> out, Set<Component<?>> passed) {
        if (!passed.add(at)) return;
        for (Part<?, ?> p : taxonomy.partsOf(at))
            for (Component<?> base : p.bases()) {
                var cast = new Casting(base, p.role());
                if (!cast.functional()) out.add(cast);   // a part in a functional role looks as it alone: its own design's
                if (!topLevel.contains(base)) {
                    if (!(base instanceof VirtualComponent<?>)) out.add(new Casting(base));
                    walk(taxonomy, base, topLevel, out, passed);
                }
            }
    }

    /**
     * Looks grouped into bundles.
     *
     * @param bundles a bundle for each component given whose design has a look among them - a look shared by several in
     *                each one's bundle
     * @param outside the looks of no design of the components given, each said
     */
    public record Bundled<Sc extends ColorScheme, S extends DesignStyle<Sc>>(List<StyleBundle<Sc, S>> bundles, List<String> outside) {
        public Bundled { bundles = List.copyOf(bundles); outside = List.copyOf(outside); }
    }

    /**
     * Looks of one style, grouped into bundles by the components whose designs they are of.
     *
     * @param components the components to bundle for, in order: top-level ones
     * @param topLevel   every component the modules list
     */
    public <Sc extends ColorScheme, S extends DesignStyle<Sc>> Bundled<Sc, S> bundle(Taxonomy taxonomy, List<? extends Component<?>> components,
            Collection<? extends Component<?>> topLevel, List<? extends CastingStyle<Sc, S, ?, ?>> looks) {
        var byCasting = new LinkedHashMap<Casting, List<CastingStyle<Sc, S, ?, ?>>>();
        var outside = new ArrayList<String>();
        for (CastingStyle<Sc, S, ?, ?> look : looks) {
            Optional<Casting> c = casting(look);
            if (c.isEmpty()) outside.add(look.getClass().getName() + " implements no casting the taxonomy can make");
            else byCasting.computeIfAbsent(c.get(), k -> new ArrayList<>()).add(look);
        }
        var bundles = new ArrayList<StyleBundle<Sc, S>>();
        var bundled = new HashSet<Casting>();
        for (Component<?> component : components) {
            var mine = new ArrayList<CastingStyle<Sc, S, ?, ?>>();
            for (Casting c : design(taxonomy, component, topLevel)) {
                List<CastingStyle<Sc, S, ?, ?>> found = byCasting.get(c);
                if (found != null) { mine.addAll(found); bundled.add(c); }
            }
            if (!mine.isEmpty()) bundles.add(new StyleBundle<>(component, mine));
        }
        for (var e : byCasting.entrySet())
            if (!bundled.contains(e.getKey()))
                for (CastingStyle<Sc, S, ?, ?> look : e.getValue())
                    outside.add(look.getClass().getName() + " implements " + e.getKey().token() + ", of no component's design given");
        return new Bundled<>(bundles, outside);
    }

    /** The casting an implementation's types name - its component's, in its role - if the taxonomy can make it. */
    public Optional<Casting> casting(CastingStyle<?, ?, ?, ?> implementation) {
        List<Class<?>> args = TypeArguments.INSTANCE.of(implementation.getClass(), CastingStyle.class);
        if (args.size() != 4 || args.contains(null)) return Optional.empty();
        return CheckDeclaration.casting(args.get(2), args.get(3));
    }

    /** The base layer a style's absence lands on: the style it derives from, and so up, to one that derives from nothing. */
    public Optional<DesignStyle<?>> baseLayer(DesignStyle<?> style) {
        var passed = new HashSet<Class<?>>();
        DesignStyle<?> at = style;
        while (at instanceof Derives<?, ?> d) {
            if (!passed.add(at.getClass())) return Optional.empty();
            at = d.base();
        }
        return Optional.of(at);
    }
}
