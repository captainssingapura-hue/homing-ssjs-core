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
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** A style's directory listed across the classpath: a folder and jars alike, every one that has it. */
class StyleFilesTest {

    private static final String DIRECTORY = "styles/com/example/styles/base/";

    @Test
    void aDirectory_listedInAJar_andAcrossTwo() throws IOException {
        Path house = jar("house.jar", "tab.css", "strip.css");
        Path extension = jar("extension.jar", "badge.css");
        try (var loader = new URLClassLoader(new URL[]{house.toUri().toURL(), extension.toUri().toURL()}, null)) {
            assertEquals(List.of("badge.css", "deeper", "strip.css", "tab.css"), StyleFiles.INSTANCE.present(loader, DIRECTORY),
                    "the extension gives the base layer's file for its leaf from its own jar (D26); a folder inside is named too, "
                            + "for the check to refuse as no leaf's file");
        }
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
