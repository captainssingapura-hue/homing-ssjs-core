package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.tao.ontology.Scalar;

/** A dependency on another phase: phaseId of the prerequisite, plus the why. */
public record Dependency(String phaseId, String reason) implements Scalar {}
