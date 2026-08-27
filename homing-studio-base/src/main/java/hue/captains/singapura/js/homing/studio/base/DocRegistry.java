package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.SimpleAppResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Boot-time UUID-indexed registry of {@link Doc}s reachable from a set of
 * {@link DocProvider}s. Mirrors {@link SimpleAppResolver}'s role for {@link AppModule}s.
 *
 * <p>Constructed once at studio startup; immutable afterwards. UUID collisions throw
 * {@link IllegalStateException} at construction time. Path / extension validation
 * also runs at construction so any developer mistake surfaces at boot rather than at
 * the first request.</p>
 *
 * <p>Per <a href="../../../../../../../../../../docs/rfcs/0004-typed-docs-and-doc-visibility.md">
 * RFC 0004</a>, this is the single source of truth the {@link DocGetAction} consults to
 * resolve {@code /doc?id=<uuid>} requests.</p>
 *
 * @since RFC 0004
 */
public final class DocRegistry {

    private final Map<UUID, Doc> byUuid;
    /**
     * RFC 0015 Phase 2 — typed-identity index. Parallel to {@link #byUuid}; for
     * the current Phase 2 deployment every Doc has a {@link DocId.ByUuid} so
     * the two maps carry the same entries. When Phase 3 introduces non-UUID
     * Doc kinds (PlanDoc, AppDoc) they will register only in this map.
     */
    private final Map<DocId, Doc> byId;

    /**
     * Build a registry from an explicit collection of docs. Validates uniqueness of UUIDs
     * and of typed {@link DocId}s.
     *
     * @throws IllegalStateException on UUID collision, DocId collision, or null UUID
     */
    public DocRegistry(Collection<? extends Doc> docs) {
        var byUuid = new LinkedHashMap<UUID, Doc>();
        var byId   = new LinkedHashMap<DocId, Doc>();
        for (Doc d : docs) {
            UUID id = d.uuid();
            if (id == null) {
                throw new IllegalStateException(
                        "Doc " + d.getClass().getName() + " has null uuid()");
            }
            Doc prev = byUuid.put(id, d);
            if (prev != null && !prev.equals(d)) {
                throw new IllegalStateException(
                        "Doc UUID collision: " + id + " is used by both "
                      + prev.getClass().getName() + " and " + d.getClass().getName());
            }
            DocId docId = d.id();
            if (docId == null) {
                throw new IllegalStateException(
                        "Doc " + d.getClass().getName() + " has null id() — Phase 2 invariant");
            }
            Doc prevById = byId.put(docId, d);
            if (prevById != null && !prevById.equals(d)) {
                throw new IllegalStateException(
                        "Doc DocId collision: " + docId + " is used by both "
                      + prevById.getClass().getName() + " and " + d.getClass().getName());
            }
            // RFC 0015 Phase 3b — collision check uses .equals() (record value
            // equality) instead of reference equality so the same value-Doc
            // (e.g. PlanDoc(MyPlan.INSTANCE)) may appear multiple times in
            // the input — harvested from multiple catalogue leaves at boot —
            // without spurious collisions.
        }
        this.byUuid = Map.copyOf(byUuid);
        this.byId   = Map.copyOf(byId);
    }

    /**
     * Build a registry by walking a {@link SimpleAppResolver}'s app closure for
     * {@link DocProvider} implementors and unioning every contributor's {@link DocProvider#docs()}.
     */
    public static DocRegistry from(SimpleAppResolver appResolver) {
        List<Doc> all = new ArrayList<>();
        for (AppModule<?, ?> app : appResolver.apps()) {
            if (app instanceof DocProvider provider) {
                all.addAll(provider.docs());
            }
        }
        return new DocRegistry(all);
    }

    /**
     * RFC 0015 Phase 3b — harvest synthetic Docs (PlanDoc, AppDoc, future
     * ProxyDoc, etc.) from catalogue leaves. Returns a fresh list ready to
     * merge with the DocProvider-contributed prose Docs before constructing
     * a {@link DocRegistry}.
     *
     * <p>After the Entry factory rewire, {@code Entry.of(host, plan)} and
     * {@code Entry.of(host, nav)} create {@code OfDoc} wrapping a synthetic
     * Doc subtype. These synthetic Docs are constructed at the catalogue
     * leaf — they don't flow through any {@link DocProvider}. Without this
     * harvest, {@link hue.captains.singapura.js.homing.studio.base.app.CatalogueRegistry}'s
     * leaf-validation step would reject the catalogue because the synthetic
     * Doc isn't registered.</p>
     *
     * <p><b>RFC 0051 — now harvests EVERY {@code Entry.OfDoc} leaf, not just
     * the synthetic kinds.</b> The type filter said prose Docs "come from
     * DocProviders", which was true only as long as somebody remembered to
     * write one. In the self-studio a browsing app happened to list them, so
     * removing that app unregistered docs its catalogues still referenced and
     * the boot failed — the app had been holding up registrations that had
     * nothing to do with browsing.</p>
     *
     * <p>Positioning a doc in a catalogue is already the strongest statement
     * anyone makes about it, so it is a strange thing to then need to declare
     * separately. Registering by position removes the second declaration and
     * the whole class of "forgot the DocProvider" boot failures with it.
     * Re-registering the same Doc is harmless — {@link DocRegistry} only
     * objects when one UUID names two DIFFERENT docs — so a doc that is both
     * positioned and listed by a provider still resolves to one entry.</p>
     */
    public static List<Doc> harvestSyntheticFromLeaves(
            java.util.Collection<? extends hue.captains.singapura.js.homing.studio.base.app.Catalogue<?>> catalogues) {
        var out = new ArrayList<Doc>();
        for (var c : catalogues) {
            for (var e : c.leaves()) {
                // RFC 0051 Phase 6 — a bound leaf's content is registered the
                // same way. A leaf with no content contributes nothing, which
                // is the point: an app tile no longer needs a fabricated Doc
                // to be placeable.
                if (e instanceof hue.captains.singapura.js.homing.studio.base.app.Entry.OfLeaf<?, ?, ?> leaf
                        && leaf.content() != null) {
                    out.add(leaf.content());
                }
            }
        }
        return out;
    }

    /**
     * RFC 0016 — harvest the Docs wrapped by tree leaves. Walks every
     * registered {@link hue.captains.singapura.js.homing.studio.base.app.tree.ContentTree ContentTree}
     * recursively; for each {@link hue.captains.singapura.js.homing.studio.base.app.tree.TreeLeaf TreeLeaf}
     * encountered, contributes the wrapped Doc. Collisions across catalogues
     * and trees collapse via record value-equality (DocRegistry's collision
     * check uses {@code .equals()} per Phase 3b).
     */
    public static List<Doc> harvestFromTrees(
            java.util.Collection<? extends hue.captains.singapura.js.homing.studio.base.app.tree.ContentTree> trees) {
        var out = new ArrayList<Doc>();
        for (var tree : trees) {
            walkBranch(tree.root(), out);
        }
        return out;
    }

    private static void walkBranch(
            hue.captains.singapura.js.homing.studio.base.app.tree.TreeBranch branch,
            List<Doc> out) {
        for (var child : branch.children()) {
            if (child instanceof hue.captains.singapura.js.homing.studio.base.app.tree.TreeBranch sub) {
                walkBranch(sub, out);
            } else if (child instanceof hue.captains.singapura.js.homing.studio.base.app.tree.TreeLeaf leaf) {
                out.add(leaf.doc());
            }
        }
    }

    /** Resolve a Doc by UUID, or null if no Doc with that UUID is registered. */
    public Doc resolve(UUID id) {
        return byUuid.get(id);
    }

    /**
     * RFC 0015 Phase 2 — resolve a Doc by typed {@link DocId}, or null if no
     * Doc with that id is registered. Dispatches uniformly across the
     * {@code ByUuid}, {@code ByClass}, and {@code ByClassAndParams} variants;
     * during Phase 2, only {@code ByUuid} resolves to a registered Doc (the
     * Class variants land with their realising subtypes in Phase 3).
     */
    public Doc resolve(DocId id) {
        return byId.get(id);
    }

    /** All Docs in registration order. */
    public Collection<Doc> all() {
        return Collections.unmodifiableCollection(byUuid.values());
    }

    /** Number of registered Docs. */
    public int size() {
        return byUuid.size();
    }
}
