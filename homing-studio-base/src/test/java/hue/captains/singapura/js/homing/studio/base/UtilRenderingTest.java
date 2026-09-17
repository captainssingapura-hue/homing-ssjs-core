package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.server.CssContentGetAction;
import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ModuleQuery;
import hue.captains.singapura.js.homing.studio.base.css.Util;
import hue.captains.singapura.js.homing.core.Theme;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0002-ext1 Phase 06 — smoke tests for the {@link Util} CssGroup. Verifies
 * that inline-bodied utilities render correctly, that auto-synthesized variants
 * (Phase 01) reuse the inline body, and that with no theme override for any
 * utility (RFC 0066) every class renders from its own body under any theme.
 */
class UtilRenderingTest {

    /** studio-base ships no theme (RFC 0066); a utility renders from its own
     *  body under any theme, so a nameless one is enough to ask under. */
    record Bare() implements Theme {
        static final Bare INSTANCE = new Bare();
        @Override public String slug() { return "bare"; }
    }

    private final CssContentGetAction action = new CssContentGetAction(List.of(), Bare.INSTANCE);

    private String renderUtil() throws Exception {
        var query = new ModuleQuery(Util.class.getCanonicalName(), null, null);
        return action.execute(query, new EmptyParam.NoHeaders()).get().body();
    }

    @Test
    void renders_baseUtility_withInlineBody() throws Exception {
        var css = renderUtil();
        assertTrue(css.contains(".bg-accent {"),
                "bg-accent class rule emitted. css:\n" + css);
        assertTrue(css.contains("background: var(--color-accent);"),
                "inline body emitted via cls.body(). css:\n" + css);
    }

    @Test
    void renders_utilityVariants_autoSynthesized() throws Exception {
        var css = renderUtil();
        // bg_accent is UtilityCssClass — gets all three default states.
        assertTrue(css.contains(".hover-bg-accent:hover {"),  "hover variant. css:\n" + css);
        assertTrue(css.contains(".focus-bg-accent:focus {"),  "focus variant. css:\n" + css);
        assertTrue(css.contains(".active-bg-accent:active {"), "active variant. css:\n" + css);

        // Each variant rule reuses the same body.
        int hoverStart = css.indexOf(".hover-bg-accent:hover {");
        int hoverEnd   = css.indexOf("}", hoverStart);
        assertTrue(css.substring(hoverStart, hoverEnd).contains("background: var(--color-accent);"),
                "hover variant must reuse the base body");
    }

    @Test
    void renders_layoutUtilities_noVariants() throws Exception {
        var css = renderUtil();
        // Layout utilities are plain CssClass — no variants emitted.
        assertTrue(css.contains(".flex {"),  "flex utility emitted. css:\n" + css);
        assertTrue(css.contains("display: flex;"),
                "flex inline body emitted. css:\n" + css);
        assertFalse(css.contains(".hover-flex:hover {"),
                "layout utilities must NOT have hover variants. css:\n" + css);
    }

    @Test
    void renders_spacingScale_referencesSpaceTokens() throws Exception {
        var css = renderUtil();
        assertTrue(css.contains(".p-4 {"),                 "p-4 emitted. css:\n" + css);
        assertTrue(css.contains("padding: var(--space-4);"),
                "p-4 references spacing token. css:\n" + css);
        assertTrue(css.contains(".gap-2 {"),               "gap-2 emitted. css:\n" + css);
        assertTrue(css.contains("gap: var(--space-2);"),
                "gap-2 references spacing token. css:\n" + css);
    }

    @Test
    void renderError_isNotEmittedForInlineBodied() throws Exception {
        var css = renderUtil();
        // No theme overrides a utility. Every class must resolve via cls.body().
        assertFalse(css.contains("/* render error"),
                "inline-bodied utilities must not fall through to impl-method dispatch. css:\n" + css);
    }
}
