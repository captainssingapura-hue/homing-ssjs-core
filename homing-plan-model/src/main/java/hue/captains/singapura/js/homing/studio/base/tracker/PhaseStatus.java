package hue.captains.singapura.js.homing.studio.base.tracker;

/** Status of a {@link Phase}. The renderer paints status badges by the slug. */
public enum PhaseStatus {
    NOT_STARTED("Not started", "not-started"),
    IN_PROGRESS("In progress", "in-progress"),
    BLOCKED    ("Blocked",     "blocked"),
    DONE       ("Done",        "done");

    public final String label;
    public final String slug;
    PhaseStatus(String label, String slug) { this.label = label; this.slug = slug; }
}
