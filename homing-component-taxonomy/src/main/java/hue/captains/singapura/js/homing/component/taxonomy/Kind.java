package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * An intermediate node of the taxonomy: a classification of components, abstract - never
 * realized and never worn on its own. A kind names its parent, so any repository may add a kind
 * or a component under any kind: classification is open.
 *
 * <pre>{@code
 * public record Button() implements Kind<Control> {
 *     public static final Button INSTANCE = new Button();
 *     @Override public Control parent() { return Control.INSTANCE; }
 * }
 * }</pre>
 *
 * @param <P> the branch this kind sits under
 */
public non-sealed interface Kind<P extends Branch> extends Branch {

    /** The branch this kind sits under. */
    P parent();
}
