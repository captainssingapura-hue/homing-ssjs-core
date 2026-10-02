package hue.captains.singapura.js.homing.studio.base.tracker;

/** Status of a {@link Decision}: OPEN (pending) or RESOLVED (chosen). */
public enum DecisionStatus {
    OPEN     ("Open",     "open"),
    RESOLVED ("Resolved", "resolved");

    public final String label;
    public final String slug;
    DecisionStatus(String label, String slug) { this.label = label; this.slug = slug; }
}
