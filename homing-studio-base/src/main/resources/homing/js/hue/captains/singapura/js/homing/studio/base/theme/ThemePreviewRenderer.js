// ThemePreviewRenderer — a theme, shown by wearing it.
//
// renderThemePreview() → Node
//
// One page carrying the elements the studio's pages are made of — kicker,
// title, cards, badges, a listing, a progress bar, a task list, a table, a
// footer — under whatever theme the address names. The picker embeds this page
// in a frame and reloads the frame per selection, so a theme is previewed by
// rendering it rather than by a strip of its palette. Backdrop, audio and body
// class come with the page, because the page is a page.
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
    fetchThemes().then(function (themes) {
        var t = themeBySlug(themes, activeThemeSlug()) || themes[0];
        if (!t) return;
        title.textContent = t.label || t.slug;
        subtitle.textContent = t.inspiration || "";
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
