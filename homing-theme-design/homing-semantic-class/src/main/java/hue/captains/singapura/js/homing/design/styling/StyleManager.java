package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.js.homing.design.local.LocalStyle;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * What an instance's styles answer (RFC 0067, appendix The Style Jars, D50): the operational interface of the central
 * style manager. Its components are its base modules' - each with its look in the base layer - and its styles the
 * base and every style registered over it, each from its modules; it answers, for a style and a casting, the
 * implementation the style gives, or - absent - its base layer's, follows a delegation to the CSS it reaches, and
 * aggregates a style's sheet and its family files. It cares nothing for colours: a style's CSS is held to its scheme
 * when its jar is built. Made by {@link StyleRegistryBuilder}, which holds what is registered to the rules as it goes.
 */
public interface StyleManager {

    /** The components taxonomy: the base modules' components, their parts with them. */
    Taxonomy taxonomy();

    /** The base layer: every casting's last answer. */
    DesignStyle<?> base();

    /** The local styles its base modules register, in the order registered: each complete over its branch, a branch the taxonomy has. */
    List<LocalStyle<?, ?>> localStyles();

    /** Every style it holds, the base first, then each in the order registered. */
    List<DesignStyle<?>> styles();

    /** A style it holds, by its token. */
    Optional<DesignStyle<?>> style(String token);

    /** The modules a style came from: the base modules for the base, a style's own for a style. */
    List<? extends StyleModule<?, ?>> modules(DesignStyle<?> style);

    /** Every implementation a style gives itself, by the casting it implements: one for each casting. */
    Map<Casting, CastingStyle<?, ?, ?, ?>> implementations(DesignStyle<?> style);

    /** A style's own implementation of a casting, if it gives one. */
    Optional<CastingStyle<?, ?, ?, ?>> implementation(DesignStyle<?> style, Casting casting);

    /**
     * How a style draws a casting.
     *
     * @param implementation the implementation answering - the style's own, or its base layer's where the style's is absent
     * @param css            the implementation of CSS it reaches, following delegations
     * @param says           whose it is, as the sheet says it
     */
    record Resolved(CastingStyle<?, ?, ?, ?> implementation, CastingStyle<?, ?, ?, ?> css, String says) implements ValueObject {}

    /** How a style draws a casting: its own implementation, else its base layer's; a delegation followed to CSS. */
    Resolved resolve(DesignStyle<?> style, Casting casting);

    /** A casting's rule as a style serves it, each casting's CSS as the reader finds it - in dev mode, on the file system. */
    String rule(DesignStyle<?> style, Casting casting, ResourceReader reader);

    /** A casting's rule as a style serves it, from the classpath. */
    default String rule(DesignStyle<?> style, Casting casting) { return rule(style, casting, ResourceReader.INSTANCE); }

    /** A style's sheet: one class for every casting of the taxonomy, in the taxonomy's order. */
    String sheet(DesignStyle<?> style);

    /** A style's family files, aggregated for reading only: each family head's file, in the taxonomy's order. */
    Map<String, String> families(DesignStyle<?> style, ResourceReader reader);

    /** A style's family files, from the classpath. */
    default Map<String, String> families(DesignStyle<?> style) { return families(style, ResourceReader.INSTANCE); }

    /** A style's file of one family, for reading only. */
    String family(DesignStyle<?> style, ComponentBranch head, ResourceReader reader);

    /** The casting an implementation's types name - its component's, in its role - if the taxonomy can make it. */
    Optional<Casting> casting(CastingStyle<?, ?, ?, ?> implementation);

    /** The base layer a style's absence lands on: the style it derives from, and so up, to one that derives from nothing. */
    Optional<DesignStyle<?>> baseLayer(DesignStyle<?> style);
}
