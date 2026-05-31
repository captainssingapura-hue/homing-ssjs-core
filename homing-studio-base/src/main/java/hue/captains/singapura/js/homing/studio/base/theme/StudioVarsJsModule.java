package hue.captains.singapura.js.homing.studio.base.theme;

import hue.captains.singapura.js.homing.core.CssVar;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * Typed-color bridge: emits a JS module exporting every {@link StudioVars}
 * {@link CssVar} as a JS string constant. Hand-written JS modules import
 * the constant instead of writing the raw {@code "var(--color-XXX)"}
 * literal — typos become missing-import errors at module-load time
 * rather than silent fallthrough to a fallback color at paint time.
 *
 * <p>Single source of truth: {@link StudioVars}. The {@code selfContent}
 * body is generated reflectively from the public static {@link CssVar}
 * fields, so adding a token to {@link StudioVars} only requires adding
 * a matching inner record below — no body edit. The
 * {@code ExportableCoverageTest} below confirms every declared field
 * has a corresponding export record (drift detector).</p>
 *
 * <p>Why an export record per token, not one dynamic list? The framework's
 * import surface is record-typed:
 * {@code new StudioVarsJsModule.COLOR_SURFACE()} ↔
 * {@code import { COLOR_SURFACE } from "…"} round-trips at compile time.
 * A token misspelled in an importer never compiles.</p>
 *
 * @since RFC 0027 follow-up — typed CSS vars for hand-written JS
 */
public record StudioVarsJsModule()
        implements DomModule<StudioVarsJsModule>, SelfContent {

    public static final StudioVarsJsModule INSTANCE = new StudioVarsJsModule();

    // ── Surface ──────────────────────────────────────────────────────
    public record COLOR_SURFACE()          implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_SURFACE_RAISED()   implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_SURFACE_RECESSED() implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_SURFACE_INVERTED() implements Exportable._Constant<StudioVarsJsModule> {}

    // ── Text ─────────────────────────────────────────────────────────
    public record COLOR_TEXT_PRIMARY()             implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_TEXT_MUTED()               implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_TEXT_ON_INVERTED()         implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_TEXT_ON_INVERTED_MUTED()   implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_TEXT_LINK()                implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_TEXT_LINK_HOVER()          implements Exportable._Constant<StudioVarsJsModule> {}

    // ── Border ───────────────────────────────────────────────────────
    public record COLOR_BORDER()          implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_BORDER_EMPHASIS() implements Exportable._Constant<StudioVarsJsModule> {}

    // ── Accent ───────────────────────────────────────────────────────
    public record COLOR_ACCENT()          implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_ACCENT_EMPHASIS() implements Exportable._Constant<StudioVarsJsModule> {}
    public record COLOR_ACCENT_ON()       implements Exportable._Constant<StudioVarsJsModule> {}

    // ── Spacing ──────────────────────────────────────────────────────
    public record SPACE_1() implements Exportable._Constant<StudioVarsJsModule> {}
    public record SPACE_2() implements Exportable._Constant<StudioVarsJsModule> {}
    public record SPACE_3() implements Exportable._Constant<StudioVarsJsModule> {}
    public record SPACE_4() implements Exportable._Constant<StudioVarsJsModule> {}
    public record SPACE_5() implements Exportable._Constant<StudioVarsJsModule> {}
    public record SPACE_6() implements Exportable._Constant<StudioVarsJsModule> {}
    public record SPACE_7() implements Exportable._Constant<StudioVarsJsModule> {}
    public record SPACE_8() implements Exportable._Constant<StudioVarsJsModule> {}

    // ── Radius ───────────────────────────────────────────────────────
    public record RADIUS_SM() implements Exportable._Constant<StudioVarsJsModule> {}
    public record RADIUS_MD() implements Exportable._Constant<StudioVarsJsModule> {}
    public record RADIUS_LG() implements Exportable._Constant<StudioVarsJsModule> {}

    @Override
    public ImportsFor<StudioVarsJsModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<StudioVarsJsModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new COLOR_SURFACE(), new COLOR_SURFACE_RAISED(),
                new COLOR_SURFACE_RECESSED(), new COLOR_SURFACE_INVERTED(),
                new COLOR_TEXT_PRIMARY(), new COLOR_TEXT_MUTED(),
                new COLOR_TEXT_ON_INVERTED(), new COLOR_TEXT_ON_INVERTED_MUTED(),
                new COLOR_TEXT_LINK(), new COLOR_TEXT_LINK_HOVER(),
                new COLOR_BORDER(), new COLOR_BORDER_EMPHASIS(),
                new COLOR_ACCENT(), new COLOR_ACCENT_EMPHASIS(), new COLOR_ACCENT_ON(),
                new SPACE_1(), new SPACE_2(), new SPACE_3(), new SPACE_4(),
                new SPACE_5(), new SPACE_6(), new SPACE_7(), new SPACE_8(),
                new RADIUS_SM(), new RADIUS_MD(), new RADIUS_LG()
        ));
    }

    /**
     * Body generated reflectively from {@link StudioVars}'s public static
     * {@link CssVar} fields — so the only edit needed to add a new token is
     * the {@link StudioVars} field declaration + the matching inner record
     * above (drift detected by {@code StudioVarsJsModuleTest}).
     */
    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        List<String> lines = new ArrayList<>();
        lines.add("// Generated from StudioVars.java — do not hand-edit.");
        lines.add("// Each constant is `var(--token)` ready for use in");
        lines.add("// CSS-text contexts (style.cssText, setProperty values, etc.).");
        for (Field f : StudioVars.class.getDeclaredFields()) {
            int m = f.getModifiers();
            if (!Modifier.isPublic(m) || !Modifier.isStatic(m)) continue;
            if (!CssVar.class.equals(f.getType())) continue;
            try {
                CssVar v = (CssVar) f.get(null);
                lines.add("const " + f.getName() + " = '" + v.ref() + "';");
            } catch (IllegalAccessException ignored) {
                // unreachable for public static — skip if it ever happens
            }
        }
        return lines;
    }
}
