package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/** The ids documents are given when they are written without one: a seed always gives the same id. */
public record DocIds() implements StatelessFunctionalObject {

    public static final DocIds INSTANCE = new DocIds();

    /** Deterministic UUID derivation for code-defined ComposedDocs. */
    public UUID composed(String seed) {
        return UUID.nameUUIDFromBytes(("composed:" + seed).getBytes(StandardCharsets.UTF_8));
    }

    /** Deterministic UUID derivation for code-defined TableDocs. */
    public UUID table(String seed) {
        return UUID.nameUUIDFromBytes(("table:" + seed).getBytes(StandardCharsets.UTF_8));
    }
}
