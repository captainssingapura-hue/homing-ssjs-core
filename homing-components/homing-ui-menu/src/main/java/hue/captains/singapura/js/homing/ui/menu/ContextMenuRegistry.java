package hue.captains.singapura.js.homing.ui.menu;

import hue.captains.singapura.js.homing.core.StampedParams;
import hue.captains.singapura.js.homing.design.Trees;
import hue.captains.singapura.js.homing.ui.menu.tree.ContextMenuKind;
import hue.captains.singapura.js.homing.ui.menu.tree.MenuRow;
import hue.captains.singapura.js.homing.ui.menu.tree.MenuTrees;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * The context menus a page offers, declared once as typed trees: the kinds,
 * each a {@link ContextMenuKind} whose rows are classes of their own — never
 * what a pick does, which is the kind's handler at runtime. Every kind is
 * validated in Java at construction ({@link MenuTrees#requireValid}), and a
 * kind declared twice is refused; the setup is deliberate, not accreted. A
 * site's own {@code EsModule} with {@code SelfContent} holds one and writes
 * {@link #js()} as its body, a module exporting {@code MENUS}: a frozen
 * object of kind → type, the same record the steward's JS builder yields —
 * {@code { kind, nodes: [ { id, label, icon?, hint?, section?, nodes? } ] }}.
 */
public record ContextMenuRegistry(List<ContextMenuKind<?>> kinds) {

    public ContextMenuRegistry {
        kinds = List.copyOf(Objects.requireNonNull(kinds, "kinds"));
        var names = new HashSet<String>();
        for (ContextMenuKind<?> k : kinds) {
            MenuTrees.requireValid(k);
            if (!names.add(k.kind())) throw new IllegalArgumentException("ContextMenuRegistry: kind declared twice: " + k.kind());
        }
    }

    public static ContextMenuRegistry of(ContextMenuKind<?>... kinds) { return new ContextMenuRegistry(List.of(kinds)); }

    /** The kind by name, or null. */
    public ContextMenuKind<?> kind(String name) {
        for (ContextMenuKind<?> k : kinds) if (k.kind().equals(name)) return k;
        return null;
    }

    /** The registry as the JS module body: {@code const MENUS = Object.freeze({...});}. */
    public List<String> js() {
        var lines = new ArrayList<String>();
        lines.add("// Generated from a ContextMenuRegistry - do not hand-edit. The kinds a page offers");
        lines.add("// and the rows each carries, as data; what a pick does is the kind's handler.");
        lines.add("const MENUS = Object.freeze(" + json() + ");");
        return lines;
    }

    /** The registry as JSON: kind → { kind, nodes }. */
    public String json() {
        var sb = new StringBuilder("{");
        for (int i = 0; i < kinds.size(); i++) {
            if (i > 0) sb.append(',');
            var k = kinds.get(i);
            sb.append(StampedParams.jsString(k.kind())).append(":{\"kind\":").append(StampedParams.jsString(k.kind())).append(",\"nodes\":");
            nodes(sb, k.children());
            sb.append('}');
        }
        return sb.append('}').toString();
    }
    private static void nodes(StringBuilder sb, List<? extends MenuRow<?, ?>> rows) {
        sb.append('[');
        for (int i = 0; i < rows.size(); i++) {
            if (i > 0) sb.append(',');
            var r = rows.get(i);
            sb.append("{\"id\":").append(StampedParams.jsString(r.id())).append(",\"label\":").append(StampedParams.jsString(r.label()));
            if (r.icon() != null) sb.append(",\"icon\":").append(StampedParams.jsString(Trees.semanticToken(r.icon())));
            if (r.hint() != null) sb.append(",\"hint\":").append(StampedParams.jsString(r.hint()));
            if (r.section() != 0) sb.append(",\"section\":").append(r.section());
            if (!r.children().isEmpty()) { sb.append(",\"nodes\":"); nodes(sb, r.children()); }
            sb.append('}');
        }
        sb.append(']');
    }
}
