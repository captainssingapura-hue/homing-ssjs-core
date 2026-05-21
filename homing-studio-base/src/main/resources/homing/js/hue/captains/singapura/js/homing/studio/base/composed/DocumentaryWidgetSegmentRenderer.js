// =============================================================================
// DocumentaryWidgetSegmentRenderer — embedded AppModule via dynamic
// ES module import (RFC 0024 P1c).
//
// renderDocumentaryWidgetSegment(branch, parent, seg, ctx) → void
//
// seg shape: { anchor, caption?, moduleUrl, params }
//
// The browser's native ES module loader handles transitive dep resolution;
// we just import(seg.moduleUrl) and call mod.appMain(host, params). The
// host is owned by the segment branch; appMain populates it via
// document.createElement (legacy contract).
//
// Per the RFC 0024 P1c design decision, embedded widgets keep the legacy
// appMain(host, params) shape — migration to the new
// mountInto(branch, parent, params) contract is opportunistic and tracked
// as separate per-widget ports.
// =============================================================================

function renderDocumentaryWidgetSegment(branch, parent, seg, ctx) {
    var section = branch.createElement('section', 'section');
    css.addClass(section, st_section);
    section.id = seg.anchor;

    var figure = branch.createElement('figure', 'figure');
    figure.style.cssText = 'margin:24px 0;padding:0 16px;'
                        + 'display:flex;flex-direction:column;align-items:center;';

    var host = branch.createElement('host', 'div');
    host.style.cssText = 'width:100%;max-width:1100px;min-height:480px;'
                      + 'border:1px solid var(--color-border,#C8C2A8);'
                      + 'border-radius:4px;overflow:hidden;'
                      + 'background:var(--color-surface-base,#fff);'
                      + 'position:relative;';

    var loading = branch.createElement('loading', 'div');
    css.addClass(loading, st_loading);
    loading.textContent = 'Loading widget…';
    loading.style.cssText += 'position:absolute;inset:0;display:flex;align-items:center;justify-content:center;';
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

    // Dynamic import — the browser handles the wrapped AppModule's typed
    // imports transitively. appMain takes the host element + a typed params
    // override so the cached EsModule can render different per-instance
    // content without query-string variation.
    import(seg.moduleUrl)
        .then(function(mod) {
            host.removeChild(loading);
            if (typeof mod.appMain === 'function') {
                mod.appMain(host, seg.params || {});
            } else {
                var err = document.createElement('div');
                css.addClass(err, st_error);
                err.textContent = 'Widget module missing appMain export: ' + seg.moduleUrl;
                host.appendChild(err);
            }
        })
        .catch(function(e) {
            if (host.contains(loading)) host.removeChild(loading);
            var err = document.createElement('div');
            css.addClass(err, st_error);
            err.textContent = 'Failed to load widget: ' + (e && e.message ? e.message : String(e));
            host.appendChild(err);
        });
}
