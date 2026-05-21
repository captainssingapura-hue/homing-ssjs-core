// =============================================================================
// SvgSegmentRenderer — inline SVG fetched from a referenced SvgDoc
// (RFC 0024 P1c).
//
// renderSvgSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, caption?, svgUrl }
// =============================================================================

function renderSvgSegment(branch, parent, seg, ctx) {
    var section = branch.createElement('section', 'section');
    css.addClass(section, st_section);
    section.id = seg.anchor;

    var figure = branch.createElement('figure', 'figure');
    figure.style.cssText = 'margin:24px 0;padding:0 16px;'
                        + 'display:flex;flex-direction:column;align-items:center;';

    var host = branch.createElement('host', 'div');
    host.style.cssText = 'width:100%;max-width:1100px;display:flex;justify-content:center;';
    var loading = branch.createElement('loading', 'div');
    css.addClass(loading, st_loading);
    loading.textContent = 'Loading SVG…';
    host.appendChild(loading);
    figure.appendChild(host);

    if (seg.caption) {
        var caption = branch.createElement('caption', 'figcaption');
        caption.style.cssText = 'margin-top:8px;font-size:13px;color:var(--st-gray-mid,#666);text-align:center;';
        caption.textContent = seg.caption;
        figure.appendChild(caption);
    }

    section.appendChild(figure);
    parent.appendChild(section);

    fetch(seg.svgUrl)
        .then(function(r) {
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return r.text();
        })
        .then(function(svg) {
            var range = document.createRange();
            range.selectNodeContents(host);
            host.replaceChildren(range.createContextualFragment(svg));
            var svgEl = host.querySelector('svg');
            if (svgEl) {
                svgEl.style.width = '100%';
                svgEl.style.height = 'auto';
            }
        })
        .catch(function(err) {
            var errEl = document.createElement('div');
            css.addClass(errEl, st_error);
            errEl.textContent = 'Failed to load SVG: ' + err.message;
            host.replaceChildren(errEl);
        });
}
