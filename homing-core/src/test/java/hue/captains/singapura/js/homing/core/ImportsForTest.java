package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ImportsForTest {

    record ModA() implements EsModule<ModA> {
        static final ModA INSTANCE = new ModA();
        record X() implements Exportable._Constant<ModA> {}
        @Override public ImportsFor<ModA> imports() { return new ImportsFor<>(); }
        @Override public ExportsOf<ModA> exports() { return new ExportsOf<>(INSTANCE, List.of(new X())); }
    }

    record ModB() implements EsModule<ModB> {
        static final ModB INSTANCE = new ModB();
        record Y() implements Exportable._Class<ModB> {}
        @Override public ImportsFor<ModB> imports() { return new ImportsFor<>(); }
        @Override public ExportsOf<ModB> exports() { return new ExportsOf<>(INSTANCE, List.of(new Y())); }
    }

    @Test
    void noImports_isEmpty() {
        var imports = new ImportsFor<>();
        assertTrue(imports.getAllImports().isEmpty());
    }

    @Test
    void builder_singleModule() {
        var moduleImports = new ModuleImports<>(List.of(new ModA.X()), ModA.INSTANCE);
        var imports = new ImportsFor._Builder<>()
                .add(moduleImports)
                .build();

        assertEquals(1, imports.getAllImports().size());
        assertSame(moduleImports, imports.getAllImports().get(ModA.INSTANCE));
    }

    @Test
    void builder_multipleModules() {
        var importsA = new ModuleImports<>(List.of(new ModA.X()), ModA.INSTANCE);
        var importsB = new ModuleImports<>(List.of(new ModB.Y()), ModB.INSTANCE);

        var imports = new ImportsFor._Builder<>()
                .add(importsA)
                .add(importsB)
                .build();

        assertEquals(2, imports.getAllImports().size());
        assertSame(importsA, imports.getAllImports().get(ModA.INSTANCE));
        assertSame(importsB, imports.getAllImports().get(ModB.INSTANCE));
    }

    @Test
    void builder_duplicateModuleOverwrites() {
        var first = new ModuleImports<>(List.of(new ModA.X()), ModA.INSTANCE);
        var second = new ModuleImports<>(List.of(new ModA.X()), ModA.INSTANCE);

        var imports = new ImportsFor._Builder<>()
                .add(first)
                .add(second)
                .build();

        assertEquals(1, imports.getAllImports().size());
        assertSame(second, imports.getAllImports().get(ModA.INSTANCE));
    }

    @Test
    void theOrder_isByTheSourcesClassName() {
        var imports = new ImportsFor._Builder<>()
                .add(new ModuleImports<>(List.of(new ModB.Y()), ModB.INSTANCE))
                .add(new ModuleImports<>(List.of(new ModA.X()), ModA.INSTANCE))
                .build();

        assertEquals(List.of(ModA.INSTANCE, ModB.INSTANCE), List.copyOf(imports.getAllImports().keySet()));
    }

    @Test
    void theOrder_followsFromWhatIsImported_notHowTheListIsWritten() {
        var one = new ImportsFor._Builder<>()
                .add(new ModuleImports<>(List.of(new ModA.X()), ModA.INSTANCE))
                .add(new ModuleImports<>(List.of(new ModB.Y()), ModB.INSTANCE))
                .build();
        var other = new ImportsFor._Builder<>()
                .add(new ModuleImports<>(List.of(new ModB.Y()), ModB.INSTANCE))
                .add(new ModuleImports<>(List.of(new ModA.X()), ModA.INSTANCE))
                .build();

        assertEquals(List.copyOf(one.getAllImports().keySet()), List.copyOf(other.getAllImports().keySet()));
    }

    @Test
    void aSourceIsNeeded() {
        var builder = new ImportsFor._Builder<>();
        assertThrows(NullPointerException.class, () -> builder.add(new ModuleImports<>(List.of(), (ModA) null)));
    }

    @Test
    void builder_resultIsImmutable() {
        var imports = new ImportsFor._Builder<>()
                .add(new ModuleImports<>(List.of(new ModA.X()), ModA.INSTANCE))
                .build();

        assertThrows(UnsupportedOperationException.class, () ->
                imports.getAllImports().put(ModB.INSTANCE, new ModuleImports<>(List.of(new ModB.Y()), ModB.INSTANCE))
        );
    }
}
