// WorkspaceGroupPathModule.js — RFC 0058: a kind is a path inside its group,
// and the anchor names it.
//
//   #ws/<section-slug>/<kind>
//
// Pure. Everything here is a function of the group the server served — its
// kinds in order, each with the section slug the SERVER derived — and the
// fragment the browser holds. Nothing re-derives a slug: the client agrees
// with the server because it reads what the server said.
//
// A group on the wire:
//   { id, title, summary, defaultKind,
//     kinds: [ { kind, title, section, sectionSlug }, … ] }   // section order, then spec order
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java WorkspaceGroupPathModule declaration — do not add import/export
// lines here.

var ANCHOR_PREFIX = "ws";

function _stripHash(hash) {
    var h = String(hash || "");
    return h.charAt(0) === "#" ? h.slice(1) : h;
}

function _find(kinds, kind) {
    for (var i = 0; i < (kinds || []).length; i++) if (kinds[i].kind === kind) return kinds[i];
    return null;
}

/**
 * "#ws/trading/trader" → { section: "trading", kind: "trader" }.
 * "#ws/trader" → { section: null, kind: "trader" } (a kind alone is accepted and
 * canonicalised). Anything not under the ws/ prefix — a heading anchor, nothing —
 * is null: not a workspace path, not this module's concern.
 */
function parseAnchor(hash) {
    var parts = _stripHash(hash).split("/").filter(function (p) { return p.length > 0; });
    if (parts.length < 2 || parts[0] !== ANCHOR_PREFIX) return null;
    if (parts.length === 2) return { section: null, kind: parts[1] };
    return { section: parts[1], kind: parts[parts.length - 1] };
}

/** The anchor (without '#') for a kind the group holds, or null. */
function anchorOf(kinds, kind) {
    var k = _find(kinds, kind);
    return k ? ANCHOR_PREFIX + "/" + k.sectionSlug + "/" + k.kind : null;
}

/**
 * Which kind the page opens, and why.
 *
 *   1. the anchor's kind, when the group holds it — canonicalised to its true
 *      section, so #ws/wrong/trader and #ws/trader both open trader at
 *      #ws/trading/trader;
 *   2. else the legacy ws_kind param, when the group holds it;
 *   3. else the group's default.
 *   A ws/ anchor that named a kind the group does not hold draws a notice in
 *   either of the last two cases — it lied, whatever opened instead. A non-ws
 *   anchor draws none: it was never a workspace path.
 *
 * → { kind, anchor, canonicalised, notice }
 */
function resolveKind(group, hash, legacyKind) {
    var kinds = group.kinds || [];
    var parsed = parseAnchor(hash);
    if (parsed && _find(kinds, parsed.kind)) {
        var canonical = anchorOf(kinds, parsed.kind);
        return { kind: parsed.kind, anchor: canonical,
                 canonicalised: _stripHash(hash) !== canonical, notice: null };
    }
    // A ws/ anchor that named a kind the group does not hold lied, whatever
    // opens instead: the notice names it either way.
    var opened = (legacyKind && _find(kinds, legacyKind)) ? _find(kinds, legacyKind)
               : (_find(kinds, group.defaultKind) || kinds[0]);
    var notice = parsed
            ? "No workspace at #" + _stripHash(hash) + " in " + group.title + " — opened " + (opened.title || opened.kind) + "."
            : null;
    return { kind: opened.kind, anchor: anchorOf(kinds, opened.kind), canonicalised: !!parsed, notice: notice };
}

/**
 * The crumbs the chrome appends after the trail the server stamped: the
 * section, then the kind. Neither is a server position, so neither links.
 */
function innerCrumbs(group, kind) {
    var k = _find(group.kinds, kind);
    if (!k) return [];
    return [ { text: k.section }, { text: k.title || k.kind } ];
}

/** A group of one — the shape a kind no group holds renders as (legacy). */
function soloGroup(spec) {
    return {
        id: spec.kind, title: spec.title || spec.kind, summary: "", defaultKind: spec.kind,
        kinds: [ { kind: spec.kind, title: spec.title || spec.kind,
                   section: spec.section || "Workspaces",
                   sectionSlug: _slug(spec.section || "Workspaces") } ]
    };
}

// Only soloGroup derives a slug, for a spec the server never grouped; a served
// group carries its slugs. Same shape as NodeName.conciseSlug for plain ASCII.
function _slug(s) {
    return String(s || "").toLowerCase().replace(/[^a-z0-9]+/g, "-").replace(/^-+|-+$/g, "").slice(0, 32) || "n";
}
