package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A module declares the rules it does not keep yet on its own class, with why: its findings of those
 * rules are its debt - a warning, even in the strictest grader, saying the reason - and a finding of
 * any other rule still fails. A debt is owed exactly as declared: a finding more is refused, and a
 * debt paid, whole or in part, is said so.
 */
class DeclaredDebtsTest {

    @ConformanceDebt(rule = NoInlineStyleRule.class, findings = 1, reason = "It styles inline until its sheet is typed")
    @ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class, findings = 2, reason = "It makes its own elements until the containers exist")
    record IndebtedModule() implements DomModule<IndebtedModule>, SelfContent {
        static final IndebtedModule INSTANCE = new IndebtedModule();
        @Override public ImportsFor<IndebtedModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<IndebtedModule> exports() { return new ExportsOf<>(INSTANCE, List.of()); }
        @Override public List<String> selfContent(ModuleNameResolver resolver) { return List.of(); }
    }

    record OneEntry(CrateEntry entry) implements Crate {
        @Override public String name() { return "one"; }
        @Override public List<CrateEntry> entries() { return List.of(entry); }
        @Override public List<Crate> requires() { return List.of(); }
    }

    private static final String MODULE = IndebtedModule.class.getName();

    private static Finding finding(JsRule rule) { return new Finding(MODULE, rule.id(), "el.style.width = '1px';", 3); }

    @Test
    void aModuleSaysWhatItOwes_byTheRule_withWhy() {
        var inline = NoInlineStyleRule.INSTANCE.id();
        var widget = WidgetDrawsOnlyComponentsRule.INSTANCE.id();
        assertEquals(Map.of(inline, new DeclaredDebts.Debt(inline, 1, "It styles inline until its sheet is typed"),
                            widget, new DeclaredDebts.Debt(widget, 2, "It makes its own elements until the containers exist")),
                DeclaredDebts.INSTANCE.of(MODULE));
        assertEquals(Map.of(), DeclaredDebts.INSTANCE.of("no.such.Module"), "a class not on the classpath owes nothing");
    }

    @Test
    void itsFindingsOfARuleOwed_areItsDebt_evenStrictly_andAnyOtherRuleStillFails() {
        var owed = FindingGrader.STRICT.grade(finding(NoInlineStyleRule.INSTANCE));
        assertEquals(Severity.WARNING, owed.severity());
        assertEquals(Disposition.DEBT, owed.disposition());
        assertEquals("debt: It styles inline until its sheet is typed", owed.note());
        var fresh = FindingGrader.STRICT.grade(finding(NoLiteralColorRule.INSTANCE));
        assertTrue(fresh.isError(), "a rule it declares no debt to");
    }

    @Test
    void aDebtIsOwedExactly_neverMore_andLoweredAsItIsPaid() {
        var crates = List.of(new OneEntry(CrateEntry.of(IndebtedModule.INSTANCE)));
        var widget = finding(WidgetDrawsOnlyComponentsRule.INSTANCE);
        var inline = finding(NoInlineStyleRule.INSTANCE);
        assertEquals(List.of(), DeclaredDebts.INSTANCE.unbalanced(crates, List.of(inline, widget, widget)), "as declared");
        assertEquals(List.of(MODULE + " owes 3 finding(s) of widget-draws-only-components but declares 2: 1 new - fix them: a debt never grows"),
                DeclaredDebts.INSTANCE.unbalanced(crates, List.of(inline, widget, widget, widget)));
        assertEquals(List.of(MODULE + " owes 1 finding(s) of widget-draws-only-components but declares 2: paid in part - lower its count to 1"),
                DeclaredDebts.INSTANCE.unbalanced(crates, List.of(inline, widget)));
        assertEquals(List.of(MODULE + " owes 0 finding(s) of widget-draws-only-components but declares 2: paid - remove its @ConformanceDebt"),
                DeclaredDebts.INSTANCE.unbalanced(crates, List.of(inline)));
    }
}
