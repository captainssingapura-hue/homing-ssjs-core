package hue.captains.singapura.js.homing.libs;

import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.ExternalModule;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.PartialModulePath;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Mermaid's address is the resolver's to give: the proxy's default until a deployment says otherwise. */
class MermaidProxyModuleTest {

    private static final String MIRROR = "https://mirror.internal/mermaid.esm.min.mjs";

    private static String served(ModuleNameResolver names) {
        return String.join("\n", MermaidProxyModule.INSTANCE.selfContent(names));
    }

    @Test
    void importsTheDefaultWhenTheResolverSaysNothing() {
        ModuleNameResolver plain = module -> new PartialModulePath("/module");
        assertTrue(served(plain).contains("import mermaidLib from \"" + MermaidProxyModule.DEFAULT_URL + "\";"));
    }

    @Test
    void importsWhereTheResolverPointsIt() {
        var mirrored = new ModuleNameResolver() {
            @Override public PartialModulePath resolve(EsModule<?> module) { return new PartialModulePath("/module"); }
            @Override public String libraryUrl(Class<? extends ExternalModule<?>> proxy, String defaultUrl) { return MIRROR; }
        };
        assertTrue(served(mirrored).contains("import mermaidLib from \"" + MIRROR + "\";"));
    }
}
