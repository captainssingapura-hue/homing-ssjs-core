package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * One implementation's CSS (RFC 0067, appendix The Style Jars): a resource in the implementation's own jar at its
 * canonical name - {@code hue/captains/singapura/js/homing/design/styles/editorial/PlainButton.css} - holding no class
 * name. Where its casting and its states go is marked in INVALID CSS, so a file served or read unfilled applies nothing:
 *
 * <ul>
 *   <li>{@code $casting { … }} - its one rule, the casting's class;</li>
 *   <li>{@code &$word { … }}, nested in it - an application state, its class for the word;</li>
 *   <li>{@code @keyframes $name} - a keyframes of its own, and {@code $name} where a value uses it.</li>
 * </ul>
 *
 * The browser's own pseudo-classes and pseudo-elements stay as they are ({@code &:hover}, {@code &::placeholder}).
 * Filled in for a casting, the marks become its classes: the same file serves every casting delegating to it, each
 * under its own class.
 */
public record CastingCss() implements StatelessFunctionalObject {

    public static final CastingCss INSTANCE = new CastingCss();

    private static final String CASTING = "$casting";
    private static final Pattern NAME = Pattern.compile("[a-z][a-z0-9-]*");
    private static final Pattern STATE = Pattern.compile("&\\$([a-z][a-z0-9-]*)");
    private static final Pattern KEYFRAMES = Pattern.compile("@keyframes\\s+\\$([a-z][a-z0-9-]*)");
    private static final Pattern USE = Pattern.compile("\\$([a-z][a-z0-9-]*)");
    private static final Pattern CLASS = Pattern.compile("(?<![\\w-])\\.[A-Za-z_-][\\w-]*");

    /** The resource an implementation's CSS is at: its canonical name as a path, then {@code .css}. */
    public String resource(Class<?> implementation) {
        String name = implementation.getCanonicalName();
        if (name == null) throw new IllegalArgumentException(implementation.getName() + " has no canonical name: an implementation is a named class");
        return name.replace('.', '/') + ".css";
    }

    /** The implementation's CSS, from its own jar: none when the jar has none at its canonical name. */
    public Optional<String> read(Class<?> implementation) {
        String path = resource(implementation);
        ClassLoader loader = implementation.getClassLoader() == null ? ClassLoader.getSystemClassLoader() : implementation.getClassLoader();
        try {
            var found = loader.getResources(path);
            String home = home(implementation);
            while (found.hasMoreElements()) {
                URL url = found.nextElement();
                if (home != null && !url.toString().contains(home)) continue;   // another jar's, of the same name: not this one's
                try (InputStream in = url.openStream()) {
                    return Optional.of(new String(in.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new IllegalStateException(path + " could not be read: " + e.getMessage(), e);
        }
    }

    /** Where a class was loaded from - its jar or its classes' directory - as the URL text its resources start with. */
    static String home(Class<?> c) {
        CodeSource source = c.getProtectionDomain() == null ? null : c.getProtectionDomain().getCodeSource();
        if (source == null || source.getLocation() == null) return null;
        String at = source.getLocation().toString();
        return at.endsWith(".jar") ? at + "!/" : at;
    }

    /**
     * What a file marks, and every problem with its marks: its one rule {@code $casting}; each state {@code &$word};
     * each keyframes {@code @keyframes $name}, once, and every {@code $name} a value uses one of them; no class name
     * anywhere, and no other {@code $}.
     *
     * @param states    the words of the states it marks, as written
     * @param keyframes the names of its keyframes
     */
    public record Marked(List<String> problems, Set<String> states, Set<String> keyframes) implements ValueObject {
        public Marked { problems = List.copyOf(problems); states = Set.copyOf(states); keyframes = Set.copyOf(keyframes); }

        public boolean passes() { return problems.isEmpty(); }
    }

    /** A file's marks, held to their forms. {@code name} names the file in what it says. */
    public Marked marks(String name, String css) {
        var problems = new ArrayList<String>();
        var states = new LinkedHashSet<String>();
        var keyframes = new LinkedHashSet<String>();
        ReadCss.Read read = ReadCss.INSTANCE.read(css);
        for (String p : read.problems()) problems.add(name + ":" + p);
        int rules = 0;
        for (ReadCss.Item item : read.items()) {
            switch (item) {
                case ReadCss.Rule r when CastingRule.squeeze(r.selector()).equals(CASTING) -> {
                    rules++;
                    if (rules > 1) problems.add(name + ":" + r.line() + ": $casting is ruled twice: an implementation's CSS holds its casting's one rule");
                    else inside(name, r.body(), states, problems);
                }
                case ReadCss.Rule r -> problems.add(name + ":" + r.line() + ": " + r.selector().trim() + " is no mark of the casting: an implementation's "
                        + "CSS rules its casting as $casting { … }, and names no class (The Style Jars)");
                case ReadCss.AtRule a -> {
                    Matcher m = KEYFRAMES.matcher("@" + a.prelude().trim());
                    if (!m.matches()) problems.add(name + ":" + a.line() + ": @" + a.prelude().trim() + ": an implementation's CSS holds no at-rule at its top "
                            + "but its own keyframes, @keyframes $name");
                    else if (m.group(1).equals("casting")) problems.add(name + ":" + a.line() + ": @keyframes $casting: the casting's mark names no keyframes");
                    else if (!keyframes.add(m.group(1))) problems.add(name + ":" + a.line() + ": @keyframes $" + m.group(1) + " is written twice");
                }
                case ReadCss.Declaration d -> problems.add(name + ":" + d.line() + ": " + d.property() + " is declared outside $casting");
            }
        }
        if (rules == 0 && read.problems().isEmpty()) problems.add(name + ": marks no $casting rule: an implementation's CSS rules its casting");
        for (ReadCss.Item item : read.items()) values(name, item, keyframes, problems);
        return new Marked(problems, states, keyframes);
    }

    /** Inside the casting's rule: each nested selector a state's mark, the browser's own, or a pseudo-element's - no class, no other $. */
    private static void inside(String name, List<ReadCss.Item> body, Set<String> states, List<String> problems) {
        for (ReadCss.Item item : body) {
            switch (item) {
                case ReadCss.Rule r -> {
                    String selector = r.selector().trim();
                    Matcher state = STATE.matcher(selector);
                    if (CLASS.matcher(selector.replace("&$", "")).find())
                        problems.add(name + ":" + r.line() + ": " + selector + " names a class: an implementation's CSS names none - a state is &$word");
                    else if (state.matches()) states.add(state.group(1));
                    else if (selector.contains("$"))
                        problems.add(name + ":" + r.line() + ": " + selector + ": a state is marked &$word alone, its word lower case");
                    inside(name, r.body(), states, problems);
                }
                case ReadCss.AtRule a -> {
                    if (a.body() != null) inside(name, a.body(), states, problems);
                }
                case ReadCss.Declaration d -> { }
            }
        }
    }

    /** Every $ in a value: a use of a keyframes the file writes. */
    private static void values(String name, ReadCss.Item item, Set<String> keyframes, List<String> problems) {
        switch (item) {
            case ReadCss.Declaration d -> {
                for (Matcher m = USE.matcher(d.value()); m.find(); )
                    if (!keyframes.contains(m.group(1)))
                        problems.add(name + ":" + d.line() + ": " + d.property() + " uses $" + m.group(1) + ", and the file writes no @keyframes $" + m.group(1));
                if (d.value().replaceAll("\\$[a-z][a-z0-9-]*", "").contains("$"))
                    problems.add(name + ":" + d.line() + ": " + d.property() + ": a $ in a value names a keyframes of the file's, $name");
            }
            case ReadCss.Rule r -> { for (ReadCss.Item i : r.body()) values(name, i, keyframes, problems); }
            case ReadCss.AtRule a -> { if (a.body() != null) for (ReadCss.Item i : a.body()) values(name, i, keyframes, problems); }
        }
    }

    /**
     * The file with its marks filled in for a casting: {@code $casting} its class, {@code &$word} its state's class for
     * the word, {@code $name} - a keyframes of its own - {@code <casting>-<name>}.
     */
    public String filled(String css, Casting casting) {
        String styled = LeafClasses.INSTANCE.styled(casting);
        String token = casting.token();
        String out = KEYFRAMES.matcher(css).replaceAll(m -> "@keyframes " + Matcher.quoteReplacement(token + "-" + m.group(1)));
        out = STATE.matcher(out).replaceAll(m -> Matcher.quoteReplacement("&." + styled + "--" + m.group(1)));
        out = out.replace(CASTING, "." + styled);
        out = USE.matcher(out).replaceAll(m -> Matcher.quoteReplacement(token + "-" + m.group(1)));
        return out;
    }

    /** Whether a word is one a mark may carry. */
    static boolean word(String w) { return NAME.matcher(w).matches(); }
}
