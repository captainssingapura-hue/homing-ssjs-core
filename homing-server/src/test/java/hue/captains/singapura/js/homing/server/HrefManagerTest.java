package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HrefManagerTest {

    // ---- Sample apps -----------------------------------------------------

    record Target() implements AppModule<AppModule._None, Target> {
        public record link() implements AppLink<Target> {}
        static final Target INSTANCE = new Target();
        @Override public String title() { return "T"; }
        @Override public ImportsFor<Target> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<Target> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record AppWithLink() implements AppModule<AppModule._None, AppWithLink> {
        public record link() implements AppLink<AppWithLink> {}
        static final AppWithLink INSTANCE = new AppWithLink();
        @Override public String title() { return "Has Link"; }
        @Override public ImportsFor<AppWithLink> imports() {
            return ImportsFor.<AppWithLink>builder()
                    .add(new ModuleImports<>(List.of(new Target.link()), Target.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<AppWithLink> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record AppNoLink() implements AppModule<AppModule._None, AppNoLink> {
        static final AppNoLink INSTANCE = new AppNoLink();
        @Override public String title() { return "No Link"; }
        @Override public ImportsFor<AppNoLink> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<AppNoLink> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record SomeStyles() implements CssGroup<SomeStyles> {
        public record btn() implements CssClass<SomeStyles> {}
        static final SomeStyles INSTANCE = new SomeStyles();
        @Override public List<CssClass<SomeStyles>> cssClasses() { return List.of(new btn()); }
    }

    record AppOnlyCss() implements AppModule<AppModule._None, AppOnlyCss> {
        static final AppOnlyCss INSTANCE = new AppOnlyCss();
        @Override public String title() { return "Only CSS"; }
        @Override public ImportsFor<AppOnlyCss> imports() {
            return ImportsFor.<AppOnlyCss>builder()
                    .add(new ModuleImports<>(List.of(new SomeStyles.btn()), SomeStyles.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<AppOnlyCss> exports() { return new ExportsOf<>(this, List.of()); }
    }

    // ---- HrefManager EsModule shape --------------------------------------

    @Test
    @DisplayName("HrefManager exports HrefManagerInstance as a constant")
    void manager_exports() {
        var exports = HrefManager.INSTANCE.exports();
        assertEquals(1, exports.exports().size());
        assertInstanceOf(HrefManager.HrefManagerInstance.class, exports.exports().get(0));
    }

    @Test
    @DisplayName("HrefManager has no imports")
    void manager_noImports() {
        assertTrue(HrefManager.INSTANCE.imports().getAllImports().isEmpty());
    }

    @Test
    @DisplayName("HrefManager.js resource is present and exposes the six API methods")
    void manager_jsResourcePresent() {
        var lines = ResourceReader.INSTANCE.getStringsFromResource(
                "homing/js/hue/captains/singapura/js/homing/server/HrefManager.js");
        var joined = String.join("\n", lines);

        assertTrue(joined.contains("HrefManagerInstance"), "exports name");
        assertTrue(joined.contains("function toAttr"), "toAttr API");
        assertTrue(joined.contains("function set"), "set API");
        assertTrue(joined.contains("function create"), "create API");
        assertTrue(joined.contains("function openNew"), "openNew API");
        assertTrue(joined.contains("function navigate"), "navigate API");
        assertTrue(joined.contains("function fragment"), "fragment API");
        assertTrue(joined.contains("Object.freeze"), "frozen API");
    }

    @Test
    @DisplayName("HrefManager.js fragment() emits href=\"#slug\" form")
    void manager_fragmentShape() {
        var joined = String.join("\n", ResourceReader.INSTANCE.getStringsFromResource(
                "homing/js/hue/captains/singapura/js/homing/server/HrefManager.js"));
        assertTrue(joined.contains("'href=\"#'"), joined);
    }

    // ---- Injection-detection helper --------------------------------------

    @Test
    @DisplayName("importsAnyAppLink → true when module imports an AppLink")
    void detection_trueForAppLinkImporter() {
        assertTrue(EsModuleGetAction.importsAnyAppLink(AppWithLink.INSTANCE));
    }

    @Test
    @DisplayName("importsAnyAppLink → false when module has no imports")
    void detection_falseWhenNoImports() {
        assertFalse(EsModuleGetAction.importsAnyAppLink(AppNoLink.INSTANCE));
    }

    @Test
    @DisplayName("importsAnyAppLink → false when only CSS imports (no AppLink)")
    void detection_falseForOnlyCss() {
        assertFalse(EsModuleGetAction.importsAnyAppLink(AppOnlyCss.INSTANCE));
    }

    @Test
    @DisplayName("HrefManager itself does not need href injection (no AppLink imports)")
    void detection_falseForHrefManagerItself() {
        assertFalse(EsModuleGetAction.importsAnyAppLink(HrefManager.INSTANCE));
    }
}
