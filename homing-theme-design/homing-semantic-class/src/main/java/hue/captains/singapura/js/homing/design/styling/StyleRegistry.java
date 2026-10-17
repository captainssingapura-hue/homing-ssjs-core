package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.js.homing.design.Extent;
import hue.captains.singapura.js.homing.design.Growth;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.TypeArguments;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.tree.NodeName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The instance's styles as registered (RFC 0067, D50): the components taxonomy its base modules bring, the base layer,
 * and each style with its modules - and every look each style gives, by its casting. Made only by
 * {@link StyleRegistryBuilder}, which holds every step to the rules; seen only as a {@link StyleManager}.
 *
 * @param taxonomy    the base modules' components, their parts with them
 * @param base        the base layer
 * @param baseModules the base modules: the components, each with its look in the base
 * @param registered  each style registered over the base, with its modules, in the order registered
 * @param looks       every look each style gives - the base's and each style's - by its casting, by the style's class
 */
record StyleRegistry(Taxonomy taxonomy, DesignStyle<?> base, List<BaseModule<?, ?>> baseModules, Map<DesignStyle<?>, List<StyleModule<?, ?>>> registered,
                     Map<Class<?>, Map<Casting, CastingStyle<?, ?, ?, ?>>> looks) implements StyleManager {

    StyleRegistry {
        baseModules = List.copyOf(baseModules);
        registered = Collections.unmodifiableMap(new LinkedHashMap<>(registered));
        var copied = new LinkedHashMap<Class<?>, Map<Casting, CastingStyle<?, ?, ?, ?>>>();
        looks.forEach((k, v) -> copied.put(k, Collections.unmodifiableMap(new LinkedHashMap<>(v))));
        looks = Collections.unmodifiableMap(copied);
    }

    @Override public List<LocalStyle<?, ?>> localStyles() {
        var out = new ArrayList<LocalStyle<?, ?>>();
        for (BaseModule<?, ?> m : baseModules) out.addAll(m.localStyles());
        return List.copyOf(out);
    }

    @Override public List<DesignStyle<?>> styles() {
        var out = new ArrayList<DesignStyle<?>>();
        out.add(base);
        out.addAll(registered.keySet());
        return List.copyOf(out);
    }

    @Override public Optional<DesignStyle<?>> style(String token) { return styles().stream().filter(s -> s.token().equals(token)).findFirst(); }

    @Override public List<? extends StyleModule<?, ?>> modules(DesignStyle<?> style) {
        if (style.getClass() == base.getClass()) return baseModules;
        for (var e : registered.entrySet()) if (e.getKey().getClass() == style.getClass()) return e.getValue();
        return List.of();
    }

    @Override public Map<Casting, CastingStyle<?, ?, ?, ?>> implementations(DesignStyle<?> style) {
        return looks.getOrDefault(style.getClass(), Map.of());
    }

    @Override public Optional<CastingStyle<?, ?, ?, ?>> implementation(DesignStyle<?> style, Casting casting) {
        return Optional.ofNullable(implementations(style).get(casting));
    }

    @Override public Resolved resolve(DesignStyle<?> style, Casting casting) {
        DesignStyle<?> layer = baseLayer(style).orElseThrow(() -> new IllegalArgumentException(style.token() + " derives, in the end, from itself"));
        Optional<CastingStyle<?, ?, ?, ?>> own = implementation(style, casting);
        CastingStyle<?, ?, ?, ?> answering = own.or(() -> implementation(layer, casting)).orElseThrow(() -> new IllegalArgumentException(
                casting.token() + " has no implementation in " + style.token() + " nor its base layer, " + layer.token()));
        String whose = own.isPresent() ? style.token() + "'s own" : layer.token() + "'s, " + style.token() + " answering absence";
        CastingStyle<?, ?, ?, ?> at = answering;
        var passed = new LinkedHashSet<Class<?>>();
        while (at.style() instanceof StylingRes.Delegation<?, ?> d) {
            if (!passed.add(at.getClass())) throw new IllegalArgumentException(casting.token() + " in " + style.token() + " delegates round to " + at.getClass().getName());
            at = d.to();
        }
        if (at != answering) whose += ", as " + styleOf(at) + "'s " + at.getClass().getSimpleName();
        return new Resolved(answering, at, whose);
    }

    @Override public String rule(DesignStyle<?> style, Casting casting, ResourceReader reader) {
        Resolved r = resolve(style, casting);
        String css = CastingCss.INSTANCE.read(r.css().getClass(), reader).orElseThrow(() -> new IllegalArgumentException(
                r.css().getClass().getName() + " answers CssContent, and its jar has no " + CastingCss.INSTANCE.resource(r.css().getClass())));
        return "/* " + casting.token() + ": " + r.says() + " */\n" + CastingCss.INSTANCE.filled(css, casting).strip() + "\n";
    }

    @Override public String sheet(DesignStyle<?> style) {
        var css = new StringBuilder();
        css.append("/* ").append(style.token()).append(" - its style-aware sheet: one class for every casting, each its implementation's CSS, its marks filled in.\n")
           .append("   Aggregated by StyleManager; do not edit. */\n")
           .append(Extent.PROPERTY).append('\n');
        for (Growth g : Growth.values()) css.append(g.property()).append('\n');
        for (Casting c : taxonomy.castings()) css.append('\n').append(rule(style, c));
        return css.toString();
    }

    @Override public Map<String, String> families(DesignStyle<?> style, ResourceReader reader) {
        var out = new LinkedHashMap<String, String>();
        for (ComponentBranch head : taxonomy.heads()) out.put(head.token(), family(style, head, reader));
        return out;
    }

    /**
     * A style's file of one family, for reading only: each of the family's castings as the style serves it, in the
     * family's order, under a line saying whose it is - the style's own, or its base layer's where it leaves the casting.
     * A casting drawn by delegation is that line alone, naming the casting whose rule it takes: its rule is the same.
     */
    @Override public String family(DesignStyle<?> style, ComponentBranch head, ResourceReader reader) {
        var css = new StringBuilder("/* " + style.token() + " - the " + head.token() + " family, as the style serves it: each casting "
                + "its own, or its base layer's. Aggregated by StyleManager, for reading; do not edit. */\n");
        for (Casting c : taxonomy.family(head)) {
            css.append('\n');
            Resolved r = resolve(style, c);
            if (r.css() == r.implementation()) {
                css.append(rule(style, c, reader));
                continue;
            }
            String to = casting(r.css()).map(Casting::token).orElse(r.css().getClass().getName());
            css.append("/* ").append(c.token()).append(": ").append(r.says()).append(" - its rule ").append(to)
               .append("'s, under .").append(LeafClasses.INSTANCE.styled(c)).append(" */\n");
        }
        return css.toString();
    }

    @Override public Optional<Casting> casting(CastingStyle<?, ?, ?, ?> implementation) { return Bundles.INSTANCE.casting(implementation); }

    @Override public Optional<DesignStyle<?>> baseLayer(DesignStyle<?> style) { return Bundles.INSTANCE.baseLayer(style); }

    /** The token of an implementation's style, read off its type. */
    private static String styleOf(CastingStyle<?, ?, ?, ?> implementation) {
        Class<?> style = TypeArguments.INSTANCE.at(implementation.getClass(), CastingStyle.class, 1);
        return style == null ? "?" : NodeName.ofType(style, "").value();
    }
}
