package hue.captains.singapura.js.homing.component;

/**
 * A delivery vehicle: a crate that ships components and says so with a
 * catalogue of its own. The crate class implements this beside
 * {@code Crate} — the physical roster in {@code entries()}, the logical one
 * here — and {@link ComponentTrees} finds it in the closure by this type,
 * since the crate contract in the core knows nothing of components.
 */
public interface ComponentVehicle {

    /** The root of this vehicle's catalogue. */
    C0_Components<?> components();
}
