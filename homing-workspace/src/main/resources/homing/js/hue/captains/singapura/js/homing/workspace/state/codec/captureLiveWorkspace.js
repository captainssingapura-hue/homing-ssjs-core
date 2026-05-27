// =============================================================================
// captureLiveWorkspace — view → WorkspaceState helper for RFC 0029.
//
// Takes an abstract "workspace view" object that exposes the four
// axes of workspace state (kind / layout / widgets / chrome), and
// constructs a typed WorkspaceState. The view's shape is the contract
// the actual workspace shell implements at integration time; the
// capture function itself doesn't know how the view's data is sourced
// (DOM walk, internal cache, computed property — all opaque).
//
// Per the RFC 0029 design pass: the three orthogonal axes are captured
// via the same single call here, with the workspace shell responsible
// for keeping its own per-axis caches up to date as events fire. The
// shell calls captureLiveWorkspace(view) → encode → persister.save just
// when the debounced save timer fires; bursts of axis-specific updates
// coalesce into one composite read.
// =============================================================================

function captureLiveWorkspace(view) {
    if (!view) {
        throw new TypeError("captureLiveWorkspace: view is required");
    }
    if (typeof view.workspaceKind !== 'function') {
        throw new TypeError("captureLiveWorkspace: view.workspaceKind() is required");
    }
    if (typeof view.layout !== 'function') {
        throw new TypeError("captureLiveWorkspace: view.layout() is required");
    }
    if (typeof view.widgets !== 'function') {
        throw new TypeError("captureLiveWorkspace: view.widgets() is required");
    }
    if (typeof view.chrome !== 'function') {
        throw new TypeError("captureLiveWorkspace: view.chrome() is required");
    }

    const widgetsById = new Map();
    for (const w of view.widgets()) {
        if (!(w instanceof WidgetInstance)) {
            throw new TypeError(
                "captureLiveWorkspace: view.widgets() must yield WidgetInstance values");
        }
        widgetsById.set(w.id, w);
    }

    return new WorkspaceState(
        WorkspaceState.CURRENT_SCHEMA_VERSION,
        view.workspaceKind(),
        new Date(),                  // wall-clock capture timestamp
        view.layout(),
        widgetsById,
        view.chrome()
    );
}
