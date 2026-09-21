package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.tree.RowDisplay;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;

/**
 * What a vertex of a composed component tree IS — the component family's
 * answer, the way the crate tree has {@code CrateDetails}: typed, with the
 * one place strings are rendered in {@link #row()}.
 */
public sealed interface ComponentDetails extends ValueObject {

    /** Narrowed to what a tree row draws. */
    RowDisplay row();

    static String plural(int n, String noun) { return n + " " + noun + (n == 1 ? "" : "s"); }

    /** The composition's root: every vehicle, counted. */
    record OfComposition(String name, int vehicleCount, int componentCount) implements ComponentDetails {
        @Override public RowDisplay row() {
            return new RowDisplay(name, "", plural(vehicleCount, "vehicle") + " · " + plural(componentCount, "component"), "composition");
        }
    }

    /** A vehicle: a crate's catalogue root. */
    record OfVehicle(String crate, String name, String summary, int componentCount) implements ComponentDetails {
        @Override public RowDisplay row() {
            return new RowDisplay(name, "vehicle", summary.isEmpty() ? plural(componentCount, "component") : summary, "vehicle");
        }
    }

    /** A family or a group within a vehicle. */
    record OfCatalogue(String name, String summary, int componentCount) implements ComponentDetails {
        @Override public RowDisplay row() {
            return new RowDisplay(name, "", summary.isEmpty() ? plural(componentCount, "component") : summary, "catalogue");
        }
    }

    /** A component: its shape, its module, the crate that ships it, the words a caller reaches it by. */
    record OfComponent(String label, UiComponent.Shape shape, String tag, String summary, String module, String crate, List<String> path) implements ComponentDetails {
        @Override public RowDisplay row() {
            return new RowDisplay(label, shape.tag(), summary.isEmpty() ? module : summary, shape.tag());
        }
    }
}
