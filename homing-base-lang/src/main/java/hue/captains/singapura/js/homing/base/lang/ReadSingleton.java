package hue.captains.singapura.js.homing.base.lang;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;

/**
 * Reads a stateless singleton's instance each time it is asked, and keeps nothing: the plain function every
 * {@link SingletonManager} comes down to. It does not judge whether a singleton holds state; that is for a check at
 * build.
 */
public record ReadSingleton() implements SingletonManager, StatelessFunctionalObject, StatelessSingleton {

    public static final ReadSingleton INSTANCE = new ReadSingleton();

    /** One read of a type, in the order a singleton is kept: what it is, what it declares, what that holds. */
    @Override
    public Read read(Class<?> type) {
        String name = type.getName();
        if (!StatelessSingleton.class.isAssignableFrom(type))
            return new Read.Refused(name + " is not a StatelessSingleton: only a stateless singleton has one instance to give");
        if (type.isInterface())
            return new Read.Refused(name + " is an interface: only a concrete type has an instance of its own");
        if (Modifier.isAbstract(type.getModifiers()))
            return new Read.Refused(name + " is abstract: only a concrete type has an instance of its own");
        if (type.isEnum()) {
            long constants = Arrays.stream(type.getDeclaredFields()).filter(Field::isEnumConstant).count();
            if (constants != 1) return new Read.Refused(name + " is an enum of " + constants + " constants: a singleton has one");
        }
        Field field;
        try {
            field = type.getDeclaredField("INSTANCE");
        } catch (NoSuchFieldException e) {
            return new Read.Refused(name + " declares no INSTANCE of its own: a stateless singleton declares its one instance"
                    + " as a static final INSTANCE, and a supertype's is not its own");
        }
        if (!Modifier.isStatic(field.getModifiers()) || !Modifier.isFinal(field.getModifiers()))
            return new Read.Refused(name + "'s INSTANCE is not static final");
        if (field.getType() != type)
            return new Read.Refused(name + "'s INSTANCE is declared as " + field.getType().getName() + ", not " + name);
        if (!field.trySetAccessible())
            return new Read.Refused(name + "'s INSTANCE cannot be read: its package is not open to " + ReadSingleton.class.getName());
        Object instance;
        try {
            instance = field.get(null);
        } catch (ExceptionInInitializerError e) {
            return new Read.Refused(name + " failed to initialize, so it has no INSTANCE: " + e.getCause());
        } catch (LinkageError e) {
            return new Read.Refused(name + " cannot be loaded, so it has no INSTANCE: " + e);
        } catch (IllegalAccessException e) {
            return new Read.Refused(name + "'s INSTANCE cannot be read: " + e.getMessage());
        }
        if (instance == null)
            return new Read.NotYet(name + "'s INSTANCE is null: asked for while " + name + " is still being initialized, or never set");
        return new Read.Held((StatelessSingleton) instance);
    }
}
