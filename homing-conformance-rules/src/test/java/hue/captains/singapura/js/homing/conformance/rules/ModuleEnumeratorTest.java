package hue.captains.singapura.js.homing.conformance.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The module enumerator's mechanism — the resource-path → FQCN mapping and a
 * directory scan with package-prefix filtering, over a synthesized resource
 * tree (no real classpath needed).
 */
class ModuleEnumeratorTest {

    @Test
    void mapsJsResourcePathToFqcn() {
        assertEquals("a.b.C", JsResources.INSTANCE.fqcnOf("homing/js/a/b/C.js"));
        assertEquals("hue.captains.x.WidgetModule",
                JsResources.INSTANCE.fqcnOf("homing/js/hue/captains/x/WidgetModule.js"));
        // Windows-style separators tolerated.
        assertEquals("a.b.C", JsResources.INSTANCE.fqcnOf("homing\\js\\a\\b\\C.js"));
        // Not a JS-module resource.
        assertNull(JsResources.INSTANCE.fqcnOf("homing/svg/a/b/C.svg"));
        assertNull(JsResources.INSTANCE.fqcnOf("META-INF/MANIFEST.MF"));
        assertNull(JsResources.INSTANCE.fqcnOf("homing/js/.js"));
    }

    @Test
    void scansADirectoryAndFiltersByPackagePrefix(@TempDir Path root) throws IOException {
        writeJs(root, "homing/js/hue/captains/singapura/js/homing/demo/FooModule.js");
        writeJs(root, "homing/js/hue/captains/singapura/js/homing/blocks/BarWidget.js");
        writeJs(root, "homing/js/other/vendor/Baz.js");        // outside the prefix
        writeJs(root, "homing/js/hue/captains/singapura/js/homing/demo/notjs.txt"); // not .js

        var found = new ArrayList<String>();
        ModuleEnumerator.HOMING.scanEntry(root, found::add);

        assertTrue(found.contains("hue.captains.singapura.js.homing.demo.FooModule"), found::toString);
        assertTrue(found.contains("hue.captains.singapura.js.homing.blocks.BarWidget"), found::toString);
        assertFalse(found.contains("other.vendor.Baz"), "prefix filter must drop out-of-package modules");
        assertEquals(2, found.size(), found::toString);
    }

    @Test
    void buildsAStableSortedDeduplicatedRegistry() {
        var reg = new ModuleRegistry(List.of("z.B", "a.A", "z.B", "a.C"));
        assertEquals(3, reg.size());
        assertEquals(List.of("a.A", "a.C", "z.B"),
                reg.modules().stream().map(ModuleRef::moduleClass).toList());
        assertEquals(List.of("a", "z"), List.copyOf(reg.byPackage().keySet()));
    }

    @Test
    void fromClasspathRunsWithoutError() {
        // Smoke: the real classpath scan must not throw (count is classpath-dependent).
        assertDoesNotThrow(() -> ModuleEnumerator.HOMING.fromClasspath());
    }

    private static void writeJs(Path root, String rel) throws IOException {
        Path p = root.resolve(rel);
        Files.createDirectories(p.getParent());
        Files.writeString(p, "// stub");
    }
}
