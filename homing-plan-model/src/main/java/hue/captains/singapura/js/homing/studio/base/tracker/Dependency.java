package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.tao.ontology.ValueObject;

/** A dependency on another phase: phaseId of the prerequisite, plus the why. */
public record Dependency(String phaseId, String reason) implements ValueObject {}
