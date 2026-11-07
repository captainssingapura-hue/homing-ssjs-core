package hue.captains.singapura.js.homing.studio.base.tracker;

/** Status of a {@link Decision}: OPEN (pending) or RESOLVED (chosen). */
public enum DecisionStatus {
    OPEN     ("Open",     "open"),
    RESOLVED ("Resolved", "resolved");

    private final String label;
    private final String slug;
    DecisionStatus(String label, String slug) { this.label = label; this.slug = slug; }

    /** Its name, as a reader sees it. */
    public String label() { return label; }

    /** Its name in a class or an address. */
    public String slug() { return slug; }
}
