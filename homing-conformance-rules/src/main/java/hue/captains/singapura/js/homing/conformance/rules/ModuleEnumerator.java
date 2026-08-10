package hue.captains.singapura.js.homing.conformance.rules;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Discovers JS modules by scanning the classpath for their {@code .js} resources
 * — every resource-backed {@code DomModule} lives at {@code homing/js/<fqcn>.js}
 * (the same path convention the module server + conformance loaders use), so the
 * resource path reverses to the module's class name.
 *
 * <p><b>Scope + limits (RFC 0044).</b> The framework deliberately forbids
 * classpath scanning for <i>runtime</i> composition (audit / determinism —
 * {@code HomingLibsRegistry}); this is a build/dev-time <i>conformance</i> tool
 * where completeness is the requirement a hand-maintained list cannot promise,
 * so scanning is the justified exception. This resource scan finds every
 * resource-backed module; it does <b>not</b> yet find {@code SelfContent}
 * (Java-emitted, no {@code .js}), {@code CssGroup}, or {@code SvgGroup} modules —
 * those need a class-scan or a build-time export and are added later.</p>
 *
 * <p>Functional Object: one {@code INSTANCE} scoped to the homing package prefix.</p>
 *
 * @param packagePrefix only modules whose class name starts with this are kept
 */
public record ModuleEnumerator(String packagePrefix) {

    /** Scans for modules under the homing package. */
    public static final ModuleEnumerator HOMING =
            new ModuleEnumerator("hue.captains.singapura.js.homing");

    private static final String JS_ROOT = "homing/js/";
    private static final String JS_EXT  = ".js";

    /** Enumerate the current process classpath into a {@link ModuleRegistry}. */
    public ModuleRegistry fromClasspath() {
        var fqcns = new TreeSet<String>();
        String cp = System.getProperty("java.class.path", "");
        for (String entry : cp.split(File.pathSeparator)) {
            if (!entry.isBlank()) scanEntry(Path.of(entry), fqcns::add);
        }
        return ModuleRegistry.ofClassNames(fqcns);
    }

    /** Scan one classpath entry (a directory or a jar). Package-visible for testing. */
    void scanEntry(Path entry, Consumer<String> sink) {
        if (Files.isDirectory(entry)) {
            scanDir(entry, sink);
        } else if (Files.exists(entry) && entry.toString().endsWith(".jar")) {
            scanJar(entry, sink);
        }
    }

    private void scanDir(Path root, Consumer<String> sink) {
        Path jsRoot = root.resolve("homing").resolve("js");
        if (!Files.isDirectory(jsRoot)) return;
        try (Stream<Path> walk = Files.walk(jsRoot)) {
            walk.filter(p -> p.toString().endsWith(JS_EXT) && Files.isRegularFile(p))
                .forEach(p -> accept(root.relativize(p).toString(), sink));
        } catch (IOException e) {
            throw new UncheckedIOException("scanning " + jsRoot, e);
        }
    }

    private void scanJar(Path jar, Consumer<String> sink) {
        try (JarFile jf = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                if (!e.isDirectory()) accept(e.getName(), sink);
            }
        } catch (IOException e) {
            throw new UncheckedIOException("scanning " + jar, e);
        }
    }

    private void accept(String resourcePath, Consumer<String> sink) {
        String fqcn = fqcnFromJsResource(resourcePath);
        if (fqcn != null && fqcn.startsWith(packagePrefix)) sink.accept(fqcn);
    }

    /**
     * Reverse {@code homing/js/a/b/C.js} → {@code a.b.C}; {@code null} if the
     * path is not a JS-module resource.
     */
    static String fqcnFromJsResource(String resourcePath) {
        String p = resourcePath.replace('\\', '/');
        if (!p.startsWith(JS_ROOT) || !p.endsWith(JS_EXT)) return null;
        String mid = p.substring(JS_ROOT.length(), p.length() - JS_EXT.length());
        if (mid.isBlank()) return null;
        return mid.replace('/', '.');
    }
}
