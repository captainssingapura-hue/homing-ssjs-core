// =============================================================================
// CounterApp — a counter that starts where its params say. appMain(el, params)
// receives the stamped params the server decoded through the app's codec:
// { start: "7" } — strings, as they travel on the wire. Its buttons are the
// shared Button builder; the page owns the row they sit in.
// =============================================================================

const _owner = Object.freeze({ toString: () => "counter" });

function appMain(el, params) {
    var branch = domOpsParty.createBranch("counter");
    branch.activate(_owner);

    var start = params && params.start ? parseInt(params.start, 10) : 0;
    if (isNaN(start)) start = 0;
    var value = start;

    var kicker = branch.createElement("kicker", "div");
    css.addClass(kicker, ga_kicker);
    kicker.textContent = "typed params";
    el.appendChild(kicker);

    var title = branch.createElement("title", "h1");
    css.addClass(title, ga_title);
    title.textContent = "Counter";
    el.appendChild(title);

    var lede = branch.createElement("lede", "p");
    css.addClass(lede, ga_lede);
    lede.textContent = "Started at " + start + ", which the server stamped into the page from the binding "
        + "and the address through the app's own codec.";
    el.appendChild(lede);

    var count = branch.createElement("count", "div");
    css.addClass(count, ga_count);
    count.setAttribute("aria-live", "polite");
    el.appendChild(count);

    function draw() { count.textContent = String(value); }

    // The three buttons are the shared Button builder: primary for the two
    // that count, plain for the way back.
    var buttons = branch.createElement("buttons", "div");
    css.addClass(buttons, ga_buttons);
    buttons.appendChild(Button(branch, "minus", { label: "−",     onClick: function () { value -= 1; draw(); } }));
    buttons.appendChild(Button(branch, "plus",  { label: "+",     onClick: function () { value += 1; draw(); } }));
    buttons.appendChild(Button(branch, "reset", { label: "reset", kind: "plain", onClick: function () { value = start; draw(); } }));
    el.appendChild(buttons);
    draw();
}
