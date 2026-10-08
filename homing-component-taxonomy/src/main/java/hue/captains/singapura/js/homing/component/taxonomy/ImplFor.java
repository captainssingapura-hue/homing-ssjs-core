package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * The implementation of one component: a leaf of the taxonomy, realized by the module's one
 * class, named as the leaf. One leaf, one module, one class - and the module's imports of other
 * components are the parts the leaf declares, no more and no fewer.
 *
 * <p>A marker in practice: the leaf is the type argument, read off the declaration - its
 * {@code INSTANCE}, as every node of the tree has one. A declaration that cannot be read so
 * overrides {@link #leaf()}.</p>
 *
 * @param <C> the leaf it implements
 */
public non-sealed interface ImplFor<C extends Component<?>> extends Impl {
    /** The leaf: the type argument's {@code INSTANCE}. */
    @SuppressWarnings("unchecked")
    default C leaf() { return (C) TypeArgument.INSTANCE.of(getClass(), ImplFor.class); }

    @Override default Taxon anchor() { return leaf(); }
}
