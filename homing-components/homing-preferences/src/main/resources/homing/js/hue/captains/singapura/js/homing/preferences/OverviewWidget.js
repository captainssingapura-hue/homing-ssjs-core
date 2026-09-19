// =============================================================================
// OverviewWidget — a group's page. construct(branch, params) → { root, dispose }
//   params: { label, summary?, settings: [{ name, label }] }
// Lists the settings under the group with the value each has in force, read
// from the steward and redrawn when it changes; one button forgets them all.
// =============================================================================

const _overviewOwner = Object.freeze({ toString: () => "overviewWidget" });

function construct(branch, params) {
    branch.activate(_overviewOwner);
    var settings = (params && params.settings) || [];

    var root = branch.createElement("root", "div");
    var kicker = branch.createElement("kicker", "div");
    css.addClass(kicker, pv_kicker);
    kicker.textContent = "group";
    root.appendChild(kicker);
    var title = branch.createElement("title", "h2");
    css.addClass(title, pv_title);
    title.textContent = params.label || "";
    root.appendChild(title);
    if (params.summary) {
        var summary = branch.createElement("summary", "p");
        css.addClass(summary, pv_summary);
        summary.textContent = params.summary;
        root.appendChild(summary);
    }

    var list = branch.createElement("settings", "ul");
    css.addClass(list, pv_children);
    var values = [];
    settings.forEach(function (s, i) {
        var li = branch.createElement("s-" + i, "li");
        css.addClass(li, pv_child);
        var label = branch.createElement("s-" + i + "-label", "span");
        label.textContent = s.label || s.name;
        var value = branch.createElement("s-" + i + "-value", "span");
        css.addClass(value, pv_option_note);
        li.appendChild(label);
        li.appendChild(value);
        list.appendChild(li);
        values.push({ name: s.name, el: value });
    });
    root.appendChild(list);

    var actions = branch.createElement("actions", "div");
    css.addClass(actions, pv_actions);
    var reset = Button(branch, "reset", { label: "Use the site's defaults for all of these", kind: "plain", onClick: function () {
        settings.forEach(function (s) { PreferenceStewardInstance.forget(s.name); });
    } });
    actions.appendChild(reset);
    root.appendChild(actions);

    function draw() {
        var any = false;
        values.forEach(function (v) {
            var stored = PreferenceViewInstance.preferred(v.name), pinned = PreferenceViewInstance.override(v.name);
            v.el.textContent = pinned ? pinned + " (pinned)" : stored ? stored : "default";
            if (stored) any = true;
        });
        setButtonOn(reset, any);
    }
    var stop = PreferenceViewInstance.onChange(draw);
    draw();

    return { root: root, dispose: stop };
}
