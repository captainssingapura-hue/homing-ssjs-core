package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CssGroupDefaultsTest {

    record TestCss() implements CssGroup<TestCss> {
        static final TestCss INSTANCE = new TestCss();
        record active() implements CssClass<TestCss> {}
        record disabled() implements CssClass<TestCss> {}

        @Override
        public List<CssClass<TestCss>> cssClasses() {
            return List.of(new active(), new disabled());
        }
    }

    @Test
    void imports_defaultsToEmpty() {
        var imports = TestCss.INSTANCE.imports();
        assertTrue(imports.getAllImports().isEmpty());
    }

    @Test
    void exports_containsAllCssClasses() {
        var exports = TestCss.INSTANCE.exports();

        assertSame(TestCss.INSTANCE, exports.module());
        assertEquals(2, exports.exports().size());
        assertInstanceOf(TestCss.active.class, exports.exports().get(0));
        assertInstanceOf(TestCss.disabled.class, exports.exports().get(1));
    }

    @Test
    void exports_areExportableConstants() {
        var exports = TestCss.INSTANCE.exports();
        for (var export : exports.exports()) {
            assertInstanceOf(Exportable._Constant.class, export);
        }
    }

    @Test
    void aGroupWhoseClassesDependOnNothing_hasNoImports() {
        var cssImports = CssImportsFor.of(TestCss.INSTANCE);
        assertSame(TestCss.INSTANCE, cssImports.cssGroup());
        assertTrue(cssImports.imports().isEmpty());
    }
}
