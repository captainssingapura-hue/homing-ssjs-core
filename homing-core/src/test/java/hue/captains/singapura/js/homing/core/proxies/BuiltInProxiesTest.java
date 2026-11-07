package hue.captains.singapura.js.homing.core.proxies;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.NavWriter;
import hue.captains.singapura.js.homing.core.SimpleAppResolver;
import hue.captains.singapura.js.homing.core.UrlTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class BuiltInProxiesTest {

    // ====================================================================
    // Mailto
    // ====================================================================

    @Test
    @DisplayName("Mailto template compiles cleanly")
    void mailtoCompiles() {
        var t = new UrlTemplate(Mailto.INSTANCE.urlTemplate(), Mailto.INSTANCE.paramsType());
        assertNotNull(t);
        assertEquals(List.of("to", "subject", "body", "cc", "bcc"), t.paramNames());
    }

    @Test
    @DisplayName("Mailto with just `to` renders cleanly")
    void mailtoBasic() {
        var t = new UrlTemplate(Mailto.INSTANCE.urlTemplate(), Mailto.INSTANCE.paramsType());
        var url = t.render(new Mailto.Params("user@example.com",
                Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
        // Optional fields render as empty: subject=&body=&cc=&bcc= (documented limitation).
        assertEquals("mailto:user%40example.com?subject=&body=&cc=&bcc=", url);
    }

    @Test
    @DisplayName("Mailto with subject and body renders both")
    void mailtoSubjectAndBody() {
        var t = new UrlTemplate(Mailto.INSTANCE.urlTemplate(), Mailto.INSTANCE.paramsType());
        var url = t.render(new Mailto.Params("u@e.com",
                Optional.of("Hi there"), Optional.of("How are you?"), Optional.empty(), Optional.empty()));
        assertTrue(url.startsWith("mailto:u%40e.com?subject="), url);
        assertTrue(url.contains("subject=Hi+there"), url);
        assertTrue(url.contains("body=How+are+you%3F"), url);
    }

    @Test
    @DisplayName("Mailto simpleName is `mailto`")
    void mailtoSimpleName() {
        assertEquals("mailto", Mailto.INSTANCE.simpleName());
    }

    @Test
    @DisplayName("Mailto.link is an AppLink<Mailto>")
    void mailtoLinkType() {
        AppLink<Mailto> link = new Mailto.link();
        assertNotNull(link);
    }

    // ====================================================================
    // Tel
    // ====================================================================

    @Test
    @DisplayName("Tel template compiles cleanly")
    void telCompiles() {
        var t = new UrlTemplate(Tel.INSTANCE.urlTemplate(), Tel.INSTANCE.paramsType());
        assertEquals(List.of("number"), t.paramNames());
    }

    @Test
    @DisplayName("Tel renders international format with + encoded as %2B")
    void telInternationalFormat() {
        var t = new UrlTemplate(Tel.INSTANCE.urlTemplate(), Tel.INSTANCE.paramsType());
        var url = t.render(new Tel.Params("+15555551234"));
        assertEquals("tel:%2B15555551234", url);
    }

    @Test
    @DisplayName("Tel simpleName is `tel`")
    void telSimpleName() {
        assertEquals("tel", Tel.INSTANCE.simpleName());
    }

    // ====================================================================
    // Sms
    // ====================================================================

    @Test
    @DisplayName("Sms template compiles cleanly")
    void smsCompiles() {
        var t = new UrlTemplate(Sms.INSTANCE.urlTemplate(), Sms.INSTANCE.paramsType());
        assertEquals(List.of("number", "body"), t.paramNames());
    }

    @Test
    @DisplayName("Sms with just number renders with empty body key")
    void smsBasic() {
        var t = new UrlTemplate(Sms.INSTANCE.urlTemplate(), Sms.INSTANCE.paramsType());
        var url = t.render(new Sms.Params("+15555551234", Optional.empty()));
        assertEquals("sms:%2B15555551234?body=", url);
    }

    @Test
    @DisplayName("Sms with body renders body")
    void smsWithBody() {
        var t = new UrlTemplate(Sms.INSTANCE.urlTemplate(), Sms.INSTANCE.paramsType());
        var url = t.render(new Sms.Params("+15555551234", Optional.of("Hello!")));
        assertTrue(url.contains("body=Hello%21"), url);
    }

    @Test
    @DisplayName("Sms simpleName is `sms`")
    void smsSimpleName() {
        assertEquals("sms", Sms.INSTANCE.simpleName());
    }

    // ====================================================================
    // SimpleAppResolver discovery via an entry app that imports the proxies
    // ====================================================================

    record AppWithBuiltIns() implements AppModule<AppModule._None, AppWithBuiltIns> {
        public record link() implements AppLink<AppWithBuiltIns> {}
        static final AppWithBuiltIns INSTANCE = new AppWithBuiltIns();
        @Override public String title() { return "Sample"; }
        @Override public ImportsFor<AppWithBuiltIns> imports() {
            return new ImportsFor._Builder<AppWithBuiltIns>()
                    .add(new ModuleImports<>(List.of(new Mailto.link()), Mailto.INSTANCE))
                    .add(new ModuleImports<>(List.of(new Tel.link()),    Tel.INSTANCE))
                    .add(new ModuleImports<>(List.of(new Sms.link()),    Sms.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<AppWithBuiltIns> exports() { return new ExportsOf<>(this, List.of()); }
    }

    @Test
    @DisplayName("All three built-in proxies discovered transitively from an entry app")
    void resolverDiscoversAllBuiltIns() {
        var resolver = new SimpleAppResolver(List.of(AppWithBuiltIns.INSTANCE));
        assertEquals(1, resolver.apps().size());
        assertEquals(3, resolver.proxies().size());
        assertSame(Mailto.INSTANCE, resolver.resolveProxy("mailto"));
        assertSame(Tel.INSTANCE,    resolver.resolveProxy("tel"));
        assertSame(Sms.INSTANCE,    resolver.resolveProxy("sms"));
    }

    // ====================================================================
    // NavWriter emits proper interpolating functions for each
    // ====================================================================

    @Test
    @DisplayName("NavWriter emits typed nav entries for all three built-in proxies")
    void navWriterEmitsForBuiltIns() {
        var imports = AppWithBuiltIns.INSTANCE.imports();
        var lines = new NavWriter(imports.getAllImports()).write();
        var joined = String.join("\n", lines);

        assertTrue(joined.contains("Mailto: function(p) { return"), joined);
        assertTrue(joined.contains("Tel: function(p) { return"), joined);
        assertTrue(joined.contains("Sms: function(p) { return"), joined);

        // Mailto must build mailto: prefix and reference each optional param.
        assertTrue(joined.contains("\"mailto:\""), "Mailto literal prefix; got:\n" + joined);
        assertTrue(joined.contains("p.subject != null"), joined);
        assertTrue(joined.contains("p.body != null"), joined);
        assertTrue(joined.contains("p.cc != null"), joined);
        assertTrue(joined.contains("p.bcc != null"), joined);

        // Tel must build tel: prefix from required number.
        assertTrue(joined.contains("\"tel:\" + encodeURIComponent(p.number)"), joined);

        // Sms must build sms: prefix and have optional body.
        assertTrue(joined.contains("\"sms:\""), joined);
        assertTrue(joined.contains("encodeURIComponent(p.number)"), joined);
    }
}
