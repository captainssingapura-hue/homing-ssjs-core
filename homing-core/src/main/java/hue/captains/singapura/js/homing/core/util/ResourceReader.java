package hue.captains.singapura.js.homing.core.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Reads resource files from classpath or filesystem.
 * <p>
 * When source roots are set - dev mode - a path is read from the first root that has it, at {@code root/path},
 * enabling live-reload during development without server restarts; a path no root has comes from the classpath.
 * Set the system property {@code homing.devRoot} to activate: one folder, or several separated by the platform's
 * path separator ({@code ;} on Windows, {@code :} elsewhere) when a site's resources live in several modules, e.g.:
 * <pre>-Dhoming.devRoot=homing-demo/src/main/resources</pre>
 */
public record ResourceReader(List<Path> sourceRoots) {
    public static final ResourceReader INSTANCE = new ResourceReader(List.of());

    public ResourceReader {
        sourceRoots = List.copyOf(sourceRoots);
    }

    /** One source root, or none for {@code null}. */
    public ResourceReader(Path sourceRoot) {
        this(sourceRoot == null ? List.of() : List.of(sourceRoot));
    }

    public static ResourceReader fromSystemProperty() {
        String devRoot = System.getProperty("homing.devRoot");
        if (devRoot == null || devRoot.isBlank()) return INSTANCE;
        return new ResourceReader(Arrays.stream(devRoot.split(Pattern.quote(File.pathSeparator)))
                .map(String::trim).filter(s -> !s.isEmpty()).map(Path::of).toList());
    }

    /** Whether it reads from source roots first: dev mode. */
    public boolean dev() {
        return !sourceRoots.isEmpty();
    }

    /** The file a path is read from in dev mode: the first root that has it; none outside dev mode. */
    public Optional<Path> sourceFile(String path) {
        for (Path root : sourceRoots) {
            Path file = root.resolve(path);
            if (Files.exists(file)) return Optional.of(file);
        }
        return Optional.empty();
    }

    public List<String> getStringsFromResource(String path) {
        Optional<Path> file = sourceFile(path);
        if (file.isPresent()) {
            try {
                return Files.readAllLines(file.get());
            } catch (IOException e) {
                throw new RuntimeException("Failed to read " + file.get(), e);
            }
        }
        try (InputStream in = this.getClass().getClassLoader().getResourceAsStream(path)) {
            Objects.requireNonNull(in, "cannot find " + path);
            return new BufferedReader(new InputStreamReader(in)).lines().toList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + path, e);
        }
    }
}
