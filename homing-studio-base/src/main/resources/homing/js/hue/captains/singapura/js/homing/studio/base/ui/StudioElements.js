// =============================================================================
// StudioElements — shared view-element builders used by every studio
// AppModule. Each function takes a props object and returns a Node; the
// consumer composes these instead of authoring HTML strings.
//
// Doctrine context:
//   - These builders use document.createElement internally; they ARE the
//     framework's view-construction code, exempt from Pure-Component Views.
//     Consumer modules (DocBrowser, StudioCatalogue, …) only call into
//     these and never createElement themselves — that's the doctrine's
//     intent.
//   - When RFC 0003 lands, each builder becomes a typed ComponentImpl.
//     Call sites stay shaped the same.
// =============================================================================

// `css` auto-injects via the StudioStyles import (ManagerInjector).
// `href` is normally auto-injected only for AppLink-importing modules; alias
// it from the explicit HrefManagerInstance import declared in
// StudioElements.java's imports().
var href = HrefManagerInstance;

// ---------- Internal helpers ----------

function _el(tag) {
    return document.createElement(tag);
}

function _withClass(node /*, ...classes */) {
    for (var i = 1; i < arguments.length; i++) {
        if (arguments[i]) css.addClass(node, arguments[i]);
    }
    return node;
}

function _appendAll(parent /*, ...children */) {
    for (var i = 1; i < arguments.length; i++) {
        var c = arguments[i];
        if (c == null) continue;
        if (typeof c === "string") parent.appendChild(document.createTextNode(c));
        else parent.appendChild(c);
    }
    return parent;
}

// ---------- Brand ----------
// Brand({ href, label, logo? }) →
//   <a class="st-brand" href={href}>
//     <span class="st-brand-(dot|logo)">…</span>
//     <span class="st-brand-word">{label}</span>
//   </a>
//
// `logo` is an optional SVG markup string (server-resolved from a typed
// SvgRef on StudioBrand). When present, parsed via DOMParser and slotted
// into a fixed 22×22 wrapper. When absent, falls back to the framework's
// coloured-square dot.
//
// Used inside Header. The brand is always a link to the studio's home.
function Brand(props) {
    var a = _withClass(_el("a"), st_brand);
    href.set(a, props.href);

    // Glyph: typed SVG logo when supplied, dot fallback otherwise. SVG markup
    // arrives as a string (server-resolved typed asset) and goes through
    // DOMParser — same path AnimalCell.js uses for typed-SVG embedding, so
    // the doctrine on no-raw-HTML is satisfied (asset, not authored markup).
    var glyph;
    if (props.logo) {
        glyph = _withClass(_el("span"), st_brand_logo);
        var svg = new DOMParser().parseFromString(props.logo, "image/svg+xml").documentElement;
        // Strip the SVG's own width/height attributes so the wrapper's CSS
        // (.st-brand-logo svg → width:100%; height:100%) controls sizing
        // unconditionally. Without this, an SVG with hardcoded dimensions
        // (e.g. width="800px") would render at full size and either overflow
        // the wrapper or get clipped to the wrapper's top-left corner.
        svg.removeAttribute("width");
        svg.removeAttribute("height");
        glyph.appendChild(svg);
    } else {
        glyph = _withClass(_el("span"), st_brand_dot);
    }
    var word = _withClass(_el("span"), st_brand_word);
    word.textContent = props.label;
    return _appendAll(a, glyph, word);
}

// ---------- Header ----------
// Header({ brand: {href, label}, crumbs: [{text, href?}, …] }) →
//   <div class="st-header">
//     <a class="st-brand" …/>
//     <div class="st-breadcrumbs">…</div>
//   </div>
//
// Each crumb with `href` becomes a link; the last (current page) typically
// omits href and renders as plain text. Separators are inserted between
// crumbs automatically.
function Header(props) {
    var bar = _withClass(_el("div"), st_header);
    bar.appendChild(Brand(props.brand));

    var trail = _withClass(_el("div"), st_breadcrumbs);
    var crumbs = props.crumbs || [];
    for (var i = 0; i < crumbs.length; i++) {
        if (i > 0) {
            var sep = _withClass(_el("span"), st_crumb_sep);
            sep.textContent = "/";
            trail.appendChild(sep);
        }
        var c = crumbs[i];
        var node;
        if (c.href) {
            node = _withClass(_el("a"), st_crumb);
            href.set(node, c.href);
        } else {
            node = _withClass(_el("span"), st_crumb);
        }
        node.textContent = c.text;
        trail.appendChild(node);
    }
    bar.appendChild(trail);

    // Theme picker. Mounts its own trigger straight into this bar, so it shares
    // the sticky band's inverted background and sticks with the header on
    // scroll. It fetches /themes itself and resolves to null when the registry
    // holds fewer than two, so there is nothing to co-ordinate here.
    //
    // This used to reparent a server-rendered slot whose markup, CSS and script
    // were a 178-line Java text block. The component now lives in ThemePicker,
    // where conformance can see it.
    mountThemePickerButton(bar, {});

    return bar;
}

// ---------- Card ----------
// Card({ href, title, summary, badge, badgeClass, link }) →
//   <a class="st-card" href={href}>
//     <h3 class="st-card-title">{title}</h3>
//     <p class="st-card-summary">{summary}</p>
//     <div class="st-card-meta">
//       <span class="st-badge {badgeClass}">{badge}</span>
//       <span class="st-card-link">{link ?? "Open →"}</span>
//     </div>
//   </a>
//
// `badgeClass` is a CssClass handle (e.g. st_badge_reference). Pass null to
// suppress the badge entirely.
function Card(props) {
    var a = _withClass(_el("a"), st_card);
    href.set(a, props.href);

    var title = _withClass(_el("h3"), st_card_title);
    title.textContent = props.title;

    var summary = _withClass(_el("p"), st_card_summary);
    summary.textContent = props.summary || "";

    var meta = _withClass(_el("div"), st_card_meta);
    if (props.badge) {
        var badge = _withClass(_el("span"), st_badge, props.badgeClass);
        badge.textContent = props.badge;
        meta.appendChild(badge);
    }
    var openLink = _withClass(_el("span"), st_card_link);
    openLink.textContent = props.link || "Open →";
    meta.appendChild(openLink);

    return _appendAll(a, title, summary, meta);
}

// ---------- Pill ----------
// Pill({ href, icon, label, desc, dark }) →
//   <a class="st-app-pill [st-app-pill-dark]" href={href}>
//     <div class="st-app-pill-icon">{icon}</div>
//     <div>
//       <div class="st-app-pill-label">{label}</div>
//       <div class="st-app-pill-desc">{desc}</div>
//     </div>
//   </a>
//
// Use for launcher tiles (icon + label + desc), as on StudioCatalogue.
function Pill(props) {
    var a = _withClass(_el("a"), st_app_pill);
    if (props.dark) css.addClass(a, st_app_pill_dark);
    href.set(a, props.href);

    var iconBox = _withClass(_el("div"), st_app_pill_icon);
    iconBox.textContent = props.icon || "";

    var textBox = _el("div");
    var label = _withClass(_el("div"), st_app_pill_label);
    label.textContent = props.label;
    var desc = _withClass(_el("div"), st_app_pill_desc);
    desc.textContent = props.desc;
    _appendAll(textBox, label, desc);

    return _appendAll(a, iconBox, textBox);
}

// ---------- NavLink ----------
// NavLink({ href, text }) → <a href={href}>{text}</a>
//
// A plain text link. The studio had no link primitive, which is why the
// legacy renderers hand-roll anchors with document.createElement and carry
// the use-dom-ops-party debt for it — there was nothing else to reach for.
// Routed through _el like every other builder in this file, so a caller adds
// no raw DOM factory of its own and stays conformant without owning a
// DomOpsParty branch.
function NavLink(props) {
    var a = _el("a");
    href.set(a, props.href);
    a.textContent = props.text;
    return a;
}

// ---------- Section ----------
// Section({ title, children }) →
//   <div class="st-section">
//     <div class="st-section-title">{title}</div>
//     <div class="st-grid">{...children}</div>
//   </div>
//
// `children` is an array of Nodes (e.g. cards, pills) that go into the
// grid slot. Pass `gridless: true` to drop the inner grid wrapper for
// non-grid contents.
function Section(props) {
    var section = _withClass(_el("div"), st_section);
    if (props.title) {
        var t = _withClass(_el("div"), st_section_title);
        t.textContent = props.title;
        section.appendChild(t);
    }
    var children = props.children || [];
    if (props.gridless) {
        for (var i = 0; i < children.length; i++) {
            if (children[i] != null) section.appendChild(children[i]);
        }
    } else {
        var grid = _withClass(_el("div"), st_grid);
        for (var j = 0; j < children.length; j++) {
            if (children[j] != null) grid.appendChild(children[j]);
        }
        section.appendChild(grid);
    }
    return section;
}

// ---------- Listing + ListItem ----------
// Listing({ title, children }) →
//   <div class="st-section">
//     <div class="st-section-title">{title}</div>
//     <div class="st-list">{...children}</div>
//   </div>
//
// Vertical-stack counterpart to Section. Use for prose-like rows
// (objectives, acceptance criteria, decisions) where each item is a
// label + description that reads naturally one-per-line. Use Section
// (with the inner st-grid) for card tiles where multi-column layout
// helps scannability.
function Listing(props) {
    var section = _withClass(_el("div"), st_section);
    if (props.title) {
        var t = _withClass(_el("div"), st_section_title);
        t.textContent = props.title;
        section.appendChild(t);
    }
    var list = _withClass(_el("div"), st_list);
    var children = props.children || [];
    for (var i = 0; i < children.length; i++) {
        if (children[i] != null) list.appendChild(children[i]);
    }
    section.appendChild(list);
    return section;
}

// ListItem({ marker, label, description, href, met }) →
//   <a class="st-list-item [st-list-item-met]" href={href}> | <div …>
//     [<div class="st-list-item-marker">{marker}</div>]
//     <div class="st-list-item-body">
//       <div class="st-list-item-label">{label}</div>
//       <div class="st-list-item-desc">{description}</div>
//     </div>
//   </a/div>
//
// `marker` may be a string (rendered as text) or a DOM node (e.g. a
// StatusBadge for decisions, a glyph for acceptance). When omitted, the
// marker column is dropped entirely. `description` may be a string or a
// node. `href` is optional; when present, the row becomes a clickable
// anchor. `met: true` adds the green-tone modifier (used by the
// acceptance section).
function ListItem(props) {
    var row;
    if (props.href) {
        row = _withClass(_el("a"), st_list_item);
        href.set(row, props.href);
    } else {
        row = _withClass(_el("div"), st_list_item);
    }
    if (props.met) css.addClass(row, st_list_item_met);

    if (props.marker != null && props.marker !== "") {
        var markerBox = _withClass(_el("div"), st_list_item_marker);
        if (typeof props.marker === "string") {
            markerBox.textContent = props.marker;
        } else {
            markerBox.appendChild(props.marker);
        }
        row.appendChild(markerBox);
    }

    var body = _withClass(_el("div"), st_list_item_body);
    if (props.label != null && props.label !== "") {
        var label = _withClass(_el("div"), st_list_item_label);
        label.textContent = props.label;
        body.appendChild(label);
    }
    if (props.description != null && props.description !== "") {
        var desc = _withClass(_el("div"), st_list_item_desc);
        if (typeof props.description === "string") {
            desc.textContent = props.description;
        } else {
            desc.appendChild(props.description);
        }
        body.appendChild(desc);
    }
    row.appendChild(body);

    return row;
}

// ---------- Footer ----------
// Footer({ children }) → <div class="st-footer">{...children}</div>
//
// `children` is an array of Nodes / strings the consumer composes itself —
// the Footer just wraps them with the studio footer chrome. Strings are
// inserted as text nodes (auto-escaped).
function Footer(props) {
    var f = _withClass(_el("div"), st_footer);
    var children = (props && props.children) || [];
    for (var i = 0; i < children.length; i++) {
        var c = children[i];
        if (c == null) continue;
        if (typeof c === "string") f.appendChild(document.createTextNode(c));
        else f.appendChild(c);
    }
    return f;
}

// =============================================================================
// Tracker-shaped builders — used by PlanRenderer to draw any plan tracker.
// =============================================================================

function _statusClass(slug) {
    switch (slug) {
        case "not-started": return st_status_not_started;
        case "in-progress": return st_status_in_progress;
        case "blocked":     return st_status_blocked;
        case "done":        return st_status_done;
        case "resolved":    return st_status_done;     // resolved decisions reuse the green badge
        default:            return st_status_in_progress;
    }
}

// ---------- StatusBadge ----------
// StatusBadge({ statusSlug, statusLabel }) → coloured pill matching the slug.
function StatusBadge(props) {
    var span = _withClass(_el("span"), st_status_badge, _statusClass(props.statusSlug));
    span.textContent = props.statusLabel;
    return span;
}

// ---------- OverallProgress ----------
// OverallProgress({ caption, summary, percent }) →
//   horizontal bar with caption + summary on the left, fill bar in middle,
//   percentage on the right. Used at the top of a plan's index page.
function OverallProgress(props) {
    var wrap = _withClass(_el("div"), st_overall_progress);

    var labels = _el("div");
    var caption = _el("div");
    caption.style.cssText = "font-size:11px;letter-spacing:3px;color:rgba(202,220,252,0.6);font-weight:700;text-transform:uppercase;";
    caption.textContent = props.caption || "Overall";
    var summary = _el("div");
    summary.style.cssText = "font-family:Georgia,serif;font-size:18px;margin-top:2px;";
    summary.textContent = props.summary || "";
    _appendAll(labels, caption, summary);

    var bar = _withClass(_el("div"), st_overall_bar);
    var fill = _withClass(_el("div"), st_overall_fill);
    fill.style.width = (props.percent || 0) + "%";
    bar.appendChild(fill);

    var pct = _withClass(_el("div"), st_overall_pct);
    pct.textContent = (props.percent || 0) + "%";

    return _appendAll(wrap, labels, bar, pct);
}

// ---------- StepCard ----------
// StepCard({ href, idLabel, title, summary, statusSlug, statusLabel,
//            progress, doneCount, totalCount, effort }) →
//   linkable phase card with id pill, title, status badge, summary, progress
//   bar and meta line ("3 / 8 tasks · 2 days"). Used on the plan index.
function StepCard(props) {
    var a = _withClass(_el("a"), st_step_card);
    href.set(a, props.href);

    var head = _withClass(_el("div"), st_step_head);
    var idEl = _withClass(_el("span"), st_step_id);
    idEl.textContent = props.idLabel;
    var labelEl = _withClass(_el("h3"), st_step_label);
    labelEl.textContent = props.title;
    head.appendChild(idEl);
    head.appendChild(labelEl);
    head.appendChild(StatusBadge({ statusSlug: props.statusSlug, statusLabel: props.statusLabel }));

    var summary = _withClass(_el("p"), st_step_summary);
    summary.textContent = props.summary || "";

    var prog = _withClass(_el("div"), st_step_progress);
    var bar = _withClass(_el("div"), st_step_progress_bar);
    var fill = _withClass(_el("div"), st_step_progress_fill);
    fill.style.width = (props.progress || 0) + "%";
    bar.appendChild(fill);
    var meta = _withClass(_el("div"), st_step_meta);
    var doneCount  = props.doneCount  != null ? props.doneCount  : 0;
    var totalCount = props.totalCount != null ? props.totalCount : 0;
    var effort     = props.effort ? " · " + props.effort : "";
    meta.textContent = doneCount + " / " + totalCount + " tasks" + effort;
    prog.appendChild(bar);
    prog.appendChild(meta);

    return _appendAll(a, head, summary, prog);
}

// ---------- DecisionCard ----------
// DecisionCard({ id, question, recommendation, rationale, chosen, statusSlug,
//                statusLabel }) → non-link card with the decision's question,
//   recommendation, rationale, optional "Chosen:" line, and status badge on
//   the right.
function DecisionCard(props) {
    var card = _withClass(_el("div"), st_card);
    card.style.cssText = "cursor:default;min-height:auto;margin-bottom:10px;";

    var row = _el("div");
    row.style.cssText = "display:flex;align-items:flex-start;justify-content:space-between;gap:12px;";

    var left = _el("div");
    left.style.cssText = "flex:1;";

    var idEl = _el("div");
    idEl.style.cssText = "font-family:Georgia,serif;font-style:italic;font-size:11px;color:var(--st-amber-dk);font-weight:700;letter-spacing:2px;";
    idEl.textContent = props.id;

    var question = _withClass(_el("h4"), st_card_title);
    question.style.cssText = "margin:4px 0 6px 0;";
    question.textContent = props.question;

    var rec = _withClass(_el("p"), st_card_summary);
    rec.style.cssText = "margin:6px 0;";
    var recStrong = _el("strong"); recStrong.textContent = "Recommendation: ";
    rec.appendChild(recStrong);
    rec.appendChild(document.createTextNode(props.recommendation || ""));

    var rationale = _el("p");
    rationale.style.cssText = "font-size:12px;color:var(--st-gray-mid);font-style:italic;margin:4px 0 0 0;";
    rationale.textContent = props.rationale || "";

    _appendAll(left, idEl, question, rec, rationale);

    if (props.chosen) {
        var chosen = _el("div");
        chosen.style.cssText = "margin-top:8px;font-size:13px;color:var(--st-navy);";
        var chosenStrong = _el("strong"); chosenStrong.textContent = "Chosen: ";
        chosen.appendChild(chosenStrong);
        chosen.appendChild(document.createTextNode(props.chosen));
        left.appendChild(chosen);
    }

    row.appendChild(left);
    row.appendChild(StatusBadge({ statusSlug: props.statusSlug, statusLabel: props.statusLabel }));

    card.appendChild(row);
    return card;
}

// ---------- TodoList ----------
// TodoList({ tasks: [{description, done}, …] }) → checklist with done/pending
//   indicators. Pure visual — not interactive. The doctrine's "OO Components"
//   future iteration would turn this into a stateful Component; today it's a
//   one-shot render of typed task data.
function TodoList(props) {
    var ul = _withClass(_el("ul"), st_task_list);
    var tasks = (props && props.tasks) || [];
    for (var i = 0; i < tasks.length; i++) {
        var t = tasks[i];
        var li = _withClass(_el("li"), st_task_item);
        if (t.done) css.addClass(li, st_task_done);
        var box = _withClass(_el("span"), st_task_box);
        box.textContent = t.done ? "✓" : "";
        li.appendChild(box);
        li.appendChild(document.createTextNode(t.description || ""));
        ul.appendChild(li);
    }
    return ul;
}

// ---------- MetricsTable ----------
// MetricsTable({ rows: [{label, before, after, delta}, …] }) →
//   compact 4-column table — Metric / Before / After / Δ. Returns null when
//   `rows` is empty so callers can `if (node) parent.appendChild(node)`
//   without an extra guard. Used on step-detail pages of cleanup / refactor
//   plans where the payoff is a quantitative before/after.
function MetricsTable(props) {
    var rows = (props && props.rows) || [];
    if (rows.length === 0) return null;

    var table = _el("table");
    table.style.cssText = "width:100%; border-collapse:collapse; font-size:13px; margin-top:6px;";

    var headerRow = _el("tr");
    var headers = ["Metric", "Before", "After", "Δ"];
    for (var hi = 0; hi < headers.length; hi++) {
        var th = _el("th");
        th.style.cssText = "text-align:left; padding:4px 10px 4px 0; font-weight:700; font-size:11px; text-transform:uppercase; letter-spacing:1px; color:var(--st-gray-mid); border-bottom:1px solid var(--st-gray-lt);";
        th.textContent = headers[hi];
        headerRow.appendChild(th);
    }
    table.appendChild(headerRow);

    for (var i = 0; i < rows.length; i++) {
        var r = rows[i];
        var tr = _el("tr");
        var labelTd  = _el("td"); labelTd.style.cssText  = "padding:6px 10px 6px 0; font-weight:600; color:var(--st-navy); vertical-align:top;"; labelTd.textContent  = r.label  || "";
        var beforeTd = _el("td"); beforeTd.style.cssText = "padding:6px 10px 6px 0; color:var(--st-gray-dk); font-family:'Consolas','Courier New',monospace; vertical-align:top;"; beforeTd.textContent = r.before || "";
        var afterTd  = _el("td"); afterTd.style.cssText  = "padding:6px 10px 6px 0; color:var(--st-gray-dk); font-family:'Consolas','Courier New',monospace; vertical-align:top;"; afterTd.textContent  = r.after  || "";
        var deltaTd  = _el("td"); deltaTd.style.cssText  = "padding:6px 0; color:var(--st-amber-dk); font-family:'Consolas','Courier New',monospace; vertical-align:top;"; deltaTd.textContent  = r.delta  || "";
        tr.appendChild(labelTd); tr.appendChild(beforeTd); tr.appendChild(afterTd); tr.appendChild(deltaTd);
        table.appendChild(tr);
    }

    return table;
}

// ---------- Panel ----------
// Panel({ title, children }) → labelled inner panel, used for grouped detail
//   blocks on the step page (Tasks, Dependencies, Acceptance, …).
function Panel(props) {
    var p = _withClass(_el("div"), st_panel);
    if (props.title) {
        var t = _withClass(_el("div"), st_panel_title);
        t.textContent = props.title;
        p.appendChild(t);
    }
    var children = (props && props.children) || [];
    for (var i = 0; i < children.length; i++) {
        var c = children[i];
        if (c == null) continue;
        if (typeof c === "string") p.appendChild(document.createTextNode(c));
        else p.appendChild(c);
    }
    return p;
}

