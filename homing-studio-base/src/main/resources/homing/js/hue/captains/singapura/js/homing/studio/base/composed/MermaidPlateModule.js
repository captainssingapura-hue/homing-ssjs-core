// =============================================================================
// MermaidPlateModule — a ```mermaid fence, drawn.
//
// renderMermaidPlate(branch, parent, source) → void
//
// Builds the fence first and renders over it:
//
//   <div class="st-mermaid">                    ← owned, 'mermaidPlate'
//     <pre><code class="language-mermaid">…</code></pre>   ← branch 'mermaidFence'
//   </div>
//
// The fence is what the reader sees while the library loads, and what it keeps
// if the library never arrives. It lives in its own sub-branch so a successful
// render retires it in one dissolveBranch call and the SVG is left alone on the
// plate — no manual detaching, and the fence's names are freed with it.
//
// The library is dynamic-imported LAZILY, from here and nowhere else, so a
// diagram-free doc never touches the network. The same-origin proxy
// (MermaidProxyModule) is the single seam that reaches a CDN; a deployment that
// can't reach it points the proxy elsewhere via
// ExternalModuleUrlRegistry.override(MermaidProxyModule.class, …). On any
// failure — offline, blocked CDN, malformed diagram — the fence stays and a
// one-line note says which.
//
// The plate itself is not decoration: Mermaid draws dark ink on a transparent
// ground and never hears about the theme, so on a dark theme the diagram is
// unreadable. st_mermaid mixes a light ground from the theme's own surface.
//
// ONE plate per branch, by construction — the element and branch names are
// fixed, so a second call on the same branch throws. That is the right shape
// here: a plate belongs to a single code segment, which has its own branch.
// =============================================================================
var MERMAID_PROXY_URL = "/module?class=hue.captains.singapura.js.homing.libs.MermaidProxyModule";

// Page-unique, and deliberately NOT the segment anchor: Mermaid addresses the
// SVG it is building by this id, and an anchor carries the node's name-path
// (slashes and all), which is not a legal selector.
var _mermaidSeq = 0;

function _mermaidNote(branch, plate, msg, err) {
    console.error("[mermaid]", msg, err);
    var note = branch.createElement('mermaidNote', 'div');
    css.addClass(note, st_mermaid_note);
    note.textContent = msg;
    plate.appendChild(note);
}

function renderMermaidPlate(branch, parent, source) {
    var plate = branch.createElement('mermaidPlate', 'div');

    var id = 'mermaid-' + (++_mermaidSeq);

    var fence = branch.createBranch('mermaidFence');
    fence.activate(Object.freeze({ toString: function () { return 'mermaidFence:' + id; } }));
    var pre = fence.createElement('pre', 'pre');
    var code = fence.createElement('code', 'code');
    // No "language-mermaid" class: that marker existed so a DOM sweep could
    // find the fence, and the sweep is what this module replaces. Nothing
    // selects it here — the plate holds the element it minted.
    code.textContent = source;
    pre.appendChild(code);
    plate.appendChild(pre);
    parent.appendChild(plate);

    import(MERMAID_PROXY_URL).then(function (mod) {
        mod.renderMermaid(id, source).then(function (svg) {
            // Parse BEFORE dissolving: a diagram that renders to malformed
            // markup must not cost the reader the source it came from.
            var frag = document.createRange().createContextualFragment(svg);
            branch.dissolveBranch('mermaidFence');
            css.addClass(plate, st_mermaid);
            plate.appendChild(frag);
        }).catch(function (err) {
            _mermaidNote(branch, plate, "Diagram failed to render: "
                + (err && err.message ? err.message : String(err)), err);
        });
    }).catch(function (err) {
        _mermaidNote(branch, plate,
            "Mermaid library could not be loaded (offline or blocked CDN). Point the proxy "
            + "at a reachable URL via ExternalModuleUrlRegistry.override(MermaidProxyModule.class, ...).", err);
    });
}
