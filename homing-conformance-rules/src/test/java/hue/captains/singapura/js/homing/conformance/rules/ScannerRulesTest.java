package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.JsModuleType;

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
        return new ServedModule("demo.Widget", JsModuleType.CONSUMER, JsSource.of(lines));
    }

    @Test
    void noRawCssFiresOnlyWhenTheCssManagerIsInjected() {
        // No css import → not policed, even with a raw classList op.
        assertTrue(NoRawCssRule.INSTANCE.check(served(
                "el.classList.add('x');")).isEmpty());
        // css injected → the same raw op is a violation.
        assertFalse(NoRawCssRule.INSTANCE.check(served(
                "import { CssClassManagerInstance as css } from \"/m?class=CssClassManager\";",
                "el.classList.add('x');")).isEmpty());
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
