package hue.captains.singapura.js.homing.core.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ResourceReaderTest {

    @Test
    void getStringsFromResource_readsFromFilesystem(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("test/hello.txt");
        Files.createDirectories(file.getParent());
        Files.writeString(file, "line1\nline2\nline3");

        var reader = new ResourceReader(tempDir);
        var lines = reader.getStringsFromResource("test/hello.txt");

        assertEquals(3, lines.size());
        assertEquals("line1", lines.get(0));
        assertEquals("line2", lines.get(1));
        assertEquals("line3", lines.get(2));
    }

    @Test
    void getStringsFromResource_throwsForMissingFile() {
        var reader = new ResourceReader((Path) null);
        assertThrows(NullPointerException.class, () ->
                reader.getStringsFromResource("nonexistent/file.txt")
        );
    }

    @Test
    void getStringsFromResource_prefersFilesystemOverClasspath(@TempDir Path tempDir) throws IOException {
        // Create a file on filesystem that shadows any classpath resource
        Path file = tempDir.resolve("shadow.txt");
        Files.writeString(file, "from-filesystem");

        var reader = new ResourceReader(tempDir);
        var lines = reader.getStringsFromResource("shadow.txt");

        assertEquals(1, lines.size());
        assertEquals("from-filesystem", lines.getFirst());
    }

    @Test
    void fromSystemProperty_returnsDefaultWhenNotSet() {
        // System property homing.devRoot should not be set in test env
        String prev = System.getProperty("homing.devRoot");
        try {
            System.clearProperty("homing.devRoot");
            var reader = ResourceReader.fromSystemProperty();
            assertEquals(java.util.List.of(), reader.sourceRoots());
            assertFalse(reader.dev());
        } finally {
            if (prev != null) System.setProperty("homing.devRoot", prev);
        }
    }

    @Test
    void fromSystemProperty_takesSeveralRoots_theFirstThatHasAPathWins(@TempDir Path tempDir) throws IOException {
        Path one = Files.createDirectories(tempDir.resolve("one")), two = Files.createDirectories(tempDir.resolve("two"));
        Files.writeString(two.resolve("only-two.txt"), "from-two");
        Files.writeString(one.resolve("both.txt"), "from-one");
        Files.writeString(two.resolve("both.txt"), "from-two");
        String prev = System.getProperty("homing.devRoot");
        try {
            System.setProperty("homing.devRoot", one + java.io.File.pathSeparator + " " + two + java.io.File.pathSeparator);
            var reader = ResourceReader.fromSystemProperty();
            assertEquals(java.util.List.of(one, two), reader.sourceRoots(), "each root, trimmed, none empty");
            assertTrue(reader.dev());
            assertEquals("from-two", reader.getStringsFromResource("only-two.txt").getFirst());
            assertEquals("from-one", reader.getStringsFromResource("both.txt").getFirst(), "the first root that has it");
            assertEquals(java.util.Optional.empty(), reader.sourceFile("nowhere.txt"));
        } finally {
            if (prev != null) System.setProperty("homing.devRoot", prev);
            else System.clearProperty("homing.devRoot");
        }
    }
}
