package hue.captains.singapura.js.homing.core.js;

import java.util.List;

/**
 * RFC 0024 — declarative composition language for the framework's
 * DomOpsParty branch tree. A {@code PartyTree} describes the
 * <i>shape</i> of an AppModule's root party (which L1 branches exist
 * under root, who owns them, what elements they hold); the matching
 * {@link PartyTreeWriter} walks the shape and emits the bootstrap JS
 * that constructs it.
 *
 * <p>The pattern is symmetric with the framework's existing
 * {@code ImportsFor} / {@code ExportsOf} — declarative records on the
 * Java side, generated JS on the JS side. The user does not write
 * {@code createBranch} / {@code activate} / {@code createElement} calls
 * by hand; they declare the shape, the writer emits the wiring.</p>
 *
 * <h2>Two L1 branch flavours</h2>
 *
 * <ul>
 *   <li>{@link FrameworkBranch} — fully declared interior. Framework
 *       owns it; the writer emits {@code createElement} calls for each
 *       {@link ElementDecl} plus {@code createBranch} calls for any
 *       nested {@link FrameworkBranch} children. Used for chrome (header
 *       with brand/breadcrumb/themePicker).</li>
 *   <li>{@link AppBodyBranch} — placeholder. The writer emits the
 *       {@code createBranch} + {@code activate} pair and exposes the
 *       resulting JS handle (using {@code name} as the variable),
 *       leaving the interior to be populated by user code (the
 *       AppModule's body JS, or a hosted widget's mountInto).</li>
 * </ul>
 *
 * <h2>Ownership</h2>
 *
 * <p>Three owner flavours encode the framework's discipline:</p>
 *
 * <ul>
 *   <li>{@link OwnerRef.PartyChief} — the framework's immortal root
 *       sentinel; same lifetime as the document; used for chrome and
 *       any branch that is structurally part of the shell itself.</li>
 *   <li>{@link OwnerRef.ShellChief} — the shell's per-document
 *       sentinel; same lifetime as the AppModule's page; used for the
 *       widget-slot branch.</li>
 *   <li>{@link OwnerRef.FreshWidgetChief} — a per-mount synthetic
 *       owner; mortal; dissolved when the widget is swapped. Used for
 *       the L2 widget branch the shell allocates under main.</li>
 * </ul>
 *
 * @param branches the L1 branches under {@code domOpsParty} (root), in
 *                 declaration order
 * @since RFC 0024 Phase P1a
 */
public record PartyTree(List<L1Branch> branches) {

    public PartyTree {
        branches = List.copyOf(branches);
    }

    // -------------------------------------------------------------------------
    // L1 branch flavours.
    // -------------------------------------------------------------------------

    /** One L1 branch under {@code domOpsParty} (the root). */
    public sealed interface L1Branch permits FrameworkBranch, AppBodyBranch {
        String name();
        OwnerRef owner();
    }

    /**
     * Framework-owned branch with a fully-declared interior. The writer
     * emits {@code createElement} calls for {@link #elements()} and
     * recursively constructs any {@link #children()} sub-branches.
     *
     * @param name     branch name (also the JS variable handle)
     * @param owner    who activates this branch
     * @param elements named elements hung off this branch
     * @param children nested framework-owned sub-branches (uncommon — most
     *                 chrome is flat at depth 2)
     */
    public record FrameworkBranch(
            String name,
            OwnerRef owner,
            List<ElementDecl> elements,
            List<FrameworkBranch> children
    ) implements L1Branch {
        public FrameworkBranch {
            elements = List.copyOf(elements);
            children = List.copyOf(children);
        }

        /** Convenience: a flat framework branch (no nested children). */
        public static FrameworkBranch flat(String name, OwnerRef owner, List<ElementDecl> elements) {
            return new FrameworkBranch(name, owner, elements, List.of());
        }
    }

    /**
     * Placeholder branch — the writer emits the {@code createBranch} +
     * {@code activate} pair and exposes the branch as a JS variable
     * named {@link #name()}. The interior is populated by user code
     * (e.g. the StandardMPA shell's widget-routing logic).
     */
    public record AppBodyBranch(
            String name,
            OwnerRef owner
    ) implements L1Branch {}

    // -------------------------------------------------------------------------
    // Element declarations (depth-2 named DOM nodes).
    // -------------------------------------------------------------------------

    /**
     * One named DOM element under a {@link FrameworkBranch}. The writer
     * emits {@code branch.createElement(name, tagName)} and stores the
     * result in a JS variable named {@code name}.
     */
    public record ElementDecl(String name, String tagName) {}

    // -------------------------------------------------------------------------
    // Owner references — sealed permits ensures the writer can switch
    // exhaustively.
    // -------------------------------------------------------------------------

    /** Whose activation token a branch carries. */
    public sealed interface OwnerRef
            permits OwnerRef.PartyChief, OwnerRef.ShellChief, OwnerRef.FreshWidgetChief {

        /** Framework-immortal sentinel, document-lifetime; chrome owner. */
        record PartyChief() implements OwnerRef {
            public static final PartyChief INSTANCE = new PartyChief();
        }

        /** Shell per-document sentinel; same lifetime as the AppModule page. */
        record ShellChief() implements OwnerRef {
            public static final ShellChief INSTANCE = new ShellChief();
        }

        /** Per-mount mortal owner; dissolved on widget swap. */
        record FreshWidgetChief() implements OwnerRef {
            public static final FreshWidgetChief INSTANCE = new FreshWidgetChief();
        }
    }
}
