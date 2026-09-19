// =============================================================================
// CounterApp — a counter that starts where its params say. appMain(el, params)
// receives the stamped params the server decoded through the app's codec:
// { start: "7" } — strings, as they travel on the wire.
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

    var buttons = branch.createElement("buttons", "div");
    css.addClass(buttons, ga_buttons);
    var minus = branch.createElement("minus", "button");
    var plus  = branch.createElement("plus", "button");
    var reset = branch.createElement("reset", "button");
    minus.type = plus.type = reset.type = "button";
    css.addClass(minus, ga_button);
    css.addClass(plus, ga_button);
    css.addClass(reset, ga_button);
    minus.textContent = "−";
    plus.textContent = "+";
    reset.textContent = "reset";
    buttons.appendChild(minus);
    buttons.appendChild(plus);
    buttons.appendChild(reset);
    el.appendChild(buttons);

    function draw() { count.textContent = String(value); }
    minus.addEventListener("click", function () { value -= 1; draw(); });
    plus.addEventListener("click",  function () { value += 1; draw(); });
    reset.addEventListener("click", function () { value = start; draw(); });
    draw();
}
