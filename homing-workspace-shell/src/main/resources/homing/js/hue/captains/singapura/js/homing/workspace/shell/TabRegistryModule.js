// =============================================================================
// TabRegistry — Phase 12 of the workspace-shell chrome.
//
// Per-workspace in-memory index: widgetInstanceUuid → {tab, slotId,
// widgetKind, controller}. Spawn paths call register(...) on mount;
// move paths call updateSlot(uuid, newSlotId); close paths call
// unregister(uuid). Event-replay handlers (Phase 9) read via
// lookup(uuid) to find the live tab corresponding to a recorded UUID.
//
// Explicit Substrate doctrine:
//   - Per-workspace instance (NOT INSTANCE — one registry per shell).
//   - No external collaborators; pure state container.
//   - All methods are instance methods.
// =============================================================================

class TabRegistry {

    constructor() {
        // widgetInstanceUuid → { tab, slotId, widgetKind, controller }
        this._byUuid = new Map();
    }

    /**
     * Record a freshly mounted widget. {tab, slotId, widgetKind} are
     * the substrate-relevant fields; controller is optional and
     * carried for callers that want to setActive on it later.
     */
    register(entry) {
        if (!entry)            throw new Error('[TabRegistry] entry required');
        if (!entry.widgetInstanceUuid) {
            throw new Error('[TabRegistry] entry.widgetInstanceUuid required');
        }
        if (!entry.tab)        throw new Error('[TabRegistry] entry.tab required');
        if (!entry.slotId)     throw new Error('[TabRegistry] entry.slotId required');
        if (!entry.widgetKind) throw new Error('[TabRegistry] entry.widgetKind required');
        this._byUuid.set(entry.widgetInstanceUuid, {
            tab:        entry.tab,
            slotId:     entry.slotId,
            widgetKind: entry.widgetKind,
            controller: entry.controller || null
        });
    }

    /** Drop the entry for a UUID. Idempotent — missing UUID is a no-op. */
    unregister(widgetInstanceUuid) {
        this._byUuid.delete(widgetInstanceUuid);
    }

    /** Update the slotId after a move. Idempotent — missing UUID is a no-op. */
    updateSlot(widgetInstanceUuid, newSlotId) {
        const e = this._byUuid.get(widgetInstanceUuid);
        if (!e) return;
        e.slotId = newSlotId;
    }

    /** Returns {tab, slotId, widgetKind, controller} or null. */
    lookup(widgetInstanceUuid) {
        return this._byUuid.get(widgetInstanceUuid) || null;
    }

    /** Returns the tabId for a UUID, or null. */
    tabIdOf(widgetInstanceUuid) {
        const e = this._byUuid.get(widgetInstanceUuid);
        return e ? e.tab.id : null;
    }

    /**
     * Reverse lookup: given an MTP-level tabId, find the
     * {widgetInstanceUuid, entry} pair. Needed because picker-spawned
     * tabs have id 'picker:N' but widgetInstanceUuid 'Kind:N+1' — the
     * two are NOT the same string, and MTP callbacks surface tab.id.
     * Returns {widgetInstanceUuid, entry} or null.
     */
    findByTabId(tabId) {
        if (tabId == null) return null;
        let hit = null;
        this._byUuid.forEach(function (entry, uuid) {
            if (hit) return;
            if (entry.tab && entry.tab.id === tabId) {
                hit = { widgetInstanceUuid: uuid, entry: entry };
            }
        });
        return hit;
    }

    /** All registered UUIDs (for inspect / dev tools). */
    uuids() {
        return Array.from(this._byUuid.keys());
    }

    size() { return this._byUuid.size; }

    /** Inspect snapshot (Diligent Secretaries pillar 2). */
    inspect() {
        const entries = [];
        this._byUuid.forEach(function (v, k) {
            entries.push({
                widgetInstanceUuid: k,
                slotId:             v.slotId,
                widgetKind:         v.widgetKind,
                tabId:              v.tab.id
            });
        });
        return { size: this._byUuid.size, entries: entries };
    }
}
