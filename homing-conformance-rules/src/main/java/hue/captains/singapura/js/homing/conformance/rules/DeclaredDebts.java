package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The debts modules declare on their classes ({@link ConformanceDebt}), read where findings are
 * graded: a finding of a rule its module declares a debt to is that debt. And held to their counts:
 * a module owes exactly the findings it declares, so a debt never grows, and shrinks only as it is
 * declared to.
 */
public record DeclaredDebts() {

    public static final DeclaredDebts INSTANCE = new DeclaredDebts();

    /**
     * One debt as declared.
     *
     * @param rule     the rule owed
     * @param findings how many of its findings
     * @param reason   why
     */
    public record Debt(RuleId rule, int findings, String reason) {}

    /** The debts a module declares, by the id of the rule owed. None for a class not on the classpath. */
    public Map<RuleId, Debt> of(String moduleClass) {
        var out = new LinkedHashMap<RuleId, Debt>();
        Class<?> c = load(moduleClass);
        if (c == null) return out;
        for (ConformanceDebt d : c.getAnnotationsByType(ConformanceDebt.class)) {
            RuleId rule = idOf(d.rule());
            out.put(rule, new Debt(rule, d.findings(), d.reason()));
        }
        return out;
    }

    /** Why a finding is owed, if its module declares a debt to its rule. */
    public Optional<String> reasonFor(Finding finding) {
        return Optional.ofNullable(of(finding.moduleClass()).get(finding.rule())).map(Debt::reason);
    }

    /**
     * Every debt the crates' modules declare that is not what they owe: one paid - no finding owes it
     * any more, and the annotation is to go; one paid in part - the count is to be lowered; one grown -
     * findings beyond the count, new violations, to be fixed. None when every debt is exactly owed.
     */
    public List<String> unbalanced(Collection<? extends Crate> crates, List<Finding> raw) {
        Map<String, Long> owed = raw.stream().collect(Collectors.groupingBy(f -> f.moduleClass() + " " + f.rule().value(), Collectors.counting()));
        var out = new ArrayList<String>();
        for (Crate crate : crates)
            for (CrateEntry e : crate.entries())
                for (Debt d : of(e.moduleClass()).values()) {
                    long n = owed.getOrDefault(e.moduleClass() + " " + d.rule().value(), 0L);
                    if (n == d.findings()) continue;
                    out.add(e.moduleClass() + " owes " + n + " finding(s) of " + d.rule().value() + " but declares " + d.findings() + ": "
                            + (n == 0 ? "paid - remove its @ConformanceDebt"
                               : n < d.findings() ? "paid in part - lower its count to " + n
                               : (n - d.findings()) + " new - fix them: a debt never grows"));
                }
        return List.copyOf(out);
    }

    /** A rule's id, from its one instance. */
    private static RuleId idOf(Class<? extends JsRule> rule) {
        try {
            return ((JsRule) rule.getField("INSTANCE").get(null)).id();
        } catch (ReflectiveOperationException | ClassCastException e) {
            try {
                return rule.getDeclaredConstructor().newInstance().id();
            } catch (ReflectiveOperationException e2) {
                throw new IllegalStateException(rule.getName() + " is a rule with neither an INSTANCE nor a constructor of no arguments", e2);
            }
        }
    }

    private static Class<?> load(String name) {
        for (ClassLoader l : List.of(Thread.currentThread().getContextClassLoader(), DeclaredDebts.class.getClassLoader())) {
            try {
                return Class.forName(name, false, l);
            } catch (ClassNotFoundException | NullPointerException e) {
                // the next loader, or none
            }
        }
        return null;
    }
}
