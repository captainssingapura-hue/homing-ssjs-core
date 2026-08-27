package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;

/**
 * RFC 0051 Phase 6 — a doc that carries an author-chosen NAME.
 *
 * <p>The name is data: the author saying "call this one rfc0051-axiom", the
 * same kind of statement as a title. What is <i>not</i> data — and is not here
 * — is turning a name into a path segment, or deciding what to do when there
 * is none. {@code DocSlugs} owns that, and consults this when a doc offers
 * one.</p>
 *
 * <p>It exists because the alternative was worse. Three doc kinds already
 * carried such a name in three different shapes ({@code RigidDoc}'s field,
 * {@code ComposedDoc}'s record component, {@code RfcDoc}'s derived
 * {@code rfcId().slug()}), and a framework-side table cannot name a downstream
 * kind at all — {@code RfcDoc} lives in the self-studio, which
 * {@code homing-studio-base} does not depend on. One seam serves all three and
 * anything downstream adds.</p>
 *
 * @since RFC 0051 Phase 6
 */
public interface AuthoredName {

    /** The author-chosen name, or null to take the framework's default. */
    NodeName authoredSlug();
}
