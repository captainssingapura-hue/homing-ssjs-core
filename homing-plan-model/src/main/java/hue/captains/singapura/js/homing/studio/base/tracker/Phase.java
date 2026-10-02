package hue.captains.singapura.js.homing.studio.base.tracker;

import java.util.List;

/**
 * One phase of a {@link Plan}. Carries everything the renderer shows on both
 * the index page (a step-card) and the detail page (a full task list,
 * dependency block, acceptance criteria, optional metrics table).
 *
 * <p>{@link #metrics()} defaults to empty; trackers that don't quantify per-phase
 * outcomes use the convenience constructor below and skip the field entirely.
 * Trackers that DO want before/after measurements (cleanup / refactor /
 * migration plans) pass a populated list — the renderer hides the metrics
 * table when the list is empty.</p>
 */
public record Phase(
        String id,
        String label,
        String summary,
        String description,
        PhaseStatus status,
        List<Task> tasks,
        List<Dependency> dependsOn,
        String verification,
        String rollback,
        String effort,
        String notes,
        List<Metric> metrics
) {
    /** Convenience constructor for trackers that don't capture per-phase metrics. */
    public Phase(String id, String label, String summary, String description,
                 PhaseStatus status, List<Task> tasks, List<Dependency> dependsOn,
                 String verification, String rollback, String effort, String notes) {
        this(id, label, summary, description, status, tasks, dependsOn,
             verification, rollback, effort, notes, List.of());
    }

    /** 0..100 — fraction of {@link #tasks()} that are {@code done}. */
    public int progressPercent() {
        if (tasks.isEmpty()) return 0;
        long done = tasks.stream().filter(Task::done).count();
        return (int) (done * 100 / tasks.size());
    }
}
