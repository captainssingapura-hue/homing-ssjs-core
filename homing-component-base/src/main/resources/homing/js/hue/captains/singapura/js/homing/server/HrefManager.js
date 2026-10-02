// =============================================================================
// Homing framework — href manager (RFC 0001 §4.0.1, Step 09)
//
// Auto-injected as `href` into any DomModule that imports an AppLink<?>.
// Sole sanctioned API for constructing or applying URLs in user JS.
//
// User code MUST use these six methods exclusively. Step 10 ships the
// conformance scanner that enforces it.
//
// A link is what its author wrote. This manager used to stamp the current
// page's ?theme= and ?locale= onto every internal link — "session keys",
// the address acting as the memory of a choice, because there was no other.
// RFC 0064 gave the choice a memory of its own (the preference steward), and
// with it the stamp became a leak: a shared ?theme= link followed the reader
// through the whole studio, outranking their own preference on every page.
// So nothing rewrites links any more. An explicit ?theme= or ?locale= an
// author writes INTO a link passes through untouched, and wins for the page
// it names — that is what an override is for.
// =============================================================================

const HrefManagerInstance = (() => {

    function _str(link, where) {
        if (typeof link !== "string") {
            throw new TypeError("href." + where + ": link must be a URL string (got " + typeof link + ")");
        }
        return link;
    }

    /** Type-check. Used by every link-accepting method; the link is returned as written. */
    function _link(link, where) {
        return _str(link, where);
    }

    function _attrEscape(s) {
        return String(s).replace(/&/g, "&amp;").replace(/"/g, "&quot;");
    }

    /** Returns an `href="..."` attribute fragment for safe inclusion in innerHTML. */
    function toAttr(link) {
        return 'href="' + _attrEscape(_link(link, "toAttr")) + '"';
    }

    /** Sets `el.href` to the link. Returns el for chaining. */
    function set(el, link) {
        if (!el) throw new Error("href.set: element required");
        el.setAttribute("href", _link(link, "set"));
        return el;
    }

    /** Creates a new <a> element with href set. opts: { text, className, target, rel, id }. */
    function create(link, opts) {
        const a = document.createElement("a");
        a.setAttribute("href", _link(link, "create"));
        if (opts) {
            if (opts.text != null)      a.textContent = opts.text;
            if (opts.className != null) a.className = opts.className;
            if (opts.target != null)    a.setAttribute("target", opts.target);
            if (opts.rel != null)       a.setAttribute("rel", opts.rel);
            if (opts.id != null)        a.id = opts.id;
        }
        return a;
    }

    /**
     * Opens the link in a new tab/window. opts: { windowFeatures, name }.
     * Returns the WindowProxy from window.open (may be null if blocked).
     */
    function openNew(link, opts) {
        const url = _link(link, "openNew");
        const target = (opts && opts.name) || "_blank";
        const features = opts && opts.windowFeatures;
        return window.open(url, target, features);
    }

    /**
     * Programmatic navigation. opts: { replace: true } to replace current entry
     * (no back-button entry); default is push (assign).
     */
    function navigate(link, opts) {
        const url = _link(link, "navigate");
        if (opts && opts.replace) {
            window.location.replace(url);
        } else {
            window.location.assign(url);
        }
    }


    /**
     * RFC 0058 — the current fragment without its '#', or "" when there is
     * none. A workspace kind is a path inside its group's page, named by the
     * anchor, and the fragment never reaches the server — so the chrome reads
     * it here, the one place window.location may be read (no-raw-href).
     */
    function hash() {
        var h = window.location.hash || "";
        return h.charAt(0) === "#" ? h.slice(1) : h;
    }

    /**
     * RFC 0058 — call fn(newHash) whenever the fragment changes; returns a
     * function that stops listening. A same-address anchor change is not a
     * navigation the browser reloads for, which is exactly why the chrome
     * needs to hear about it.
     */
    function onHashChange(fn) {
        if (typeof fn !== "function") {
            throw new TypeError("href.onHashChange: fn must be a function (got " + typeof fn + ")");
        }
        var handler = function () { fn(hash()); };
        window.addEventListener("hashchange", handler);
        return function () { window.removeEventListener("hashchange", handler); };
    }


    /**
     * Rewrite the current fragment in place — no history entry, no hashchange
     * event — so a canonicalised anchor shows as what it resolved to. Pass ""
     * to clear it.
     */
    function replaceHash(anchor) {
        if (typeof anchor !== "string") {
            throw new TypeError("href.replaceHash: anchor must be a string (got " + typeof anchor + ")");
        }
        var base = window.location.pathname + window.location.search;
        window.history.replaceState(window.history.state, "", anchor ? base + "#" + anchor : base);
    }
    /**
     * RFC 0064 — rewrite one query parameter of the current address in place:
     * no history entry, no navigation, fragment and history state preserved.
     * The sibling of replaceHash for the query. Transport only: what the
     * parameter means is the caller's business.
     */
    function replaceParam(name, value) {
        window.history.replaceState(window.history.state, "", withParam(name, value));
    }

    /** Reload the current address as it stands, fragment included. */
    function reload() {
        window.location.reload();
    }
    /** `href="#<slug>"` for same-page anchors. Slug must be a string. */
    function fragment(slug) {
        if (typeof slug !== "string") {
            throw new TypeError("href.fragment: slug must be a string (got " + typeof slug + ")");
        }
        return 'href="#' + _attrEscape(slug) + '"';
    }

    /**
     * The URL currently displayed, as path + query. The one sanctioned way for
     * a component to ask "where am I": no-raw-href forbids window.location
     * outside this manager, and until now offered no alternative — which is why
     * callers that genuinely needed it simply broke the rule.
     */
    function current() {
        return window.location.pathname + window.location.search;
    }

    /**
     * The current URL with one query parameter set, or removed when the value is
     * null/empty. Every other parameter survives, which is the point: a theme
     * switch must not silently drop the app id it is sitting on — nor, since
     * RFC 0058 put the workspace kind there, the fragment.
     */
    function withParam(name, value) {
        if (typeof name !== "string" || !name) {
            throw new TypeError("href.withParam: name must be a non-empty string");
        }
        var params = new URLSearchParams(window.location.search);
        if (value === null || value === undefined || value === "") params.delete(name);
        else params.set(name, String(value));
        var q = params.toString();
        return window.location.pathname + (q ? "?" + q : "") + (window.location.hash || "");
    }

    return Object.freeze({
        toAttr,
        set,
        create,
        openNew,
        navigate,
        fragment,
        current,
        withParam,
        hash,
        onHashChange,
        replaceHash,
        replaceParam,
        reload,
    });
})();
