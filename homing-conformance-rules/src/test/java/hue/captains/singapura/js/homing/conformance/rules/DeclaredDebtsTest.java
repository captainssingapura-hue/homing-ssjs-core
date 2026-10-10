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
 * any other rule still fails. A debt no finding owes any more is paid, and said so.
 */
class DeclaredDebtsTest {

    @ConformanceDebt(rule = NoInlineStyleRule.class, reason = "It styles inline until its sheet is typed")
    @ConformanceDebt(rule = WidgetDrawsOnlyComponentsRule.class, reason = "It makes its own elements until the containers exist")
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
        assertEquals(Map.of(NoInlineStyleRule.INSTANCE.id(), "It styles inline until its sheet is typed",
                            WidgetDrawsOnlyComponentsRule.INSTANCE.id(), "It makes its own elements until the containers exist"),
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
    void aDebtNoFindingOwes_isPaid() {
        var crates = List.of(new OneEntry(CrateEntry.of(IndebtedModule.INSTANCE)));
        assertEquals(List.of(), DeclaredDebts.INSTANCE.paid(crates,
                List.of(finding(NoInlineStyleRule.INSTANCE), finding(WidgetDrawsOnlyComponentsRule.INSTANCE))));
        assertEquals(List.of(MODULE + " declares a debt to widget-draws-only-components it no longer owes: remove its @ConformanceDebt"),
                DeclaredDebts.INSTANCE.paid(crates, List.of(finding(NoInlineStyleRule.INSTANCE))));
    }
}
