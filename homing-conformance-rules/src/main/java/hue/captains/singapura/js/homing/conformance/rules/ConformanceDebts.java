package hue.captains.singapura.js.homing.conformance.rules;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The debts a module declares, when it owes more than one rule: written as several {@link ConformanceDebt}s. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConformanceDebts {
    ConformanceDebt[] value();
}
