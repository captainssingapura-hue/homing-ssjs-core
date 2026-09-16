package hue.captains.singapura.js.homing.core;

/**
 * Generic guard — an opt-in marker that types a {@link CssClass} (or a
 * theme override) to a specific {@link Layer}.
 *
 * <p>Java forbids implementing the same parameterised interface twice with
 * different type arguments, so a record cannot accidentally declare two
 * layers; the compiler catches it as <i>"InLayer cannot be inherited with
 * different arguments"</i>. No runtime validation needed.</p>
 *
 * <p>Absence of {@code InLayer} is the implicit default → {@link Component}.
 * Records that ARE Components don't need the marker; the marker is the
 * deliberate-departure-from-default signal.</p>
 *
 * <p>Use site:</p>
 * <pre>{@code
 * public record st_layout() implements CssClass<StudioStyles>, InLayer<Layout> { … }
 * public record st_card()   implements CssClass<StudioStyles>                  { … }  // implicit Component
 *
 * // Compile error:
 * public record bad() implements CssClass<X>, InLayer<Layout>, InLayer<State> { … }
 * }</pre>
 *
 * @param <L> the cascade layer this class belongs to
 */
public interface InLayer<L extends Layer> {}
