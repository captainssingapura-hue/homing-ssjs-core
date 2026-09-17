package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/**
 * A theme's word on a {@link CssGroup} — RFC 0002's shape, at the grain
 * RFC 0066 gives it. Stateless typed pairing of a group with a {@link Theme};
 * the type parameters carry all identity, the methods give runtime access to
 * the singletons. The framework's CSS-serving action resolves the impl for
 * a {@code (group, theme.slug)} pair and consults it per class.
 *
 * <p>Two kinds of word, one contract:</p>
 * <ul>
 *   <li><b>Provision</b> — a {@link PaletteClass} has no body of its own; the
 *       theme's {@link PaletteProvision} IS its body. Required for every
 *       palette the deployment reaches (the completeness law).</li>
 *   <li><b>Override</b> — a drawn class has a declared body; a method on the
 *       impl named after the class ({@code st_card()} for record
 *       {@code st_card}) returning {@code CssBlock<st_card>} is appended
 *       <em>inside the same rule</em>, after the declared body. The theme says
 *       only what differs — a font, a shadow, a nested {@code &:active} — and
 *       wins by source order within the rule: same layer, same specificity,
 *       no {@code @layer theme}, no {@code !important}. A class the impl has
 *       no method for is untouched. Optional, any number, any tier.</li>
 * </ul>
 *
 * <p>A group declares its override contract as a nested interface so a theme's
 * record names the group once and the compiler checks each block's class:</p>
 * <pre>{@code
 * public record StudioStyles() implements CssGroup<StudioStyles> {
 *     public interface Overrides<TH extends Theme> extends CssGroupImpl<StudioStyles, TH> {
 *         @Override default StudioStyles group() { return INSTANCE; }
 *     }
 * }
 * public record BrutalistStudio() implements StudioStyles.Overrides<HomingBrutalist> {
 *     @Override public HomingBrutalist theme() { return HomingBrutalist.INSTANCE; }
 *     public CssBlock<StudioStyles.st_card> st_card() { return CssBlock.of("""
 *         box-shadow: var(--bru-shadow);
 *         &:active { transform: translate(8px, 8px); box-shadow: none; }
 *         """); }
 * }
 * }</pre>
 *
 * <p>Overrides reach one group. A theme that has something to say about
 * another product's classes — the workspace's buttons — says it in an impl for
 * that group, registered by a module that can see both; the dependency the
 * old string overlay hid becomes a pom edge.</p>
 *
 * @param <CG> the CssGroup this impl speaks for
 * @param <TH> the Theme it speaks for; bound to {@link Theme} (not F-bounded)
 *
 * @see PaletteProvision
 * @see CssGroup
 */
public interface CssGroupImpl<CG extends CssGroup<CG>, TH extends Theme> extends StatelessFunctionalObject {

    /** Identity: which {@link CssGroup} this impl applies to. */
    CG group();

    /** Identity: which {@link Theme} this impl realizes. */
    TH theme();
}
