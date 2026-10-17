package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * The shared base of the components under one branch: an abstract class, named as the branch,
 * that the leaves' own modules extend - the button's behaviour, which every button leaf takes and
 * only binds its own look to. A branch is never realized, so the module exports no component, and
 * only the modules of leaves under it may import it.
 *
 * <p>A marker in practice, as {@link ImplFor} is: the branch is the type argument's
 * {@code INSTANCE}; a declaration that cannot be read so overrides {@link #branch()}.</p>
 *
 * @param <B> the branch whose leaves it is the base of
 */
public non-sealed interface ImplForBranch<B extends ComponentBranch> extends Impl {
    /** The branch: the type argument's {@code INSTANCE}. */
    @SuppressWarnings("unchecked")
    default B branch() { return (B) TypeArgument.INSTANCE.of(getClass(), ImplForBranch.class); }

    @Override default Taxon anchor() { return branch(); }
}
