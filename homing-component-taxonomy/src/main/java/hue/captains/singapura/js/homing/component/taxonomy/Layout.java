package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A layout (owner, 21 Oct): how an umbrella container's room is arranged among positions, each a sub-container of one
 * kind, holding a component or a widget. It is no component: strictly style-agnostic, it has no casting, no gap and no
 * look of its own - the containers manage the spacing, and what it places is designed through the layouts' styling
 * ({@link LayoutStyling}); a line between positions is a splitter, a designed component it uses as it will. The layouts
 * are a flat list of their own, apart from the component taxonomy. Static, its arrangement the author's; or dynamic, the
 * user's, who splits, merges and moves its positions - the split grid.
 *
 * @param token        its name: lower-case words joined by hyphens
 * @param when         static, the author's arrangement; or dynamic, the user's
 * @param subContainer the container at each of its positions: one kind for every position
 */
public record Layout(String token, Cardinality.StaticOrDynamic when, Component<?> subContainer) implements ValueObject {

    private static final Pattern TOKEN = Pattern.compile("[a-z][a-z0-9]*(-[a-z0-9]+)*");

    public Layout {
        Objects.requireNonNull(token, "Layout.token");
        if (!TOKEN.matcher(token).matches()) throw new IllegalArgumentException("Layout: '" + token + "' is no layout's name - lower-case words joined by hyphens");
        Objects.requireNonNull(when, "Layout.when: static or dynamic");
        Objects.requireNonNull(subContainer, "Layout.subContainer");
        if (subContainer instanceof LayoutStyling<?>) throw new IllegalArgumentException("Layout " + token + ": its sub-container is a container, never the layouts' styling");
    }

    /** Whether the user arranges it: splits, merges and moves its positions. */
    public boolean dynamic() { return when instanceof Cardinality.Dynamic; }
}
