package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.SimpleAppResolver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0051 Phase 3 — the server stamps a coded app's params into the page,
 * and leaves an uncoded app's page exactly as it was.
 */
class AppHtmlStampTest {

    public record Coded(String id, String path) implements AppModule._Param {}

    public static final class CodedApp implements AppModule<Coded, CodedApp> {
        public static final CodedApp INSTANCE = new CodedApp();
        record appMain() implements AppModule._AppMain<Coded, CodedApp> {}
        private CodedApp() {}
        @Override public String simpleName() { return "coded"; }
        @Override public String title()      { return "Coded"; }
        @Override public Class<Coded> paramsType() { return Coded.class; }
        @Override public ParamCodec<Coded> paramCodec() {
            return new ParamCodec<>() {
                @Override public Decoded<Coded> from(Map<String, List<String>> q) {
                    String id = QueryString.first(q, "id");
                    if (id == null) return Decoded.missing("id");
                    return Decoded.ok(new Coded(id, QueryString.first(q, "path")));
                }
                @Override public Map<String, List<String>> to(Coded p) {
                    var out = QueryString.params();
                    QueryString.put(out, "id", p.id());
                    QueryString.put(out, "path", p.path());
                    return out;
                }
            };
        }
        @Override public ImportsFor<CodedApp> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<CodedApp> exports() {
            return new ExportsOf<>(this, List.<Exportable<CodedApp>>of(new appMain()));
        }
    }

    public static final class PlainApp implements AppModule<AppModule._None, PlainApp> {
        public static final PlainApp INSTANCE = new PlainApp();
        record appMain() implements AppModule._AppMain<AppModule._None, PlainApp> {}
        private PlainApp() {}
        @Override public String simpleName() { return "plain"; }
        @Override public String title()      { return "Plain"; }
        @Override public ImportsFor<PlainApp> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<PlainApp> exports() {
            return new ExportsOf<>(this, List.<Exportable<PlainApp>>of(new appMain()));
        }
    }

    private String render(String simpleName, Map<String, List<String>> query) throws Exception {
        var resolver = new SimpleAppResolver(List.of(CodedApp.INSTANCE, PlainApp.INSTANCE));
        var action = new AppHtmlGetAction(
                new QueryParamResolver(), resolver);
        var q = new AppQuery(simpleName, null, null, null, query);
        return action.execute(q, new EmptyParam.NoHeaders()).get().body();
    }

    @Test
    void codedAppGetsItsParamsStamped() throws Exception {
        String html = render("coded", QueryString.parse("id=animals&path=cute/otter"));
        assertTrue(html.contains("const params = Object.freeze({"), html);
        assertTrue(html.contains("\"id\":\"animals\""), html);
        assertTrue(html.contains("appMain(document.getElementById(\"app\"), params)"), html);
    }

    @Test
    void keysTheAppDoesNotClaimAreNotStamped() throws Exception {
        // theme belongs to the action, not to the app: params go through the
        // codec both ways, so the page sees the app's params and nothing else.
        String html = render("coded", QueryString.parse("id=animals&theme=forest&junk=1"));
        assertFalse(html.contains("\"junk\""), html);
        assertFalse(html.contains("\"theme\":\"forest\""), html);
    }

    @Test
    void uncodedAppIsUntouched() throws Exception {
        // The migration is per-app: an app without a codec keeps today's
        // one-argument call and its own client-side params const.
        String html = render("plain", QueryString.parse("anything=1"));
        assertFalse(html.contains("const params ="), html);
        assertTrue(html.contains("appMain(document.getElementById(\"app\"))"), html);
    }

    @Test
    void undecodableParamsLeaveThePageAsItWas() throws Exception {
        // id is required and absent — stamping nothing is today's behaviour;
        // turning this into a 400 belongs with the route work.
        String html = render("coded", QueryString.parse("path=orphan"));
        assertFalse(html.contains("const params ="), html);
    }

    @Test
    void aHostileParamCannotBreakOutOfTheScript() throws Exception {
        String html = render("coded", QueryString.parse("id=" +
                java.net.URLEncoder.encode("</script><script>alert(1)</script>",
                        java.nio.charset.StandardCharsets.UTF_8)));
        assertFalse(html.contains("</script><script>alert(1)"), html);
        assertTrue(html.contains("const params = Object.freeze({"), html);
    }
}
