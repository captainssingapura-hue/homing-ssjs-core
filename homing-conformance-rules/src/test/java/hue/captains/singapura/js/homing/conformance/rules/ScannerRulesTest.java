package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.StandardJsModuleType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Phase 7 proof — the four scanners ported from the standalone conformance
 * tests ({@link NoRawCssRule}, {@link NoRawHrefRule}, {@link
 * NoManagerRedeclarationRule}, {@link ViewDoctrineRule}), each exercised on a
 * synthesized served text.
 */
class ScannerRulesTest {

    private static ServedModule served(String... lines) {
        return new ServedModule("demo.Widget", StandardJsModuleType.CONSUMER, JsSource.of(lines));
    }

    @Test
    void noRawCssIsUngated() {
        // No css import → still policed: the module with no typed class to use
        // is exactly the one being told to adopt one.
        assertFalse(NoRawCssRule.INSTANCE.check(served(
                "el.classList.add('x');")).isEmpty());
        assertFalse(NoRawCssRule.INSTANCE.check(served(
                "el.className = 'hsp-root';")).isEmpty());
        // With the manager injected, the same raw op is a violation.
        assertFalse(NoRawCssRule.INSTANCE.check(served(
                "import { CssClassManagerInstance as css } from \"/m?class=CssClassManager\";",
                "el.classList.add('x');")).isEmpty());
        // The typed path is clean.
        assertTrue(NoRawCssRule.INSTANCE.check(served(
                "import { CssClassManagerInstance as css } from \"/m?class=CssClassManager\";",
                "css.addClass(el, Styles.active());")).isEmpty());
    }

    @Test
    void noRawCssFlagsAStylesheetMintedFromJs() {
        // Raw and branch-minted <style> elements alike: an untyped sheet.
        assertFalse(NoRawCssRule.INSTANCE.check(served(
                "var s = document.createElement(\"style\");")).isEmpty());
        assertFalse(NoRawCssRule.INSTANCE.check(served(
                "var s = branch.createElement('sheet', 'style');")).isEmpty());
        // Any other tag is not this rule's concern.
        assertTrue(NoRawCssRule.INSTANCE.check(served(
                "var d = branch.createElement('host', 'div');")).isEmpty());
        // A comment mentioning it does not trip the rule.
        assertTrue(NoRawCssRule.INSTANCE.check(served(
                "// never document.createElement('style') here")).isEmpty());
    }

    @Test
    void noRawHrefFlagsPropertyLocationAndAttribute() {
        assertFalse(NoRawHrefRule.INSTANCE.check(served("a.href = url;")).isEmpty());
        assertFalse(NoRawHrefRule.INSTANCE.check(served("window.location.assign(url);")).isEmpty());
        assertFalse(NoRawHrefRule.INSTANCE.check(served("el.setAttribute('href', u);")).isEmpty());
        // The injected manager prologue must not self-trip, and href.* usage is fine.
        assertTrue(NoRawHrefRule.INSTANCE.check(served(
                "import { HrefManagerInstance as href } from \"/m?class=HrefManager\";",
                "href.set(a, link);")).isEmpty());
    }

    @Test
    void noManagerRedeclarationCatchesRedeclarationOfAnAliasedImport() {
        assertFalse(NoManagerRedeclarationRule.INSTANCE.check(served(
                "import { CssClassManagerInstance as css } from \"/m?class=CssClassManager\";",
                "const css = makeShim();")).isEmpty());
        // Using the binding (not redeclaring it) is fine.
        assertTrue(NoManagerRedeclarationRule.INSTANCE.check(served(
                "import { CssClassManagerInstance as css } from \"/m?class=CssClassManager\";",
                "css.addClass(el, Styles.active());")).isEmpty());
    }

    @Test
    void viewDoctrineFlagsHtmlAndLookupsButAllowsClearingToEmpty() {
        assertFalse(ViewDoctrineRule.INSTANCE.check(served("var s = '<div>' + x;")).isEmpty());
        assertFalse(ViewDoctrineRule.INSTANCE.check(served("host.innerHTML = render();")).isEmpty());
        assertFalse(ViewDoctrineRule.INSTANCE.check(served("var el = document.getElementById('x');")).isEmpty());
        // Clearing to "" is governed by the no-destruction rule, not this one.
        assertTrue(ViewDoctrineRule.INSTANCE.check(served("host.innerHTML = \"\";")).isEmpty());
    }
}
