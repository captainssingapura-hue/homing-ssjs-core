package hue.captains.singapura.js.homing.conformance.rules;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A rule a module does not keep yet, declared on the module's own class, with how many findings it
 * owes and why: the module's findings of that rule are graded as its debt - a warning that says the
 * reason - where they would otherwise fail. Explicit and beside the code that owes it; one per rule
 * owed. The count is exact ({@link DeclaredDebts#unbalanced}): one finding more is a new violation,
 * and refused - a debt never grows; one fewer is a debt paid in part, and the count is to be lowered;
 * none is a debt paid, and the annotation is to go. So debts only ever shrink.
 *
 * <pre>{@code
 * @ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class, findings = 12,
 *         reason = "It makes its own elements: waits for the designed containers")
 * public record CatalogueCardsModule() implements DomModule<CatalogueCardsModule> { … }
 * }</pre>
 *
 * <p>Build-time only, as the rules are: a module that declares a debt has the rules as a
 * {@code provided} dependency, never on its run-time path.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(ConformanceDebts.class)
public @interface ConformanceDebt {

    /** The rule not kept yet. */
    Class<? extends JsRule> rule();

    /** How many findings of the rule the module owes: exactly - never more, and lowered as they are paid. */
    int findings();

    /** Why it is owed, and what would pay it. */
    String reason();
}
