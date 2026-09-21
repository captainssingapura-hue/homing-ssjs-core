package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.core.StampedParams;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The context menus a page offers, declared once as data: the kinds and the
 * constant items each kind carries — never what a pick does, which is the
 * kind's handler at runtime. A site's own {@code EsModule} with
 * {@code SelfContent} holds one and writes {@link #js()} as its body, a
 * module exporting {@code MENUS}: a frozen object of kind → type, the same
 * record the steward's JS builder yields. A kind declared twice, or an item
 * id repeated within a kind, is refused at construction — the setup is
 * deliberate, not accreted.
 */
public record ContextMenuRegistry(List<ContextMenuType> types) {

    public ContextMenuRegistry {
        types = List.copyOf(Objects.requireNonNull(types, "types"));
        var kinds = new HashSet<String>();
        for (ContextMenuType t : types)
            if (!kinds.add(t.kind())) throw new IllegalArgumentException("ContextMenuRegistry: kind declared twice: " + t.kind());
    }

    public static ContextMenuRegistry of(ContextMenuType... types) { return new ContextMenuRegistry(List.of(types)); }

    /** One kind: its name and its items, in order. */
    public record ContextMenuType(String kind, List<MenuItem> items) {
        public ContextMenuType {
            MenuItem.requireId(kind, "ContextMenuType.kind");
            items = List.copyOf(Objects.requireNonNull(items, "items"));
            if (items.isEmpty()) throw new IllegalArgumentException("ContextMenuType " + kind + ": no items");
            var ids = new HashSet<String>();
            for (MenuItem i : items) i.collectIds(ids, kind);
        }
        public static ContextMenuType of(String kind, MenuItem... items) { return new ContextMenuType(kind, List.of(items)); }
    }

    /**
     * One row: an item with an id, a label and an optional hint; a separator;
     * or a submenu — an item whose pick opens its own items beside it, one
     * level deep. What varies with the bound object — disabled, checked,
     * hidden — is the handler's, asked at bind, not the item's.
     */
    public record MenuItem(String id, String label, String hint, boolean separator, List<MenuItem> items) {
        public MenuItem {
            if (separator) {
                if (id != null || label != null || hint != null || (items != null && !items.isEmpty()))
                    throw new IllegalArgumentException("MenuItem: a separator carries nothing");
                items = List.of();
            } else {
                requireId(id, "MenuItem.id");
                requireId(label, "MenuItem.label");
                items = items == null ? List.of() : List.copyOf(items);
                for (MenuItem i : items) if (!i.items().isEmpty()) throw new IllegalArgumentException("MenuItem " + id + ": a submenu holds no submenu");
            }
        }
        public static MenuItem of(String id, String label) { return new MenuItem(id, label, null, false, List.of()); }
        public static MenuItem of(String id, String label, String hint) { return new MenuItem(id, label, hint, false, List.of()); }
        public static MenuItem divider() { return new MenuItem(null, null, null, true, List.of()); }
        public static MenuItem submenu(String id, String label, MenuItem... items) {
            if (items.length == 0) throw new IllegalArgumentException("MenuItem " + id + ": a submenu with no items");
            return new MenuItem(id, label, null, false, List.of(items));
        }
        public boolean submenu() { return !items.isEmpty(); }

        static String requireId(String v, String what) {
            Objects.requireNonNull(v, what);
            if (v.isEmpty()) throw new IllegalArgumentException(what + ": must not be empty");
            return v;
        }
        void collectIds(Set<String> ids, String kind) {
            if (separator) return;
            if (!ids.add(id)) throw new IllegalArgumentException("ContextMenuType " + kind + ": item id repeated: " + id);
            for (MenuItem i : items) i.collectIds(ids, kind);
        }
    }

    /** The registry as the JS module body: {@code const MENUS = Object.freeze({...});}. */
    public List<String> js() {
        var lines = new ArrayList<String>();
        lines.add("// Generated from a ContextMenuRegistry - do not hand-edit. The kinds a page offers");
        lines.add("// and the items each carries, as data; what a pick does is the kind's handler.");
        lines.add("const MENUS = Object.freeze(" + json() + ");");
        return lines;
    }

    /** The registry as JSON: kind → { kind, items }. */
    public String json() {
        var sb = new StringBuilder("{");
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) sb.append(',');
            var t = types.get(i);
            sb.append(StampedParams.jsString(t.kind())).append(":{\"kind\":").append(StampedParams.jsString(t.kind())).append(",\"items\":");
            items(sb, t.items());
            sb.append('}');
        }
        return sb.append('}').toString();
    }
    private static void items(StringBuilder sb, List<MenuItem> items) {
        sb.append('[');
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) sb.append(',');
            var it = items.get(i);
            if (it.separator()) { sb.append("{\"separator\":true}"); continue; }
            sb.append("{\"id\":").append(StampedParams.jsString(it.id())).append(",\"label\":").append(StampedParams.jsString(it.label()));
            if (it.hint() != null) sb.append(",\"hint\":").append(StampedParams.jsString(it.hint()));
            if (it.submenu()) { sb.append(",\"items\":"); items(sb, it.items()); }
            sb.append('}');
        }
        sb.append(']');
    }
}
