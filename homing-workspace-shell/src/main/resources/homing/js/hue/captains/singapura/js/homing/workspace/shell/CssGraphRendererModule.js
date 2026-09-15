// =============================================================================
// CssGraphRendererModule — RFC 0064, the CSS dependency graph seen from inside.
//
//   renderCssGraph(branch, host, opts) → { refresh }
//
// The workbench draws what the CSS manager knows: the theme the page wears,
// every group as a node — its dependencies, whether it is a prior, whether it
// was ever declared or only named by another's dependencies — and every
// sheet with the theme it is under. The nodes sit in COLUMNS BY WAVE: the
// waves the manager's plan answers, which is the order a load or a switch
// runs in. That is the plan made visible before anything is touched.
//
// Two deliberate actions, both on the plan bar. PLAN asks the manager for the
// waves a switch to the chosen theme would run and says so in the note —
// nothing is appended. RUN performs the switch, and the note says how long it
// took. Refresh is manual (RFC 0063: no timers); a switch that lands — from
// here, from the picker, from another tab — refreshes the columns itself.
//
// A snapshot is immutable, so each refresh draws into a NEW sub-branch and
// dissolves the previous — the fence move from MermaidPlate, as the party
// monitor does it.
// =============================================================================

var _PREFIX = "hue.captains.singapura.js.homing.";

function _short(id) {
    return id.indexOf(_PREFIX) === 0 ? id.slice(_PREFIX.length) : id;
}

function renderCssGraph(branch, host, opts) {
    opts = opts || {};
    // `css` is the injected manager (the class API); `manager` is the same object
    // by another name, for the graph, the plan and the switch — injectable so a
    // test hands in a fixture.
    var manager = opts.manager || CssClassManagerInstance;
    var seq = 0;

    var root  = branch.createElement("root", "div");    css.addClass(root,  cg_root);
    var head  = branch.createElement("head", "div");    css.addClass(head,  cg_head);
    var title = branch.createElement("title", "span");  css.addClass(title, cg_title);
    var worn  = branch.createElement("worn", "span");   css.addClass(worn,  cg_worn);
    var btn   = branch.createElement("refresh", "button"); css.addClass(btn, cg_btn);
    title.textContent = "CSS graph";
    btn.textContent = "Refresh";
    btn.setAttribute("type", "button");
    head.appendChild(title); head.appendChild(worn); head.appendChild(btn);

    var bar    = branch.createElement("bar", "div");       css.addClass(bar, cg_bar);
    var label  = branch.createElement("label", "span");    label.textContent = "Switch to";
    var select = branch.createElement("target", "select"); css.addClass(select, cg_select);
    var plan   = branch.createElement("plan", "button");   css.addClass(plan, cg_btn);
    var run    = branch.createElement("run", "button");    css.addClass(run,  cg_btn);
    var note   = branch.createElement("note", "span");     css.addClass(note, cg_note);
    plan.textContent = "Plan"; plan.setAttribute("type", "button");
    run.textContent  = "Run";  run.setAttribute("type", "button");
    bar.appendChild(label); bar.appendChild(select); bar.appendChild(plan); bar.appendChild(run); bar.appendChild(note);

    var wavesHost = branch.createElement("waves", "div"); css.addClass(wavesHost, cg_waves);
    root.appendChild(head); root.appendChild(bar); root.appendChild(wavesHost);
    host.appendChild(root);

    // The registry's themes, for the target list; a failed fetch leaves the
    // bar with only the theme worn, which is still a workbench.
    fetch("/themes").then(function (r) { return r.ok ? r.json() : { themes: [] }; })
        .then(function (j) {
            var themes = (j && j.themes) || [];
            for (var i = 0; i < themes.length; i++) {
                var o = branch.createElement("opt" + i, "option");
                o.value = themes[i].slug;
                o.textContent = themes[i].label || themes[i].slug;
                select.appendChild(o);
            }
            if (manager.theme()) select.value = manager.theme();
        }).catch(function () { /* the bar stays; the graph does not need the list */ });

    function say(text, isError) {
        note.textContent = text;
        css.toggleClass(note, cg_note_err, !!isError);
    }

    var current = null;   // the sub-branch holding the latest columns

    function refresh() {
        var snap = manager.snapshot();
        worn.textContent = "wears: " + (snap.theme || "—");

        if (current) { branch.dissolveBranch(current.name); current = null; }
        current = branch.createBranch("cols" + (++seq));
        current.activate(opts.owner || root, "cssGraph:" + seq);

        var byId = {};
        for (var i = 0; i < snap.graph.length; i++) byId[snap.graph[i].id] = snap.graph[i];
        var sheetsOf = {};
        for (var s = 0; s < snap.sheets.length; s++) {
            var e = snap.sheets[s];
            (sheetsOf[e.id] = sheetsOf[e.id] || []).push(e);
        }

        var waves;
        try { waves = manager.plan(); }
        catch (err) { say(String(err && err.message || err), true); return; }

        for (var w = 0; w < waves.length; w++) {
            var col = current.createElement("w" + w, "div"); css.addClass(col, cg_wave);
            var wh  = current.createElement("wh" + w, "div"); css.addClass(wh, cg_wave_head);
            wh.textContent = "wave " + w;
            col.appendChild(wh);
            for (var n = 0; n < waves[w].length; n++) {
                col.appendChild(_node(current, w + "_" + n, waves[w][n], byId[waves[w][n]], sheetsOf[waves[w][n]] || []));
            }
            wavesHost.appendChild(col);
        }
    }

    function _node(b, key, id, info, sheets) {
        var card = b.createElement("n" + key, "div"); css.addClass(card, cg_node);
        var name = b.createElement("id" + key, "div"); css.addClass(name, cg_node_id);
        if (info && info.prior) name.appendChild(_badge(b, "bp" + key, "prior", cg_badge_prior));
        if (info && !info.known) name.appendChild(_badge(b, "bu" + key, "by name", cg_badge_unknown));
        name.appendChild(b.createElement("nt" + key, "span")).textContent = _short(id);
        card.appendChild(name);
        var deps = b.createElement("d" + key, "div"); css.addClass(deps, cg_deps);
        deps.textContent = info && info.deps.length ? "← " + info.deps.map(_short).join(", ") : "no dependencies";
        card.appendChild(deps);
        var sh = b.createElement("s" + key, "div"); css.addClass(sh, cg_sheets);
        if (!sheets.length) sh.textContent = "no sheet";
        for (var i = 0; i < sheets.length; i++) {
            var one = b.createElement("s" + key + "_" + i, "span");
            one.textContent = (i ? " · " : "") + sheets[i].theme + (sheets[i].applied ? "" : " (pending)");
            if (!sheets[i].applied) css.addClass(one, cg_sheet_pending);
            sh.appendChild(one);
        }
        card.appendChild(sh);
        return card;
    }

    function _badge(b, key, text, extra) {
        var badge = b.createElement(key, "span");
        css.addClass(badge, cg_badge, extra);
        badge.textContent = text;
        return badge;
    }

    btn.addEventListener("click", refresh);

    plan.addEventListener("click", function () {
        var target = select.value;
        if (!target) return;
        try {
            var waves = manager.plan();
            var groups = 0;
            for (var i = 2; i < waves.length; i++) groups += waves[i].length;
            say("switch to " + target + ": " + waves.length + " waves — bundle, then "
                + groups + " group" + (groups === 1 ? "" : "s") + " in " + (waves.length - 2)
                + " wave" + (waves.length - 2 === 1 ? "" : "s") + "; nothing appended", false);
        } catch (err) { say(String(err && err.message || err), true); }
    });

    run.addEventListener("click", function () {
        var target = select.value;
        if (!target) return;
        var t0 = performance.now();
        say("switching to " + target + "…", false);
        manager.switchTheme(target).then(function () {
            say("now wears " + target + " — " + Math.round(performance.now() - t0) + " ms", false);
        }, function (err) {
            say("switch failed, page unchanged: " + String(err && err.message || err), true);
            refresh();
        });
    });

    var off = manager.onThemeApplied(function () { refresh(); });

    refresh();
    return { refresh: refresh, dispose: off };
}
