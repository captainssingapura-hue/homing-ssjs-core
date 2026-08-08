package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.EsModule;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * RFC 0044 — a build/dev-time authoring aid for the Crate rollout (NOT part of
 * conformance itself). Given any class in a Maven module, it prints paste-ready
 * {@code entries()} for that module's crate: the {@code import} lines and one
 * {@code CrateEntry.of(Xxx.INSTANCE)} per served module (falling back to
 * {@code new Xxx()} where a module has no {@code INSTANCE} field). The
 * OrphanCheck still guards completeness — this only removes the tedium of
 * hand-transcribing a large module's module list.
 *
 * <p>Run it from a throwaway test/main in the target module, e.g.
 * {@code System.out.println(CrateSeed.suggest(SomeClassInThatModule.class))}.</p>
 */
public final class CrateSeed {

    private CrateSeed() {}

    public static String suggest(Class<?> anchorInModule) {
        Set<String> fqcns = OrphanCheck.servedModuleClassesInModuleOf(anchorInModule);
        var imports = new TreeSet<String>();
        var lines = new ArrayList<String>();
        ClassLoader loader = anchorInModule.getClassLoader();

        for (String fqcn : fqcns) {
            String ref = referenceExpression(fqcn, loader); // e.g. "TreeRendererModule.INSTANCE" or "new Foo()"
            String simple = topLevelSimpleName(fqcn);
            imports.add("import " + topLevelName(fqcn) + ";");
            lines.add("                CrateEntry.of(" + ref + "),");
        }
        if (!lines.isEmpty()) {
            String last = lines.get(lines.size() - 1);
            lines.set(lines.size() - 1, last.substring(0, last.length() - 1)); // drop trailing comma
        }

        var sb = new StringBuilder();
        sb.append("// ---- imports ----\n");
        imports.forEach(i -> sb.append(i).append('\n'));
        sb.append("// ---- entries() body ----\n");
        sb.append("        return List.of(\n");
        lines.forEach(l -> sb.append(l).append('\n'));
        sb.append("        );\n");
        return sb.toString();
    }

    /** {@code Simple.INSTANCE} if a public static INSTANCE field of the module's type exists, else {@code new Simple()}. */
    private static String referenceExpression(String fqcn, ClassLoader loader) {
        String simple = topLevelSimpleName(fqcn);
        try {
            Class<?> c = Class.forName(fqcn, false, loader);
            for (Field f : c.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())
                        && Modifier.isPublic(f.getModifiers())
                        && EsModule.class.isAssignableFrom(f.getType())) {
                    return simple + "." + f.getName();
                }
            }
        } catch (Throwable ignored) {
            // fall through to constructor form
        }
        return "new " + simple + "()";
    }

    /** The top-level (outer) class name — nested modules are referenced via their outer's import. */
    private static String topLevelName(String fqcn) {
        int dollar = fqcn.indexOf('$');
        return dollar < 0 ? fqcn : fqcn.substring(0, dollar);
    }

    private static String topLevelSimpleName(String fqcn) {
        String top = topLevelName(fqcn);
        int dot = top.lastIndexOf('.');
        return dot < 0 ? top : top.substring(dot + 1);
    }
}
