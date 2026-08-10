package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * RFC 0044 — the completeness guard for the Crate model: scans <b>only the
 * crate's own Maven-module build output</b> for concrete {@link EsModule}
 * implementers and reports any that the crate does not declare (an
 * <i>orphan</i>), and any it declares that no longer exist (a <i>stale</i>
 * entry). Run in each module's own test phase, this makes a hand-written crate
 * as complete as a scan — the one thing a hand list otherwise can't promise —
 * while keeping every scan narrow (one module) and build-time only.
 *
 * <p>Scope comes for free from the crate's own class: its code source is that
 * module's build output ({@code target/classes} in a test run, or its jar), so
 * the scan never leaks into dependencies.</p>
 */
public final class OrphanCheck {

    private OrphanCheck() {}

    /** Findings: orphan (found but undeclared) and stale (declared but absent) modules. Empty = compliant. */
    public static List<String> check(Crate crate) {
        Set<String> declared = crate.entries().stream()
                .map(CrateEntry::moduleClass)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> found = concreteEsModulesIn(crate);

        var findings = new ArrayList<String>();
        for (String fqcn : found) {
            if (!declared.contains(fqcn)) {
                findings.add("orphan: " + fqcn + " is a served JS module in crate '" + crate.name()
                        + "' but is not declared in it");
            }
        }
        for (String fqcn : declared) {
            if (!found.contains(fqcn)) {
                findings.add("stale: crate '" + crate.name() + "' declares " + fqcn
                        + " but it was not found in the module's build output");
            }
        }
        return List.copyOf(findings);
    }

    /** Fully-qualified names of concrete {@link EsModule} classes in the crate's own build output. */
    static Set<String> concreteEsModulesIn(Crate crate) {
        return servedModuleClassesInModuleOf(crate.getClass());
    }

    /**
     * Fully-qualified names of the concrete {@link EsModule} classes in the
     * Maven-module build output that {@code anchorInModule} lives in. Shared by
     * the check and the {@code CrateSeed} authoring aid.
     */
    public static Set<String> servedModuleClassesInModuleOf(Class<?> anchorInModule) {
        Path root = codeSourceRoot(anchorInModule);
        var names = new TreeSet<String>();
        ClassLoader loader = anchorInModule.getClassLoader();
        for (String fqcn : classNamesUnder(root)) {
            if (isConcreteEsModule(fqcn, loader)) names.add(fqcn);
        }
        return names;
    }

    private static Path codeSourceRoot(Class<?> anchor) {
        try {
            var cs = anchor.getProtectionDomain().getCodeSource();
            if (cs == null || cs.getLocation() == null) {
                throw new IllegalStateException("no code source for " + anchor.getName());
            }
            return Path.of(cs.getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("bad code source URI for " + anchor.getName(), e);
        }
    }

    private static Set<String> classNamesUnder(Path root) {
        var names = new LinkedHashSet<String>();
        if (Files.isDirectory(root)) {
            try (Stream<Path> walk = Files.walk(root)) {
                walk.filter(p -> p.toString().endsWith(".class") && Files.isRegularFile(p))
                    .forEach(p -> {
                        String rel = root.relativize(p).toString().replace('\\', '/');
                        names.add(rel.substring(0, rel.length() - ".class".length()).replace('/', '.'));
                    });
            } catch (IOException e) {
                throw new UncheckedIOException("scanning " + root, e);
            }
        } else if (Files.exists(root) && root.toString().endsWith(".jar")) {
            try (JarFile jar = new JarFile(root.toFile())) {
                Enumeration<JarEntry> es = jar.entries();
                while (es.hasMoreElements()) {
                    JarEntry e = es.nextElement();
                    String n = e.getName();
                    if (!e.isDirectory() && n.endsWith(".class")) {
                        names.add(n.substring(0, n.length() - ".class".length()).replace('/', '.'));
                    }
                }
            } catch (IOException e) {
                throw new UncheckedIOException("scanning " + root, e);
            }
        }
        return names;
    }

    private static boolean isConcreteEsModule(String fqcn, ClassLoader loader) {
        // Load without initializing — only real modules get initialized (via INSTANCE, elsewhere).
        Class<?> c;
        try {
            c = Class.forName(fqcn, false, loader);
        } catch (Throwable t) {
            return false; // unloadable / not our concern
        }
        int m = c.getModifiers();
        return EsModule.class.isAssignableFrom(c)
                && !c.isInterface()
                && !Modifier.isAbstract(m)
                && !c.isAnonymousClass();
    }
}
