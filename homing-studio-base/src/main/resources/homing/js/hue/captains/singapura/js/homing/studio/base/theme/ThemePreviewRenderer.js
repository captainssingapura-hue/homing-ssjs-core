// ThemePreviewRenderer — a theme, shown by wearing it.
//
// renderThemePreview() → Node
//
// One page carrying the elements the studio's pages are made of — kicker,
// title, cards, badges, a listing, a progress bar, a task list, a table, a
// footer — under whatever theme the address names. The picker embeds this page
// in a frame and reloads the frame per selection, so a theme is previewed by
// rendering it rather than by a strip of its palette. The palette and the
// overrides come with the page, because the page is a page.
//
// No header. The header carries the picker, and a picker inside the picker's
// own preview is a hall of mirrors.
//
// The framework's EsModuleWriter appends the import/export prologue from the
// matching Java ThemePreviewRenderer declaration — do not add import/export
// lines here.

// Module-scoped, frozen owner — the page lives as long as the document does.
var _previewOwner = Object.freeze({ toString: function () { return "themePreview"; } });

function renderThemePreview() {
    var branch = domOpsParty.createBranch("themePreview");
    branch.activate(_previewOwner);

    var root = branch.createElement("root", "div");
    css.addClass(root, st_root);
    var main = branch.createElement("main", "main");
    css.addClass(main, st_main);
    root.appendChild(main);

    var kicker = branch.createElement("kicker", "div");
    css.addClass(kicker, st_kicker);
    kicker.textContent = "Theme preview";
    main.appendChild(kicker);

    var title = branch.createElement("title", "h1");
    css.addClass(title, st_title);
    title.textContent = "…";
    main.appendChild(title);

    var subtitle = branch.createElement("subtitle", "p");
    css.addClass(subtitle, st_subtitle);
    main.appendChild(subtitle);

    // The name and the inspiration line come from the registry, so the page
    // says which theme it is wearing. Everything below renders before the
    // fetch answers — the elements are the preview, the name is a caption.
    fetchRegistry().then(function (reg) {
        var at = decompose(reg.themes, activeThemeSlug()) || { theme: reg.themes[0], palette: null };
        var t = at.theme;
        if (!t) return;
        var p = null;
        for (var i = 0; i < reg.palettes.length; i++) if (reg.palettes[i].slug === at.palette) p = reg.palettes[i];
        title.textContent = (t.label || t.slug) + (p ? " in " + (p.label || p.slug) : "");
        subtitle.textContent = p ? (t.inspiration || "") + " " + (p.inspiration || "") : (t.inspiration || "");
    }).catch(function () { title.textContent = "Theme"; });

    main.appendChild(Section({
        title: "Cards",
        children: [
            Card({ href: "#cards", title: "A featured card",
                   summary: "Title, summary, a badge and an open link — the shape every catalogue draws.",
                   badge: "RFC", badgeClass: st_badge_rfc }),
            Card({ href: "#cards", title: "A second card",
                   summary: "Two cards make a grid; the grid is the section's business.",
                   badge: "Brand", badgeClass: st_badge_brand, link: "Browse →" })
        ]
    }));

    main.appendChild(Section({
        title: "Table",
        gridless: true,
        children: [ _table(branch) ]
    }));

    main.appendChild(Section({
        title: "Status",
        gridless: true,
        children: [
            _badges(branch),
            OverallProgress({ caption: "Overall", summary: "5 of 8 steps", percent: 62 })
        ]
    }));

    main.appendChild(Listing({
        title: "Listing",
        children: [
            ListItem({ marker: "1", label: "A plain row",
                       description: "Marker, label and description." }),
            ListItem({ marker: "2", label: "A linked row", href: "#listing",
                       description: "Rows may be anchors; this one is." }),
            ListItem({ marker: "✓", label: "A met row", met: true,
                       description: "The green tone the acceptance sections use." })
        ]
    }));

    main.appendChild(Panel({
        title: "Panel",
        children: [
            TodoList({ tasks: [
                { description: "A task that is done", done: true },
                { description: "A task that is pending", done: false }
            ] }),
            MetricsTable({ rows: [
                { label: "Modules",  before: "42", after: "47", delta: "+5" },
                { label: "Findings", before: "9",  after: "3",  delta: "−6" }
            ] })
        ]
    }));

    main.appendChild(Footer({ children: [ NavLink({ href: "#top", text: "A footer link" }) ] }));

    return root;
}

/**
 * A plain HTML table whose rows are interactive: one is selected, one is
 * current, all take focus. Every state is a pseudo-class or an attribute the
 * row's pairs have a slot for, so the design says how a hovered, selected,
 * current or focused row looks and the row carries one class.
 */
function _table(branch) {
    var table = branch.createElement("tbl", "table");
    css.addClass(table, st_table);

    var thead = branch.createElement("tblh", "thead");
    css.addClass(thead, st_thead);
    var hr = branch.createElement("tblhr", "tr");
    ["Module", "Findings", "Status"].forEach(function (h, i) {
        var th = branch.createElement("tblth" + i, "th");
        css.addClass(th, st_th);
        th.textContent = h;
        hr.appendChild(th);
    });
    thead.appendChild(hr);
    table.appendChild(thead);

    var rows = [
        ["CssClassManager",  "0", ["Done",        st_td_badge_success]],
        ["ThemePicker",      "2", ["In progress", st_td_badge_warning], "selected"],
        ["WorkspaceLayout",  "0", ["Done",        st_td_badge_success]],
        ["PartyMonitor",     "5", ["Blocked",     st_td_badge_error],   "current"],
        ["TreeRenderer",     "1", ["In progress", st_td_badge_warning]]
    ];
    var tbody = branch.createElement("tblb", "tbody");
    rows.forEach(function (r, i) {
        var tr = branch.createElement("tblr" + i, "tr");
        css.addClass(tr, st_tr);
        tr.tabIndex = 0;                                     // rows take focus: :focus-visible is a slot too
        if (r[3] === "selected") tr.setAttribute("aria-selected", "true");
        if (r[3] === "current")  tr.setAttribute("aria-current", "true");
        var name = branch.createElement("tblc" + i + "a", "td");
        css.addClass(name, st_td);
        name.textContent = r[0];
        var count = branch.createElement("tblc" + i + "b", "td");
        css.addClass(count, st_td, st_td_align_right);
        count.textContent = r[1];
        var status = branch.createElement("tblc" + i + "c", "td");
        css.addClass(status, st_td);
        var badge = branch.createElement("tblc" + i + "d", "span");
        css.addClass(badge, r[2][1]);
        badge.textContent = r[2][0];
        status.appendChild(badge);
        tr.appendChild(name); tr.appendChild(count); tr.appendChild(status);
        tbody.appendChild(tr);
    });
    table.appendChild(tbody);
    return table;
}

/** The four status badges in a row, in their own element so they wrap as one. */
function _badges(branch) {
    var row = branch.createElement("badges", "div");
    var states = [
        ["not-started", "Not started"], ["in-progress", "In progress"],
        ["blocked", "Blocked"], ["done", "Done"]
    ];
    for (var i = 0; i < states.length; i++) {
        row.appendChild(StatusBadge({ statusSlug: states[i][0], statusLabel: states[i][1] }));
        row.append(" ");   // a text node between badges, minted by the row itself
    }
    return row;
}
