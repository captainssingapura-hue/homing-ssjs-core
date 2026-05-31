// =============================================================================
// WorkspaceDirectory — Phase 5 of the workspace-shell chrome.
//
// Owns workspace identity. The URL may carry:
//   ?workspace=<uuid>   — look up an existing entry by UUID
//   ?name=Y             — look up by name; create-on-miss with a fresh UUID
//   (neither)           — placeholder UUID derived from kind → "default"
//
// resolveIdentity(kind) returns Promise<{id, name, isDefault, isNew}>.
// Downstream phases (EventEmitter, CheckpointService, ReplayEngine,
// WorkspaceBadge) chain off this Promise — identity is fixed before
// they boot.
//
// registerInCatalogue(kind, identity) idempotently writes the row
// (create for isNew or first-boot-default; touch for existing). Never
// throws — registration failures are logged and absorbed.
//
// Explicit Substrate doctrine:
//   - Instance methods on INSTANCE singleton (no static).
//   - ALL collaborators in constructor: _catalogueStore, _urlSource,
//     _uuidGen, _clock. Per-call methods receive only data
//     (workspaceKind, identity).
//   - No async sugar — naive Promise chains keep the I/O boundary
//     visible.
//
// Instance fields (graph-traversable):
//   _catalogueStore : WorkspaceCatalogueStore | null
//   _urlSource      : () => URLSearchParams | null
//   _uuidGen        : () => string
//   _clock          : () => number
// =============================================================================

class WorkspaceDirectory {

    constructor(deps) {
        deps = deps || {};
        // Catalogue store. Explicit null means "no catalogue, use
        // placeholder everywhere" — useful for tests and for environments
        // without IndexedDB. If unset, build via the factory if available;
        // construction failures are logged + the store stays null.
        if (deps.catalogueStore !== undefined) {
            this._catalogueStore = deps.catalogueStore;
        } else {
            try {
                this._catalogueStore = (typeof createWorkspaceCatalogueStore === 'function')
                    ? createWorkspaceCatalogueStore()
                    : null;
            } catch (e) {
                console.warn('[WorkspaceDirectory] catalogue attach failed '
                           + '(registration disabled):',
                    e && e.message ? e.message : e);
                this._catalogueStore = null;
            }
        }
        // URL params source — read on each resolveIdentity. Default
        // reads window.location.search; tests inject a fixed string.
        this._urlSource = deps.urlSource || function () {
            if (typeof URLSearchParams === 'undefined') return null;
            if (typeof location === 'undefined')         return null;
            try { return new URLSearchParams(location.search); }
            catch (e) { return null; }
        };
        // UUID generator — used when ?name=Y creates a fresh workspace.
        this._uuidGen = deps.uuidGen || function () {
            if (typeof crypto !== 'undefined' && crypto.randomUUID) {
                return crypto.randomUUID();
            }
            return 'w-' + Date.now().toString(16) + '-'
                        + Math.random().toString(16).slice(2);
        };
        // Wall-clock — for createdAt / lastOpenedAt timestamps.
        this._clock = deps.clock || function () { return Date.now(); };
    }

    /**
     * Pure transform: deterministic UUID derived from kind. Same input ⇒
     * same UUID across sessions. Used as the "default workspace" id for
     * a kind when no URL hint is present and as a stable fallback when
     * the catalogue is unreachable. Hash + UUID-v5-shape produces a
     * stable opaque identifier the storage layer can key against.
     */
    placeholderUuidForKind(kindStr) {
        const s = 'workspace:' + String(kindStr);
        let h = 0;
        for (let i = 0; i < s.length; i++) {
            h = ((h << 5) - h + s.charCodeAt(i)) | 0;
        }
        const hex = ('0000000' + (h >>> 0).toString(16)).slice(-8);
        return hex + '-7000-5000-9000-' + hex + '0001';
    }

    /**
     * Resolve {id, name, isDefault, isNew} from the URL via the
     * catalogue. Precedence: ?workspace=<uuid> → ?name=Y → default.
     * Returns a Promise so the entire boot sequence chains off the
     * same async point.
     */
    resolveIdentity(workspaceKind) {
        if (!workspaceKind) {
            throw new Error('[WorkspaceDirectory] workspaceKind required');
        }
        const self = this;
        const fallback = {
            id:        this.placeholderUuidForKind(workspaceKind),
            name:      'default',
            isDefault: true,
            isNew:     false
        };
        if (!this._catalogueStore) return Promise.resolve(fallback);

        const params = this._urlSource();
        const workspaceParam = params ? params.get('workspace') : null;
        const nameParam      = params ? params.get('name')      : null;

        if (workspaceParam) {
            return this._catalogueStore.lookupByUuid(workspaceKind, workspaceParam)
                .then(function (entry) {
                    if (entry) {
                        return {
                            id:        entry.id,
                            name:      entry.name,
                            isDefault: !!entry.isDefault,
                            isNew:     false
                        };
                    }
                    console.warn('[WorkspaceDirectory] ?workspace=' + workspaceParam
                        + ' not found in catalogue — falling back to default');
                    return fallback;
                });
        }
        if (nameParam) {
            return this._catalogueStore.lookupByName(workspaceKind, nameParam)
                .then(function (entry) {
                    if (entry) {
                        return {
                            id:        entry.id,
                            name:      entry.name,
                            isDefault: !!entry.isDefault,
                            isNew:     false
                        };
                    }
                    return {
                        id:        self._uuidGen(),
                        name:      nameParam,
                        isDefault: false,
                        isNew:     true
                    };
                });
        }
        return Promise.resolve(fallback);
    }

    /**
     * Idempotent catalogue registration:
     *   identity.isNew=true   → create row (isDefault always false).
     *   existing row found    → touch lastOpenedAt.
     *   no row, not isNew     → create with identity.isDefault preserved
     *                           (first boot of default workspace).
     * Never throws — registration failures are logged and absorbed so a
     * broken catalogue doesn't break boot.
     */
    registerInCatalogue(workspaceKind, identity) {
        if (!workspaceKind) {
            throw new Error('[WorkspaceDirectory] workspaceKind required');
        }
        if (!identity || !identity.id || !identity.name) {
            throw new Error('[WorkspaceDirectory] identity {id, name} required');
        }
        if (!this._catalogueStore) return Promise.resolve();
        const self = this;
        const now  = this._clock();

        if (identity.isNew) {
            return this._catalogueStore.create({
                kind:         workspaceKind,
                id:           identity.id,
                name:         identity.name,
                createdAt:    now,
                lastOpenedAt: now,
                isDefault:    false
            }).then(function () {
                console.log('[WorkspaceDirectory] catalogue: created new workspace "'
                          + identity.name + '"');
            }).catch(function (e) {
                console.warn('[WorkspaceDirectory] catalogue create failed:',
                    e && e.message ? e.message : e);
            });
        }
        return this._catalogueStore.lookupByUuid(workspaceKind, identity.id)
            .then(function (existing) {
                if (existing) {
                    return self._catalogueStore.touch(workspaceKind, identity.id);
                }
                // First boot for the default workspace — create with
                // identity.isDefault preserved.
                return self._catalogueStore.create({
                    kind:         workspaceKind,
                    id:           identity.id,
                    name:         identity.name,
                    createdAt:    now,
                    lastOpenedAt: now,
                    isDefault:    !!identity.isDefault
                }).then(function () {
                    console.log('[WorkspaceDirectory] catalogue: registered "'
                              + identity.name + '" — first boot');
                });
            })
            .catch(function (e) {
                console.warn('[WorkspaceDirectory] catalogue registration failed:',
                    e && e.message ? e.message : e);
            });
    }
}

WorkspaceDirectory.INSTANCE = new WorkspaceDirectory();
