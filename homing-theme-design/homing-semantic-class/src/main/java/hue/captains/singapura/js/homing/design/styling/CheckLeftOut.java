package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/** The part of a jar's check that reads the jar itself ({@link CheckDeclaration}): nothing of it left out of its declaration. */
record CheckLeftOut() implements StatelessFunctionalObject {

    static final CheckLeftOut INSTANCE = new CheckLeftOut();

    /** Whatever of the jar its declaration leaves out, in its styles' packages: an implementation's class, a CSS file. */
    List<String> problems(Class<?> declaration, Set<Class<?>> declared, Set<String> packages) {
        var problems = new ArrayList<String>();
        var cssDeclared = new HashMap<String, Class<?>>();
        for (Class<?> c : declared) cssDeclared.put(CastingCss.INSTANCE.resource(c), c);
        for (String entry : entries(declaration)) {
            if (packages.stream().noneMatch(entry::startsWith) || entry.substring(entry.lastIndexOf('/') + 1).isEmpty()) continue;
            if (packages.stream().noneMatch(p -> entry.lastIndexOf('/') == p.length() - 1)) continue;   // its own package, not one below
            if (entry.endsWith(".css")) {
                if (!cssDeclared.containsKey(entry))
                    problems.add(entry + " is no CSS of an implementation the declaration lists: a jar's CSS is its declared implementations'");
            } else if (entry.endsWith(".class") && !entry.contains("$")) {
                String name = entry.substring(0, entry.length() - ".class".length()).replace('/', '.');
                try {
                    Class<?> c = Class.forName(name, false, declaration.getClassLoader());
                    if (CastingStyle.class.isAssignableFrom(c) && !c.isInterface() && !declared.contains(c))
                        problems.add(name + " implements a casting, and the declaration does not list it: a jar declares every implementation it holds");
                } catch (ClassNotFoundException | LinkageError ignored) { }
            }
        }
        return problems;
    }

    /** Every entry of the jar - or classes' directory - a class was loaded from, as a path under its root. */
    private List<String> entries(Class<?> of) {
        var out = new ArrayList<String>();
        var source = of.getProtectionDomain() == null ? null : of.getProtectionDomain().getCodeSource();
        if (source == null || source.getLocation() == null) return out;
        try {
            Path at = Path.of(source.getLocation().toURI());
            if (Files.isDirectory(at)) {
                try (Stream<Path> walk = Files.walk(at)) {
                    walk.filter(Files::isRegularFile).forEach(p -> out.add(at.relativize(p).toString().replace('\\', '/')));
                }
            } else {
                try (JarFile jar = new JarFile(at.toFile())) {
                    for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
                        JarEntry entry = e.nextElement();
                        if (!entry.isDirectory()) out.add(entry.getName());
                    }
                }
            }
        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            out.clear();
        }
        return out;
    }
}
