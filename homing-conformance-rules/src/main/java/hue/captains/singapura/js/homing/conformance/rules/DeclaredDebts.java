package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The debts modules declare on their classes ({@link ConformanceDebt}), read where findings are
 * graded: a finding of a rule its module declares a debt to is that debt. And the other way: a debt a
 * module declares that none of its findings owes is paid, and the annotation is to go.
 */
public record DeclaredDebts() {

    public static final DeclaredDebts INSTANCE = new DeclaredDebts();

    /** The debts a module declares, by the id of the rule owed: each with its reason. None for a class not on the classpath. */
    public Map<RuleId, String> of(String moduleClass) {
        var out = new LinkedHashMap<RuleId, String>();
        Class<?> c = load(moduleClass);
        if (c == null) return out;
        for (ConformanceDebt d : c.getAnnotationsByType(ConformanceDebt.class)) out.put(idOf(d.rule()), d.reason());
        return out;
    }

    /** Why a finding is owed, if its module declares a debt to its rule. */
    public Optional<String> reasonFor(Finding finding) {
        return Optional.ofNullable(of(finding.moduleClass()).get(finding.rule()));
    }

    /** Every debt the crates' modules declare that no finding owes: paid - the annotation is to be removed. */
    public List<String> paid(Collection<? extends Crate> crates, List<Finding> raw) {
        Set<String> owed = raw.stream().map(f -> f.moduleClass() + " " + f.rule().value()).collect(Collectors.toSet());
        var out = new ArrayList<String>();
        for (Crate crate : crates)
            for (CrateEntry e : crate.entries())
                for (RuleId rule : of(e.moduleClass()).keySet())
                    if (!owed.contains(e.moduleClass() + " " + rule.value()))
                        out.add(e.moduleClass() + " declares a debt to " + rule.value() + " it no longer owes: remove its @ConformanceDebt");
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
