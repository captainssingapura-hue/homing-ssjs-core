package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Line;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Wraps prose into lines at the {@value Line#MAX_CHARS}-char cap. */
public record LineWrapper() implements StatelessFunctionalObject {

    public static final LineWrapper INSTANCE = new LineWrapper();

    /** The words of {@code text} in lines: whitespace runs collapse to one space, an over-long word is hard-split. */
    public List<Line.Plain> wrap(String text) {
        Objects.requireNonNull(text, "ParagraphSegment: text");
        var lines = new ArrayList<Line.Plain>();
        var cur = new StringBuilder();
        for (String w : text.trim().split("\\s+")) {
            if (w.isEmpty()) continue;
            while (w.length() > Line.MAX_CHARS) {
                if (cur.length() > 0) { lines.add(new Line.Plain(cur.toString())); cur.setLength(0); }
                lines.add(new Line.Plain(w.substring(0, Line.MAX_CHARS)));
                w = w.substring(Line.MAX_CHARS);
            }
            int need = (cur.length() == 0) ? w.length() : cur.length() + 1 + w.length();
            if (need > Line.MAX_CHARS) {
                lines.add(new Line.Plain(cur.toString()));
                cur.setLength(0);
            }
            if (cur.length() > 0) cur.append(' ');
            cur.append(w);
        }
        if (cur.length() > 0) lines.add(new Line.Plain(cur.toString()));
        if (lines.isEmpty()) lines.add(new Line.Plain(""));   // empty input → one empty line
        return lines;
    }
}
