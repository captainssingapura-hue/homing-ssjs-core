package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.core.util.ResourceReader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Writes an instance's family files into its build's classes (RFC 0067, appendix The Style Jars), so that its jar carries
 * them and a deployed site reads them from the classpath ({@link StyleFamilies}). Run by the instance's jar at
 * {@code process-classes}, as homing-libs writes its bundled files; what was there before is cleared first.
 *
 * <pre>WriteStyleFamilies &lt;the build's classes&gt; &lt;the instance's class: a static INSTANCE, a StylingInstance&gt;</pre>
 */
public final class WriteStyleFamilies {

    private WriteStyleFamilies() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("usage: WriteStyleFamilies <the build's classes> <the instance's class>");
        Object instance = Class.forName(args[1]).getField("INSTANCE").get(null);
        if (!(instance instanceof StylingInstance s)) throw new IllegalArgumentException(args[1] + ".INSTANCE is no StylingInstance");
        Map<String, String> files = write(Path.of(args[0]), s.styling());
        System.out.println("[WriteStyleFamilies] " + files.size() + " files under " + Path.of(args[0]).resolve(StyleFamilies.ROOT));
    }

    /** The styling's files, written under the classes' {@link StyleFamilies#ROOT}, cleared first; what was written. */
    public static Map<String, String> write(Path classes, Styling styling) throws IOException {
        Path root = classes.resolve(StyleFamilies.ROOT);
        if (Files.exists(root)) try (Stream<Path> old = Files.walk(root)) {
            for (Path p : old.sorted(Comparator.reverseOrder()).toList()) Files.delete(p);
        }
        Map<String, String> files = new StyleFamilies(styling, ResourceReader.INSTANCE,
                WriteStyleFamilies.class.getClassLoader()).files();
        for (var e : files.entrySet()) {
            Path file = classes.resolve(e.getKey());
            Files.createDirectories(file.getParent());
            Files.writeString(file, e.getValue(), StandardCharsets.UTF_8);
        }
        return files;
    }
}
