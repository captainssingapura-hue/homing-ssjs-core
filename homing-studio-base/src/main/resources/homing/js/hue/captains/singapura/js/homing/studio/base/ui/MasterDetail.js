// MasterDetail.js
//
// A tree on the LEFT, whatever the selected row is about on the RIGHT.
//
//   mountMasterDetail(opts) -> { navEl, bodyEl, renderer }
//
// The catalogue listing and the theme picker had arrived at the same fifteen
// lines independently — make a nav, make a detail, build a TreeRenderer over the
// nav, repaint the detail from onSelect — and had already drifted: one put the
// detail first and divided by the golden ratio, the other put the tree first and
// sized it to content. This is the one copy.
//
// What differs between them is only what the detail DRAWS, which stays with the
// caller: onSelect receives the selection and the body element and does as it
// likes with both.
//
// The nav is sized by its content (see MasterDetailStyles.md_nav), which is what
// makes table-of-contents-on-the-left workable. Not knowing how wide a tree
// wanted to be is why the catalogue used a ratio instead; that is answered now.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java MasterDetail declaration — do not add import/export lines here.

var _mdSeq = 0;

/**
 * opts:
 *   branch       (required) DomOpsParty branch — the caller owns the lifetime
 *   host         (required) element to append the pair into
 *   data         (required) tree payload for TreeRenderer.setData
 *   onSelect     (bodyEl, sel) => void   — repaint the detail
 *   onActivate   (sel) => void           — Enter / double-click
 *   expandDepth, showBadge, showNote, showRoot, hrefForPath — passed through
 *
 * Returns { navEl, bodyEl, renderer }. The caller forwards keydown: the host
 * owns WHEN keys flow, the renderer owns what they mean.
 */
function mountMasterDetail(opts) {
    if (!opts || !opts.branch) throw new Error("mountMasterDetail: opts.branch is required");
    if (!opts.host)            throw new Error("mountMasterDetail: opts.host is required");
    if (!opts.data)            throw new Error("mountMasterDetail: opts.data is required");

    var branch = opts.branch;
    var seq = ++_mdSeq;

    var split = branch.createElement("mdsplit" + seq, "div");
    css.addClass(split, md_split);

    // Nav first: table of contents beside content is the order a reader knows,
    // and it is the reading order too.
    var navEl = branch.createElement("mdnav" + seq, "div");
    css.addClass(navEl, md_nav);
    navEl.setAttribute("tabindex", "0");
    split.appendChild(navEl);

    var bodyEl = branch.createElement("mdbody" + seq, "div");
    css.addClass(bodyEl, md_body);
    split.appendChild(bodyEl);

    opts.host.appendChild(split);

    var onSelect = opts.onSelect || function () {};
    var onActivate = opts.onActivate || function () {};

    var renderer = new TreeRenderer({
        branch:      branch,
        container:   navEl,
        expandDepth: (typeof opts.expandDepth === "number") ? opts.expandDepth : 0,
        showBadge:   !!opts.showBadge,
        showNote:    !!opts.showNote,
        showRoot:    !!opts.showRoot,
        hrefForPath: opts.hrefForPath || null,
        onSelect:    function (sel) { onSelect(bodyEl, sel); },
        onActivate:  function (sel) { onActivate(sel); }
    });
    renderer.setData(opts.data);

    return { splitEl: split, navEl: navEl, bodyEl: bodyEl, renderer: renderer };
}
