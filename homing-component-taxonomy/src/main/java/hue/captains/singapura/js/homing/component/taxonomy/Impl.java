package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * An implementation, anchored at the taxonomy: what a module that implements a component says it
 * implements. The anchor is a node of the tree - a {@link ImplFor leaf}, which the module's one
 * class realizes; a {@link ImplForBranch branch}, whose shared base class the leaves' modules
 * under it extend; or a {@link ImplForLeavesOf branch whose every leaf} the module's one class
 * realizes, made with the leaf, when they differ by their look alone. Nothing else implements a
 * component: a component's code that finds no anchor is refused by the gate that reads the anchors
 * against the taxonomy.
 *
 * <p>Declared on the module's own record, beside the module it is:</p>
 *
 * <pre>{@code
 * public record PrimaryButtonModule() implements DomModule<PrimaryButtonModule>, ImplFor<HouseControls.PrimaryButton> { ... }
 * public record ButtonModule()        implements DomModule<ButtonModule>, ImplForBranch<HouseBranches.Button> { ... }
 * public record IconModule()          implements DomModule<IconModule>, ImplForLeavesOf<HouseBranches.Icon> { ... }
 * }</pre>
 */
public sealed interface Impl permits ImplFor, ImplForBranch, ImplForLeavesOf {
    /** The node it is anchored at. */
    Taxon anchor();
}
