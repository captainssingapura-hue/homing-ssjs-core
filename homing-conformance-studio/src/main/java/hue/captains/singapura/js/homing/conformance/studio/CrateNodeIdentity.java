package hue.captains.singapura.js.homing.conformance.studio;

import hue.captains.singapura.js.homing.tree.NodeIdentity;

import java.util.Objects;

/**
 * The crate tree's own {@link NodeIdentity} — and the proof that the extension
 * point is genuinely open.
 *
 * <p>This module sits <b>downstream</b> of {@code homing-rigid-tree} and builds
 * {@code NormalizedNode}s of its own, which is exactly why {@code NodeIdentity}
 * cannot be a sealed set: the substrate can never enumerate the kinds of tree its
 * consumers will bring. Each subtree supplies its own identity kind and its own
 * resolver, and grafting merges those resolvers by union.</p>
 *
 * <p>Every value below is anchored on something globally unique in its own right —
 * a crate name, a fully-qualified package, a fully-qualified class — never on a
 * position in the tree. A crate node would carry the same identity whether this
 * tree stood alone or were grafted under an umbrella studio.</p>
 *
 * @param kind  which sort of thing is named; distinguishes the value's namespace
 * @param value the globally unique name within that namespace
 * @since RFC 0053
 */
public record CrateNodeIdentity(String kind, String value) implements NodeIdentity {

    public CrateNodeIdentity {
        Objects.requireNonNull(kind,  "CrateNodeIdentity.kind");
        Objects.requireNonNull(value, "CrateNodeIdentity.value");
    }

    /** The forest root — one per studio, so the constant is its whole identity. */
    public static CrateNodeIdentity root() {
        return new CrateNodeIdentity("crates", "");
    }

    public static CrateNodeIdentity ofCrate(String crateName) {
        return new CrateNodeIdentity("crate", crateName);
    }

    /** A package node, qualified by its crate — package names repeat across crates. */
    public static CrateNodeIdentity ofPackage(String crateName, String packagePath) {
        return new CrateNodeIdentity("package", crateName + ":" + packagePath);
    }

    /** A module leaf, named by its FQCN — already global, so it needs no qualifying. */
    public static CrateNodeIdentity ofModule(String moduleFqcn) {
        return new CrateNodeIdentity("module", moduleFqcn);
    }
}
