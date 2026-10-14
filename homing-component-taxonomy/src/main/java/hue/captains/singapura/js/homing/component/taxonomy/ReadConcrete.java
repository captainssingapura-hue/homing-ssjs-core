package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

/**
 * Reads a concrete leaf's declaration: the virtual leaf and the design it gives {@link ConcreteComponent},
 * wherever along its supertypes it gives them, and each one's {@code INSTANCE}. A reader of its own, beside
 * {@link TypeArgument}, which reads an implementation's anchor: this one follows a type variable down the
 * declaration - a leaf's {@code Concrete<D>} passes its {@code D} to {@code ConcreteComponent} - and refuses
 * in a concrete leaf's own terms.
 */
record ReadConcrete() implements StatelessFunctionalObject {

    static final ReadConcrete INSTANCE = new ReadConcrete();

    /** The virtual leaf a concrete leaf draws: the first argument it gives {@code ConcreteComponent}. */
    VirtualComponent<?> virtualOf(Class<?> concrete) {
        return (VirtualComponent<?>) instance(concrete, 0, "virtual leaf", "implement the leaf's Concrete<YourDesign>");
    }

    /** The design a concrete leaf belongs to: the third argument it gives {@code ConcreteComponent}. */
    Design<?, ?> designOf(Class<?> concrete) {
        return (Design<?, ?>) instance(concrete, 2, "design", "name a design record in the leaf's Concrete<YourDesign>, not a raw Concrete");
    }

    private static Object instance(Class<?> concrete, int at, String what, String how) {
        Class<?> named = argument(concrete, new HashMap<>(), at);
        if (named == null)
            throw new IllegalStateException(concrete.getName() + " names no " + what + " to ConcreteComponent as a class: " + how);
        try {
            Field f = named.getDeclaredField("INSTANCE");
            f.trySetAccessible();
            return f.get(null);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(concrete.getName() + ": its " + what + " " + named.getName() + " has no INSTANCE to read", e);
        }
    }

    /**
     * The class {@code ConcreteComponent}'s argument at a place resolves to, walking a type's supertypes
     * with what each one's type variables are bound to on the way; null where it is left unbound.
     */
    private static Class<?> argument(Type type, Map<TypeVariable<?>, Type> bound, int at) {
        Class<?> raw;
        Map<TypeVariable<?>, Type> here = new HashMap<>();
        if (type instanceof ParameterizedType p) {
            raw = (Class<?>) p.getRawType();
            TypeVariable<?>[] variables = raw.getTypeParameters();
            Type[] arguments = p.getActualTypeArguments();
            for (int i = 0; i < variables.length; i++) {
                Type a = arguments[i];
                here.put(variables[i], a instanceof TypeVariable<?> v && bound.containsKey(v) ? bound.get(v) : a);
            }
            if (raw == ConcreteComponent.class)
                return here.get(variables[at]) instanceof Class<?> c ? c : null;
        } else if (type instanceof Class<?> c) {
            raw = c;
            if (raw == ConcreteComponent.class) return null;   // named raw: no argument given
        } else {
            return null;
        }
        for (Type t : raw.getGenericInterfaces()) {
            Class<?> found = argument(t, here, at);
            if (found != null) return found;
        }
        Type above = raw.getGenericSuperclass();
        return above == null ? null : argument(above, here, at);
    }
}
