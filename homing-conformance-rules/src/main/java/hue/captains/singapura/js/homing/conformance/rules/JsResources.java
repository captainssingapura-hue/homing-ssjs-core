package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/** Reads a module's class off where its JS is kept. */
public record JsResources() implements StatelessFunctionalObject {

    public static final JsResources INSTANCE = new JsResources();

    /**
     * Reverse {@code homing/js/a/b/C.js} → {@code a.b.C}; {@code null} if the
     * path is not a JS-module resource.
     */
    String fqcnOf(String resourcePath) {
        String p = resourcePath.replace('\\', '/');
        if (!p.startsWith(ModuleEnumerator.JS_ROOT) || !p.endsWith(ModuleEnumerator.JS_EXT)) return null;
        String mid = p.substring(ModuleEnumerator.JS_ROOT.length(), p.length() - ModuleEnumerator.JS_EXT.length());
        if (mid.isBlank()) return null;
        return mid.replace('/', '.');
    }
}
