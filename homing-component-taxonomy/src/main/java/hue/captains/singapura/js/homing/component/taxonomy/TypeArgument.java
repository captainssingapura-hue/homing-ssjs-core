package hue.captains.singapura.js.homing.component.taxonomy;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Reads the node an implementation names: the type argument it gives {@link ImplFor} or
 * {@link ImplForBranch}, wherever along its supertypes it gives it, and that node's
 * {@code INSTANCE}.
 */
record TypeArgument() {

    static final TypeArgument INSTANCE = new TypeArgument();

    /** The node {@code impl} names as {@code marker}'s argument. */
    Taxon of(Class<?> impl, Class<?> marker) {
        Class<?> node = argument(impl, marker);
        if (node == null)
            throw new IllegalStateException(impl.getName() + ": names no node to " + marker.getSimpleName()
                    + " as a class; name it, or override the accessor");
        try {
            Field f = node.getDeclaredField("INSTANCE");
            f.trySetAccessible();
            return (Taxon) f.get(null);
        } catch (ReflectiveOperationException | ClassCastException e) {
            throw new IllegalStateException(impl.getName() + ": " + node.getName() + " has no INSTANCE to read", e);
        }
    }

    private static Class<?> argument(Class<?> type, Class<?> marker) {
        for (Type t : type.getGenericInterfaces()) {
            Class<?> found = argument(t, marker);
            if (found != null) return found;
        }
        Type sup = type.getGenericSuperclass();
        return sup == null ? null : argument(sup, marker);
    }

    private static Class<?> argument(Type t, Class<?> marker) {
        if (t instanceof ParameterizedType p && p.getRawType() == marker)
            return p.getActualTypeArguments()[0] instanceof Class<?> c ? c : null;
        Class<?> raw = t instanceof ParameterizedType p ? (Class<?>) p.getRawType() : t instanceof Class<?> c ? c : null;
        return raw == null || raw == marker ? null : argument(raw, marker);
    }
}
