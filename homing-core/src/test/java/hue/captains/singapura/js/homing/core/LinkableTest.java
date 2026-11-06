package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LinkableTest {

    // ---- defaultSimpleName ------------------------------------------------

    @Test
    @DisplayName("simple class name → kebab-case")
    void simpleSplit() {
        assertEquals("pitch-deck",       SimpleNames.INSTANCE.of(SampleClasses.PitchDeck.class));
        assertEquals("wonderland-demo",  SimpleNames.INSTANCE.of(SampleClasses.WonderlandDemo.class));
        assertEquals("doc-browser",      SimpleNames.INSTANCE.of(SampleClasses.DocBrowser.class));
        assertEquals("rfc0001-step",     SimpleNames.INSTANCE.of(SampleClasses.Rfc0001Step.class));
    }

    @Test
    @DisplayName("single-letter and short class names")
    void shortNames() {
        assertEquals("a", SimpleNames.INSTANCE.of(SampleClasses.A.class));
        assertEquals("ab", SimpleNames.INSTANCE.of(SampleClasses.Ab.class));
    }

    @Test
    @DisplayName("acronyms followed by a normal word")
    void acronymBoundary() {
        assertEquals("http-handler",   SimpleNames.INSTANCE.of(SampleClasses.HTTPHandler.class));
        assertEquals("xml-parser",     SimpleNames.INSTANCE.of(SampleClasses.XMLParser.class));
        assertEquals("sql-query",      SimpleNames.INSTANCE.of(SampleClasses.SQLQuery.class));
    }

    @Test
    @DisplayName("trailing acronym is one segment")
    void trailingAcronym() {
        assertEquals("user-id",  SimpleNames.INSTANCE.of(SampleClasses.UserID.class));
        assertEquals("dns-ttl",  SimpleNames.INSTANCE.of(SampleClasses.DnsTTL.class));
    }

    @Test
    @DisplayName("all-uppercase class name remains a single segment")
    void allUpper() {
        assertEquals("api",  SimpleNames.INSTANCE.of(SampleClasses.API.class));
        assertEquals("rfc",  SimpleNames.INSTANCE.of(SampleClasses.RFC.class));
    }

    @Test
    @DisplayName("digits are treated as continuation of the current word")
    void digits() {
        assertEquals("step01",         SimpleNames.INSTANCE.of(SampleClasses.Step01.class));
        assertEquals("api2",           SimpleNames.INSTANCE.of(SampleClasses.API2.class));
        assertEquals("v2-handler",     SimpleNames.INSTANCE.of(SampleClasses.V2Handler.class));
    }

    // ---- AppModule simpleName / paramsType defaults -----------------------

    @Test
    @DisplayName("AppModule.simpleName() defaults via Linkable.defaultSimpleName")
    void appModuleDefaultSimpleName() {
        assertEquals("sample-app", new SampleClasses.SampleApp().simpleName());
    }

    @Test
    @DisplayName("AppModule.paramsType() defaults to AppModule._None.class")
    void appModuleDefaultParamsType() {
        assertEquals(AppModule._None.class, new SampleClasses.SampleApp().paramsType());
    }

    @Test
    @DisplayName("AppModule simpleName override wins")
    void appModuleSimpleNameOverride() {
        assertEquals("custom", new SampleClasses.RenamedApp().simpleName());
    }

    // ---- AppLink basic shape ----------------------------------------------

    @Test
    @DisplayName("AppLink<L> may be implemented by an inner record on a Linkable")
    void appLinkImplementable() {
        AppLink<SampleClasses.SampleApp> link = new SampleClasses.SampleApp.link();
        assertNotNull(link);
        assertTrue(link instanceof Exportable);
    }

    // ---- shared sample classes for the tests above ------------------------

    private static final class SampleClasses {
        static class PitchDeck {}
        static class WonderlandDemo {}
        static class DocBrowser {}
        static class Rfc0001Step {}
        static class A {}
        static class Ab {}
        static class HTTPHandler {}
        static class XMLParser {}
        static class SQLQuery {}
        static class UserID {}
        static class DnsTTL {}
        static class API {}
        static class RFC {}
        static class Step01 {}
        static class API2 {}
        static class V2Handler {}

        public record SampleApp() implements AppModule<AppModule._None, SampleApp> {
            public record link() implements AppLink<SampleApp> {}
            @Override public String title() { return "Sample"; }
            @Override public ImportsFor<SampleApp> imports() { return new ImportsFor<>(); }
            @Override public ExportsOf<SampleApp> exports() { return new ExportsOf<>(this, java.util.List.of()); }
        }

        public record RenamedApp() implements AppModule<AppModule._None, RenamedApp> {
            @Override public String simpleName() { return "custom"; }
            @Override public String title() { return "Renamed"; }
            @Override public ImportsFor<RenamedApp> imports() { return new ImportsFor<>(); }
            @Override public ExportsOf<RenamedApp> exports() { return new ExportsOf<>(this, java.util.List.of()); }
        }
    }
}
