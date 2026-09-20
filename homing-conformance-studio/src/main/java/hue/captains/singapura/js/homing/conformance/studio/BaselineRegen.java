package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.conformance.engine.ConformanceEngine;
import hue.captains.singapura.js.homing.conformance.rules.Finding;
import hue.captains.singapura.js.homing.conformance.rules.GradedFinding;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeSet;

/**
 * Regenerates the framework's conformance baseline deliberately and UTF-8
 * safely: runs the engine over the closure, grades with the baseline as it
 * stands, and writes the file back with every NEW error's fingerprint added
 * and every stale fingerprint dropped — the header kept, the lines sorted.
 * Never wired into the build; run by hand when a new rule lands and the
 * framework's own debt under it is to be recorded, or when debt is paid.
 *
 * <pre>
 *   mvn -pl homing-conformance-studio exec:java
 *       -Dexec.mainClass=hue.captains.singapura.js.homing.conformance.studio.BaselineRegen
 *       -Dexec.args=src/main/resources/conformance-baseline.txt
 * </pre>
 *
 * <p>Why a program and not a console capture: the rules' messages carry an
 * em-dash, which a Windows console re-encodes into a byte the loader
 * refuses, and one bad byte empties the whole baseline.</p>
 */
public final class BaselineRegen {

    private BaselineRegen() {}

    public static void main(String[] args) throws IOException {
        if (args.length != 1) throw new IllegalArgumentException("Usage: BaselineRegen <conformance-baseline.txt>");
        Path file = Paths.get(args[0]);
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        var header = new ArrayList<String>();
        for (String l : lines) { if (l.startsWith("#") || l.isBlank()) header.add(l); else break; }

        List<Finding> raw = new ConformanceEngine().checkCrates(HomingConformance.closure());
        var live = new LinkedHashSet<String>();
        for (Finding f : raw) live.add(f.fingerprint());
        var kept = new TreeSet<String>();
        int stale = 0;
        for (String l : lines) {
            if (l.startsWith("#") || l.isBlank()) continue;
            if (live.contains(l)) kept.add(l); else stale++;
        }
        int added = 0;
        for (GradedFinding g : HomingConformance.grader(true).grade(raw)) {
            if (!g.isError()) continue;
            if (kept.add(g.finding().fingerprint())) added++;
        }
        var out = new ArrayList<>(header);
        out.addAll(kept);
        Files.write(file, out, StandardCharsets.UTF_8);
        System.out.println("[BaselineRegen] " + file + ": " + kept.size() + " fingerprints (" + added + " added, " + stale + " stale dropped)");
    }
}
