// =============================================================================
// PlanHostRenderer — shared renderer for PlanAppHost (RFC 0005-ext1).
//
// renderPlanHost({ planId, phase }) → Node
//
// Fetches /plan?id=<planId> for the full plan payload, then dispatches:
//   - phase param present → step-detail view for that phase
//   - phase param absent  → index view (all phases / decisions / acceptance)
//
// Server pre-resolves brand + URLs; renderer constructs no URLs of its own
// beyond plan-internal navigation (next/prev phase, back to index).
// =============================================================================

// `href` is auto-injected by the AppLink import (DocReader.link); no manual
// `var href = HrefManagerInstance;` needed here. Declaring it would duplicate
// the framework's auto-injection and throw a SyntaxError at load time.
// Pinned by ManagerInjectionConformanceTest.

function renderPlanHost(props) {
    var planId = props.planId;
    var phase  = props.phase;
    var stampedCrumbs = props.crumbs;

    var root = document.createElement("div");
    css.addClass(root, st_root);

    var loading = document.createElement("div");
    css.addClass(loading, st_loading);
    loading.textContent = "Loading…";
    loading.style.cssText = "padding:24px;";
    root.appendChild(loading);

    if (!planId) {
        var errMsg = document.createElement("div");
        css.addClass(errMsg, st_error);
        errMsg.appendChild(document.createTextNode("No plan specified. Use "));
        var errCode = document.createElement("code"); errCode.textContent = "?id=<class-fqn>";
        errMsg.appendChild(errCode);
        errMsg.appendChild(document.createTextNode("."));
        root.replaceChildren(errMsg);
        return root;
    }

    fetch("/plan?id=" + encodeURIComponent(planId))
        .then(function(r) {
            if (!r.ok) throw new Error("HTTP " + r.status);
            return r.json();
        })
        .then(function(data) {
            // Browser tab title — `<plan> · <brand>` (or `<plan> · Phase X · <brand>`
            // when drilled into a phase). Same pattern as DocReader / CatalogueHost.
            var subject = data.name + (phase ? " · Phase " + phase : "");
            document.title = subject
                + (data.brand && data.brand.label ? " · " + data.brand.label : "");
            if (phase) {
                _renderStep(root, data, planId, phase, stampedCrumbs);
            } else {
                _renderIndex(root, data, planId, stampedCrumbs);
            }
        })
        .catch(function(err) {
            var errEl = document.createElement("div");
            css.addClass(errEl, st_error);
            errEl.appendChild(document.createTextNode("Failed to load plan: " + err.message));
            root.replaceChildren(errEl);
        });

    return root;
}

// RFC 0051 — the server supplies the plan's own address; these fall back to
// the flat form only when the plan has no position in the catalogue.
function _planUrl(data, planId) {
    return (data && data.selfUrl) || ("/app?app=plan&id=" + encodeURIComponent(planId));
}
function _phaseUrl(data, planId, phaseId) {
    var base = _planUrl(data, planId);
    return base + (base.indexOf("?") >= 0 ? "&" : "?") + "phase=" + encodeURIComponent(phaseId);
}

function _brandHeader(data, stampedCrumbs) {
    var brand = data.brand || { label: "studio", homeUrl: "/" };
    // RFC 0051 — the server's stamp, taken whole.
    //
    // This used to compose the trail itself: data.breadcrumbs for the
    // ancestors, data.name appended as the plan's own crumb, and on a phase
    // view a "Phase N" crumb after that with the plan name turned into a link
    // back to the index. Two of those three were the app describing a position
    // the catalogue never gave it. The stamp already ends at the plan, which
    // IS where this page sits — being at phase 6 of a plan is being at that
    // plan — so moving between phases is this app's own navigation to render.
    //
    // No stamp means no CatalogueRegistry on this studio, and then
    // data.breadcrumbs is absent too: PlanGetAction emits brand and chain
    // together, both gated on the registry. So the fallback is not a second
    // trail, it is the absence of one.
    var crumbs = (stampedCrumbs && stampedCrumbs.length)
            ? stampedCrumbs.slice()
            : [{ text: brand.label, href: brand.homeUrl }, { text: data.name }];
    return Header({
        brand:  { href: brand.homeUrl, label: brand.label, logo: brand.logo },
        crumbs: crumbs
    });
}

function _renderIndex(root, data, planId, stampedCrumbs) {
    var children = [];
    children.push(_brandHeader(data, stampedCrumbs));

    var main = document.createElement("div");
    css.addClass(main, st_main);

    if (data.kicker) {
        var kicker = document.createElement("div");
        css.addClass(kicker, st_kicker);
        kicker.textContent = data.kicker;
        main.appendChild(kicker);
    }

    var title = document.createElement("h1");
    css.addClass(title, st_title);
    title.textContent = data.name;
    main.appendChild(title);

    if (data.subtitle) {
        var subtitle = document.createElement("p");
        css.addClass(subtitle, st_subtitle);
        subtitle.textContent = data.subtitle;
        main.appendChild(subtitle);
    }

    // Overall progress at the very top of the main area — instant ship-status
    // signal before the reader scrolls into objectives / decisions / phases.
    main.appendChild(OverallProgress({ percent: data.totalProgress }));

    // Objectives section — optional 4th pillar; rendered just below the
    // progress bar so a reader sees the "why" before the "what". Hidden
    // when the list is empty.
    if (data.objectives && data.objectives.length) {
        var objectiveItems = data.objectives.map(function(o) {
            return ListItem({ label: o.label, description: o.description });
        });
        main.appendChild(Listing({ title: "Objectives", children: objectiveItems }));
    }

    // Decisions section — list rows with status badge as marker.
    if (data.decisions && data.decisions.length) {
        var decisionItems = data.decisions.map(function(d) {
            return ListItem({
                marker:      StatusBadge({ statusSlug: (d.status || "").toLowerCase(), statusLabel: d.status || "" }),
                label:       d.question,
                description: _decisionBody(d)
            });
        });
        main.appendChild(Listing({ title: "Open decisions (" + data.openDecisions + ")", children: decisionItems }));
    }

    // Acceptance section (RFC 0005-ext1 third pillar) — list rows with ✓/○ marker.
    if (data.acceptance && data.acceptance.length) {
        var acceptanceItems = data.acceptance.map(function(a) {
            return ListItem({
                marker:      a.met ? "✓" : "○",
                met:         a.met,
                label:       a.label,
                description: a.description
            });
        });
        main.appendChild(Listing({ title: "Acceptance (" + data.acceptanceMet + " of " + data.acceptance.length + " met)",
                                   children: acceptanceItems }));
    }

    // Phases section — uniform list format, matching Objectives / Decisions /
    // Acceptance. Each row: status badge as marker, "id — label" as the
    // headline, summary as the description, click-through to the per-phase
    // detail view. Per-phase progress percent is rolled into the marker
    // column beneath the badge.
    var phaseItems = data.phases.map(function(p) {
        return ListItem({
            href:        _phaseUrl(data, planId, p.id),
            marker:      _phaseMarker(p),
            met:         (p.status || "").toLowerCase() === "done",
            label:       p.id + " — " + p.label,
            description: p.summary
        });
    });
    main.appendChild(Listing({ title: "Phases", children: phaseItems }));

    // Footer with executionDoc/dossierDoc links.
    var footerChildren = [];
    if (data.executionDoc) {
        var execA = document.createElement("a");
        href.set(execA, data.executionUrl || ("/app?app=doc-reader&doc=" + encodeURIComponent(data.executionDoc)));
        execA.textContent = "Execution Plan (prose)";
        footerChildren.push(execA);
    }
    if (data.dossierDoc) {
        var dosA = document.createElement("a");
        href.set(dosA, data.dossierUrl || ("/app?app=doc-reader&doc=" + encodeURIComponent(data.dossierDoc)));
        dosA.textContent = "Dossier";
        if (footerChildren.length) footerChildren.push(document.createTextNode(" · "));
        footerChildren.push(dosA);
    }
    if (footerChildren.length) main.appendChild(Footer({ children: footerChildren }));

    children.push(main);
    root.replaceChildren.apply(root, children);
}

// Marker node for a Phase row — status badge above per-phase progress %.
// Returned as a Node so ListItem renders it verbatim.
function _phaseMarker(p) {
    var box = document.createElement("div");
    box.style.cssText = "display:flex;flex-direction:column;align-items:center;gap:4px;min-width:64px;";
    box.appendChild(StatusBadge({
        statusSlug:  (p.status || "").toLowerCase(),
        statusLabel: p.status || ""
    }));
    var pct = document.createElement("div");
    pct.style.cssText = "font-size:11px;color:var(--color-text-muted);";
    pct.textContent = (p.progressPercent || 0) + "%";
    box.appendChild(pct);
    return box;
}

// Rich description node for a Decision row — recommendation prose plus an
// optional rationale line. Returned as a Node so ListItem renders it
// verbatim (rather than escaping the line break into a single string).
function _decisionBody(d) {
    var box = document.createElement("div");
    if (d.recommendation) {
        var p = document.createElement("div");
        p.textContent = d.recommendation;
        box.appendChild(p);
    }
    if (d.rationale) {
        var r = document.createElement("div");
        r.style.cssText = "margin-top:6px;font-style:italic;opacity:0.85;";
        r.textContent = d.rationale;
        box.appendChild(r);
    }
    return box;
}

function _renderStep(root, data, planId, phaseId, stampedCrumbs) {
    var phase = null;
    var phaseIdx = -1;
    for (var i = 0; i < data.phases.length; i++) {
        if (data.phases[i].id === phaseId) { phase = data.phases[i]; phaseIdx = i; break; }
    }
    if (!phase) {
        var errEl = document.createElement("div");
        css.addClass(errEl, st_error);
        errEl.textContent = "Phase " + phaseId + " not found in plan " + data.name;
        root.replaceChildren(errEl);
        return;
    }

    var children = [];
    children.push(_brandHeader(data, stampedCrumbs));

    var main = document.createElement("div");
    css.addClass(main, st_main);

    var statusRow = document.createElement("div");
    statusRow.appendChild(StatusBadge({ status: phase.status }));
    var effort = document.createElement("span");
    effort.style.cssText = "margin-left:12px; color: var(--st-gray-mid);";
    effort.textContent = phase.effort;
    statusRow.appendChild(effort);
    main.appendChild(statusRow);

    var title = document.createElement("h1");
    css.addClass(title, st_title);
    title.textContent = "Phase " + phase.id + ": " + phase.label;
    main.appendChild(title);

    if (phase.summary) {
        var summary = document.createElement("p");
        css.addClass(summary, st_subtitle);
        summary.textContent = phase.summary;
        main.appendChild(summary);
    }

    if (phase.description) {
        var desc = document.createElement("p");
        desc.textContent = phase.description;
        main.appendChild(desc);
    }

    if (phase.tasks && phase.tasks.length) {
        main.appendChild(Panel({ title: "Tasks", children: [TodoList({ tasks: phase.tasks })] }));
    }

    if (phase.metrics && phase.metrics.length) {
        main.appendChild(Panel({ title: "Metrics", children: [MetricsTable({ rows: phase.metrics })] }));
    }

    if (phase.dependsOn && phase.dependsOn.length) {
        var depsList = document.createElement("ul");
        for (var di = 0; di < phase.dependsOn.length; di++) {
            var d = phase.dependsOn[di];
            var li = document.createElement("li");
            var a = document.createElement("a");
            href.set(a, _phaseUrl(data, planId, d.phaseId));
            a.textContent = "Phase " + d.phaseId;
            li.appendChild(a);
            li.appendChild(document.createTextNode(" — " + d.reason));
            depsList.appendChild(li);
        }
        main.appendChild(Panel({ title: "Dependencies", children: [depsList] }));
    }

    if (phase.verification) {
        var verEl = document.createElement("p");
        verEl.textContent = phase.verification;
        main.appendChild(Panel({ title: "Verification", children: [verEl] }));
    }

    if (phase.rollback) {
        var rbEl = document.createElement("p");
        rbEl.textContent = phase.rollback;
        main.appendChild(Panel({ title: "Rollback", children: [rbEl] }));
    }

    if (phase.notes) {
        var notesEl = document.createElement("p");
        notesEl.style.cssText = "white-space: pre-wrap;";
        notesEl.textContent = phase.notes;
        main.appendChild(Panel({ title: "Notes", children: [notesEl] }));
    }

    // Prev / next nav.
    var navRow = document.createElement("div");
    navRow.style.cssText = "display:flex; justify-content:space-between; margin-top:24px;";
    if (phaseIdx > 0) {
        var prev = document.createElement("a");
        href.set(prev, _phaseUrl(data, planId, data.phases[phaseIdx - 1].id));
        prev.textContent = "← Phase " + data.phases[phaseIdx - 1].id;
        navRow.appendChild(prev);
    } else {
        navRow.appendChild(document.createElement("span"));
    }
    if (phaseIdx < data.phases.length - 1) {
        var next = document.createElement("a");
        href.set(next, _phaseUrl(data, planId, data.phases[phaseIdx + 1].id));
        next.textContent = "Phase " + data.phases[phaseIdx + 1].id + " →";
        navRow.appendChild(next);
    }
    main.appendChild(navRow);

    children.push(main);
    root.replaceChildren.apply(root, children);
}
