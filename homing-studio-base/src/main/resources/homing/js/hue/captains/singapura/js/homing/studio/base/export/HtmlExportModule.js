// =============================================================================
// HtmlExportModule — client-side standalone HTML export for any rendered page.
//
// exportPageAsHtml(filename) → Promise<void>
//
// Produces a self-contained .html file the browser downloads immediately.
// Designed as the offline-reading alternative to Chrome's MHTML export, which
// strips all JavaScript and absolutizes fragment hrefs (breaking TOC nav).
//
// What this function does:
//
//   1. Fetches every linked stylesheet in parallel from the live server
//      and inlines them as a single <style> block — so the file renders
//      correctly with no server.
//
//   2. Clones the fully-rendered document and strips <script> elements
//      from the clone.
//
//   3. Fragment hrefs (#anchor) are correct by construction: HrefManager
//      stores the raw "#anchor" string as the DOM content attribute.
//      innerHTML serialisation uses content attributes, not the IDL-resolved
//      property, so bare #anchor hrefs appear in the output unchanged —
//      no rewriting needed.
//
//   4. Serialises head.innerHTML + body.innerHTML into a complete
//      <!DOCTYPE html> document and triggers a Blob-URL download.
//
// Image src attributes that point to the live server (e.g. /asset?id=...)
// will not resolve offline — accepted limitation for v1.
// =============================================================================

function exportPageAsHtml(filename) {

    // ── 1. Collect stylesheet <link> hrefs ──────────────────────────────────
    var linkEls = document.querySelectorAll('link[rel="stylesheet"]');
    var fetchPromises = [];
    for (var li = 0; li < linkEls.length; li++) {
        var linkHref = linkEls[li].href; // resolved absolute URL
        // Skip non-http origins (data:, cid: embedded in MHTML, etc.)
        if (!linkHref || linkHref.indexOf('http') !== 0) {
            fetchPromises.push(Promise.resolve(null));
            continue;
        }
        // Capture linkHref for the closure
        (function(url) {
            fetchPromises.push(
                fetch(url)
                    .then(function(r) { return r.ok ? r.text() : null; })
                    .catch(function()  { return null; })
            );
        })(linkHref);
    }

    return Promise.all(fetchPromises).then(function(cssTexts) {

        // ── 2. Build the inlined CSS block ───────────────────────────────────
        var cssBlocks = [];
        for (var ci = 0; ci < cssTexts.length; ci++) {
            if (cssTexts[ci]) cssBlocks.push(cssTexts[ci]);
        }
        var cssText = cssBlocks.join('\n\n/* ---- */\n\n');

        // ── 3. Clone the rendered document ───────────────────────────────────
        var clone     = document.documentElement.cloneNode(true);
        var cloneHead = clone.querySelector('head');
        var cloneBody = clone.querySelector('body');

        // ── 4. Strip scripts and any element marked data-export-exclude ─────
        // data-export-exclude: convention for interactive controls (export
        // button, theme picker actions, etc.) that have no meaning in a
        // static offline file.
        var scripts = clone.querySelectorAll('script, [data-export-exclude]');
        for (var si = 0; si < scripts.length; si++) {
            scripts[si].remove();
        }

        // ── 5. Replace <link rel=stylesheet> with a single inlined <style> ───
        var styleLinks = clone.querySelectorAll('link[rel="stylesheet"]');
        for (var sli = 0; sli < styleLinks.length; sli++) {
            styleLinks[sli].remove();
        }
        if (cloneHead && cssText) {
            var styleEl = document.createElement('style');
            styleEl.textContent = cssText;
            cloneHead.insertBefore(styleEl, cloneHead.firstChild);
        }

        // ── 6. Serialize ──────────────────────────────────────────────────────
        // innerHTML uses the DOM content attribute for href (the raw string set
        // via setAttribute), not the IDL-resolved property. HrefManager stores
        // "#anchor" as the content attribute, so TOC and ref links come out as
        // href="#anchor" in the output — correct for offline navigation without
        // any explicit rewriting.
        var lang     = document.documentElement.getAttribute('lang') || 'en';
        var headHtml = cloneHead ? cloneHead.innerHTML : '';
        var bodyHtml = cloneBody ? cloneBody.innerHTML : '';
        var html = '<!DOCTYPE html>\n'
                 + '<html lang="' + lang + '">\n'
                 + '<head>' + headHtml + '</head>\n'
                 + '<body>' + bodyHtml + '</body>\n'
                 + '</html>';

        // ── 7. Trigger download ───────────────────────────────────────────────
        var blob    = new Blob([html], { type: 'text/html;charset=utf-8' });
        var blobUrl = URL.createObjectURL(blob);
        var dlEl    = document.createElement('a');
        dlEl.href     = blobUrl;
        dlEl.download = filename || 'export.html';
        document.body.appendChild(dlEl);
        dlEl.click();
        document.body.removeChild(dlEl);
        // Revoke after a generous delay — the download dialog may need a moment.
        setTimeout(function() { URL.revokeObjectURL(blobUrl); }, 60000);
    });
}
