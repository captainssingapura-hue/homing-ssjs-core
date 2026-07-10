// =============================================================================
// CaptionRenderer — a node's optional highlighted header.
//
// renderCaption(text, host) → void
//
// Drawn above the node's body as <h2 class="st_section_title"> — the "fancy"
// header. A plain element appended to the branch-owned content host (it cascades
// out with the host on dissolve, like a list's <li>s), so it needs no branch of
// its own. Injected into renderDocTree as the renderCaption callback.
// =============================================================================

function renderCaption(text, host) {
    var h = document.createElement('h2');
    css.addClass(h, st_section_title);
    h.textContent = text;
    host.appendChild(h);
}
