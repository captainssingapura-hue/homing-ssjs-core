package hue.captains.singapura.js.homing.design.styling;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A style's directory listed across the classpath: a folder and jars alike, every one that has it; a file's family by its name. */
class StyleFilesTest {

    private static final String DIRECTORY = "styles/com/example/styles/base/";

    @Test
    void aDirectory_listedInAJar_andAcrossTwo_aFileGivenTwiceCounted() throws IOException {
        Path house = jar("house.jar", "control.css", "gauge.css");
        Path extension = jar("extension.jar", "control.extension.css", "gauge.css");
        try (var loader = new URLClassLoader(new URL[]{house.toUri().toURL(), extension.toUri().toURL()}, null)) {
            assertEquals(List.of("control.css", "control.extension.css", "deeper", "gauge.css"), StyleFiles.INSTANCE.present(loader, DIRECTORY),
                    "the extension gives its own file of the controls from its own jar (D26, D30); a folder inside is named too, "
                            + "for the check to refuse as no family's file");
            assertEquals(2, StyleFiles.INSTANCE.counted(loader, DIRECTORY).get("gauge.css"), "two jars give one file, one hiding the other: "
                    + "the check refuses it");
        }
    }

    @Test
    void aFilesFamily_isReadOffItsName() {
        var files = StyleFiles.INSTANCE;
        assertEquals(Optional.of("button"), files.family("button.css"));
        assertEquals(Optional.of("button"), files.family("button.pan-zoom.css"), "a module's own file of the family");
        assertEquals(Optional.empty(), files.family("button.pan.zoom.css"));
        assertEquals(Optional.empty(), files.family("Button.css"));
        assertEquals(Optional.empty(), files.family("deeper"));
    }

    @TempDir
    Path dir;

    private Path jar(String name, String... files) throws IOException {
        Path jar = dir.resolve(name);
        try (OutputStream out = Files.newOutputStream(jar); var j = new JarOutputStream(out)) {
            String at = "";
            for (String part : DIRECTORY.split("/")) {
                at += part + "/";
                j.putNextEntry(new JarEntry(at));
                j.closeEntry();
            }
            for (String file : files) {
                j.putNextEntry(new JarEntry(DIRECTORY + file));
                j.write(".x {}".getBytes());
                j.closeEntry();
            }
            j.putNextEntry(new JarEntry(DIRECTORY + "deeper/no.css"));
            j.closeEntry();
        }
        return jar;
    }
}
