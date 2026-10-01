package hue.captains.singapura.js.homing.studio.base.tracker;

import hue.captains.singapura.tao.ontology.ValueObject;

/** A single task inside a {@link Phase}. */
public record Task(String description, boolean done) implements ValueObject {}
