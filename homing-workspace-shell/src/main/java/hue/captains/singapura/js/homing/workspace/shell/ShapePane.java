package hue.captains.singapura.js.homing.workspace.shell;

import hue.captains.singapura.js.homing.workspace.state.PaneId;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;

/**
 * RFC 0060 — a pane <b>within a shape</b>: what {@link PaneArrangements#IDE} calls
 * its "editor", not where the editor is now.
 *
 * <h2>Why this is not a {@link PaneId}</h2>
 *
 * <p>At seed time the two coincide exactly — a shape's pane name <i>becomes</i> the
 * live slot id, and {@code _slotIdOfPaneId} resolves one to the other by identity.
 * That coincidence is temporary, and the temptation it creates is the reason this
 * type exists.</p>
 *
 * <p>A workspace stops matching its shape the moment anyone uses it. Split a pane
 * and MTP mints an id the shape never named. Close or merge one and the name
 * resolves to nothing, so a caller silently falls back to the default slot. And
 * under D10 a saved workspace ignores the arrangement altogether, so from the
 * second visit onward the shape describes only how things <i>began</i>.</p>
 *
 * <p>So {@code Ide.EDITOR} answers "which pane did this shape call the editor",
 * and never "where is the editor now". Allocation is the one operation for which
 * that is the right question, because allocation only ever happens at t=0.
 * Anything addressing a running workspace takes a {@link PaneId} — and cannot be
 * handed one of these, which is the whole point: the compiler enforces the
 * distinction rather than a comment describing it.</p>
 *
 * <p>Obtained from the shape that owns it, never constructed loose:
 * {@link PaneArrangement#pane(String)} validates the name against the shape and
 * fails immediately if it is not there. So a constant like</p>
 *
 * <pre>{@code
 * public static final ShapePane EDITOR = PaneArrangements.IDE.pane("editor");
 * }</pre>
 *
 * <p>cannot drift from the playbook that built the shape — a typo fails at class
 * initialisation, naming the panes that do exist.</p>
 *
 * @param shapeName the shape this pane belongs to, for the error message that
 *                  matters most: placing into a pane from a different shape
 * @param name      the pane's name, which becomes its slot id at seed time
 *
 * @since RFC 0060
 */
public record ShapePane(String shapeName, String name) implements ValueObject {

    public ShapePane {
        Objects.requireNonNull(shapeName, "ShapePane.shapeName");
        Objects.requireNonNull(name,      "ShapePane.name");
        // Same grammar as PaneId — this name becomes one, so it must be
        // constructible as one. Delegated rather than restated.
        new PaneId(name);
    }

    /**
     * The live pane id this name seeds. Valid <b>at seed time</b>; afterwards the
     * workspace may have no such pane, which is exactly what this type is here to
     * stop callers assuming.
     */
    public PaneId asSeededPaneId() { return new PaneId(name); }

    @Override public String toString() { return shapeName + "." + name; }
}
