// =============================================================================
// ComposedSegmentRenderer — recursive ComposedDoc embedding (RFC 0024 P1c).
//
// renderComposedSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, caption?, composedDocId }
// ctx shape: { renderingStack: [docId, ...], mountComposed: function }
//
// The new value RFC 0024 P1c adds: a ComposedDoc can embed another
// ComposedDoc inline. The recursion threads ctx.renderingStack forward;
// when the nested doc's id appears in the stack already (a cycle), a
// "cycle detected" placeholder renders instead of infinite recursion.
//
// Visual depth indication
// ───────────────────────
// The embedded subtree is wrapped in a depth-coloured callout container
// (left border + tinted background + a "Level N" badge top-right), so
// recursion levels are visually distinguishable at a glance. The colour
// palette cycles every five levels (depth 1 = blue, 2 = purple, 3 = pink,
// 4 = red, 5 = orange). Adjacent levels carry contrasting hues; deep
// recursion stays legible because the badge always names the level.
//
// DomOpsParty handles the ownership cascade: dissolving the outer widget
// dissolves every nested ComposedWidget's branch, recursively, in one
// call. No widget-author care required.
// =============================================================================

// Palette of (border, background-tint) pairs per nesting depth. Index = depth
// modulo palette length; deeper levels cycle back through the same hues with
// the badge text disambiguating "this is actually level 7" etc.
var COMPOSED_LEVEL_PALETTE = [
    { border: '#5599ff', tint: 'rgba(85,153,255,0.05)' },  // depth 1 — blue
    { border: '#b47bff', tint: 'rgba(180,123,255,0.05)' }, // depth 2 — purple
    { border: '#ff7bea', tint: 'rgba(255,123,234,0.05)' }, // depth 3 — pink
    { border: '#ff7b7b', tint: 'rgba(255,123,123,0.05)' }, // depth 4 — red
    { border: '#ffaa3b', tint: 'rgba(255,170,59,0.05)' }   // depth 5 — orange
];

function renderComposedSegment(branch, parent, seg, ctx) {
    var section = branch.createElement('section', 'section');
    css.addClass(section, st_section);
    section.id = seg.anchor;

    if (seg.caption) {
        var h = branch.createElement('caption', 'h2');
        css.addClass(h, st_section_title);
        h.textContent = seg.caption;
        section.appendChild(h);
    }

    // Depth at the about-to-mount level: renderingStack.length tracks how
    // many docs are CURRENTLY being rendered above; the nested one will be
    // length+1. depth=1 means "first embed" (i.e., the outer doc was 0).
    var stack = (ctx && ctx.renderingStack) ? ctx.renderingStack : [];
    var depth = stack.length;  // the depth the nested mount will be at
    var palette = COMPOSED_LEVEL_PALETTE[(depth - 1) % COMPOSED_LEVEL_PALETTE.length];

    var host = branch.createElement('host', 'div');
    host.style.cssText = 'position:relative;margin:16px 0;padding:24px 20px 20px 20px;'
                      + 'border-left:4px solid ' + palette.border + ';'
                      + 'border-radius:0 4px 4px 0;'
                      + 'background:' + palette.tint + ';';

    // Top-right depth badge — small, low-key, unambiguous about which
    // recursion level this is. Width-content allows the doc title beside it.
    var badge = branch.createElement('badge', 'div');
    badge.textContent = 'Embedded · Level ' + depth;
    badge.style.cssText = 'position:absolute;top:6px;right:10px;'
                       + 'font-size:11px;font-weight:600;'
                       + 'color:' + palette.border + ';'
                       + 'letter-spacing:0.04em;text-transform:uppercase;';
    host.appendChild(badge);

    section.appendChild(host);
    parent.appendChild(section);

    // ── Cycle detection ──
    if (stack.indexOf(seg.composedDocId) >= 0) {
        var cycle = branch.createElement('cycle', 'div');
        css.addClass(cycle, st_error);
        cycle.textContent = 'Cycle detected — composed doc '
                          + seg.composedDocId
                          + ' is already being rendered higher in the stack.';
        host.appendChild(cycle);
        return;
    }

    // ── Recursive mount ──
    // The nested ComposedWidget lives in its own sub-branch under this
    // segment's branch. ctx.mountComposed is the orchestrator's mountInto
    // function — calling it here re-enters the same code path with a
    // sub-branch and an extended renderingStack.
    var nestedBranch = branch.createBranch('nested');
    var nestedOwner = Object.freeze({ toString: function(){
        return 'composedSegmentNested:' + seg.composedDocId;
    } });
    nestedBranch.activate(nestedOwner);

    ctx.mountComposed(nestedBranch, host, {
        id: seg.composedDocId,
        __renderingStack: stack
    });
}
