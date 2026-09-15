package hue.captains.singapura.js.homing.core.util;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CssClassNameTest {

    record TestCss() implements CssGroup<TestCss> {
        static final TestCss INSTANCE = new TestCss();
        record pg_theme_btn() implements CssClass<TestCss> {}
        record subway_cell() implements CssClass<TestCss> {}
        record active() implements CssClass<TestCss> {}
        record very_long_class_name() implements CssClass<TestCss> {}

        @Override public List<CssClass<TestCss>> cssClasses() { return List.of(); }
    }

    @Test
    void toCssName_multipleUnderscores() {
        assertEquals("pg-theme-btn", CssClassName.toCssName(TestCss.pg_theme_btn.class));
    }

    @Test
    void toCssName_singleUnderscore() {
        assertEquals("subway-cell", CssClassName.toCssName(TestCss.subway_cell.class));
    }

    @Test
    void toCssName_noUnderscore() {
        assertEquals("active", CssClassName.toCssName(TestCss.active.class));
    }

    @Test
    void toCssName_manyUnderscores() {
        assertEquals("very-long-class-name", CssClassName.toCssName(TestCss.very_long_class_name.class));
    }
}
