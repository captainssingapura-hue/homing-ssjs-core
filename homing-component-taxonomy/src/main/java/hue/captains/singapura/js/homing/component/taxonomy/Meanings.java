package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.js.homing.component.taxonomy.TaxonomyProblem.Rule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Where a node's {@link Meaning} is written, and how it is read: the section headed by the node's
 * name within its declaring class, in {@code meanings/<the declaring class's path>.md} on the
 * node's classpath. Read when asked, kept nowhere.
 */
record Meanings() implements StatelessFunctionalObject {

    static final Meanings INSTANCE = new Meanings();

    /** A node's meaning, if its section is written and says something. */
    Optional<Meaning> of(Object node) {
        var text = read(outermost(node.getClass()));
        if (text.isEmpty()) return Optional.empty();
        var body = sections(text.get()).sections().get(heading(node.getClass()));
        return body == null || body.isBlank() ? Optional.empty() : Optional.of(new Meaning(body));
    }

    /** A node's meaning, which a taxonomy read has made sure of. */
    Meaning meant(Object node) {
        return of(node).orElseThrow(() -> new IllegalStateException(Names.qualified(node) + " means nothing: no section '## "
                + heading(node.getClass()) + "' in " + path(outermost(node.getClass()))));
    }

    /**
     * What reading refuses of the nodes' meanings: a node with none; a file with a section that
     * names no node its class declares, a section twice, or words outside every section; two
     * nodes that mean the same.
     */
    List<TaxonomyProblem> problems(Collection<?> nodes) {
        var out = new ArrayList<TaxonomyProblem>();
        var files = new LinkedHashSet<Class<?>>();
        var said = new LinkedHashMap<String, List<Object>>();
        for (Object node : nodes) {
            Class<?> top = outermost(node.getClass());
            files.add(top);
            var meaning = of(node);
            if (meaning.isEmpty()) {
                out.add(new TaxonomyProblem(Rule.NO_MEANING, Names.qualified(node) + " means nothing: no section '## "
                        + heading(node.getClass()) + "' with words in it, in " + path(top)));
                continue;
            }
            said.computeIfAbsent(normalized(meaning.get().markdown()), x -> new ArrayList<>()).add(node);
        }
        for (Class<?> top : files) {
            var text = read(top);
            if (text.isEmpty()) continue;
            var read = sections(text.get());
            for (String astray : read.astray())
                out.add(new TaxonomyProblem(Rule.MEANING_ASTRAY, path(top) + ": " + astray));
            for (String heading : read.sections().keySet())
                if (!declares(top, heading))
                    out.add(new TaxonomyProblem(Rule.MEANING_ASTRAY, path(top) + ": '## " + heading
                            + "' names no node " + top.getSimpleName() + " declares"));
        }
        said.values().forEach(same -> {
            if (same.size() > 1)
                out.add(new TaxonomyProblem(Rule.MEANING_TWICE, String.join(" and ", same.stream().map(Names::qualified).toList())
                        + " mean the same: one node, or two meanings"));
        });
        return out;
    }

    // ── where ───────────────────────────────────────────────────────────────

    /** The class a node is declared in, outermost: its file. */
    private static Class<?> outermost(Class<?> c) {
        Class<?> top = c;
        while (top.getEnclosingClass() != null) top = top.getEnclosingClass();
        return top;
    }

    /** {@code meanings/com/example/House.md}. */
    private static String path(Class<?> top) { return "meanings/" + top.getName().replace('.', '/') + ".md"; }

    /** The node's name within its file: {@code Card} for {@code House.Card}, {@code Here.Badge} two deep, its own name for the class itself. */
    private static String heading(Class<?> c) {
        Class<?> top = outermost(c);
        return c == top ? c.getSimpleName() : c.getName().substring(top.getName().length() + 1).replace('$', '.');
    }

    /** Whether a heading names a node of either tree that the file's class declares, or is. */
    private static boolean declares(Class<?> top, String heading) {
        if (heading.equals(top.getSimpleName()) && isNode(top)) return true;
        try {
            return isNode(Class.forName(top.getName() + "$" + heading.replace('.', '$'), false, top.getClassLoader()));
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private static boolean isNode(Class<?> c) { return Taxon.class.isAssignableFrom(c) || RoleNode.class.isAssignableFrom(c); }

    private static Optional<String> read(Class<?> top) {
        try (InputStream in = top.getClassLoader().getResourceAsStream(path(top))) {
            return in == null ? Optional.empty() : Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new UncheckedIOException(path(top), e);
        }
    }

    // ── what ────────────────────────────────────────────────────────────────

    /** A file read: its sections by heading, and what is astray in it. */
    private record Read(Map<String, String> sections, List<String> astray) {}

    /**
     * The file's sections, each from its {@code ## } heading to the next - a fenced block's lines
     * are never headings - and what is astray: a heading twice, and anything before the first
     * section but a {@code # } title.
     */
    private static Read sections(String text) {
        var sections = new LinkedHashMap<String, String>();
        var astray = new ArrayList<String>();
        String at = null;
        var body = new StringBuilder();
        boolean fenced = false;
        for (String line : text.split("\r?\n", -1)) {
            String trimmed = line.strip();
            if (trimmed.startsWith("```") || trimmed.startsWith("~~~")) fenced = !fenced;
            if (!fenced && line.startsWith("## ")) {
                if (at != null) sections.put(at, body.toString().strip());
                at = line.substring(3).strip();
                body.setLength(0);
                if (sections.containsKey(at)) astray.add("'## " + at + "' twice");
                continue;
            }
            if (at != null) body.append(line).append('\n');
            else if (!trimmed.isEmpty() && !line.startsWith("# ")) astray.add("words before the first section: '" + trimmed + "'");
        }
        if (at != null) sections.put(at, body.toString().strip());
        return new Read(sections, astray);
    }

    /** A meaning's words alone, so two that say the same are seen to. */
    private static String normalized(String markdown) { return markdown.strip().replaceAll("\\s+", " "); }
}
