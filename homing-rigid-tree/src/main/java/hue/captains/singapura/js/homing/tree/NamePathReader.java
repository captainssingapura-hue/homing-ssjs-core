package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayList;

/** Reads a name path off the wire ({@link NamePath}). */
public record NamePathReader() implements StatelessFunctionalObject {

    public static final NamePathReader INSTANCE = new NamePathReader();

    /**
     * Read a name-path from its {@code '/'}-joined wire form. Empty or {@code null}
     * input is the {@link NamePath#ROOT}; a leading or trailing {@code '/'} is tolerated.
     *
     * @return the path, or {@code null} if any segment is not a legal
     *         {@link NodeName} — including an empty one from a doubled separator
     */
    public NamePath parse(String wire) {
        if (wire == null) return NamePath.ROOT;
        String s = wire.trim();
        while (s.startsWith("/")) s = s.substring(1);
        while (s.endsWith("/"))   s = s.substring(0, s.length() - 1);
        if (s.isEmpty()) return NamePath.ROOT;

        var out = new ArrayList<NodeName>();
        for (String part : s.split(String.valueOf(NodeName.SEPARATOR))) {
            if (part.isEmpty()) return null;              // "//" — not a path we mint
            try {
                out.add(new NodeName(part));
            } catch (IllegalArgumentException notASegment) {
                return null;                              // ".." and friends land here
            }
        }
        return new NamePath(out);
    }
}
