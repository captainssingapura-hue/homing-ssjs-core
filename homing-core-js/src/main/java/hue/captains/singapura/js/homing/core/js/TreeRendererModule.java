package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * Generic tree renderer — the JS half of the rigid tree substrate
 * (homing-tree-views). Lives in homing-core-js alongside DomOpsParty
 * because, like DomOpsParty, it is a substrate primitive any downstream
 * consumer can build on.
 *
 * <p>{@code TreeRenderer} consumes the canonical {@code TreeNode} JSON
 * produced by {@code TreeNodeJsonWriter} (level discriminator + typed
 * dimensions + children) and draws a collapsible tree, emitting a
 * selection callback on click. It is <b>tree-kind agnostic</b>: it reads
 * only the substrate's universal dimensions ({@code displayLabel},
 * {@code kind}, {@code summary}) — the same renderer draws a catalogue
 * tree, a pivot grouping, or any future tree shape with zero per-kind JS.
 * This is the substrate's headline promise made concrete.</p>
 *
 * <p>No imports: the consumer passes in a DomOpsParty branch, so the
 * renderer owns its DOM through the caller's ownership tree (clean
 * dissolve cascade) without depending on the singleton.</p>
 *
 * @since homing-tree-views v1
 */
public record TreeRendererModule() implements DomModule<TreeRendererModule> {

    public static final TreeRendererModule INSTANCE = new TreeRendererModule();

    /** The {@code TreeRenderer} JS class. */
    public record TreeRenderer() implements Exportable._Constant<TreeRendererModule> {}

    @Override
    public ImportsFor<TreeRendererModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<TreeRendererModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new TreeRenderer()));
    }
}
