package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Where a style's files are (RFC 0067, appendix Two Classes per Leaf, D9, D30): one per style and family, named after
 * the family's head - {@code styles/com/example/styles/editorial/button.css} - each casting's rule whole in it; and one
 * more for each module that adds to the family, named after the module - {@code editorial/button.pan-zoom.css} - from
 * its own jar, so a module that adds a casting gives its CSS with it (D26), and never hides another's file. The
 * directory is the style's package, then its token.
 */
public record StyleFiles() implements StatelessFunctionalObject {

    public static final StyleFiles INSTANCE = new StyleFiles();

    private static final Pattern FILE = Pattern.compile("([a-z0-9]+(?:-[a-z0-9]+)*)(?:\\.([a-z0-9]+(?:-[a-z0-9]+)*))?\\.css");

    /**
     * A casting's rule as a style's files write it.
     *
     * @param file the file it is in: {@code button.css}
     * @param rule the rule, read
     * @param text the rule as written
     */
    public record Written(String file, ReadCss.Rule rule, String text) implements ValueObject {}

    /** {@code editorial/button.css}: a file as messages name it. */
    public String name(DesignStyle<?> style, String file) { return style.token() + "/" + file; }

    /** {@code styles/com/example/styles/editorial/}: the style's directory. */
    public String directory(DesignStyle<?> style) {
        return "styles/" + style.getClass().getPackageName().replace('.', '/') + "/" + style.token() + "/";
    }

    /** {@code styles/com/example/styles/editorial/button.css}: one of the style's files. */
    public String path(DesignStyle<?> style, String file) { return directory(style) + file; }

    /** {@code button.css}: the family's own file, its head's token. */
    public String file(ComponentBranch head) { return head.token() + ".css"; }

    /** The token of the head whose family a file is of, by its name - {@code <head>.css}, {@code <head>.<module>.css} - or none. */
    public Optional<String> family(String file) {
        Matcher m = FILE.matcher(file);
        return m.matches() ? Optional.of(m.group(1)) : Optional.empty();
    }

    /** One of the style's files, read; none when there is none. */
    public Optional<String> read(DesignStyle<?> style, String file) {
        try (InputStream in = loader(style).getResourceAsStream(path(style, file))) {
            return in == null ? Optional.empty() : Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(path(style, file) + " cannot be read", e);
        }
    }

    /** Every name in the style's directory, across the classpath, sorted: {@code button.css}. */
    public List<String> present(DesignStyle<?> style) { return List.copyOf(counted(loader(style), directory(style)).keySet()); }

    /** Every name in the style's directory given more than once - by two jars, or a jar and a folder - so one hides the other. */
    public List<String> twice(DesignStyle<?> style) {
        return counted(loader(style), directory(style)).entrySet().stream().filter(e -> e.getValue() > 1).map(Map.Entry::getKey).toList();
    }

    /**
     * Every casting's rule the style's family files write, by the casting - read once, each file in its family's order;
     * a rule that is no casting's is left out, and of a casting ruled twice, the first. The check holds the files to
     * the castings; this only finds them.
     */
    public Map<Casting, Written> written(Taxonomy taxonomy, DesignStyle<?> style) {
        var byClass = new HashMap<String, Casting>();
        for (Casting c : taxonomy.castings()) byClass.put("." + LeafClasses.INSTANCE.of(taxonomy, c).styled(), c);
        var out = new LinkedHashMap<Casting, Written>();
        for (String file : present(style)) {
            if (family(file).isEmpty()) continue;
            String css = read(style, file).orElse("");
            for (ReadCss.Piece piece : ReadCss.INSTANCE.split(css).pieces())
                if (piece.item() instanceof ReadCss.Rule r) {
                    Casting c = byClass.get(CheckStyle.squeeze(r.selector()));
                    if (c != null) out.putIfAbsent(c, new Written(file, r, piece.text()));
                }
        }
        return out;
    }

    /** Every name in a directory, in every jar and folder of the class loader's that has it, with how many give it. */
    Map<String, Integer> counted(ClassLoader loader, String directory) {
        var out = new TreeMap<String, Integer>();
        try {
            Enumeration<URL> found = loader.getResources(directory);
            var seen = new LinkedHashSet<String>();
            while (found.hasMoreElements()) {
                URL url = found.nextElement();
                if (!seen.add(url.toString())) continue;
                var names = new LinkedHashSet<String>();
                switch (url.getProtocol()) {
                    case "file" -> {
                        try (Stream<Path> in = Files.list(Path.of(url.toURI()))) { in.forEach(p -> names.add(p.getFileName().toString())); }
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
                                names.add(slash < 0 ? rest : rest.substring(0, slash));
                            }
                        }
                    }
                    default -> throw new IllegalStateException(directory + " is on the classpath as " + url + ", which cannot be listed");
                }
                for (String n : names) out.merge(n, 1, Integer::sum);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(directory + " cannot be listed", e);
        } catch (URISyntaxException e) {
            throw new IllegalStateException(directory + " cannot be listed", e);
        }
        return out;
    }

    /** Every name in a directory, in every jar and folder of the class loader's that has it, sorted. */
    List<String> present(ClassLoader loader, String directory) { return new ArrayList<>(counted(loader, directory).keySet()); }

    private static ClassLoader loader(DesignStyle<?> style) {
        ClassLoader l = style.getClass().getClassLoader();
        return l != null ? l : ClassLoader.getSystemClassLoader();
    }
}
