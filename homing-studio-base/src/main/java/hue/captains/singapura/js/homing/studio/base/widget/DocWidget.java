package hue.captains.singapura.js.homing.studio.base.widget;

import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import hue.captains.singapura.js.homing.core.Widget;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0024 — abstract base for doc-fetching widgets. The {@link Widget}-side
 * successor to {@code DocViewer}'s body hooks; chrome composition is gone
 * (the {@code StandardMPA} shell owns chrome).
 *
 * <p>Splits a doc-fetching widget into two layers:</p>
 * <ol>
 *   <li><b>Body (subclass-supplied)</b> — the kind-specific parts:
 *       {@link #bodyJs()} (JS that populates the widget's L2 branch from
 *       the fetched Doc), {@link #bodyImports()} (extra imports the body
 *       needs), {@link #mountInto()} (the typed {@code mountInto} record),
 *       {@link #simpleName()} / {@link #paramsType()} / {@link #title()}.</li>
 *   <li><b>Mount wrapper (final here)</b> — the
 *       {@code function mountInto(branch, parent, params)} JS scaffold
 *       with a defensive try/catch. The body runs with three things in
 *       scope:
 *       <ul>
 *         <li>{@code branch} — DomOpsParty L2 widget branch the shell
 *             allocated. Use {@code branch.createElement(name, tagName)}
 *             to mint owned elements and {@code branch.createBranch(name)}
 *             for sub-branches. The branch's dissolve cascades to
 *             everything created under it.</li>
 *         <li>{@code parent} — the DOM element the shell wants the
 *             widget's top-level content attached to (typically the
 *             shell's main-slot host element). Branch-owned elements are
 *             detached by default — the body is responsible for
 *             {@code parent.appendChild(...)}-ing them. Branch ownership
 *             handles cleanup; DOM placement is the body's call.</li>
 *         <li>{@code params} — plain JS object of URL query params. The
 *             body reads {@code params.id}, {@code params.chapter}, etc.
 *             directly. Typed coercion is deferred to a follow-up.</li>
 *       </ul></li>
 * </ol>
 *
 * <p>Concrete subclasses follow the same {@code final class + INSTANCE
 * singleton} pattern as today's {@code DocViewer}-derived viewers:</p>
 *
 * <pre>{@code
 * public final class SvgWidget extends DocWidget<SvgWidget.Params, SvgWidget> {
 *     public static final SvgWidget INSTANCE = new SvgWidget();
 *     private SvgWidget() {}
 *
 *     public record Params(String id) implements Widget._Param {}
 *     private record mountInto() implements Widget._MountInto<Params, SvgWidget> {}
 *
 *     @Override public String simpleName() { return "svg-widget"; }
 *     @Override public Class<Params> paramsType() { return Params.class; }
 *     @Override public String title() { return "svg"; }
 *
 *     @Override protected Widget._MountInto<Params, SvgWidget> mountInto() { return new mountInto(); }
 *     @Override protected List<ModuleImports<? extends Importable>> bodyImports() { return List.of(); }
 *     @Override protected List<String> bodyJs() {
 *         return List.of(
 *             "    if (!params.id) { console.warn('No SVG id'); return; }",
 *             "    // ... fetch + render into branch ..."
 *         );
 *     }
 * }
 * }</pre>
 *
 * @param <P> the widget's typed Params
 * @param <W> the concrete widget subclass (CRTP self-bound)
 * @since RFC 0024 Phase P1a
 */
public abstract class DocWidget<P extends Widget._Param, W extends DocWidget<P, W>>
        implements Widget<P, W>, SelfContent {

    // -----------------------------------------------------------------------
    // Subclass contract — these stay abstract; each widget supplies them.
    // -----------------------------------------------------------------------

    /** The typed mountInto record this widget exports. Implement as
     *  {@code new mountInto()} where {@code mountInto} is a nested record
     *  inside the concrete widget. */
    protected abstract Widget._MountInto<P, W> mountInto();

    /** Any extra imports the body JS needs. Return an empty list when
     *  the body uses only primitives the framework provides. */
    protected abstract List<ModuleImports<? extends Importable>> bodyImports();

    /** The body JS lines. Executes inside the framework's
     *  {@code mountInto(branch, parent, params)} function with three
     *  things in scope: {@code branch} (DomOpsParty L2 widget branch),
     *  {@code parent} (the DOM element to attach top-level content to),
     *  and {@code params} (plain JS object of URL params). The body is
     *  responsible for both creating owned elements via the branch AND
     *  attaching them to {@code parent}; the wrapper handles the
     *  try/catch boundary only. */
    protected abstract List<String> bodyJs();

    // -----------------------------------------------------------------------
    // Mount wrapper — final. Subclasses cannot override.
    // -----------------------------------------------------------------------

    @Override
    public final ImportsFor<W> imports() {
        var b = ImportsFor.<W>builder();
        for (ModuleImports<? extends Importable> extra : bodyImports()) {
            b.add(extra);
        }
        return b.build();
    }

    @SuppressWarnings("unchecked")
    @Override
    public final ExportsOf<W> exports() {
        return new ExportsOf<>((W) this, List.of(mountInto()));
    }

    @Override
    public final List<String> selfContent(ModuleNameResolver resolver) {
        var lines = new ArrayList<String>();
        lines.add("function mountInto(branch, parent, params) {");
        lines.add("    try {");
        lines.addAll(bodyJs());
        lines.add("    } catch (e) {");
        lines.add("        console.error('Widget \"' + branch.name + '\" mountInto failed:', e);");
        // Visual error rendering inside `branch` is deferred — the branch's
        // owned-element namespace may already be partly populated by bodyJs,
        // so re-creating elements under collision-able names risks compounding
        // failures. P1b/P1c will likely settle the pattern via a dedicated
        // error sub-branch the wrapper allocates lazily.
        lines.add("    }");
        lines.add("}");
        return lines;
    }
}
