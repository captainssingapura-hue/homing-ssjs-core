package hue.captains.singapura.js.homing.design;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.function.Function;

/**
 * One design's fulfilment of one design class. Typed to the class —
 * {@code ImplProvider<Feedback.Danger.Color_Surface>} — so a design is a set
 * of these, one per class it has a word for, and a second provider for the
 * same class in the same design is a refusal, not last-wins.
 *
 * <p>A provider may be <b>lazy</b>: it is handed a {@link Resolver} and may
 * ask for the resolved impl of any other class in the same design —
 * {@code Up} as {@code Success}, a theme's danger as the base's one tone
 * darker, a leaf as its branch's default. Whatever it delegates to, what it
 * returns is an {@link Impl}: flat. Delegation lives here in Java; the sheet
 * never sees it, because CSS has no way to say "as that one".</p>
 *
 * @param <D> the design class fulfilled
 */
public interface ImplProvider<D extends DesignClass<?, ?>> extends StatelessFunctionalObject {

    Class<D> designClass();

    Impl provide(Resolver resolver);

    /** What a lazy provider may ask of the design it belongs to. */
    interface Resolver {
        /** The flattened impl of another class in this design; refused on a cycle. */
        Impl resolve(Class<? extends DesignClass<?, ?>> other);

        /** The base design's impl for a class, when this design has one; {@code null} otherwise. */
        Impl fromBase(Class<? extends DesignClass<?, ?>> cls);
    }

    // ── factories ─────────────────────────────────────────────────────────

    static <D extends DesignClass<?, ?>> ImplProvider<D> of(Class<D> cls, Impl impl) {
        return new Fixed<>(cls, impl);
    }

    static <D extends DesignClass<?, ?>> ImplProvider<D> lazy(Class<D> cls, Function<Resolver, Impl> f) {
        return new Lazy<>(cls, f);
    }

    /** {@code cls} as another class of the same design. */
    static <D extends DesignClass<?, ?>> ImplProvider<D> as(Class<D> cls, Class<? extends DesignClass<?, ?>> other) {
        return new Lazy<>(cls, r -> r.resolve(other));
    }

    record Fixed<D extends DesignClass<?, ?>>(Class<D> designClass, Impl impl) implements ImplProvider<D> {
        @Override public Impl provide(Resolver resolver) { return impl; }
    }

    record Lazy<D extends DesignClass<?, ?>>(Class<D> designClass, Function<Resolver, Impl> f) implements ImplProvider<D> {
        @Override public Impl provide(Resolver resolver) { return f.apply(resolver); }
    }
}
