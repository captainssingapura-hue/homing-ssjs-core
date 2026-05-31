// =============================================================================
// ImageViewerRenderer — shared renderer for ImageViewer (RFC 0020).
//
// renderImage({ docId, host }) → void
//
// Fetches /doc?id=<docId> (JSON envelope: { alt, caption, dataUrl,
// width?, height? }) and renders a <figure> with the data-URL <img>
// and optional caption.
// =============================================================================

function renderImage(props) {
    var docUrl = props.docUrl;
    var docId  = props.docId;
    var host   = props.host;

    var fetchUrl;
    if (docUrl) {
        fetchUrl = docUrl;
    } else if (docId) {
        fetchUrl = "/doc?id=" + encodeURIComponent(docId);
    } else {
        var errMsg = document.createElement("div");
        css.addClass(errMsg, st_error);
        errMsg.textContent = "No image reference supplied. Use docUrl or docId.";
        host.replaceChildren(errMsg);
        return;
    }

    var loading = document.createElement("div");
    css.addClass(loading, st_loading);
    loading.textContent = "Loading…";
    host.replaceChildren(loading);

    fetch(fetchUrl)
        .then(function(r) {
            if (!r.ok) throw new Error("HTTP " + r.status);
            return r.json();
        })
        .then(function(env) {
            var figure = document.createElement("figure");
            css.addClass(figure, st_image_figure);

            var img = document.createElement("img");
            css.addClass(img, st_image_img);
            img.src = env.dataUrl;
            img.alt = env.alt || "";
            if (env.width)  img.setAttribute("width",  String(env.width));
            if (env.height) img.setAttribute("height", String(env.height));
            figure.appendChild(img);

            if (env.caption) {
                var cap = document.createElement("figcaption");
                css.addClass(cap, st_image_caption);
                cap.textContent = env.caption;
                figure.appendChild(cap);
            }

            host.replaceChildren(figure);
        })
        .catch(function(err) {
            var errEl = document.createElement("div");
            css.addClass(errEl, st_error);
            errEl.textContent = "Failed to load image: " + err.message;
            host.replaceChildren(errEl);
        });
}
