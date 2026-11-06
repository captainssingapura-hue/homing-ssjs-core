package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ExportWriterTest {

    record TestModule() implements EsModule<TestModule> {
        static final TestModule INSTANCE = new TestModule();
        record Foo() implements Exportable._Constant<TestModule> {}
        record Bar() implements Exportable._Class<TestModule> {}

        @Override public ImportsFor<TestModule> imports() { return new ImportsFor<>(); }
        @Override public ExportsOf<TestModule> exports() {
            return new ExportsOf<>(INSTANCE, List.of(new Foo(), new Bar()));
        }
    }

    private final ExportWriter writer = ExportWriter.INSTANCE;

    @Test
    void writeExports_emptyList() {
        var exports = new ExportsOf<>(TestModule.INSTANCE, List.of());
        var result = writer.writeExports(exports);
        assertTrue(result.isEmpty());
    }

    @Test
    void writeExports_singleExport() {
        var exports = new ExportsOf<>(TestModule.INSTANCE, List.of(new TestModule.Foo()));
        var result = writer.writeExports(exports);

        assertEquals(1, result.size());
        assertEquals("export {Foo};", result.getFirst());
    }

    @Test
    void writeExports_multipleExports() {
        var exports = new ExportsOf<>(TestModule.INSTANCE, List.of(new TestModule.Foo(), new TestModule.Bar()));
        var result = writer.writeExports(exports);

        assertEquals(1, result.size());
        assertEquals("export {Foo, Bar};", result.getFirst());
    }
}
