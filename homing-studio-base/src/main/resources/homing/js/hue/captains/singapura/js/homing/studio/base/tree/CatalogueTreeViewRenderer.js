// =============================================================================
// CatalogueTreeViewRenderer — RFC 0053 frontend renderer for CatalogueTreeView.
//
// renderCatalogueTreeView() -> Node
//
// Fetches /catalogue-parity and renders the catalogue as a TREE listing: one
// row per vertex, indented by depth, each linking to the authentic URL that
// vertex resolves at today.
//
// Every row is also a comparison. The path shown is DERIVED by chaining the
// normalized tree's segments; the link is the AUTHENTIC path the live registry
// gives. A row marked by identity had those two answers produced independently,
// so its agreement is a real check. A row marked structural means the registry
// holds no identity-keyed entry for that vertex and re-derived the path the same
// way the tree did — the gap RFC 0053 Phase 1 closes, shown rather than hidden.
//
// Composed entirely from StudioElements builders, so this file mints no raw DOM
// of its own and owns no DomOpsParty branch.
// =============================================================================

function renderCatalogueTreeView() {
    var host = Panel({ children: ["Loading catalogue tree..."] });

    fetch("/catalogue-parity")
        .then(function (r) {
            if (!r.ok) throw new Error("HTTP " + r.status);
            return r.json();
        })
        .then(function (data) {
            host.replaceChildren(_summary(data), _listing(data));
        })
        .catch(function (err) {
            host.replaceChildren(Panel({
                title: "Could not load the catalogue tree",
                children: [String(err && err.message ? err.message : err)]
            }));
        });

    return host;
}

// ---------- summary ----------
// The counts, so a regression is a number rather than a visual diff.
function _summary(data) {
    var checked = "" + data.byIdentity + " checked against the identity index";
    var derivedOnly = "" + data.byStructure + " resolved structurally only";
    var verdict = data.differ === 0
        ? "No disagreement."
        : data.differ + " vertices resolve somewhere other than the tree says.";

    return Panel({
        title: "Parity — " + data.total + " vertices",
        children: [
            Listing({
                children: [
                    ListItem({
                        marker: data.differ === 0 ? "OK" : "!!",
                        label: verdict,
                        description: checked + " · " + derivedOnly,
                        met: data.differ === 0
                    }),
                    ListItem({
                        marker: "" + data.unplaced,
                        label: "unplaced",
                        description: "vertices the registry places nowhere at all"
                    })
                ]
            })
        ]
    });
}

// ---------- the tree ----------
function _listing(data) {
    var rows = data.rows || [];
    var items = [];
    for (var i = 0; i < rows.length; i++) {
        items.push(_row(rows[i]));
    }
    return Listing({ title: "Catalogue tree", children: items });
}

function _row(r) {
    var marker = r.status === "AGREE" ? "✓"
               : r.status === "DIFFER" ? "✗"
               : "·";

    // Indent by depth. Non-breaking, because HTML collapses runs of spaces.
    var indent = "";
    for (var d = 0; d < r.depth; d++) indent += "   ";

    var note = r.via === "IDENTITY" ? "by identity"
             : r.via === "STRUCTURAL" ? "structural only"
             : "unplaced";

    var detail = r.derived + "  ·  " + note;
    if (r.status === "DIFFER") {
        detail = "derived " + r.derived + "  ≠  authentic " + r.authentic;
    }

    return ListItem({
        marker: marker,
        label: indent + r.label,
        description: detail,
        href: r.authentic && r.authentic.length > 0 ? r.authentic : null,
        // Green only where the check was real: agreement reached through two
        // independent derivations. A structural row agrees with itself.
        met: r.status === "AGREE" && r.via === "IDENTITY"
    });
}
