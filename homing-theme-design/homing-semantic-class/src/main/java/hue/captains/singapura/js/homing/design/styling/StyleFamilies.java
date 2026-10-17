package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentBranch;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A styling's family files, for reading (RFC 0067, appendix The Style Jars): for each style the instance registers, its
 * base layer among them, a file for each family of the taxonomy - each of the family's castings as the style serves it
 * ({@link StyleManager#family}). Read as the modules' scripts are: in dev mode - {@code homing.devRoot} naming the
 * source folders ({@link ResourceReader}) - made afresh on every read from the casting files as they are on the file
 * system; deployed, the files the instance's jar was built with ({@link WriteStyleFamilies}), from the classpath -
 * refused when the styling installed is not the one they were made from.
 */
public final class StyleFamilies {

    /** Where a jar carries the files: {@code homing/style-families/<style>/<family>.css}. */
    public static final String ROOT = "homing/style-families/";

    /** What the files were made from: a line for each style and casting, naming the implementation answering it. */
    public static final String INDEX = ROOT + "index.txt";

    private final Styling styling;
    private final ResourceReader reader;
    private final ClassLoader loader;
    private final AtomicReference<String> verdict = new AtomicReference<>();

    /**
     * @param styling the instance's styling
     * @param reader  dev mode when it names source roots: each file made from the casting files on the file system
     * @param loader  where a deployed instance's built files are read from
     */
    public StyleFamilies(Styling styling, ResourceReader reader, ClassLoader loader) {
        this.styling = Objects.requireNonNull(styling, "StyleFamilies.styling");
        this.reader = Objects.requireNonNull(reader, "StyleFamilies.reader");
        this.loader = Objects.requireNonNull(loader, "StyleFamilies.loader");
    }

    /** A styling's family files as this process reads them: dev mode by {@code homing.devRoot}, else the classpath's. */
    public static StyleFamilies of(Styling styling) {
        return new StyleFamilies(styling, ResourceReader.fromSystemProperty(), StyleFamilies.class.getClassLoader());
    }

    /** Whether each file is made afresh from the file system. */
    public boolean dev() { return reader.dev(); }

    /** The styles there are files for: every style the instance registers, the default first. */
    public List<String> styles() { return styling.manager().styles().stream().<String>map(DesignStyle::token).toList(); }

    /** The families, in the taxonomy's order. */
    public List<String> families() { return styling.taxonomy().heads().stream().map(ComponentBranch::token).toList(); }

    /** Where a style's file of a family is, in a jar. */
    public static String path(String style, String family) { return ROOT + style + "/" + family + ".css"; }

    /** A style's file of a family: made now from the file system in dev mode, deployed the one built into the jar. */
    public String read(String style, String family) {
        DesignStyle<?> s = styling.manager().style(style).orElseThrow(() -> new IllegalArgumentException(
                "no style '" + style + "' is registered; one of " + styles()));
        ComponentBranch head = styling.taxonomy().heads().stream().filter(h -> h.token().equals(family)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no family '" + family + "'; one of " + families()));
        if (reader.dev()) return styling.manager().family(s, head, reader);
        String refused = verdict.updateAndGet(v -> v != null ? v : check());
        if (!refused.isEmpty()) throw new IllegalStateException(refused);
        return classpath(path(style, family)).orElseThrow(() -> new IllegalStateException(
                path(style, family) + " is not on the classpath, though its index is: the jar was built with another taxonomy"));
    }

    /** Every file a jar carries: the index, then each style's file of each family, made from the classpath's casting files. */
    public Map<String, String> files() {
        var out = new LinkedHashMap<String, String>();
        out.put(INDEX, index());
        for (DesignStyle<?> s : styling.manager().styles())
            styling.manager().families(s, ResourceReader.INSTANCE).forEach((family, css) -> out.put(path(s.token(), family), css));
        return out;
    }

    /** What the files are made from: for each style and casting, the implementation answering it. */
    public String index() {
        var out = new StringBuilder("# The styling the family files were made from: a style, a casting, the implementation answering it.\n");
        for (DesignStyle<?> s : styling.manager().styles())
            for (Casting c : styling.taxonomy().castings())
                out.append(s.token()).append(' ').append(c.token()).append(' ')
                   .append(styling.manager().resolve(s, c).implementation().getClass().getName()).append('\n');
        return out.toString();
    }

    /** Why the built files cannot be read for this styling - none, or another's - or nothing when they can. */
    private String check() {
        Optional<String> built = classpath(INDEX);
        if (built.isEmpty()) return "no family files are on the classpath (" + INDEX + "): the instance's jar writes them when it is "
                + "built (WriteStyleFamilies), or a site reads them made afresh in dev mode, homing.devRoot naming its source folders";
        if (!built.get().equals(index())) return "the family files on the classpath were made from another styling than the one installed: "
                + "the instance's jar wrote them for its own; read them in dev mode, or build the jar of this instance";
        return "";
    }

    private Optional<String> classpath(String path) {
        try (InputStream in = loader.getResourceAsStream(path)) {
            return in == null ? Optional.empty() : Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException(path + " could not be read: " + e.getMessage(), e);
        }
    }
}
