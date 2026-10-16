package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModules;
import hue.captains.singapura.js.homing.component.taxonomy.EventType;
import hue.captains.singapura.js.homing.component.taxonomy.Role;
import hue.captains.singapura.js.homing.component.taxonomy.StateType;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.Derives;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Builds an instance's styles (RFC 0067, D50), in stages: the base modules first - the components, each with its look in
 * the base, which are at once the components taxonomy and the base layer - then each style over the base, with its
 * modules, then the manager. Each step refuses at once, with every problem it sees, what it can already tell; the
 * builder is immutable, each step a builder of its own.
 *
 * <pre>
 * StyleManager styles = new StyleRegistryBuilder&lt;&gt;(baseModules)
 *         .style(Editorial.INSTANCE, editorialModule)
 *         .style(Brutalism.INSTANCE, brutalismModule)
 *         .build();
 * </pre>
 *
 * @param <Sc> the base's scheme
 * @param <B>  the base layer
 */
public final class StyleRegistryBuilder<Sc extends ColorScheme, B extends DesignStyle<Sc>> {

    private final Taxonomy taxonomy;
    private final B base;
    private final List<BaseModule<?, ?>> baseModules;
    private final Set<Component<?>> topLevel;
    private final Map<DesignStyle<?>, List<StyleModule<?, ?>>> styles;
    private final Map<Class<?>, Map<Casting, CastingStyle<?, ?, ?, ?>>> looks;

    /** The base modules, the components' words only those they name. */
    public StyleRegistryBuilder(List<? extends BaseModule<Sc, B>> baseModules) { this(baseModules, List.of(), List.of(), List.of()); }

    /**
     * The base modules, with the instance's words - its catalogue of roles and its trees of states and events, whole
     * whatever is registered ({@link ComponentModules}): refused when there are no modules; when they are not all of one
     * base, or the base derives; when the components cannot be read - a module twice, a component listed by two; when a
     * module bundles a component it does not list, or one twice; when a component's bundle misses or exceeds its design;
     * when one casting is given two looks; when a component needs a look in the base and has none.
     */
    public StyleRegistryBuilder(List<? extends BaseModule<Sc, B>> baseModules, List<? extends Role<?>> roles,
                                List<? extends StateType<?>> states, List<? extends EventType<?>> events) {
        var problems = new ArrayList<String>();
        if (baseModules.isEmpty()) refuse(List.of("it registers no base module: the base layer gives every component its look"));
        B b = baseModules.get(0).style();
        for (BaseModule<Sc, B> m : baseModules)
            if (m.style().getClass() != b.getClass())
                problems.add(name(m) + "'s looks are " + m.style().token() + "'s, and the base is " + b.token() + ": an instance has one base");
        if (b instanceof Derives<?, ?> d) problems.add("the base, " + b.token() + ", is written over " + d.base().token() + ": a base derives from nothing");
        Taxonomy t;
        try {
            t = ComponentModules.INSTANCE.read(baseModules.stream().map(BaseModule::components).toList(), roles, states, events);
        } catch (RuntimeException e) {
            problems.add(e.getMessage());
            refuse(problems);
            throw e;
        }
        var top = new LinkedHashSet<Component<?>>();
        for (BaseModule<Sc, B> m : baseModules) top.addAll(m.components().components());
        var given = new LinkedHashMap<Casting, CastingStyle<?, ?, ?, ?>>();
        for (BaseModule<Sc, B> m : baseModules) {
            List<? extends Component<?>> listed = m.components().components();
            var bundled = new HashSet<Component<?>>();
            for (StyleBundle<Sc, B> bundle : m.bundles()) {
                if (!listed.contains(bundle.component()))
                    problems.add(name(m) + " bundles " + bundle.component().token() + ", which it does not list: a module bundles its own components");
                if (!bundled.add(bundle.component())) problems.add(name(m) + " bundles " + bundle.component().token() + " twice");
                Set<Casting> design = Bundles.INSTANCE.design(t, bundle.component(), top);
                Set<Casting> held = looks(bundle, b, design, given, problems);
                var missing = new ArrayList<String>();
                for (Casting c : design) if (!held.contains(c)) missing.add(c.token());
                if (!missing.isEmpty())
                    problems.add(bundle.component().token() + "'s bundle in the base has no look of " + String.join(", ", missing)
                            + ": the base gives every casting of a component's design");
            }
            for (Component<?> c : listed)
                if (!bundled.contains(c) && !Bundles.INSTANCE.design(t, c, top).isEmpty())
                    problems.add(name(m) + " gives " + c.token() + " no look in the base: every component needs one");
        }
        if (!problems.isEmpty()) refuse(problems);
        this.taxonomy = t;
        this.base = b;
        this.baseModules = List.copyOf(baseModules);
        this.topLevel = Collections.unmodifiableSet(top);
        this.styles = Map.of();
        var all = new LinkedHashMap<Class<?>, Map<Casting, CastingStyle<?, ?, ?, ?>>>();
        all.put(b.getClass(), given);
        this.looks = all;
    }

    private StyleRegistryBuilder(StyleRegistryBuilder<Sc, B> before, DesignStyle<?> style, List<StyleModule<?, ?>> modules,
                                 Map<Casting, CastingStyle<?, ?, ?, ?>> given) {
        this.taxonomy = before.taxonomy;
        this.base = before.base;
        this.baseModules = before.baseModules;
        this.topLevel = before.topLevel;
        var s = new LinkedHashMap<>(before.styles);
        s.put(style, List.copyOf(modules));
        this.styles = Collections.unmodifiableMap(s);
        var l = new LinkedHashMap<>(before.looks);
        l.put(style.getClass(), given);
        this.looks = Collections.unmodifiableMap(l);
    }

    /**
     * A style over the base, with its modules: refused when it is the base, or registered already; when it is not written
     * over this base; when it comes in no module; when a bundle is for a component not registered, or a second for one
     * component; when a bundle holds a look of no casting of its component's design; when one casting is given two looks.
     */
    @SafeVarargs
    public final <S2 extends ColorScheme, S extends DesignStyle<S2>> StyleRegistryBuilder<Sc, B> style(S style, StyleModule<S2, S>... modules) {
        var problems = new ArrayList<String>();
        if (style.getClass() == base.getClass()) problems.add(style.token() + " is the base: it comes in the base modules");
        if (styles.keySet().stream().anyMatch(s -> s.getClass() == style.getClass())) problems.add(style.token() + " is registered twice");
        Optional<DesignStyle<?>> layer = Bundles.INSTANCE.baseLayer(style);
        if (layer.isEmpty()) problems.add(style.token() + " derives, in the end, from itself");
        else if (layer.get().getClass() != base.getClass())
            problems.add(style.token() + " is written over " + layer.get().token() + ", and the base is " + base.token() + ": a style is written over the instance's base");
        if (modules.length == 0) problems.add(style.token() + " comes in no module");
        var given = new LinkedHashMap<Casting, CastingStyle<?, ?, ?, ?>>();
        var bundled = new HashMap<Component<?>, String>();
        for (StyleModule<S2, S> m : modules) {
            if (m.style().getClass() != style.getClass()) problems.add(name(m) + "'s looks are " + m.style().token() + "'s, not " + style.token() + "'s");
            for (StyleBundle<S2, S> bundle : m.bundles()) {
                Component<?> c = bundle.component();
                if (!topLevel.contains(c)) {
                    problems.add(style.token() + ": " + name(m) + " bundles " + c.token() + ", which no base module lists: a style styles the components registered");
                    continue;
                }
                String before = bundled.putIfAbsent(c, name(m));
                if (before != null) problems.add(style.token() + " has two bundles for " + c.token() + ", in " + before + " and " + name(m)
                        + ": one bundle of a component in a style");
                looks(bundle, style, Bundles.INSTANCE.design(taxonomy, c, topLevel), given, problems);
            }
        }
        if (!problems.isEmpty()) refuse(problems);
        return new StyleRegistryBuilder<>(this, style, List.of(modules), given);
    }

    /** The manager of what is registered. */
    public StyleManager build() {
        return new StyleRegistry(taxonomy, base, baseModules, styles, looks);
    }

    /** A bundle's looks into what is given: each of its design, once, and one look for each casting across the style's bundles. */
    private Set<Casting> looks(StyleBundle<?, ?> bundle, DesignStyle<?> style, Set<Casting> design, Map<Casting, CastingStyle<?, ?, ?, ?>> given,
                               List<String> problems) {
        var held = new LinkedHashSet<Casting>();
        for (CastingStyle<?, ?, ?, ?> look : bundle.looks()) {
            Optional<Casting> casting = Bundles.INSTANCE.casting(look);
            if (casting.isEmpty()) { problems.add(look.getClass().getName() + " implements no casting the taxonomy can make"); continue; }
            Casting c = casting.get();
            if (!design.contains(c)) {
                problems.add(bundle.component().token() + "'s bundle in " + style.token() + " holds " + look.getClass().getName() + ", a look of " + c.token()
                        + ", of no part of " + bundle.component().token() + "'s design");
                continue;
            }
            if (!held.add(c)) problems.add(bundle.component().token() + "'s bundle in " + style.token() + " has two looks of " + c.token());
            CastingStyle<?, ?, ?, ?> before = given.putIfAbsent(c, look);
            if (before != null && before.getClass() != look.getClass())
                problems.add(c.token() + " is given two looks in " + style.token() + ", " + before.getClass().getName() + " and " + look.getClass().getName()
                        + ": a look several components share is one implementation");
        }
        return held;
    }

    private static String name(Object module) {
        return module instanceof BaseModule<?, ?> b ? b.components().getClass().getSimpleName() : module.getClass().getSimpleName();
    }

    private static void refuse(List<String> problems) {
        throw new IllegalArgumentException("the instance's styles are refused:\n  " + String.join("\n  ", problems));
    }
}
