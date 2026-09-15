package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class NavWriterTest {

    // ---- Fixtures --------------------------------------------------------

    record TargetA() implements AppModule<AppModule._None, TargetA> {
        public record link() implements AppLink<TargetA> {}
        static final TargetA INSTANCE = new TargetA();
        @Override public String title() { return "A"; }
        @Override public ImportsFor<TargetA> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<TargetA> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record TargetB() implements AppModule<AppModule._None, TargetB> {
        public record link() implements AppLink<TargetB> {}
        static final TargetB INSTANCE = new TargetB();
        @Override public String simpleName() { return "custom-b"; }
        @Override public String title() { return "B"; }
        @Override public ImportsFor<TargetB> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<TargetB> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record GitHubProxy() implements ProxyApp<GitHubProxy.Params, GitHubProxy> {
        public record link() implements AppLink<GitHubProxy> {}
        public record Params(String repo, Optional<String> path) implements AppModule._Param {}
        static final GitHubProxy INSTANCE = new GitHubProxy();
        @Override public String simpleName() { return "github"; }
        @Override public Class<Params> paramsType() { return Params.class; }
        @Override public String urlTemplate() { return "https://github.com/{repo}/{path?}"; }
    }

    record StaticProxy() implements ProxyApp<AppModule._None, StaticProxy> {
        public record link() implements AppLink<StaticProxy> {}
        static final StaticProxy INSTANCE = new StaticProxy();
        @Override public String simpleName() { return "static"; }
        @Override public String urlTemplate() { return "https://example.com/static"; }
    }

    record OnlyCss() implements CssGroup<OnlyCss> {
        public record btn() implements CssClass<OnlyCss> {}
        static final OnlyCss INSTANCE = new OnlyCss();
        @Override public List<CssClass<OnlyCss>> cssClasses() { return List.of(new btn()); }
    }

    // ---- Tests -----------------------------------------------------------

    @Test
    @DisplayName("no AppLink imports → empty output (no nav block emitted)")
    void noAppLinks_emptyOutput() {
        var writer = new NavWriter(java.util.Map.of());
        assertEquals(List.of(), writer.write());
    }

    @Test
    @DisplayName("CSS-only imports → no nav block")
    void onlyCss_noNav() {
        var imports = ImportsFor.<TargetA>builder()
                .add(new ModuleImports<>(List.of(new OnlyCss.btn()), OnlyCss.INSTANCE))
                .build();
        var writer = new NavWriter(imports.getAllImports());
        assertEquals(List.of(), writer.write());
    }

    @Test
    @DisplayName("AppModule target → entry calls _homingBuildAppUrl with simpleName")
    void appModuleTarget() {
        var imports = ImportsFor.<TargetA>builder()
                .add(new ModuleImports<>(List.of(new TargetA.link()), TargetA.INSTANCE))
                .build();
        var lines = new NavWriter(imports.getAllImports()).write();
        var joined = String.join("\n", lines);

        assertTrue(joined.contains("// === homing generated nav"), joined);
        assertTrue(joined.contains("function _homingBuildAppUrl"), joined);
        assertTrue(joined.contains("const nav = Object.freeze({"), joined);
        assertTrue(joined.contains("TargetA: function(p) { return _homingBuildAppUrl(\"target-a\", p); }"), joined);
        assertTrue(joined.contains("// === end homing generated nav"), joined);
    }

    @Test
    @DisplayName("AppModule with overridden simpleName uses the override")
    void appModuleWithOverriddenName() {
        var imports = ImportsFor.<TargetA>builder()
                .add(new ModuleImports<>(List.of(new TargetB.link()), TargetB.INSTANCE))
                .build();
        var joined = String.join("\n", new NavWriter(imports.getAllImports()).write());
        assertTrue(joined.contains("TargetB: function(p) { return _homingBuildAppUrl(\"custom-b\", p); }"), joined);
    }

    @Test
    @DisplayName("ProxyApp target with template params → emits interpolating function")
    void proxyAppWithParams() {
        var imports = ImportsFor.<TargetA>builder()
                .add(new ModuleImports<>(List.of(new GitHubProxy.link()), GitHubProxy.INSTANCE))
                .build();
        var joined = String.join("\n", new NavWriter(imports.getAllImports()).write());

        // Required {repo} → encodeURIComponent(p.repo)
        assertTrue(joined.contains("encodeURIComponent(p.repo)"), joined);
        // Optional {path?} → ternary check on p.path
        assertTrue(joined.contains("p.path != null"), joined);
        assertTrue(joined.contains("encodeURIComponent(p.path)"), joined);
        // Identifier matches the simple class name
        assertTrue(joined.contains("GitHubProxy: function(p) { return"), joined);
    }

    @Test
    @DisplayName("ProxyApp with no params → emits literal-only expression")
    void proxyAppLiteralOnly() {
        var imports = ImportsFor.<TargetA>builder()
                .add(new ModuleImports<>(List.of(new StaticProxy.link()), StaticProxy.INSTANCE))
                .build();
        var joined = String.join("\n", new NavWriter(imports.getAllImports()).write());
        assertTrue(joined.contains("StaticProxy: function(p) { return \"https:\\/\\/example.com\\/static\""),
                "Got: " + joined);
    }

    @Test
    @DisplayName("multiple targets → multiple entries in insertion order")
    void multipleTargetsInOrder() {
        var imports = ImportsFor.<TargetA>builder()
                .add(new ModuleImports<>(List.of(new TargetA.link()), TargetA.INSTANCE))
                .add(new ModuleImports<>(List.of(new GitHubProxy.link()), GitHubProxy.INSTANCE))
                .add(new ModuleImports<>(List.of(new TargetB.link()), TargetB.INSTANCE))
                .build();
        var joined = String.join("\n", new NavWriter(imports.getAllImports()).write());

        assertTrue(joined.contains("TargetA:"), joined);
        assertTrue(joined.contains("TargetB:"), joined);
        assertTrue(joined.contains("GitHubProxy:"), joined);

        // All three present; trailing entry has no comma
        int countCommas = (int) joined.lines().filter(l -> l.contains("function(p) { return") && l.endsWith(",")).count();
        int countWithoutComma = (int) joined.lines().filter(l -> l.contains("function(p) { return") && !l.endsWith(",")).count();
        assertEquals(2, countCommas, "two of three entries should end with comma; got:\n" + joined);
        assertEquals(1, countWithoutComma, "one (last) entry should not end with comma; got:\n" + joined);
    }

    @Test
    @DisplayName("theme/locale propagation appears in the helper")
    void themePropagation() {
        var imports = ImportsFor.<TargetA>builder()
                .add(new ModuleImports<>(List.of(new TargetA.link()), TargetA.INSTANCE))
                .build();
        var joined = String.join("\n", new NavWriter(imports.getAllImports()).write());
        assertTrue(joined.contains("here.get(\"theme\")"), joined);
        assertTrue(joined.contains("here.get(\"locale\")"), joined);
        // and the override-wins logic
        assertTrue(joined.contains("params.theme  == null"), joined);
        assertTrue(joined.contains("params.locale == null"), joined);
    }

    @Test
    @DisplayName("nav object is wrapped in Object.freeze")
    void navIsFrozen() {
        var imports = ImportsFor.<TargetA>builder()
                .add(new ModuleImports<>(List.of(new TargetA.link()), TargetA.INSTANCE))
                .build();
        var joined = String.join("\n", new NavWriter(imports.getAllImports()).write());
        assertTrue(joined.contains("Object.freeze({"), joined);
    }

    @Test
    @DisplayName("URL contract uses ?app=<simpleName>, not ?class=")
    void urlContractIsApp() {
        var imports = ImportsFor.<TargetA>builder()
                .add(new ModuleImports<>(List.of(new TargetA.link()), TargetA.INSTANCE))
                .build();
        var joined = String.join("\n", new NavWriter(imports.getAllImports()).write());
        assertTrue(joined.contains("/app?app="), joined);
        assertFalse(joined.contains("?class="), joined);
    }

    @Test
    @DisplayName("UrlTemplate.toJsExpression emits required-only expression")
    void toJsExpressionRequired() {
        record TwoP(String a, String b) {}
        var t = UrlTemplate.compile("https://x.test/{a}/{b}", TwoP.class);
        assertEquals(
                "\"https:\\/\\/x.test\\/\" + encodeURIComponent(p.a) + \"\\/\" + encodeURIComponent(p.b)",
                t.toJsExpression("p"));
    }

    @Test
    @DisplayName("UrlTemplate.toJsExpression emits optional with default")
    void toJsExpressionOptionalDefault() {
        record P(Optional<String> q) {}
        var t = UrlTemplate.compile("/x?q={q?:none}", P.class);
        var js = t.toJsExpression("p");
        assertTrue(js.contains("p.q != null ? encodeURIComponent(p.q) : encodeURIComponent(\"none\")"), js);
    }

    @Test
    @DisplayName("UrlTemplate.toJsExpression emits optional without default")
    void toJsExpressionOptionalEmpty() {
        record P(Optional<String> q) {}
        var t = UrlTemplate.compile("/x/{q?}", P.class);
        var js = t.toJsExpression("p");
        assertTrue(js.contains("p.q != null ? encodeURIComponent(p.q) : \"\""), js);
    }

    @Test
    @DisplayName("a literal-only template → JS expression is just a quoted string")
    void toJsExpressionLiteralOnly() {
        var t = UrlTemplate.compile("/health", Void.class);
        assertEquals("\"\\/health\"", t.toJsExpression("p"));
    }
}
