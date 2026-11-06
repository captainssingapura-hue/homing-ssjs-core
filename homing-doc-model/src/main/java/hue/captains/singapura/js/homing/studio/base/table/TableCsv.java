package hue.captains.singapura.js.homing.studio.base.table;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Reads a table written as CSV ({@link TableData}). */
public record TableCsv() implements StatelessFunctionalObject {

    public static final TableCsv INSTANCE = new TableCsv();

    /**
     * Build a TableData from a CSV string. First line is the header row;
     * subsequent lines are data rows. Cells are split on commas; no
     * styling, no spans (use the JSON constructor when those are needed).
     *
     * <p>Quoting: minimal — a field wrapped in {@code "…"} may contain
     * embedded commas; a literal {@code "} inside a quoted field is
     * escaped as {@code ""}. No line-break-in-quoted-field support; for
     * richer ingestion, construct a {@link TableData} directly.</p>
     */
    public TableData read(String csv) {
        Objects.requireNonNull(csv, "csv");
        String[] lines = csv.strip().split("\\r?\\n", -1);
        if (lines.length == 0 || (lines.length == 1 && lines[0].isBlank())) {
            return new TableData(List.of(), List.of());
        }
        var headers = parseCsvLine(lines[0]).stream().map(TableData.Cell::new).toList();
        var rows = new ArrayList<List<TableData.Cell>>(lines.length - 1);
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i];
            if (line.isBlank()) continue;
            rows.add(parseCsvLine(line).stream().map(TableData.Cell::new).toList());
        }
        return new TableData(headers, rows);
    }

    private List<String> parseCsvLine(String line) {
        var out = new ArrayList<String>();
        var sb = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        sb.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    sb.append(c);
                }
            } else {
                if (c == ',') {
                    out.add(sb.toString());
                    sb.setLength(0);
                } else if (c == '"' && sb.length() == 0) {
                    inQuotes = true;
                } else {
                    sb.append(c);
                }
            }
        }
        out.add(sb.toString());
        return out;
    }
}
