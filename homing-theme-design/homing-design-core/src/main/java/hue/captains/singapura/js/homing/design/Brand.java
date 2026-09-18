package hue.captains.singapura.js.homing.design;

/**
 * Brand — what identifies the house. {@code Mark} is the logo, as an asset
 * and as a fill; {@code House} is the house's own face and ink, where a
 * design wants the brand set differently from the body.
 */
public interface Brand extends Semantic {

    record Mark() implements Brand {}

    record House() implements Brand {}
}
