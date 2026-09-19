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
// Two exports, by authority:
//
//   PreferenceViewInstance     read-only — preferred, override, resolve, and
//                              onChange. What every component holds. A holder
//                              of the view can ask and can listen; it cannot
//                              change anything, by construction (RFC 0063's
//                              observation-by-construction, one layer over).
//   PreferenceStewardInstance  the writer — remember, forget. Held only by
//                              the surfaces that let a user choose.
//
// A change notification carries NOTHING. A listener that wants details goes
// to the view: the one order of resolution stays in one place, and a listener
// cannot be handed a stale value. Changes made in another tab arrive the same
// way, through the storage event.
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

// ── Change notification ───────────────────────────────────────────────────────

const _listeners = new Set();

function _notify() {
    // A copy, so a listener that unsubscribes mid-notification is harmless.
    for (const fn of Array.from(_listeners)) {
        try { fn(); } catch (e) { console.error("[steward] listener failed", e); }
    }
}

// Another tab's pick reaches this one as a storage event; only ours count.
try {
    window.addEventListener("storage", function (ev) {
        if (ev && typeof ev.key === "string" && ev.key.indexOf(_PREFIX) === 0) _notify();
    });
} catch (_) { /* no window — a test context; same-page notifications still work */ }

// ── The view — read-only ──────────────────────────────────────────────────────

const PreferenceViewInstance = (() => {

    /** The stored value for a preference, or null. */
    function preferred(name) {
        const k = _key(name);          // a bad name is refused, not swallowed below
        const s = _store();
        if (!s) return null;
        try { return s.getItem(k); } catch (_) { return null; }
    }

    /**
     * The address's value for a preference — the override for this page — or
     * null when the address is silent. Read through href, the one reader of
     * the location.
     */
    function override(name) {
        _key(name);
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

    /**
     * Call fn() — with no arguments — whenever a preference changes, here or
     * in another tab. Returns the function that stops listening.
     */
    function onChange(fn) {
        if (typeof fn !== "function") {
            throw new TypeError("steward.onChange: fn must be a function (got " + typeof fn + ")");
        }
        _listeners.add(fn);
        return function () { _listeners.delete(fn); };
    }

    return Object.freeze({ preferred, override, resolve, onChange });
})();

// ── The steward — the writer ──────────────────────────────────────────────────

const PreferenceStewardInstance = (() => {

    /** Keep an explicit pick. Empty or null forgets. Listeners hear either way. */
    function remember(name, value) {
        if (value === null || value === undefined || value === "") { forget(name); return; }
        const k = _key(name);
        const s = _store();
        if (s) {
            try { s.setItem(k, String(value)); } catch (_) { /* cannot remember; still resolves */ }
        }
        _notify();
    }

    function forget(name) {
        const k = _key(name);
        const s = _store();
        if (s) {
            try { s.removeItem(k); } catch (_) { /* nothing to forget */ }
        }
        _notify();
    }

    return Object.freeze({ remember, forget });
})();
