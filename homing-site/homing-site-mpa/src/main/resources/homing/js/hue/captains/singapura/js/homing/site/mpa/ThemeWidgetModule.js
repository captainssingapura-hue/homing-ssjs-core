// =============================================================================
// ThemeWidget — the theme as a preference. new ThemeWidget(branch, params) → root, dispose
//   params: { name: "theme", label, summary? }
//
// Two listboxes from /themes: the designs, and the colours the chosen design
// is offered in. A pick is field.set(slug) — the steward's remember — and the
// CSS manager, which follows the store, switches every sheet; the field
// redraws on the same change, so the marks and the note move together.
// =============================================================================

const _owner = Object.freeze({ toString: () => "themeWidget" });

class ThemeWidget {
    constructor(branch, params) {
        branch.activate(_owner);
        var self = this;
        this._branch = branch;
        this._reg = null;
        this._seq = 0;
        this._listsName = null;
        this._field = new PreferenceField(branch.createBranch("field"), params, { kicker: "theme", onValue: function (v) { self._draw(v); } });
        this.root = this._field.root;

        var pending = branch.createElement("pending", "div");
        css.addClass(pending, pv_kicker);
        pending.textContent = "Loading the site's themes…";
        this._field.body.appendChild(pending);

        fetch("/themes").then(function (r) { if (!r.ok) throw new Error("/themes HTTP " + r.status); return r.json(); })
            .then(function (j) { self._reg = j; pending.remove(); self._draw(self._field.value()); })
            .catch(function (e) { pending.textContent = "The site's themes could not be loaded: " + e.message; });
    }

    _decompose(slug) {
        var reg = this._reg;
        if (!reg || !slug) return null;
        for (var i = 0; i < reg.themes.length; i++) {
            var t = reg.themes[i];
            for (var j = 0; j < t.colours.length; j++) if (t.colours[j].slug === slug) return { base: t, colour: t.colours[j] };
        }
        return null;
    }
    _paletteOf(slug) {
        var reg = this._reg;
        for (var i = 0; i < reg.palettes.length; i++) if (reg.palettes[i].slug === slug) return reg.palettes[i];
        return { label: slug, inspiration: "" };
    }

    // Every draw is a fresh sub-branch: the lists are rebuilt whole, and the
    // names on the previous branch go with it.
    _draw(worn) {
        var reg = this._reg, self = this;
        if (!reg) return;
        worn = worn || reg.default;          // nothing kept and nothing pinned: the site's default is what the page wears
        if (this._listsName) this._branch.dissolveBranch(this._listsName);
        this._listsName = "lists" + (++this._seq);
        var listsBranch = this._branch.createBranch(this._listsName);
        listsBranch.activate(_owner);
        var at = this._decompose(worn) || { base: reg.themes[0], colour: null };

        this._field.body.appendChild(this._list(listsBranch, "designs", "Designs", reg.themes.map(function (t) {
            return { value: t.slug, label: t.label, note: t.inspiration, selected: t.slug === at.base.slug };
        })));
        var offered = at.base.colours.filter(function (c) { return c.own || c.fits; });
        if (offered.length > 1) {
            var k = listsBranch.createElement("coloursKicker", "div");
            css.addClass(k, pv_kicker);
            k.textContent = "Colours for " + at.base.label;
            this._field.body.appendChild(k);
            this._field.body.appendChild(this._list(listsBranch, "colours", "Colours", offered.map(function (c) {
                var p = self._paletteOf(c.palette);
                return { value: c.slug, label: p.label, note: c.own ? "the design's own" : p.inspiration, selected: c.slug === worn };
            })));
        }
    }

    _list(b, key, ariaLabel, options) {
        var self = this;
        var ul = b.createElement(key, "div");
        css.addClass(ul, pv_options);
        ul.setAttribute("role", "listbox");
        ul.setAttribute("aria-label", ariaLabel);
        options.forEach(function (o, i) {
            var row = b.createElement(key + "-" + i, "button");
            row.type = "button";
            css.addClass(row, pv_option);
            row.setAttribute("role", "option");
            row.setAttribute("aria-selected", o.selected ? "true" : "false");
            var text = b.createElement(key + "-" + i + "-label", "span");
            text.textContent = o.label;
            row.appendChild(text);
            if (o.note) {
                var note = b.createElement(key + "-" + i + "-note", "span");
                css.addClass(note, pv_option_note);
                note.textContent = o.note;
                row.appendChild(note);
            }
            row.addEventListener("click", function () { self._field.set(o.value); });
            ul.appendChild(row);
        });
        return ul;
    }

    dispose() { this._field.dispose(); }
}
