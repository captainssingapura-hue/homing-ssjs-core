// =============================================================================
// CatalogueTreeViewRenderer — RFC 0053 frontend renderer for CatalogueTreeView.
//
// renderCatalogueTreeView() -> Node
//
// Draws the catalogue as an INTERACTIVE tree using the generic TreeRenderer:
// collapsible branches, arrow-key navigation, Enter / double-click to open.
// Same wiring as the studio workspace's tree widgets (ModuleTreeWidget and the
// catalogue Navigator) — a DomOpsParty branch, a container, and the canonical
// TreeNode JSON — except this is an MPA page rather than a workspace pane, so
// the branch comes off the global domOpsParty singleton instead of a widget
// host, and activation is a real navigation instead of a party message.
//
// The tree itself costs no bespoke rendering code: /catalogue-parity serves the
// substrate's own TreeNode payload, and TreeRenderer reads only the universal
// dimensions. What this file adds is the join back to parity — the renderer
// addresses rows by child-index path (positional, per RFC 0040), so the endpoint
// ships the verdicts keyed the same way, and selection looks them up.
//
// Ownership: every element is minted through the branch, so dissolving it tears
// the page down cleanly. Styling is typed StudioStyles classes throughout — no
// inline style, no raw DOM factory, and navigation goes through the href
// manager rather than touching window.location.
// =============================================================================

// Navigation goes through HrefManagerInstance under its imported name, never an
// `href` alias. The manager is auto-injected AS `href` only for modules that
// import an AppLink; this one navigates to a path the server computed rather than
// to a named app, so no injection happens — and hand-writing `var href = ...`
// trips no-raw-href, whose first pattern is a bare `href =`. StudioElements
// carries exactly that line as baselined debt; there is no need to add more of it
// when the imported name works and window.location is still never touched.

// The branch's owner, tracked by WeakRef. Module-scoped and frozen so it is
// never collected while the page lives — the same sentinel shape the substrate
// uses for the root party itself. A workspace widget passes `this`; an MPA page
// has no such object, and a function-local owner would be collectible.
const _catalogueTreeViewOwner = Object.freeze({ toString: () => 'catalogueTreeView' });

function renderCatalogueTreeView() {
    var branch = domOpsParty.createBranch('catalogueTreeView');
    branch.activate(_catalogueTreeViewOwner);

    var root = branch.createElement('root', 'div');
    css.addClass(root, st_root);

    var main = branch.createElement('main', 'div');
    css.addClass(main, st_main);
    root.appendChild(main);

    // ── Summary ────────────────────────────────────────────────────────────
    var summary = branch.createElement('summary', 'div');
    css.addClass(summary, st_section);

    var verdict = branch.createElement('verdict', 'div');
    css.addClass(verdict, st_section_title);
    verdict.textContent = 'Catalogue tree';
    summary.appendChild(verdict);

    var counts = branch.createElement('counts', 'div');
    css.addClass(counts, st_subtitle);
    summary.appendChild(counts);

    // Follows the selection: what the tree derived, and which index answered.
    var detail = branch.createElement('detail', 'div');
    css.addClass(detail, st_subtitle);
    detail.textContent = 'Select a node — Enter or double-click opens it at its authentic path.';
    summary.appendChild(detail);

    main.appendChild(summary);

    // ── Tree ───────────────────────────────────────────────────────────────
    var container = branch.createElement('treeContainer', 'div');
    css.addClass(container, st_section);
    main.appendChild(container);

    var status = branch.createElement('status', 'div');
    css.addClass(status, st_loading);
    status.textContent = 'Loading catalogue tree...';
    container.appendChild(status);

    var renderer = null;
    var byPath = {};

    // The host owns WHEN keys flow; the renderer owns what they mean.
    var keyHandler = function (ev) {
        if (renderer && renderer.handleKeydown(ev)) ev.preventDefault();
    };
    document.addEventListener('keydown', keyHandler);

    fetch('/catalogue-parity')
        .then(function (r) {
            if (!r.ok) throw new Error('HTTP ' + r.status);
            return r.json();
        })
        .then(function (data) {
            var rows = data.rows || [];
            for (var i = 0; i < rows.length; i++) {
                byPath[(rows[i].path || []).join(',')] = rows[i];
            }

            verdict.textContent = data.differ === 0
                ? 'Catalogue tree — no disagreement'
                : 'Catalogue tree — ' + data.differ + ' disagree';
            counts.textContent = data.total + ' vertices · '
                + data.byIdentity + ' checked against the identity index · '
                + data.byStructure + ' resolved structurally only · '
                + data.unplaced + ' unplaced';

            container.removeChild(status);

            renderer = new TreeRenderer({
                branch:      branch,
                container:   container,
                data:        data.tree,
                expandDepth: 2,
                onSelect:    function (sel) {
                    var row = _rowFor(byPath, sel);
                    detail.textContent = row
                        ? row.derived + '  ·  ' + _note(row)
                        : '(no parity row for this position)';
                },
                onActivate:  function (sel) {
                    var row = _rowFor(byPath, sel);
                    if (row && row.authentic && row.authentic.length > 0) {
                        HrefManagerInstance.navigate(row.authentic);
                    }
                }
            });
        })
        .catch(function (err) {
            css.removeClass(status, st_loading);
            css.addClass(status, st_error);
            status.textContent = 'Failed to load the catalogue tree: '
                + (err && err.message ? err.message : String(err));
        });

    return root;
}

// The renderer addresses rows positionally; the endpoint ships verdicts keyed
// the same way. This is the join, and the only place the ordinal path is used.
function _rowFor(byPath, sel) {
    return byPath[((sel && sel.path) || []).join(',')];
}

function _note(row) {
    if (row.status === 'DIFFER') {
        return 'DISAGREES — registry says ' + row.authentic;
    }
    if (row.status === 'UNPLACED') {
        return 'unplaced — the registry positions this nowhere';
    }
    return row.via === 'IDENTITY'
        ? 'agrees, checked by identity'
        : 'agrees, but resolved structurally only';
}
