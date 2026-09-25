package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Components are classes: an exported class or data constant is clean; an exported function, expression or arrow is a finding; appMain and construct are the framework's entries. */
class ExportsAreClassesRuleTest {

    private static ServedModule mod(String... lines) {
        return new ServedModule("demo.M", StandardJsModuleType.CONSUMER, JsSource.of(lines));
    }

    private static List<String> names(List<Finding> findings) {
        return findings.stream().map(f -> f.message().replaceAll(".*exported function '([^']+)'.*", "$1")).toList();
    }

    @Test
    void aClassAndADataConstantAreClean() {
        assertEquals(List.of(), ExportsAreClassesRule.INSTANCE.check(mod(
                "class Button { constructor(el, props) { this._el = el; } }",
                "const Shape = class { };",
                "var KINDS = Object.freeze([\"a\", \"b\"]);",
                "const DEMOS = Object.freeze({ a: 1 });",
                "export {Button, Shape, KINDS, DEMOS};")));
    }

    @Test
    void anExportedFunctionExpressionOrArrowIsAFinding() {
        var findings = ExportsAreClassesRule.INSTANCE.check(mod(
                "function mountThing(opts) { return {}; }",
                "var createThing = function (o) { return {}; };",
                "const openThing = (o) => ({});",
                "const holdThing = o => o;",
                "async function loadThing() {}",
                "class Fine {}",
                "export {mountThing, createThing, openThing, holdThing, loadThing, Fine};"));
        assertEquals(List.of("mountThing", "createThing", "openThing", "holdThing", "loadThing"), names(findings));
        assertTrue(findings.stream().allMatch(f -> f.rule().value().equals("exports-are-classes")));
    }

    @Test
    void appMainIsTheScaffoldsEntryAndExempt() {
        assertEquals(List.of(), ExportsAreClassesRule.INSTANCE.check(mod(
                "function appMain(el, params) { }",
                "export {appMain};")));
    }

    /**
     * An on-demand widget's module exports {@code construct}: the loader imports
     * the module when the widget is first opened and calls that one name to have
     * the widget built. An entry, like appMain, not a component; the widget it
     * builds is the class, declared and judged where it lives.
     */
    @Test
    void constructIsAnOnDemandWidgetsEntryAndExempt() {
        assertEquals(List.of(), ExportsAreClassesRule.INSTANCE.check(mod(
                "function construct(branch, params, host) {",
                "    return new NoteWidget(branch, params, host);",
                "}",
                "export {construct};")));
    }

    /** The exemption is by name, and only for the entries: any other function beside one is still a finding. */
    @Test
    void anEntryDoesNotCoverTheFunctionsBesideIt() {
        assertEquals(List.of("mountNote"), names(ExportsAreClassesRule.INSTANCE.check(mod(
                "function construct(branch, params, host) { return mountNote(branch); }",
                "function mountNote(branch) { return { root: branch.createElement('n', 'div') }; }",
                "export {construct, mountNote};"))));
    }

    @Test
    void anExportWithNoTopLevelDeclarationIsNotJudged() {
        assertEquals(List.of(), ExportsAreClassesRule.INSTANCE.check(mod(
                "// the manager is injected by the prologue",
                "export {css};")));
    }

    @Test
    void commentsDoNotHideADeclarationOrMakeOne() {
        var findings = ExportsAreClassesRule.INSTANCE.check(mod(
                "// function mountThing() — this is prose",
                "function mountThing() {}   // the real one",
                "export {mountThing};"));
        assertEquals(List.of("mountThing"), names(findings));
        assertEquals(1, findings.get(0).line());
    }
}
