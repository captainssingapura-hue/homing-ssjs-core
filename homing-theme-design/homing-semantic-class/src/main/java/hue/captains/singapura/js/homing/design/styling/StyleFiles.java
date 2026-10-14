package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * Where a style's files are (RFC 0067, appendix Two Classes per Leaf, D9): one per style and leaf, at a path derived
 * from the two types - the style's package, its token, then the leaf's -
 * {@code styles/com/example/styles/editorial/plain-button.css}, each whole. On the classpath, so a module that adds a
 * leaf gives the base layer's file for it from its own jar, at the base layer's path (D26).
 */
public record StyleFiles() implements StatelessFunctionalObject {

    public static final StyleFiles INSTANCE = new StyleFiles();

    private static final String CSS = ".css";

    /** {@code editorial/plain-button.css}: the file as messages name it. */
    public String name(DesignStyle<?> style, Casting leaf) { return style.token() + "/" + leaf.token() + CSS; }

    /** {@code styles/com/example/styles/editorial/}: the style's directory. */
    public String directory(DesignStyle<?> style) {
        return "styles/" + style.getClass().getPackageName().replace('.', '/') + "/" + style.token() + "/";
    }

    /** {@code styles/com/example/styles/editorial/plain-button.css}: the style's file for a leaf. */
    public String path(DesignStyle<?> style, Casting leaf) { return directory(style) + leaf.token() + CSS; }

    /** The style's file for a leaf, read; none when there is none. */
    public Optional<String> read(DesignStyle<?> style, Casting leaf) {
        try (InputStream in = loader(style).getResourceAsStream(path(style, leaf))) {
            return in == null ? Optional.empty() : Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(path(style, leaf) + " cannot be read", e);
        }
    }

    /** Every name in the style's directory, across the classpath, sorted: {@code plain-button.css}. */
    public List<String> present(DesignStyle<?> style) { return present(loader(style), directory(style)); }

    /** Every name in a directory, in every jar and folder of the class loader's that has it. */
    List<String> present(ClassLoader loader, String directory) {
        var out = new TreeSet<String>();
        try {
            Enumeration<URL> found = loader.getResources(directory);
            while (found.hasMoreElements()) {
                URL url = found.nextElement();
                switch (url.getProtocol()) {
                    case "file" -> {
                        try (Stream<Path> in = Files.list(Path.of(url.toURI()))) { in.forEach(p -> out.add(p.getFileName().toString())); }
                    }
                    case "jar" -> {
                        var connection = (JarURLConnection) url.openConnection();
                        connection.setUseCaches(false);
                        try (JarFile jar = connection.getJarFile()) {
                            for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
                                String entry = e.nextElement().getName();
                                if (!entry.startsWith(directory) || entry.length() == directory.length()) continue;
                                String rest = entry.substring(directory.length());
                                int slash = rest.indexOf('/');
                                out.add(slash < 0 ? rest : rest.substring(0, slash));
                            }
                        }
                    }
                    default -> throw new IllegalStateException(directory + " is on the classpath as " + url + ", which cannot be listed");
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(directory + " cannot be listed", e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(directory + " cannot be listed", e);
        }
        return List.copyOf(out);
    }

    private static ClassLoader loader(DesignStyle<?> style) {
        ClassLoader l = style.getClass().getClassLoader();
        return l != null ? l : ClassLoader.getSystemClassLoader();
    }
}
