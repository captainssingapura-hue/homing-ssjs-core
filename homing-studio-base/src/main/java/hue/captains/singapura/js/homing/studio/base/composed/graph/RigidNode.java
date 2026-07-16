package hue.captains.singapura.js.homing.studio.base.composed.graph;

import hue.captains.singapura.js.homing.studio.base.composed.text.NodeName;
import hue.captains.singapura.js.homing.studio.base.composed.text.Title;

import java.util.Objects;
import java.util.OptionalInt;

/**
 * A node of a "bring your own tree" input — the parent-pointer form (RigidDocV2,
 * RFC 0039), a thin adapter around one of <b>your</b> source nodes of type
 * {@code T}. Each node holds an object reference to its {@code parent} (a root's is
 * {@code null}), its stamped {@code level}, the identity/heading it exposes
 * ({@link NodeName} + {@link Title}), and — crucially — a reference to the original
 * {@code source} node it stands for.
 *
 * <p>Content is <b>not</b> stored here. A single {@code Function<T, RigidNodeContent>}
 * (handed to the normalizer / {@code RigidDocV2.fromNodes}) resolves a body from the
 * {@code source} of each node — one provider for the whole tree, free to dispatch
 * however the downstream sees fit. That keeps structure (this node) and content
 * (the one function) apart, with no per-node content thunks.</p>
 *
 * <p>This representation makes the common malformations <i>unrepresentable</i>:</p>
 * <ul>
 *   <li><b>multiple parents</b> — one {@code parent} field;</li>
 *   <li><b>cycles</b> — immutable, and the parent must exist first, so construction is
 *       inherently parent-first; you can't tie a loop;</li>
 *   <li><b>a wrong level</b> — pinned to {@code parent.level + 1} at construction.</li>
 * </ul>
 *
 * <p>Identity is the object itself — no ids; the parent reference is the edge. Build
 * top-down: {@link #root} then {@link #child}, exactly as a DFS descends. Collect
 * every node you create into a flat list for the normalizer.</p>
 *
 * @param <T> the caller's source-node type this adapter wraps
 */
public final class RigidNode<T> {

    private final RigidNode<T> parent;   // null == root
    private final int level;
    private final NodeName name;
    private final Title title;
    private final T source;
    private final OptionalInt order;

    /**
     * The general constructor. {@code level} must equal {@code parent.level + 1}
     * (or {@code 0} when {@code parent} is {@code null}) — enforced <b>now</b>, so a
     * mis-stamped level is a construction-time failure. Prefer {@link #root}/{@link #child}.
     */
    public RigidNode(RigidNode<T> parent, int level, NodeName name, Title title,
                     T source, OptionalInt order) {
        int expected = (parent == null) ? 0 : parent.level + 1;
        if (level != expected) {
            throw new IllegalArgumentException("RigidNode.level " + level + " must equal " + expected
                    + (parent == null ? " (a root is level 0)" : " (parent.level + 1)"));
        }
        this.parent = parent;
        this.level = level;
        this.name = Objects.requireNonNull(name, "RigidNode.name");
        this.title = Objects.requireNonNull(title, "RigidNode.title");
        this.source = Objects.requireNonNull(source, "RigidNode.source");
        this.order = Objects.requireNonNull(order, "RigidNode.order");
    }

    /** A root node — level {@code 0}, no parent — wrapping {@code source}. */
    public static <T> RigidNode<T> root(T source, NodeName name, Title title) {
        return new RigidNode<>(null, 0, name, title, source, OptionalInt.empty());
    }

    /** A root whose heading defaults to the {@linkplain NodeName#defaultTitle() humanized name}. */
    public static <T> RigidNode<T> root(T source, NodeName name) {
        return root(source, name, name.defaultTitle());
    }

    /** A child of this node — level {@code this.level + 1}, parent {@code this} — wrapping {@code source}. */
    public RigidNode<T> child(T source, NodeName name, Title title) {
        return new RigidNode<>(this, this.level + 1, name, title, source, OptionalInt.empty());
    }

    /** A child with an explicit sibling ordinal. */
    public RigidNode<T> child(T source, NodeName name, Title title, int order) {
        return new RigidNode<>(this, this.level + 1, name, title, source, OptionalInt.of(order));
    }

    /** A child whose heading defaults to the {@linkplain NodeName#defaultTitle() humanized name}. */
    public RigidNode<T> child(T source, NodeName name) {
        return child(source, name, name.defaultTitle());
    }

    /** A child with a default heading and an explicit sibling ordinal. */
    public RigidNode<T> child(T source, NodeName name, int order) {
        return child(source, name, name.defaultTitle(), order);
    }

    public RigidNode<T> parent() { return parent; }
    public int level()           { return level; }
    public NodeName name()       { return name; }
    public Title title()         { return title; }
    public T source()            { return source; }
    public OptionalInt order()   { return order; }
}
