package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.RoleNode;
import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.color.ColorLeaf;
import hue.captains.singapura.js.homing.design.color.ColorScale;
import hue.captains.singapura.js.homing.design.semantic.ColorTree;
import hue.captains.singapura.js.homing.design.semantic.DesignStyle;
import hue.captains.singapura.js.homing.design.semantic.TypeArguments;
import hue.captains.singapura.js.homing.design.styles.CastingStyle;
import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.js.homing.design.styles.StylingRes;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

/**
 * One jar's declaration held alone (RFC 0067, appendix The Style Jars): every implementation it lists, each by itself,
 * and the jar as a whole - so a jar's own test promises what it ships, whatever else is on the classpath.
 *
 * <p>Each implementation is a stateless record of the jar, typed by its style, its component and its role; the pair a
 * casting of the taxonomy; and what it answers held:</p>
 * <ul>
 *   <li>{@code CssContent}: its CSS at its canonical name in the jar, its marks in their forms, and, filled in for its
 *       casting, the rule a casting's rule may be (D9, D12, D29) - its colours the tree's, seen by the style's scheme,
 *       and used and imported both ways (D32);</li>
 *   <li>{@code Delegation}: no CSS of its own; its chain followed to an implementation of CSS, never round to itself,
 *       of the same component, every colour of it one the delegating style's scheme sees.</li>
 * </ul>
 *
 * <p>And the jar: no two implementations of one (style, component, role) - by their type arguments, never their names;
 * and nothing of it left out - every implementation of a casting it holds, and every CSS file under
 * {@code hue/captains/singapura/js/homing/design/styles/}, is one its declaration lists.</p>
 */
public record CheckDeclaration() implements StatelessFunctionalObject {

    public static final CheckDeclaration INSTANCE = new CheckDeclaration();

    /** Where a jar's implementations live, and their CSS: under a package per style. */
    public static final String ROOT = "hue/captains/singapura/js/homing/design/styles/";

    /**
     * One implementation, held alone.
     *
     * @param type     its class
     * @param style    its style's token
     * @param casting  the casting it implements, by its token - or what its types name, when they name none
     * @param problems what refuses it
     */
    public record Implementation(Class<?> type, String style, String casting, List<String> problems) implements ValueObject {
        public Implementation { problems = List.copyOf(problems); }

        public boolean passes() { return problems.isEmpty(); }

        /** How a test names it: its style's token, then its class's simple name. */
        public String name() { return style + "." + type.getSimpleName(); }
    }

    /**
     * What the check found: each implementation's own, and the jar's.
     *
     * @param implementations each implementation the declaration lists, held alone
     * @param problems        the jar's: two of one casting, something of it left out
     */
    public record Checked(List<Implementation> implementations, List<String> problems) implements ValueObject {
        public Checked { implementations = List.copyOf(implementations); problems = List.copyOf(problems); }

        public boolean passes() { return problems.isEmpty() && implementations.stream().allMatch(Implementation::passes); }

        /** Every problem, the jar's then each implementation's, named. */
        public List<String> all() {
            var out = new ArrayList<>(problems);
            for (Implementation i : implementations) for (String p : i.problems()) out.add(i.name() + ": " + p);
            return out;
        }
    }

    /** A declaration held, refused with every problem at once. */
    public void require(StyleDeclaration declaration, Taxonomy taxonomy, ColorTree tree) {
        Checked c = check(declaration, taxonomy, tree);
        if (!c.passes())
            throw new IllegalArgumentException(declaration.getClass().getSimpleName() + " is refused:\n  " + String.join("\n  ", c.all()));
    }

    /** A declaration held: each implementation, then the jar. */
    public Checked check(StyleDeclaration declaration, Taxonomy taxonomy, ColorTree tree) {
        String jar = CastingCss.home(declaration.getClass());
        var implementations = new ArrayList<Implementation>();
        var problems = new ArrayList<String>();
        var byKey = new LinkedHashMap<String, String>();
        var declared = new LinkedHashSet<Class<?>>();
        for (StyleDeclaration.Provided<?, ?> provided : declaration.provided()) {
            DesignStyle<?> style = provided.style();
            for (CastingStyle<?, ?, ?, ?> implementation : provided.castings()) {
                Implementation one = one(implementation, style, jar, taxonomy, tree);
                implementations.add(one);
                Class<?> type = implementation.getClass();
                if (!declared.add(type)) {
                    problems.add(type.getName() + " is listed twice");
                    continue;
                }
                Optional<String> key = key(type);
                if (key.isPresent()) {
                    String first = byKey.putIfAbsent(key.get(), type.getName());
                    if (first != null)
                        problems.add(type.getName() + " and " + first + " both implement " + one.casting() + " in " + style.token()
                                + ": one implementation of a casting in a style, by its type arguments");
                }
            }
        }
        var packages = new LinkedHashSet<String>();
        for (StyleDeclaration.Provided<?, ?> provided : declaration.provided()) packages.add(packageOf(provided.style()));
        problems.addAll(leftOut(declaration.getClass(), declared, packages));
        return new Checked(implementations, problems);
    }

    /** One implementation, alone. */
    private Implementation one(CastingStyle<?, ?, ?, ?> implementation, DesignStyle<?> style, String jar, Taxonomy taxonomy, ColorTree tree) {
        Class<?> type = implementation.getClass();
        var problems = new ArrayList<String>();
        if (!type.isRecord() || type.getRecordComponents().length > 0)
            problems.add("is no stateless record: an implementation is a functional object, a record of no components");
        if (jar != null && !jar.equals(CastingCss.home(type)))
            problems.add("is not of the declaring jar: a jar declares its own implementations alone");
        String home = packageOf(style);
        if (!type.getName().replace('.', '/').startsWith(home) || type.getName().substring(home.length()).contains("."))
            problems.add("is not in " + home.substring(0, home.length() - 1).replace('/', '.') + ": an implementation's package is its style's, design.styles.<style>");
        List<Class<?>> args = TypeArguments.INSTANCE.of(type, CastingStyle.class);
        if (args.size() != 4 || args.contains(null)) {
            problems.add("does not bind CastingStyle's four type arguments to classes: its scheme, its style, its component, its role");
            return new Implementation(type, style.token(), "?", problems);
        }
        if (args.get(1) != style.getClass())
            problems.add("is " + args.get(1).getSimpleName() + "'s, listed for " + style.token());
        Optional<Class<?>> scheme = tree.scheme(style);
        if (scheme.isEmpty()) problems.add(style.token() + " requires no scheme: no colour can be held to it");
        Casting casting = casting(args.get(2), args.get(3)).orElse(null);
        if (casting == null) {
            problems.add(args.get(2).getSimpleName() + " as " + args.get(3).getSimpleName() + " names no component and role the taxonomy can make");
            return new Implementation(type, style.token(), args.get(2).getSimpleName() + "_" + args.get(3).getSimpleName(), problems);
        }
        if (!taxonomy.castings().contains(casting)) {
            boolean functional = !casting.standalone() && casting.functional();
            problems.add(casting.token() + " is no casting of the taxonomy" + (functional
                    ? ": a role that only places a part folds into its component on its own - implement it as RoleRoot"
                    : ": no owner casts " + casting.cast().token() + " in that role"));
            return new Implementation(type, style.token(), casting.token(), problems);
        }
        StylingRes<?, ?> res = implementation.style();
        switch (res) {
            case null -> problems.add("answers nothing: CssContent, or a Delegation");
            case StylingRes.CssContent<?, ?> content -> content(type, style, casting, content, scheme.orElse(null), taxonomy, tree, problems);
            case StylingRes.Delegation<?, ?> delegation -> delegation(type, style, casting, delegation, scheme.orElse(null), tree, problems);
        }
        return new Implementation(type, style.token(), casting.token(), problems);
    }

    /**
     * What a casting's CSS was found to be, as each implementation's is held.
     *
     * @param marked   whether its marks are in form: past them its rule is read, and its colours are its rule's
     * @param problems what refuses it, each naming its file and line
     * @param reports  what the soft check says and does not refuse: a state drawn out of the casting's precedence (D23)
     * @param uses     the colours of the tree its rule uses, by token, each with where it first does
     */
    record CssChecked(boolean marked, List<String> problems, List<String> reports, Map<String, String> uses) {}

    /** A casting's CSS, its marks in: their form, then - filled in for the casting - a casting's rule, held to its leaf and the tree. */
    static CssChecked css(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, Casting casting, String name, String text) {
        return css(taxonomy, tree, style, casting, name, text, List.of());
    }

    /** As {@link #css(Taxonomy, ColorTree, DesignStyle, Casting, String, String)}, with the implementation's own spectra (D41). */
    static CssChecked css(Taxonomy taxonomy, ColorTree tree, DesignStyle<?> style, Casting casting, String name, String text,
                          List<? extends ColorScale<?, ?>> spectra) {
        CastingCss.Marked marked = CastingCss.INSTANCE.marks(name, text);
        if (!marked.passes()) return new CssChecked(false, marked.problems(), List.of(), Map.of());
        var problems = new ArrayList<>(marked.problems());
        ReadCss.Read read = ReadCss.INSTANCE.read(CastingCss.INSTANCE.filled(text, casting));
        problems.addAll(read.problems());
        CastingRule rule = new CastingRule(taxonomy, tree, style, casting, name, spectra);
        for (ReadCss.Item item : read.items()) {
            switch (item) {
                case ReadCss.Rule r -> rule.check(r);
                case ReadCss.AtRule a -> rule.keyframes(a);
                case ReadCss.Declaration d -> { }
            }
        }
        CastingRule.Checked checked = rule.finish();
        problems.addAll(checked.problems());
        return new CssChecked(true, problems, checked.reports(), rule.uses());
    }

    /** Its own CSS: at its canonical name, marked in form, and - filled in for its casting - a casting's rule, its colours imported both ways. */
    private static void content(Class<?> type, DesignStyle<?> style, Casting casting, StylingRes.CssContent<?, ?> content, Class<?> scheme,
                                Taxonomy taxonomy, ColorTree tree, List<String> problems) {
        var css = CastingCss.INSTANCE;
        String file = css.resource(type);
        Optional<String> text = css.read(type);
        if (text.isEmpty()) {
            problems.add("answers CssContent, and its jar has no " + file + ": its CSS is at its canonical name");
            return;
        }
        String name = type.getSimpleName() + ".css";
        if (scheme == null) {
            problems.addAll(css.marks(name, text.get()).problems());
            return;
        }
        var spectra = new ArrayList<ColorScale<?, ?>>();
        for (ColorLeaf<?, ?, ?> c : content.colours())
            if (c instanceof ColorScale<?, ?> s && !tree.has(c)) spectra.add(s);
        CssChecked checked = css(taxonomy, tree, style, casting, name, text.get(), spectra);
        problems.addAll(checked.problems());
        if (!checked.marked()) return;
        var imported = new LinkedHashMap<String, ColorLeaf<?, ?, ?>>();
        for (ColorLeaf<?, ?, ?> c : content.colours()) {
            imported.put(c.token(), c);
            if (c instanceof ColorScale<?, ?> s && !tree.has(c)) {   // its own spectrum: its anchors the tree's, its positions its own (D41)
                for (ColorScale.Knot<?> k : s.knots()) {
                    if (!tree.has(k.color())) problems.add("its spectrum " + s.token() + " is anchored at " + k.color().token() + ", which the tree has not: a spectrum's anchors are the tree's colours");
                    else if (!tree.sees(scheme, k.color()))
                        problems.add("its spectrum " + s.token() + " is anchored at " + k.color().token() + ", which " + style.token() + "'s scheme, "
                                + tree.schemeName(scheme) + ", does not see");
                }
            } else if (!tree.has(c)) problems.add("imports " + c.token() + ", which the tree has not");
            else if (!tree.sees(scheme, c))
                problems.add("imports " + c.token() + ": it is " + tree.schemeName(tree.scheme(c)) + "'s, which " + style.token()
                        + "'s scheme, " + tree.schemeName(scheme) + ", does not see");
        }
        Map<String, String> uses = checked.uses();
        for (Map.Entry<String, String> u : uses.entrySet())
            if (!imported.containsKey(u.getKey()))
                problems.add(u.getValue() + ": uses " + u.getKey() + ", which it does not import: CssContent lists each colour its CSS uses (D32)");
        for (String i : imported.keySet())
            if (!uses.containsKey(i)) problems.add("imports " + i + ", and its CSS uses it nowhere: it imports what it uses (D32)");
    }

    /** A delegation: no CSS of its own; its chain to CSS, never round, of the same component; its colours the delegating scheme's. */
    private static void delegation(Class<?> type, DesignStyle<?> style, Casting casting, StylingRes.Delegation<?, ?> delegation, Class<?> scheme,
                                   ColorTree tree, List<String> problems) {
        if (CastingCss.INSTANCE.read(type).isPresent())
            problems.add("delegates, and its jar has " + CastingCss.INSTANCE.resource(type) + ": a delegation has no CSS of its own");
        var passed = new LinkedHashSet<Class<?>>();
        passed.add(type);
        CastingStyle<?, ?, ?, ?> at = delegation.to();
        while (true) {
            if (!passed.add(at.getClass())) {
                problems.add("delegates round to " + at.getClass().getSimpleName() + " again: "
                        + String.join(" -> ", passed.stream().map(Class::getSimpleName).toList()) + " -> " + at.getClass().getSimpleName());
                return;
            }
            Class<?> component = TypeArguments.INSTANCE.at(at.getClass(), CastingStyle.class, 2);
            if (component != casting.cast().getClass()) {
                problems.add("delegates to " + at.getClass().getSimpleName() + ", an implementation of another component: a delegation reaches its own");
                return;
            }
            StylingRes<?, ?> next = at.style();
            if (next instanceof StylingRes.Delegation<?, ?> d) {
                at = d.to();
                continue;
            }
            if (next instanceof StylingRes.CssContent<?, ?> c && scheme != null)
                for (ColorLeaf<?, ?, ?> colour : c.colours())
                    if (tree.has(colour) && !tree.sees(scheme, colour))
                        problems.add("delegates to " + at.getClass().getSimpleName() + ", whose " + colour.token() + " " + style.token()
                                + "'s scheme, " + tree.schemeName(scheme) + ", does not see");
            return;
        }
    }

    /** An implementation's key: its style, component and role, by their classes. */
    static Optional<String> key(Class<?> type) {
        List<Class<?>> args = TypeArguments.INSTANCE.of(type, CastingStyle.class);
        if (args.size() != 4 || args.contains(null)) return Optional.empty();
        return Optional.of(args.get(1).getName() + "|" + args.get(2).getName() + "|" + args.get(3).getName());
    }

    /** The casting an implementation's component and role classes name. */
    static Optional<Casting> casting(Class<?> component, Class<?> role) {
        Object c = instance(component);
        if (!(c instanceof Component<?> comp)) return Optional.empty();
        if (role == RoleRoot.class) return Optional.of(new Casting(comp));
        Object r = instance(role);
        return r instanceof RoleNode node ? Optional.of(new Casting(comp, node)) : Optional.empty();
    }

    /** A node's one instance: its INSTANCE, or a record of no components made. */
    private static Object instance(Class<?> type) {
        try {
            var field = type.getField("INSTANCE");
            return field.get(null);
        } catch (ReflectiveOperationException ignored) { }
        try {
            if (type.isRecord() && type.getRecordComponents().length == 0) return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException ignored) { }
        return null;
    }

    /** A style's package, as a path: design.styles and its token, its dashes dropped - {@code design/styles/neobrutalism/}. */
    public static String packageOf(DesignStyle<?> style) { return ROOT + style.token().replace("-", "") + "/"; }

    /** Whatever of the jar its declaration leaves out, in its styles' packages: an implementation's class, a CSS file. */
    private static List<String> leftOut(Class<?> declaration, Set<Class<?>> declared, Set<String> packages) {
        var problems = new ArrayList<String>();
        var cssDeclared = new HashMap<String, Class<?>>();
        for (Class<?> c : declared) cssDeclared.put(CastingCss.INSTANCE.resource(c), c);
        for (String entry : entries(declaration)) {
            if (packages.stream().noneMatch(entry::startsWith) || entry.substring(entry.lastIndexOf('/') + 1).isEmpty()) continue;
            if (packages.stream().noneMatch(p -> entry.lastIndexOf('/') == p.length() - 1)) continue;   // its own package, not one below
            if (entry.endsWith(".css")) {
                if (!cssDeclared.containsKey(entry))
                    problems.add(entry + " is no CSS of an implementation the declaration lists: a jar's CSS is its declared implementations'");
            } else if (entry.endsWith(".class") && !entry.contains("$")) {
                String name = entry.substring(0, entry.length() - ".class".length()).replace('/', '.');
                try {
                    Class<?> c = Class.forName(name, false, declaration.getClassLoader());
                    if (CastingStyle.class.isAssignableFrom(c) && !c.isInterface() && !declared.contains(c))
                        problems.add(name + " implements a casting, and the declaration does not list it: a jar declares every implementation it holds");
                } catch (ClassNotFoundException | LinkageError ignored) { }
            }
        }
        return problems;
    }

    /** Every entry of the jar - or classes' directory - a class was loaded from, as a path under its root. */
    private static List<String> entries(Class<?> of) {
        var out = new ArrayList<String>();
        var source = of.getProtectionDomain() == null ? null : of.getProtectionDomain().getCodeSource();
        if (source == null || source.getLocation() == null) return out;
        try {
            Path at = Path.of(source.getLocation().toURI());
            if (Files.isDirectory(at)) {
                try (Stream<Path> walk = Files.walk(at)) {
                    walk.filter(Files::isRegularFile).forEach(p -> out.add(at.relativize(p).toString().replace('\\', '/')));
                }
            } else {
                try (JarFile jar = new JarFile(at.toFile())) {
                    for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
                        JarEntry entry = e.nextElement();
                        if (!entry.isDirectory()) out.add(entry.getName());
                    }
                }
            }
        } catch (IOException | URISyntaxException | IllegalArgumentException e) {
            out.clear();
        }
        return out;
    }
}
