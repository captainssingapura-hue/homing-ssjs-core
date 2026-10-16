package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModules;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Modules collected from declarations made before modules were (RFC 0067, D50): a collector jar's work - the files stay
 * where they are, each jar's style declaration as it is, and the modules are gathered from them. Every look a style's
 * declarations give is bundled by the top-level component whose design it is of, wherever its file lives; a look of
 * no component given is refused.
 */
public record CollectModules() implements StatelessFunctionalObject {

    public static final CollectModules INSTANCE = new CollectModules();

    /** A base module gathered: a component module, and its components' bundles in the base. */
    public record CollectedBase<Sc extends ColorScheme, B extends DesignStyle<Sc>>(B style, ComponentModule components, List<StyleBundle<Sc, B>> bundles)
            implements BaseModule<Sc, B> {
        public CollectedBase {
            Objects.requireNonNull(style, "CollectedBase.style");
            Objects.requireNonNull(components, "CollectedBase.components");
            bundles = List.copyOf(bundles);
        }
    }

    /** A style's module gathered: its bundles. */
    public record CollectedStyle<Sc extends ColorScheme, S extends DesignStyle<Sc>>(S style, List<StyleBundle<Sc, S>> bundles) implements StyleModule<Sc, S> {
        public CollectedStyle {
            Objects.requireNonNull(style, "CollectedStyle.style");
            bundles = List.copyOf(bundles);
        }
    }

    /**
     * The base modules: one for each component module, its components' looks in the base bundled from every declaration
     * given - a look's file may live in another module's jar, and comes into the bundle of the component whose design it is of.
     */
    public <Sc extends ColorScheme, B extends DesignStyle<Sc>> List<BaseModule<Sc, B>> base(B base, List<? extends ComponentModule> definitions,
                                                                                          List<? extends StyleDeclaration> declarations) {
        Taxonomy taxonomy = taxonomy(definitions);
        Set<Component<?>> top = top(definitions);
        List<CastingStyle<Sc, B, ?, ?>> looks = looks(base, declarations);
        var out = new ArrayList<BaseModule<Sc, B>>();
        for (ComponentModule m : definitions) {
            Bundles.Bundled<Sc, B> b = Bundles.INSTANCE.bundle(taxonomy, m.components(), top, looks);
            out.add(new CollectedBase<>(base, m, b.bundles()));
        }
        refuseOutside(base, Bundles.INSTANCE.bundle(taxonomy, List.copyOf(top), top, looks).outside());
        return List.copyOf(out);
    }

    /** A style's module: its looks from every declaration given, bundled by the top-level components of the definitions. */
    public <Sc extends ColorScheme, S extends DesignStyle<Sc>> StyleModule<Sc, S> style(S style, List<? extends ComponentModule> definitions,
                                                                                      List<? extends StyleDeclaration> declarations) {
        Set<Component<?>> top = top(definitions);
        Bundles.Bundled<Sc, S> b = Bundles.INSTANCE.bundle(taxonomy(definitions), List.copyOf(top), top, looks(style, declarations));
        refuseOutside(style, b.outside());
        return new CollectedStyle<>(style, b.bundles());
    }

    /** A style's looks, typed, from every declaration given: each its implementations of that style. */
    @SuppressWarnings("unchecked")
    public <Sc extends ColorScheme, S extends DesignStyle<Sc>> List<CastingStyle<Sc, S, ?, ?>> looks(S style, List<? extends StyleDeclaration> declarations) {
        var out = new ArrayList<CastingStyle<Sc, S, ?, ?>>();
        for (StyleDeclaration d : declarations)
            for (StyleDeclaration.Provided<?, ?> p : d.provided())
                if (p.style().getClass() == style.getClass()) out.addAll((List<? extends CastingStyle<Sc, S, ?, ?>>) p.castings());
        return out;
    }

    private static Taxonomy taxonomy(List<? extends ComponentModule> definitions) {
        return ComponentModules.INSTANCE.read(definitions, List.of(), List.of(), List.of());
    }

    private static Set<Component<?>> top(List<? extends ComponentModule> definitions) {
        var out = new LinkedHashSet<Component<?>>();
        for (ComponentModule m : definitions) out.addAll(m.components());
        return out;
    }

    private static void refuseOutside(DesignStyle<?> style, List<String> outside) {
        if (!outside.isEmpty())
            throw new IllegalArgumentException(style.token() + "'s looks cannot be collected:\n  " + String.join("\n  ", outside));
    }
}
