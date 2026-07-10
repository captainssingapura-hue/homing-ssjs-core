// =============================================================================
// NodeContent — the client twin of a doc-tree node's content object.
//
// new NodeContent(bundle) → { caption, segments }
//
// Accepts BOTH wire forms:
//   • legacy array:  [ seg, ... ]                  → { caption: null, segments }
//   • object form:   { caption, segments: [ ... ] } → { caption, segments }
//
// So the renderer reads one stable shape; caption-less nodes keep the old array.
// =============================================================================

function NodeContent(bundle) {
    if (Array.isArray(bundle)) {
        this.caption = null;
        this.segments = bundle;
    } else if (bundle && typeof bundle === 'object') {
        this.caption = bundle.caption || null;
        this.segments = bundle.segments || [];
    } else {
        this.caption = null;
        this.segments = [];
    }
}
