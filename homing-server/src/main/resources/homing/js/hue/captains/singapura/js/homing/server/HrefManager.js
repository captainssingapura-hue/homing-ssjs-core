// =============================================================================
// Homing framework — href manager (RFC 0001 §4.0.1, Step 09)
//
// Auto-injected as `href` into any DomModule that imports an AppLink<?>.
// Sole sanctioned API for constructing or applying URLs in user JS.
//
// User code MUST use these six methods exclusively. Step 10 ships the
// conformance scanner that enforces it.
//
// Cross-page session keys (theme, locale) — every internal link constructed
// through this manager auto-inherits the current page's `?theme=` and
// `?locale=` query params, unless the link already specifies them. This
// keeps the user's theme + locale choice sticky across navigation without
// per-call boilerplate. External URLs and same-page fragments are left
// untouched.
// =============================================================================

const HrefManagerInstance = (() => {

    // Query-string keys that the framework propagates from the current page
    // onto every outgoing internal link. Keep this list small — these are
    // session-style preferences, not arbitrary data. Adding a key here makes
    // it sticky across every navigation site-wide.
    const _SESSION_KEYS = ["theme", "locale"];

    function _str(link, where) {
        if (typeof link !== "string") {
            throw new TypeError("href." + where + ": link must be a URL string (got " + typeof link + ")");
        }
        return link;
    }

    /**
     * Append the current page's session keys (`theme`, `locale`) to an
     * outgoing internal link if not already present. Returns the link
     * untouched when:
     *   - it's a same-page fragment (starts with `#`)
     *   - it has a URL scheme (mailto:, https:, tel:, …)
     *   - it's protocol-relative (starts with `//`)
     *   - the current page has no session keys to propagate
     *   - the link already sets the key (caller intent wins)
     */
    function _propagateSessionKeys(link) {
        if (typeof link !== "string" || link === "") return link;
        if (link.charAt(0) === "#") return link;
        if (link.indexOf("//") === 0) return link;       // protocol-relative
        if (/^[a-z][a-z0-9+.\-]*:/i.test(link)) return link;   // any scheme

        let pageSearch;
        try {
            pageSearch = new URLSearchParams(window.location.search);
        } catch (_) {
            return link;
        }

        // Split the link into path / query / fragment so we can mutate just the
        // query part without touching the rest.
        const hashIdx  = link.indexOf("#");
        const fragment = hashIdx >= 0 ? link.slice(hashIdx) : "";
        const beforeHash = hashIdx >= 0 ? link.slice(0, hashIdx) : link;
        const queryIdx = beforeHash.indexOf("?");
        const path     = queryIdx >= 0 ? beforeHash.slice(0, queryIdx) : beforeHash;
        const linkSearch = new URLSearchParams(queryIdx >= 0 ? beforeHash.slice(queryIdx + 1) : "");

        let mutated = false;
        for (let i = 0; i < _SESSION_KEYS.length; i++) {
            const k = _SESSION_KEYS[i];
            if (linkSearch.has(k)) continue;
            const v = pageSearch.get(k);
            if (v == null) continue;
            linkSearch.set(k, v);
            mutated = true;
        }
        if (!mutated) return link;

        const qs = linkSearch.toString();
        return path + (qs ? "?" + qs : "") + fragment;
    }

    /** Type-check + propagate session keys. Used by every link-accepting method. */
    function _link(link, where) {
        return _propagateSessionKeys(_str(link, where));
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
     * switch must not silently drop the app id it is sitting on.
     */
    function withParam(name, value) {
        if (typeof name !== "string" || !name) {
            throw new TypeError("href.withParam: name must be a non-empty string");
        }
        var params = new URLSearchParams(window.location.search);
        if (value === null || value === undefined || value === "") params.delete(name);
        else params.set(name, String(value));
        var q = params.toString();
        return window.location.pathname + (q ? "?" + q : "");
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
    });
})();
