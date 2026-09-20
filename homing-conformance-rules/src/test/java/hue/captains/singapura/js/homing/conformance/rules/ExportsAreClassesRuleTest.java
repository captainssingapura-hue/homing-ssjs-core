package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Components are classes: an exported class or data constant is clean; an exported function, expression or arrow is a finding; appMain is the scaffold's. */
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
