// =============================================================================
// CodecRegistrar — Phase 3 of the workspace-shell chrome.
//
// Two-step registration of widget Params codecs:
//   1. Defaults (sync): every spec.entries[i] gets the substrate's
//      identity codec.
//   2. Custom (async overlay): spec.widgetCodecs entries are dynamic-
//      imported and registered under widgetKind.
//
// Explicit Substrate doctrine:
//   - Instance methods on INSTANCE singleton (no static).
//   - No async sugar — Promises returned explicitly.
//   - ALL collaborators in constructor: _registry, _importer.
//
// Instance fields (graph-traversable):
//   _registry : WidgetParamsCodecRegistry — defaults to the global
//   _importer : (url) => Promise<module>  — defaults to native dynamic import
// =============================================================================

class CodecRegistrar {

    constructor(deps) {
        deps = deps || {};
        this._registry = deps.registry || WidgetParamsCodecRegistry;
        this._importer = deps.importer || (url => import(url));
    }

    /**
     * Two-step registration. Synchronous defaults fire immediately;
     * custom overlays fire when their dynamic imports resolve.
     *
     * @return Promise<void>
     */
    registerAll(opts) {
        if (!opts) throw new Error('[CodecRegistrar] opts required');

        // 1. Sync defaults.
        this._registerDefaults(opts.entries || []);

        // 2. Async custom overlay.
        const refs = opts.widgetCodecs || [];
        if (refs.length === 0) return Promise.resolve();
        const self = this;
        return Promise.all(refs.map(r => self._importer(r.moduleUrl)))
            .then(modules => self._registerCustom(refs, modules));
    }

    /** SYNC: register identity codec for each entry's simpleName. */
    _registerDefaults(entries) {
        const identity = this._identityCodec();
        for (const e of entries) {
            this._registry.register(e.simpleName, identity);
        }
    }

    /** SYNC: walk (refs, modules) pairwise, register each codec under
     *  its widgetKind. Skips refs whose module is missing the export. */
    _registerCustom(refs, modules) {
        for (let i = 0; i < refs.length; i++) {
            const mod = modules[i];
            const ref = refs[i];
            const codec = mod && mod[ref.exportName];
            if (!codec) {
                console.error('[CodecRegistrar] codec export "' + ref.exportName
                            + '" not found in module ' + ref.moduleUrl
                            + ' — widget kind "' + ref.widgetKind
                            + '" stays on identity codec.');
                continue;
            }
            if (typeof codec.transformTo !== 'function'
             || typeof codec.transformFrom !== 'function') {
                console.error('[CodecRegistrar] codec for "' + ref.widgetKind
                            + '" missing transformTo / transformFrom — skipped.');
                continue;
            }
            this._registry.register(ref.widgetKind, codec);
        }
    }

    /** The substrate's no-op Params codec: empty in, empty out. */
    _identityCodec() {
        return {
            transformTo:   function (_p) { return {}; },
            transformFrom: function (_w) { return {}; }
        };
    }
}

CodecRegistrar.INSTANCE = new CodecRegistrar();
