package hue.captains.singapura.js.homing.core;

import java.util.List;

/**
 * 梱包 — a crate: the JS modules one Maven module packs and ships, plus the
 * crates it requires. Exactly one per Maven module (RFC 0044, Crate model).
 * The unit of JS-module organization, discovery, and dependency conformance —
 * modelled on a Rust crate / JPMS module: a self-contained set of modules that
 * declares what it {@link #requires()}.
 *
 * <p><b>Implemented by a class in its own Maven module</b> (a singleton
 * {@code INSTANCE}), never a shared record — deliberately. The implementing
 * class's code source <i>is</i> that module's build output, which is how the
 * orphan check scopes its scan to "this module only" with no configuration.</p>
 *
 * <h2>Two graphs, one edge set</h2>
 * <ul>
 *   <li><b>Discovery is transitive:</b> the whole module universe is the
 *       transitive closure of {@link #requires()} from an app's crate — walk it
 *       to enumerate every module (no classpath scan needed at runtime).</li>
 *   <li><b>Visibility is non-transitive:</b> a module may import another only
 *       if the importer's crate lists the importee's crate in its OWN
 *       {@link #requires()} — reaching <i>through</i> a required crate is a
 *       violation (transitive deps must be declared explicitly). Enforced by
 *       {@code CrateDependencyRule}.</li>
 * </ul>
 *
 * <p>Because {@link #requires()} returns typed {@code Crate} references, a crate
 * can only require another whose Maven module it actually depends on — {@code
 * javac} guarantees {@code requires ⊆ Maven dependencies}; there is nothing to
 * validate there.</p>
 */
public interface Crate {

    /** Stable crate id — conventionally the Maven module's artifactId. */
    String name();

    /** The JS modules this crate packs. Completeness is guarded per-module by the orphan check. */
    List<CrateEntry> entries();

    /** The crates this crate directly requires. Direct only — visibility does not transit. */
    default List<Crate> requires() {
        return List.of();
    }
}
