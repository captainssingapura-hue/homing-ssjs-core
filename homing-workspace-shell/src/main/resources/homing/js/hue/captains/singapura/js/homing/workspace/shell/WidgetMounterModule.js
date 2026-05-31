// =============================================================================
// WidgetMounter — Phase 15 of the workspace-shell chrome.
//
// Three orthogonal pieces matching the I/O / pure-transform / DOM-mutation
// boundary:
//
//   resolve(entry)                    → Promise<module>      (I/O)
//   mount(mod, branch, entry, p, ctx) → controller            (pure transform)
//   attach(controller, tab, holder)   → void                  (DOM mutation)
//
// Explicit Substrate doctrine:
//   - Instance methods on INSTANCE singleton (no static).
//   - resolve returns Promise EXPLICITLY (no async sugar).
//   - mount + attach are sync.
//   - The one I/O collaborator (_importer) is held in constructor.
//
// Instance fields (graph-traversable):
//   _importer : (url) => Promise<module> — defaults to native dynamic import
// =============================================================================

class WidgetMounter {

    constructor(deps) {
        deps = deps || {};
        this._importer = deps.importer || (url => import(url));
    }

    /**
     * I/O: dynamic-import the widget's JS module. Returns Promise<module>.
     * The ONE async boundary in the entire mount flow.
     */
    resolve(entry) {
        if (!entry || !entry.moduleUrl) {
            return Promise.reject(new Error('[WidgetMounter] entry.moduleUrl is required'));
        }
        return this._importer(entry.moduleUrl);
    }

    /**
     * Pure transform: given a loaded module, construct + validate the
     * widget. Synchronous. Throws on invalid module or controller shape.
     */
    mount(mod, branch, entry, params, workspaceCtx) {
        if (!mod || typeof mod.construct !== 'function') {
            throw new Error('[WidgetMounter] module missing construct(): '
                          + (entry && entry.simpleName));
        }
        const controller = mod.construct(branch, params || {}, workspaceCtx || {});
        return this._validate(controller, entry && entry.simpleName);
    }

    /**
     * Pure DOM mutation: swap loading element for controller root, wire
     * controller callbacks onto the tab. Synchronous.
     */
    attach(controller, tab, holder) {
        if (holder && holder.contentEl) {
            while (holder.contentEl.firstChild) {
                holder.contentEl.removeChild(holder.contentEl.firstChild);
            }
            holder.contentEl.appendChild(controller.root);
        }
        tab.controller = controller;
        tab.setActive  = controller.setActive;
        if (typeof controller.partyDeregister === 'function') {
            tab.partyDeregister = controller.partyDeregister;
        }
    }

    /** Throws on bad controller shape. Returns the controller on success. */
    _validate(controller, simpleName) {
        if (!controller || !controller.root || typeof controller.setActive !== 'function') {
            throw new Error(
                '[WidgetMounter] ' + (simpleName || 'widget')
              + '.construct must return { root: Element, setActive: (boolean) => void } — '
              + 'got: ' + controller);
        }
        return controller;
    }
}

WidgetMounter.INSTANCE = new WidgetMounter();
