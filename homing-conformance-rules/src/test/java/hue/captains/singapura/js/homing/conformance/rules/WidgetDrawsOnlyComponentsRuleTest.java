package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;
import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A widget draws nothing but components: every way of making, holding or touching an element of its
 * own is flagged - a comment mentioning one is not - and what a widget may do passes: sub-branches,
 * components made with a maker, sub-widgets, their API. The policy holds widgets to it, and only them.
 */
class WidgetDrawsOnlyComponentsRuleTest {

    private static ServedModule served(String... lines) {
        return new ServedModule("demo.Board", StandardJsModuleType.WIDGET, JsSource.of(lines));
    }

    private static List<Finding> check(String... lines) { return WidgetDrawsOnlyComponentsRule.INSTANCE.check(served(lines)); }

    @Test
    void whatAWidgetMayDo_passes() {
        assertEquals(List.of(), check(
                "// a widget never calls branch.createElement('box', 'div') nor touches .el",
                "var actions = this.branch.createBranch('actions');",
                "var save = new PrimaryButton(actions.elementMaker('save'), { label: 'Save' });",
                "save.setOnClick(this._saver);",
                "save.enabled = false;",
                "var bar = new ActionBar(this.branch.createBranch('bar'), {});",
                "this._party.join(this);"));
    }

    @Test
    void everyWayOfDrawingItsOwn_isFlagged_aLineAtATime() {
        String[] offending = {
                "var box = this.branch.createElement('box', 'div');",
                "var go = new PlainButton(PlainButtonFactory.create(this.branch, 'go'), {});",
                "var t = document.createTextNode('x');",
                "var copy = node.cloneNode(true);",
                "host.innerHTML = '<b>x</b>';",
                "var w = document.body;",
                "host.appendChild(box);",
                "row.append(cell);",
                "var hit = host.querySelector('.x');",
                "box.classList.add('mine');",
                "css.addClass(box, wg_scroll);",
                "box.setAttribute('role', 'list');",
                "box.style.width = '10px';",
                "note.textContent = 'none yet';",
                "this.root = this._panel.el;"};
        var findings = check(offending);
        assertEquals(offending.length, findings.size(), () -> String.join("\n", findings.stream().map(Finding::toString).toList()));
        assertTrue(findings.get(1).message().contains("hand the component its maker"), findings.get(1).message());
    }

    @Test
    void thePolicyHoldsWidgets_andOnlyWidgets_toIt() {
        var policy = DefaultJsRulePolicy.INSTANCE;
        assertTrue(policy.rulesFor(StandardJsModuleType.WIDGET).rules().contains(WidgetDrawsOnlyComponentsRule.INSTANCE));
        assertTrue(policy.rulesFor(StandardJsModuleType.WIDGET).rules().containsAll(DefaultJsRulePolicy.DOM_OWNER_DISCIPLINE),
                "a widget owes everything a DOM owner does, and more");
        for (JsModuleType t : List.of(StandardJsModuleType.CONSUMER, StandardJsModuleType.PRIMITIVE))
            assertTrue(!policy.rulesFor(t).rules().contains(WidgetDrawsOnlyComponentsRule.INSTANCE), t + " is not held to it");
    }
}
