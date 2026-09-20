package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Components are classes. A served module may export a class, or a data
 * constant, and nothing else: an exported {@code function}, a function
 * expression or an arrow is a finding. A component built as a closure — a
 * mount or create function returning a bag of inner functions — has state
 * no one can name, no identity, no {@code instanceof}, no method set a
 * contract can hold it to, and a lifetime that is whatever its captures
 * make it; a class has all four, and its dependencies come in by the
 * constructor where they can be read. The framework's two shapes: an
 * <b>element</b> component tells its caller which element to mint
 * ({@code static TAG}) and takes it; a <b>branch</b> component takes the
 * sub-branch its caller made for it. Either way, everything it needs is a
 * constructor argument.
 *
 * <p>Read from the served text: the names in the emitted {@code export
 * {…};} line, each looked up at its top-level declaration. One exemption,
 * by name: {@code appMain}, an app's page entry, which the framework's
 * scaffold calls as a function by contract.</p>
 */
public record ExportsAreClassesRule() implements JsRule {

    public static final ExportsAreClassesRule INSTANCE = new ExportsAreClassesRule();

    /** The framework's page entry: called by the scaffold, never a component. */
    public static final Set<String> ENTRY_POINTS = Set.of("appMain");

    private static final Pattern EXPORT_LINE = Pattern.compile("^\\s*export\\s*\\{([^}]*)\\}\\s*;?\\s*$");

    @Override public RuleId      id()     { return new RuleId("exports-are-classes"); }
    @Override public String      intent() { return "A module exports classes and data constants only; a component is a class with its dependencies in the constructor, never an exported function."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("components-are-classes"); }

    @Override
    public List<Finding> check(ServedModule module) {
        List<String> raw = module.lines();
        List<String> code = JsText.stripComments(raw);
        var findings = new ArrayList<Finding>();
        for (int i = 0; i < code.size(); i++) {
            Matcher m = EXPORT_LINE.matcher(code.get(i));
            if (!m.find()) continue;
            for (String name : m.group(1).split(",")) {
                String n = name.trim();
                if (n.isEmpty()) continue;
                if (n.contains(" as ")) n = n.substring(0, n.indexOf(" as ")).trim();
                if (ENTRY_POINTS.contains(n)) continue;
                int at = declarationOf(code, n);
                if (at >= 0 && isFunction(code.get(at), n)) {
                    findings.add(new Finding(module.moduleClass(), id(),
                            "exported function '" + n + "' — a component is a class: " + raw.get(at).trim(), at));
                }
            }
        }
        return List.copyOf(findings);
    }

    /** The line that declares the name at the top level, or -1. */
    static int declarationOf(List<String> code, String name) {
        Pattern p = Pattern.compile("^(?:async\\s+)?(?:function\\s*\\*?\\s*|class\\s+|(?:var|let|const)\\s+)" + Pattern.quote(name) + "\\b");
        for (int i = 0; i < code.size(); i++) if (p.matcher(code.get(i)).find()) return i;
        return -1;
    }

    /** A function declaration, a function expression, or an arrow bound to the name. A class, or any other value, is not. */
    static boolean isFunction(String line, String name) {
        String q = Pattern.quote(name);
        if (Pattern.compile("^(?:async\\s+)?function\\s*\\*?\\s*" + q + "\\b").matcher(line).find()) return true;
        Matcher m = Pattern.compile("^(?:var|let|const)\\s+" + q + "\\s*=\\s*(.*)$").matcher(line);
        if (!m.find()) return false;
        String rhs = m.group(1).trim();
        if (rhs.startsWith("class")) return false;
        return rhs.matches("(?:async\\s+)?function\\b.*")
            || rhs.matches("(?:async\\s+)?\\(?[A-Za-z_$][\\w$]*(?:\\s*,\\s*[A-Za-z_$][\\w$]*)*\\)?\\s*=>.*")
            || rhs.matches("(?:async\\s+)?\\(\\s*\\)\\s*=>.*");
    }
}
