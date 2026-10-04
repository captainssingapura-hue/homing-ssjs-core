package hue.captains.singapura.js.homing.core.util;

import hue.captains.singapura.js.homing.core.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleImportsWriterResolverTest {

    record Dep() implements EsModule<Dep> {
        static final Dep INSTANCE = new Dep();
        record Val() implements Exportable._Constant<Dep> {}
        @Override public ImportsFor<Dep> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<Dep> exports() { return new ExportsOf<>(INSTANCE, List.of(new Val())); }
    }

    private final ModuleNameResolver nameResolver = m -> new PartialModulePath("/mod?class=" + m.getClass().getCanonicalName());

    @Test
    void resolve_writesTheModulesOwnPath() {
        var resolver = new SimpleImportsWriterResolver(nameResolver);
        var writer = resolver.resolve(Dep.INSTANCE);

        var imports = new ModuleImports<>(List.of(new Dep.Val()), Dep.INSTANCE);
        String result = writer.writeImports(imports);

        assertEquals("import {Val} from \"/mod?class=" + Dep.class.getCanonicalName() + "\";", result);
    }
}
