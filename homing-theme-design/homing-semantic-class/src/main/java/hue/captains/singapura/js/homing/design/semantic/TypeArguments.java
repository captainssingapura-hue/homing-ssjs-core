package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The type arguments a class binds on a generic type above it, however far up: read off the declarations, the
 * variables of every type between substituted. What the colour tree's reader reads a node's scheme, parent and kind
 * with, and what a style's and a palette's scheme is read with.
 */
public record TypeArguments() implements StatelessFunctionalObject {

    public static final TypeArguments INSTANCE = new TypeArguments();

    /**
     * The classes {@code declared} binds on {@code generic}'s parameters, in order - an argument that is no class, a
     * variable or a wildcard, as null - or none when it is not a {@code generic}, or binds it raw.
     */
    public List<Class<?>> of(Class<?> declared, Class<?> generic) {
        Type[] found = find(declared, generic, Map.of());
        if (found == null) return List.of();
        var out = new ArrayList<Class<?>>();
        for (Type t : found) out.add(t instanceof Class<?> c ? c : t instanceof ParameterizedType p ? (Class<?>) p.getRawType() : null);
        return out;
    }

    /** The argument at an index, or null when there is none to read. */
    public Class<?> at(Class<?> declared, Class<?> generic, int index) {
        List<Class<?>> args = of(declared, generic);
        return index < args.size() ? args.get(index) : null;
    }

    private static Type[] find(Type type, Class<?> generic, Map<TypeVariable<?>, Type> bindings) {
        Class<?> raw;
        Type[] args;
        if (type instanceof ParameterizedType p) {
            raw = (Class<?>) p.getRawType();
            args = p.getActualTypeArguments().clone();
            for (int i = 0; i < args.length; i++) if (args[i] instanceof TypeVariable<?> v && bindings.containsKey(v)) args[i] = bindings.get(v);
        } else if (type instanceof Class<?> c) {
            raw = c;
            args = null;
        } else return null;
        if (raw == generic) return args;
        var next = new HashMap<TypeVariable<?>, Type>();
        TypeVariable<?>[] vars = raw.getTypeParameters();
        if (args != null) for (int i = 0; i < vars.length; i++) next.put(vars[i], args[i]);
        for (Type up : raw.getGenericInterfaces()) {
            Type[] r = find(up, generic, next);
            if (r != null) return r;
        }
        Type up = raw.getGenericSuperclass();
        return up == null ? null : find(up, generic, next);
    }
}
