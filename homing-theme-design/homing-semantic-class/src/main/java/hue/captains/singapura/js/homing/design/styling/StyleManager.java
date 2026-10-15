package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.Extent;
import hue.captains.singapura.js.homing.design.Growth;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.Derives;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.TypeArguments;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The central style manager (RFC 0067, appendix The Style Jars): an instance's styles, registered at bootstrap in two
 * stages - each style with the jars implementing it ({@link StyleRegistration}), then the instance's list of them - and
 * held to the cross-jar rules it is given ({@link StyleRule#DEFAULT} unless it says). Made only when they hold: an
 * instance whose registrations fail them does not start.
 *
 * <p>It answers, for a style and a casting, the implementation the style's own jars give, or - absent - its base
 * layer's; follows a delegation to the CSS it reaches; and aggregates a style's sheet: one class for every casting of the
 * taxonomy, each its implementation's CSS with its marks filled in for the casting served - a delegating casting under
 * its own class. The family files are aggregated the same way, for reading only.</p>
 *
 * @param taxonomy      the castings
 * @param tree          the colour tree
 * @param registrations the styles the instance includes, each with its jars, the default first
 */
public record StyleManager(Taxonomy taxonomy, ColorTree tree, List<StyleRegistration> registrations) implements ValueObject {

    public StyleManager {
        registrations = List.copyOf(registrations);
    }

    /** An instance of its registrations, held to the default rules. */
    public static StyleManager of(Taxonomy taxonomy, ColorTree tree, List<StyleRegistration> registrations) {
        return of(taxonomy, tree, registrations, StyleRule.DEFAULT);
    }

    /** An instance of its registrations, held to the rules it is given; refused with every problem at once. */
    public static StyleManager of(Taxonomy taxonomy, ColorTree tree, List<StyleRegistration> registrations, List<StyleRule> rules) {
        var manager = new StyleManager(taxonomy, tree, registrations);
        var problems = new ArrayList<String>();
        if (registrations.isEmpty()) problems.add("it registers no style");
        for (StyleRule rule : rules) problems.addAll(rule.check(manager));
        if (!problems.isEmpty()) throw new IllegalArgumentException("the instance's styles are refused:\n  " + String.join("\n  ", problems));
        return manager;
    }

    /** The styles the instance includes, the default first. */
    public List<DesignStyle<?>> styles() { return registrations.stream().<DesignStyle<?>>map(StyleRegistration::style).toList(); }

    /** A style the instance includes, by its token. */
    public Optional<DesignStyle<?>> style(String token) { return styles().stream().filter(s -> s.token().equals(token)).findFirst(); }

    /** Every implementation the style's registered jars give, by the casting it implements - two for one casting kept, for the rules to see. */
    public Map<Casting, List<CastingStyle<?, ?, ?, ?>>> implementations(DesignStyle<?> style) {
        var out = new LinkedHashMap<Casting, List<CastingStyle<?, ?, ?, ?>>>();
        for (StyleRegistration r : registrations) {
            if (r.style().getClass() != style.getClass()) continue;
            for (StyleDeclaration jar : r.jars())
                for (StyleDeclaration.Provided<?, ?> p : jar.provided()) {
                    if (p.style().getClass() != style.getClass()) continue;
                    for (CastingStyle<?, ?, ?, ?> i : p.castings())
                        casting(i).ifPresent(c -> out.computeIfAbsent(c, k -> new ArrayList<>()).add(i));
                }
        }
        return out;
    }

    /** The style's own implementation of a casting, if its jars give one. */
    public Optional<CastingStyle<?, ?, ?, ?>> implementation(DesignStyle<?> style, Casting casting) {
        List<CastingStyle<?, ?, ?, ?>> found = implementations(style).get(casting);
        return found == null || found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    /**
     * How a style draws a casting.
     *
     * @param implementation the implementation answering - the style's own, or its base layer's where the style's is absent
     * @param css            the implementation of CSS it reaches, following delegations
     * @param says           whose it is, as the sheet says it
     */
    public record Resolved(CastingStyle<?, ?, ?, ?> implementation, CastingStyle<?, ?, ?, ?> css, String says) implements ValueObject {}

    /** How a style draws a casting: its own implementation, else its base layer's; a delegation followed to CSS. */
    public Resolved resolve(DesignStyle<?> style, Casting casting) {
        DesignStyle<?> base = baseLayer(style).orElseThrow(() -> new IllegalArgumentException(style.token() + " derives, in the end, from itself"));
        Optional<CastingStyle<?, ?, ?, ?>> own = implementation(style, casting);
        CastingStyle<?, ?, ?, ?> answering = own.or(() -> implementation(base, casting)).orElseThrow(() -> new IllegalArgumentException(
                casting.token() + " has no implementation in " + style.token() + " nor its base layer, " + base.token()));
        String whose = own.isPresent() ? style.token() + "'s own" : base.token() + "'s, " + style.token() + " answering absence";
        CastingStyle<?, ?, ?, ?> at = answering;
        var passed = new LinkedHashSet<Class<?>>();
        while (at.style() instanceof StylingRes.Delegation<?, ?> d) {
            if (!passed.add(at.getClass())) throw new IllegalArgumentException(casting.token() + " in " + style.token() + " delegates round to " + at.getClass().getName());
            at = d.to();
        }
        if (at != answering) whose += ", as " + styleOf(at) + "'s " + at.getClass().getSimpleName();
        return new Resolved(answering, at, whose);
    }

    /** A casting's rule as a style serves it: the CSS its implementation reaches, its marks filled in for the casting. */
    public String rule(DesignStyle<?> style, Casting casting) {
        Resolved r = resolve(style, casting);
        String css = CastingCss.INSTANCE.read(r.css().getClass()).orElseThrow(() -> new IllegalArgumentException(
                r.css().getClass().getName() + " answers CssContent, and its jar has no " + CastingCss.INSTANCE.resource(r.css().getClass())));
        return "/* " + casting.token() + ": " + r.says() + " */\n" + CastingCss.INSTANCE.filled(css, casting).strip() + "\n";
    }

    /** A style's sheet: one class for every casting of the taxonomy, in the taxonomy's order. */
    public String sheet(DesignStyle<?> style) {
        var css = new StringBuilder();
        css.append("/* ").append(style.token()).append(" - its style-aware sheet: one class for every casting, each its implementation's CSS, its marks filled in.\n")
           .append("   Aggregated by StyleManager; do not edit. */\n")
           .append(Extent.PROPERTY).append('\n');
        for (Growth g : Growth.values()) css.append(g.property()).append('\n');
        for (Casting c : taxonomy.castings()) css.append('\n').append(rule(style, c));
        return css.toString();
    }

    /** A style's family files, aggregated for reading only: each family head's castings, in its order. */
    public Map<String, String> families(DesignStyle<?> style) {
        var out = new LinkedHashMap<String, String>();
        for (ComponentBranch head : taxonomy.heads()) {
            var css = new StringBuilder();
            for (Casting c : taxonomy.family(head)) css.append(css.isEmpty() ? "" : "\n").append(rule(style, c));
            out.put(head.token(), css.toString());
        }
        return out;
    }

    /** The casting an implementation's types name - its component's, in its role - if the taxonomy can make it. */
    public static Optional<Casting> casting(CastingStyle<?, ?, ?, ?> implementation) {
        List<Class<?>> args = TypeArguments.INSTANCE.of(implementation.getClass(), CastingStyle.class);
        if (args.size() != 4 || args.contains(null)) return Optional.empty();
        return CheckDeclaration.casting(args.get(2), args.get(3));
    }

    /** The token of an implementation's style, read off its type. */
    private static String styleOf(CastingStyle<?, ?, ?, ?> implementation) {
        Class<?> style = TypeArguments.INSTANCE.at(implementation.getClass(), CastingStyle.class, 1);
        return style == null ? "?" : NodeName.ofType(style, "").value();
    }

    /** The base layer a style's absence lands on: the style it derives from, and so up, to one that derives from nothing. */
    public static Optional<DesignStyle<?>> baseLayer(DesignStyle<?> style) {
        var passed = new HashSet<Class<?>>();
        DesignStyle<?> at = style;
        while (at instanceof Derives<?, ?> d) {
            if (!passed.add(at.getClass())) return Optional.empty();
            at = d.base();
        }
        return Optional.of(at);
    }
}
