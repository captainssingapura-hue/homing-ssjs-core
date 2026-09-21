package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Keys come through the party: a module with key(ev) and the convention is
 * clean; a key listener in any form is a finding, worded by where it sits —
 * the document or the capture phase is the steward's job, an element's own
 * is a key(ev) to write; a listener in a comment is nothing; the steward is
 * exempt by name.
 */
class KeysThroughThePartyRuleTest {

    private static ServedModule mod(String... lines) {
        return new ServedModule("demo.M", StandardJsModuleType.CONSUMER, JsSource.of(lines));
    }

    private static List<String> kinds(List<Finding> findings) {
        return findings.stream().map(f -> f.message().startsWith("captures") ? "captures" : "listens").toList();
    }

    @Test
    void aMemberWithKeyAndTheConventionIsClean() {
        assertEquals(List.of(), KeysThroughThePartyRule.INSTANCE.check(mod(
                "class Slider {",
                "    constructor(branch, p) { this._kbId = p.keyboard.join(branch.name, { keyDown: function (ev) { return self.key(ev); } }); Keys.claimOn(this.root, p.keyboard, this._kbId); }",
                "    key(ev) { if (ev.key === \"ArrowUp\") { this._set(this._value + 1); return true; } return false; }",
                "}",
                "root.addEventListener(\"pointerdown\", function () {});   // the hand is not the keys",
                "export {Slider};")));
    }

    @Test
    void anElementsOwnListenerIsAFinding_toWriteAsKeyEv() {
        var findings = KeysThroughThePartyRule.INSTANCE.check(mod(
                "knob.addEventListener(\"keydown\", function (ev) { });",
                "list.addEventListener('keyup', onUp);",
                "el.onkeydown = function (e) {};",
                "export {X};"));
        assertEquals(List.of("listens", "listens", "listens"), kinds(findings));
        assertEquals(List.of(0, 1, 2), findings.stream().map(Finding::line).toList());
        assertTrue(findings.stream().allMatch(f -> f.rule().value().equals("keys-through-the-party")));
        assertTrue(findings.get(0).message().endsWith("knob.addEventListener(\"keydown\", function (ev) { });"), "the offending line in the message");
    }

    @Test
    void aDocumentOrCapturePhaseListenerIsAFinding_theStewardsJob() {
        var findings = KeysThroughThePartyRule.INSTANCE.check(mod(
                "document.addEventListener(\"keydown\", this._keys, true);",
                "document[f](\"keydown\", this._onKey, true);",
                "window.addEventListener(\"keyup\", up);",
                "frame.addEventListener(\"keydown\", fn, true);",
                "document.removeEventListener(\"keydown\", this._keys, true);   // the pair of the first: not a second finding",
                "export {X};"));
        assertEquals(List.of("captures", "captures", "captures", "captures"), kinds(findings));
    }

    @Test
    void aListenerInACommentIsNothing_andAnEqualityIsNotAnAssignment() {
        assertEquals(List.of(), KeysThroughThePartyRule.INSTANCE.check(mod(
                "// the keys used to be el.addEventListener(\"keydown\", …); they come through the party now",
                "/* document.addEventListener(\"keyup\", up, true) */",
                "if (el.onkeydown == null) {}",
                "export {X};")));
    }

    @Test
    void theStewardIsExemptByName() {
        var steward = new ServedModule("hue.captains.singapura.js.homing.component.keyboard.KeyboardStewardModule", StandardJsModuleType.PRIMITIVE, JsSource.of(
                "document[f](\"keydown\", this._onDown, true);",
                "document[f](\"keyup\", this._onUp, true);",
                "export {KeyboardSteward};"));
        assertEquals(List.of(), KeysThroughThePartyRule.INSTANCE.check(steward));
    }

    @Test
    void itIsInTheDomOwnerDiscipline() {
        assertTrue(DefaultJsRulePolicy.DOM_OWNER_DISCIPLINE.contains(KeysThroughThePartyRule.INSTANCE));
        assertTrue(DefaultJsRulePolicy.INSTANCE.rulesFor(StandardJsModuleType.PRIMITIVE).rules().contains(KeysThroughThePartyRule.INSTANCE));
        assertTrue(!DefaultJsRulePolicy.INSTANCE.rulesFor(StandardJsModuleType.SECRETARY).rules().contains(KeysThroughThePartyRule.INSTANCE), "a secretary touches no DOM at all: the headless rule covers it");
    }
}
