package hue.captains.singapura.js.homing.conformance.self;

import hue.captains.singapura.js.homing.conformance.self.NoReflectionAtRunTime.Finding;
import hue.captains.singapura.js.homing.conformance.self.NoReflectionAtRunTime.Use;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Core's gate on no reflection at run time ({@link NoReflectionAtRunTime}): a new use of reflection in a run-time
 * module fails core's build, naming its file and line.
 */
class NoReflectionAtRunTimeTest {

    private static final NoReflectionAtRunTime RULE = NoReflectionAtRunTime.INSTANCE;

    /** Core's root: this module's parent, as the build runs its tests from the module's folder. */
    private static Path root() {
        Path root = Path.of("").toAbsolutePath().getParent();
        assertTrue(Files.isDirectory(root.resolve("homing-conformance-self")) && Files.isDirectory(root.resolve("homing-server")),
                () -> root + " is not core's root: the test runs from homing-conformance-self");
        return root;
    }

    @Test
    void nothingOnARunTimePath_turnsANameIntoAClass() {
        Path root = root();
        List<Finding> reported = RULE.reported(root);
        if (!reported.isEmpty())
            System.out.println("[" + RULE.name() + "] " + reported.size() + " reported, the owner's to decide:\n  "
                    + reported.stream().map(Finding::toString).collect(Collectors.joining("\n  ")));
        List<Finding> findings = RULE.findings(root);
        assertEquals(List.of(), findings.stream().map(Finding::toString).toList(), () -> RULE.name() + " (" + findings.size()
                + "): a run-time module turns a name into a class, or finds an instance by reflection - reach it through what "
                + "was built at startup, or classify it in NoReflectionAtRunTime with its reason");
    }

    @Test
    void everyExemptionAndClassification_isLive() {
        assertEquals(List.of(), RULE.stale(root()), "a line for code that is no more: remove it from NoReflectionAtRunTime");
    }

    @Test
    void theUses_seenOnALine_aNameWrittenDownNot() {
        assertTrue(seen("class-by-name", "Class<?> c = Class.forName(query.className());"));
        assertTrue(seen("instance-field", "var f = clazz.getField(\"INSTANCE\");"));
        assertTrue(seen("new-instance", "instance = clazz.getDeclaredConstructor().newInstance();"));
        assertTrue(seen("resource-by-computed-name", "try (InputStream in = loader.getResourceAsStream(path)) {"));
        assertFalse(seen("resource-by-computed-name", "try (InputStream in = X.class.getResourceAsStream(\"/baseline.txt\")) {"),
                "a resource whose name is written down");
    }

    private static boolean seen(String name, String line) {
        Use use = RULE.uses().stream().filter(u -> u.name().equals(name)).findFirst().orElseThrow();
        return use.shows().matcher(line).find();
    }
}
