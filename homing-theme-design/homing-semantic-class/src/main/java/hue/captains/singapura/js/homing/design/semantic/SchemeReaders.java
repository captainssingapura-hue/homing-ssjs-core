package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * How an instance finds a scheme's tree reader (RFC 0067, appendix Semantic Colours and Flavours, D47): one functional
 * object for the whole instance, asked for the reader of each scheme it needs - the schemes its styles require, and those
 * its palettes are of - and never handed a reader it does not ask for. A style says only its scheme; nothing names the
 * reader but this. The default, {@link #BY_NAME}, finds it by a fixed naming convention; an instance may give another.
 */
public interface SchemeReaders extends StatelessFunctionalObject {

    /** The default: a scheme {@code <Name>Scheme}'s reader is {@code <Name>Colours}, beside it ({@link ByName}). */
    SchemeReaders BY_NAME = new ByName();

    /** What finding a scheme's reader comes to: the reader, or why there is none. */
    sealed interface Found permits Found.Reader, Found.Missing {
        /** The scheme's reader. */
        record Reader(SchemeTreeReader<?> reader) implements Found {}
        /** No reader, and why, said whole. */
        record Missing(String why) implements Found {}
    }

    /** The reader of a scheme, or why none is found. */
    Found find(Class<?> scheme);

    /**
     * Schemes read: each one's normalized tree, as its reader reads it.
     *
     * @param trees    each scheme read, by its scheme, in the order asked
     * @param problems why a scheme is not, each said whole
     */
    record Read(Map<Class<?>, ColorTree> trees, List<String> problems) {
        public Read {
            trees = Collections.unmodifiableMap(new LinkedHashMap<>(trees));
            problems = List.copyOf(problems);
        }
    }

    /**
     * The schemes asked, each read by the reader found for it: each reader's layers sitting on the readers found for the
     * schemes below - a layer on another reader of a scheme found would read that scheme twice, differently - and each
     * tree read and refused nothing.
     */
    default Read read(Collection<Class<?>> schemes) {
        var problems = new ArrayList<String>();
        var readers = new LinkedHashMap<Class<?>, SchemeTreeReader<?>>();
        for (Class<?> scheme : schemes) {
            if (readers.containsKey(scheme)) continue;
            switch (find(scheme)) {
                case Found.Reader r -> readers.put(scheme, r.reader());
                case Found.Missing m -> problems.add(m.why());
            }
        }
        var trees = new LinkedHashMap<Class<?>, ColorTree>();
        readers.forEach((scheme, r) -> {
            var passed = new HashSet<Class<?>>(List.of(r.getClass()));   // a layer that comes back is the reader's to refuse
            boolean sits = true;
            for (Optional<? extends SchemeTreeReader<?>> b = r.below(); b.isPresent() && passed.add(b.get().getClass()); b = b.get().below()) {
                Optional<Class<?>> below = Schemes.INSTANCE.of(b.get());
                if (below.isEmpty()) continue;   // the reader refuses it
                if (find(below.get()) instanceof Found.Reader found && found.reader().getClass() != b.get().getClass()) {
                    problems.add(r.getClass().getName() + " sits on " + b.get().getClass().getName() + ", and " + found.reader().getClass().getName()
                            + " is " + Schemes.INSTANCE.name(below.get()) + "'s reader: a layer sits on the reader of the scheme below");
                    sits = false;
                }
            }
            if (!sits) return;
            try { trees.put(scheme, r.read()); }
            catch (RuntimeException e) { problems.add(r.getClass().getName() + "'s tree is refused: " + e.getMessage()); }
        });
        return new Read(trees, problems);
    }

    /**
     * The default: the reader of a scheme named {@code <Name>Scheme} is the class {@code <Name>Colours} beside it - in its
     * package, or in the class it is nested in - double-checked to read exactly that scheme, and made by its
     * {@code INSTANCE}, else by its constructor of nothing.
     */
    record ByName() implements SchemeReaders {

        private static final String SCHEME = "Scheme";
        private static final String COLOURS = "Colours";

        @Override public Found find(Class<?> scheme) {
            String name = Schemes.INSTANCE.name(scheme);
            if (scheme.isInterface() || !ColorScheme.class.isAssignableFrom(scheme) || !Modifier.isAbstract(scheme.getModifiers()))
                return new Found.Missing(scheme.getName() + " is no scheme: a scheme is an abstract class of ColorScheme");
            if (!name.endsWith(SCHEME) || name.length() == SCHEME.length())
                return new Found.Missing(name + " is not named <Name>Scheme: its reader is found by the scheme's name, <Name>Colours beside it");
            String binary = scheme.getName();
            String readerName = binary.substring(0, binary.length() - SCHEME.length()) + COLOURS;
            String simple = name.substring(0, name.length() - SCHEME.length()) + COLOURS;
            Class<?> reader;
            try {
                reader = Class.forName(readerName, false, scheme.getClassLoader());
            } catch (ClassNotFoundException e) {
                return new Found.Missing(name + "'s reader is " + simple + " beside it, and there is none: a scheme and its colours live together");
            }
            if (!SchemeTreeReader.class.isAssignableFrom(reader))
                return new Found.Missing(simple + ", beside " + name + ", is no tree reader: a scheme's reader is <Name>Colours");
            Optional<Class<?>> reads = Schemes.INSTANCE.readBy(reader);
            if (reads.isEmpty() || reads.get() != scheme)
                return new Found.Missing(simple + " reads " + reads.map(Schemes.INSTANCE::name).orElse("no scheme") + ", not " + name
                        + ": a scheme's reader reads exactly it");
            try {
                return new Found.Reader(made(reader));
            } catch (ReflectiveOperationException | RuntimeException e) {
                return new Found.Missing(simple + " cannot be made: a reader has an INSTANCE, or a constructor of nothing");
            }
        }

        private static SchemeTreeReader<?> made(Class<?> reader) throws ReflectiveOperationException {
            try {
                Field instance = reader.getField("INSTANCE");
                if (Modifier.isStatic(instance.getModifiers()) && reader.isAssignableFrom(instance.getType()))
                    return (SchemeTreeReader<?>) instance.get(null);
            } catch (NoSuchFieldException ignored) {
                // made by its constructor of nothing
            }
            Constructor<?> c = reader.getDeclaredConstructor();
            c.setAccessible(true);
            return (SchemeTreeReader<?>) c.newInstance();
        }
    }
}
