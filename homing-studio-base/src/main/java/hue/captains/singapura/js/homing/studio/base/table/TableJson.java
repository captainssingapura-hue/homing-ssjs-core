package hue.captains.singapura.js.homing.studio.base.table;

import hue.captains.singapura.js.homing.studio.base.table.TableData.Cell;

/**
 * A table's data as the table viewers read it: the stable JSON envelope - headers,
 * then rows, each cell its text and, when not the default, its spans, badge and
 * alignment. The legacy wire's, made from the data; the data knows nothing of it.
 */
public final class TableJson {

    private TableJson() {}


    /** The envelope for {@code data}. */
    public static String write(TableData data) {
        var sb = new StringBuilder("{\"headers\":[");
        for (int i = 0; i < data.headers().size(); i++) {
            if (i > 0) sb.append(',');
            appendCell(sb, data.headers().get(i));
        }
        sb.append("],\"rows\":[");
        for (int r = 0; r < data.rows().size(); r++) {
            if (r > 0) sb.append(',');
            sb.append('[');
            var row = data.rows().get(r);
            for (int c = 0; c < row.size(); c++) {
                if (c > 0) sb.append(',');
                appendCell(sb, row.get(c));
            }
            sb.append(']');
        }
        sb.append("]}");
        return sb.toString();
    }

    private static void appendCell(StringBuilder sb, Cell c) {
        sb.append("{\"text\":").append(jstr(c.text()));
        if (c.colspan() != 1) sb.append(",\"colspan\":").append(c.colspan());
        if (c.rowspan() != 1) sb.append(",\"rowspan\":").append(c.rowspan());
        if (c.badge() != null) sb.append(",\"badge\":").append(jstr(c.badge().name().toLowerCase()));
        if (c.align() != null) sb.append(",\"align\":").append(jstr(c.align().name().toLowerCase()));
        sb.append('}');
    }

    private static String jstr(String v) {
        var sb = new StringBuilder("\"");
        for (int i = 0; i < v.length(); i++) {
            char ch = v.charAt(i);
            switch (ch) {
                case '\\' -> sb.append("\\\\");
                case '"'  -> sb.append("\\\"");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (ch < 0x20) sb.append(String.format("\\u%04x", (int) ch));
                    else sb.append(ch);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
