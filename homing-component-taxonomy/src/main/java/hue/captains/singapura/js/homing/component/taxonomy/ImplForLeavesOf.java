package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * The one component that realizes every leaf under a branch whose leaves differ by their look
 * alone - the icons, each a picture its look draws (D52). One class, named as the branch, made with
 * the leaf it realizes, whose look it wears: so an owner can mount any leaf of the branch, one its
 * slot names or one its data picks, those of a module registered after it among them. The leaves
 * hold no parts, since their behaviour is the one class's; any module whose leaf holds a part one
 * of them plays imports it.
 *
 * <p>A marker in practice, as {@link ImplFor} is: the branch is the type argument's
 * {@code INSTANCE}; a declaration that cannot be read so overrides {@link #branch()}.</p>
 *
 * @param <B> the branch whose every leaf it realizes
 */
public non-sealed interface ImplForLeavesOf<B extends ComponentBranch> extends Impl {
    /** The branch: the type argument's {@code INSTANCE}. */
    @SuppressWarnings("unchecked")
    default B branch() { return (B) TypeArgument.INSTANCE.of(getClass(), ImplForLeavesOf.class); }

    @Override default Taxon anchor() { return branch(); }
}
