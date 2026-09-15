// =============================================================================
// Homing framework — preference steward (RFC 0064)
//
// Where a preference lives, and the one module that may go there. A
// preference is a value the user chose and the browser remembers — the theme
// today, the locale next, whatever Wish 0019 adds after — and it is resolved
// in one order, everywhere:
//
//   1. the ADDRESS, as an override for this page only  (?theme=forest)
//   2. the STORED preference                            (localStorage)
//   3. the fallback the caller names                    (the registry default)
//
// The address never writes the store: following a link is viewing, not
// choosing. Only an explicit pick — remember() — is kept.
//
// localStorage, not a cookie: the server does not resolve a context, so there
// is nothing for a cookie to carry to it; and localStorage is per origin
// (scheme, host AND port), so two studios on one host keep separate choices.
// State Belongs to the User names it as the default surface; No Stealth Data
// asks that the entry be inspectable, which "homing.theme" in DevTools is.
//
// Wrapped throughout: storage throws outright in some contexts (private mode
// with site data blocked), and a steward that cannot remember still resolves.
// =============================================================================

const PreferenceStewardInstance = (() => {

    const _PREFIX = "homing.";

    function _store() {
        try { return window.localStorage; } catch (_) { return null; }
    }

    function _key(name) {
        if (typeof name !== "string" || !name) {
            throw new TypeError("steward: preference name must be a non-empty string");
        }
        return _PREFIX + name;
    }

    /** The stored value for a preference, or null. */
    function preferred(name) {
        const s = _store();
        if (!s) return null;
        try { return s.getItem(_key(name)); } catch (_) { return null; }
    }

    /** Keep an explicit pick. Empty or null forgets. */
    function remember(name, value) {
        if (value === null || value === undefined || value === "") { forget(name); return; }
        const s = _store();
        if (!s) return;
        try { s.setItem(_key(name), String(value)); } catch (_) { /* cannot remember; still resolves */ }
    }

    function forget(name) {
        const s = _store();
        if (!s) return;
        try { s.removeItem(_key(name)); } catch (_) { /* nothing to forget */ }
    }

    /**
     * The address's value for a preference — the override for this page — or
     * null when the address is silent. Read through href, the one reader of
     * the location.
     */
    function override(name) {
        const url = HrefManagerInstance.current();
        const q = url.indexOf("?");
        if (q < 0) return null;
        const v = new URLSearchParams(url.slice(q + 1)).get(name);
        return v ? v : null;
    }

    /** Address, then store, then the caller's fallback (or null). */
    function resolve(name, fallback) {
        return override(name) || preferred(name) || fallback || null;
    }

    return Object.freeze({ preferred, remember, forget, override, resolve });
})();
