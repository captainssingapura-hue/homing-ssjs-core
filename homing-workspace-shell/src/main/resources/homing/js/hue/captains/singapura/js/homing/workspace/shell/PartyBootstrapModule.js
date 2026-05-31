// =============================================================================
// PartyBootstrap — Phase 2 of the workspace-shell chrome.
//
// Walks spec.parties — each a typed PartyDecl on the wire — and produces
// constructed Parties + a workspaceCtx map.
//
// Explicit Substrate doctrine:
//   - Instance methods on INSTANCE singleton (no static).
//   - No `async` sugar — Promises returned explicitly.
//   - ALL collaborators held as instance fields, resolved in constructor.
//     Method bodies use `this._dep` — never bare global lookups.
//     Substitution is constructor-only, no per-call opts overrides.
//   - The dep-graph walker can therefore traverse from any instance.
//
// Instance fields (graph-traversable):
//   _PartyCtor : Party class — defaults to the global Party
//   _importer  : (url) => Promise<module> — defaults to native dynamic import
// =============================================================================

class PartyBootstrap {

    constructor(deps) {
        deps = deps || {};
        this._PartyCtor = deps.PartyCtor || Party;
        this._importer  = deps.importer  || (url => import(url));
    }

    /**
     * Walk spec.parties → constructed Parties + workspaceCtx.
     * Returns Promise — explicitly, not via async/await.
     */
    bootstrap(partyDecls) {
        if (!Array.isArray(partyDecls) || partyDecls.length === 0) {
            return Promise.resolve({ parties: {}, workspaceCtx: {} });
        }
        const decls = partyDecls.slice();
        const self  = this;
        return Promise.all(decls.map(d => self._importer(d.secretaryModuleUrl)))
            .then(modules => self.assemble(decls, modules));
    }

    /**
     * Pure synchronous core: given decls + already-imported modules,
     * build { parties, workspaceCtx } using the configured PartyCtor.
     * Stateless behaviour — testable without Promises.
     */
    assemble(decls, modules) {
        const PartyCtor    = this._PartyCtor;
        const parties      = {};
        const workspaceCtx = {};
        for (let i = 0; i < decls.length; i++) {
            const decl = decls[i];
            const mod  = modules[i];
            const secretaryExport = mod && mod[decl.secretaryExportName];
            if (!secretaryExport) {
                console.error('[PartyBootstrap] Secretary export "' + decl.secretaryExportName
                            + '" not found in module ' + decl.secretaryModuleUrl
                            + ' — Party "' + decl.name + '" skipped.');
                continue;
            }
            if (typeof secretaryExport.behavior !== 'function'
             || secretaryExport.initial === undefined) {
                console.error('[PartyBootstrap] Secretary "' + decl.secretaryExportName
                            + '" missing { initial, behavior } — Party "' + decl.name
                            + '" skipped.');
                continue;
            }

            const party = new PartyCtor({
                name: decl.name,
                root: {
                    path:     decl.name,
                    initial:  secretaryExport.initial,
                    behavior: secretaryExport.behavior
                }
            });

            const actors = Array.isArray(decl.actors) ? decl.actors : [];
            for (const actor of actors) {
                party.joinActor({
                    id:              actor.id,
                    parentSecretary: actor.parentSecretary,
                    reactors:        {}     // sender-only initial actors
                });
            }

            parties[decl.name] = party;
            if (decl.exposedAs) {
                workspaceCtx[decl.exposedAs] = party;
            }
        }
        return { parties, workspaceCtx };
    }
}

PartyBootstrap.INSTANCE = new PartyBootstrap();
