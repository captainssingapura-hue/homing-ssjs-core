package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.LinkedHashSet;
import java.util.List;

/**
 * The roles with a look of their own a component may be cast in (RFC 0067, D55): a stateless object - its roles listed,
 * each once, none that only places a part - so a component's castings are its own, fixed by its type
 * ({@link WithRoles}), and never by an owner. Components cast alike share one set; a set may gather others
 * ({@link Union}).
 *
 * <pre>{@code
 * public record Committing() implements RoleSet {
 *     public static final Committing INSTANCE = new Committing();
 *     @Override public List<Role<?>> roles() { return List.of(Go.INSTANCE, Cancel.INSTANCE, Action.INSTANCE); }
 * }
 * }</pre>
 */
public interface RoleSet extends StatelessFunctionalObject {

    /** Its roles, each once. */
    List<Role<?>> roles();

    /** A set gathered from others: their roles, in the order given, each once. */
    interface Union extends RoleSet {
        /** The sets it gathers. */
        List<RoleSet> sets();

        @Override default List<Role<?>> roles() {
            var out = new LinkedHashSet<Role<?>>();
            for (RoleSet s : sets()) out.addAll(s.roles());
            return List.copyOf(out);
        }
    }
}
