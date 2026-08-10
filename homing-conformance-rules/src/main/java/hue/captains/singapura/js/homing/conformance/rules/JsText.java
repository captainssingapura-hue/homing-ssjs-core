package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0044 Phase 7 — served-JS text helpers shared by the pattern-scanning
 * rules. Chiefly {@link #stripComments}: blanks {@code //} line comments and
 * {@code /* ... *}{@code /} block comments (comment text → spaces, preserving
 * line/column positions) so commentary describing a ban doesn't trip the rule
 * that enforces it. String literals containing comment markers are not handled
 * with full fidelity — if a real code line trips a rule because of one, that
 * module is allowlisted with a justification.
 */
public final class JsText {

    private JsText() {}

    public static List<String> stripComments(List<String> lines) {
        var out = new ArrayList<String>(lines.size());
        boolean inBlock = false;
        for (String raw : lines) {
            var sb = new StringBuilder(raw.length());
            int i = 0;
            while (i < raw.length()) {
                if (inBlock) {
                    int end = raw.indexOf("*/", i);
                    if (end < 0) {
                        while (i < raw.length()) { sb.append(' '); i++; }
                    } else {
                        for (int j = i; j < end + 2; j++) sb.append(' ');
                        i = end + 2;
                        inBlock = false;
                    }
                } else if (i + 1 < raw.length() && raw.charAt(i) == '/' && raw.charAt(i + 1) == '/') {
                    while (i < raw.length()) { sb.append(' '); i++; }
                } else if (i + 1 < raw.length() && raw.charAt(i) == '/' && raw.charAt(i + 1) == '*') {
                    inBlock = true;
                    sb.append(' ').append(' ');
                    i += 2;
                } else {
                    sb.append(raw.charAt(i));
                    i++;
                }
            }
            out.add(sb.toString());
        }
        return out;
    }
}
