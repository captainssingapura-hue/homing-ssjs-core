package hue.captains.singapura.js.homing.studio.base.ui.layout;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * ModalModule — third of the workspace primitives. A standalone draggable,
 * resizable floating panel. Self-contained: no dependency on SplitPane
 * or MultiTabPane.
 *
 * <p><b>Reusable beyond the workspace.</b> Modal is the building block
 * for any floating UI element: settings dialogs, tool palettes, picture-
 * in-picture viewers, detached widgets, custom confirmation prompts,
 * inspector windows. The MultiTabPane's detach-to-modal feature is one
 * consumer; the primitive itself has no awareness of tabs.</p>
 *
 * <h2>Constructor options</h2>
 *
 * <pre>
 * new Modal({
 *   container : HTMLElement,       // required; use document.body
 *   title     : string,
 *   content   : HTMLElement,       // optional initial body content
 *   x, y      : number,            // initial position
 *   width, height : number,
 *   minWidth, minHeight : number,
 *   resizable : boolean,           // default true; eight resize handles
 *   closable  : boolean,           // default true; × in title bar
 *   bounds    : HTMLElement,       // optional clamp region (default: viewport)
 *   onClose   : function,
 *   onMove    : function(x, y),
 *   onResize  : function(w, h),
 *   onFocus   : function,          // fires on any mousedown on the panel
 * });
 * </pre>
 *
 * <h2>API</h2>
 *
 * <ul>
 *   <li>{@code modal.el} — the root DOM element (for advanced composition).</li>
 *   <li>{@code modal.setTitle(s)} / {@code setContent(el)}</li>
 *   <li>{@code modal.moveTo(x, y)} / {@code resize(w, h)}</li>
 *   <li>{@code modal.open()} / {@code close()} / {@code toggle()} / {@code isOpen()}</li>
 *   <li>{@code modal.destroy()}</li>
 * </ul>
 *
 * <p>Container constraint: the panel converts mouse {@code clientX/Y}
 * directly into CSS {@code left/top}, so the container must have its
 * origin at the viewport origin. In practice, <b>always use
 * {@code document.body}</b> as the container — mounting under a nested
 * positioned element with an offset causes a first-move jump equal to
 * the container's offset.</p>
 *
 * <p>Z-index / focus stacking is intentionally out of scope. Each modal
 * is independent; consumers wire the {@code onFocus} callback to a
 * z-index manager if they need stacking discipline.</p>
 */
public record ModalModule() implements DomModule<ModalModule> {

    /** The single export — the {@code Modal} JS class. */
    public record Modal() implements Exportable._Constant<ModalModule> {}

    public static final ModalModule INSTANCE = new ModalModule();

    @Override
    public ImportsFor<ModalModule> imports() {
        // Standalone — no upstream module dependencies. Injects own CSS.
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<ModalModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new Modal()));
    }
}
