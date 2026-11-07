package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UrlTemplateTest {

    // ---- Sample Params records used by tests ----

    record GitHub(String repo, Optional<String> path) implements AppModule._Param {}
    record OneRequired(String tab) {}
    record OneOptional(Optional<String> q) {}
    record TwoRequired(String org, String repo) {}
    record DocsRef(String section, Optional<String> anchor) {}

    // ---- Compilation: literals only --------------------------------------

    @Test
    @DisplayName("pure-literal template renders unchanged")
    void pureLiteral() {
        var t = new UrlTemplate("https://example.com/static", Void.class);
        assertEquals("https://example.com/static", t.render(null));
        assertEquals(List.of(), t.paramNames());
    }

    @Test
    @DisplayName("pure-literal template with no Params record is fine")
    void pureLiteralVoidParams() {
        var t = new UrlTemplate("/health", Void.class);
        assertEquals("/health", t.render(null));
    }

    // ---- Required interpolation ------------------------------------------

    @Test
    @DisplayName("required {name} renders the param value, URL-encoded")
    void requiredInterpolation() {
        var t = new UrlTemplate("https://github.com/{repo}", GitHub.class);
        assertEquals("https://github.com/acme%2Fproj", t.render(new GitHub("acme/proj", Optional.empty())));
    }

    @Test
    @DisplayName("two required slots render in order")
    void twoRequired() {
        var t = new UrlTemplate("https://github.com/{org}/{repo}", TwoRequired.class);
        assertEquals("https://github.com/acme/proj", t.render(new TwoRequired("acme", "proj")));
    }

    // ---- Optional interpolation ------------------------------------------

    @Test
    @DisplayName("optional {name?} substitutes empty string when absent")
    void optionalAbsent() {
        var t = new UrlTemplate("https://github.com/{repo}/{path?}", GitHub.class);
        assertEquals("https://github.com/acme%2Fproj/", t.render(new GitHub("acme/proj", Optional.empty())));
    }

    @Test
    @DisplayName("optional {name?} substitutes the value when present")
    void optionalPresent() {
        var t = new UrlTemplate("https://github.com/{repo}/{path?}", GitHub.class);
        assertEquals("https://github.com/acme%2Fproj/README.md",
                t.render(new GitHub("acme/proj", Optional.of("README.md"))));
    }

    @Test
    @DisplayName("optional {name?:default} uses default when absent")
    void optionalDefault() {
        var t = new UrlTemplate("https://docs.example.com/{section}#{anchor?:top}", DocsRef.class);
        assertEquals("https://docs.example.com/api#top",
                t.render(new DocsRef("api", Optional.empty())));
    }

    @Test
    @DisplayName("optional {name?:default} uses present value, not default")
    void optionalDefaultIgnoredWhenPresent() {
        var t = new UrlTemplate("https://docs.example.com/{section}#{anchor?:top}", DocsRef.class);
        assertEquals("https://docs.example.com/api#install",
                t.render(new DocsRef("api", Optional.of("install"))));
    }

    // ---- paramNames() ----------------------------------------------------

    @Test
    @DisplayName("paramNames lists each interpolation by first appearance")
    void paramNamesInOrder() {
        var t = new UrlTemplate("https://x.test/{org}/{repo}/{org}", TwoRequired.class);
        assertEquals(List.of("org", "repo"), t.paramNames());
    }

    // ---- Compile-time errors ---------------------------------------------

    @Test
    @DisplayName("required {name} on an Optional component is rejected at compile time")
    void requiredSlotOnOptionalComponent() {
        var ex = assertThrows(IllegalStateException.class,
                () -> new UrlTemplate("/p/{q}", OneOptional.class));
        assertTrue(ex.getMessage().contains("required"), ex.getMessage());
        assertTrue(ex.getMessage().contains("Optional"), ex.getMessage());
    }

    @Test
    @DisplayName("optional {name?} on a required component is rejected at compile time")
    void optionalSlotOnRequiredComponent() {
        var ex = assertThrows(IllegalStateException.class,
                () -> new UrlTemplate("/p/{tab?}", OneRequired.class));
        assertTrue(ex.getMessage().contains("optional"), ex.getMessage());
    }

    @Test
    @DisplayName("unknown param name is rejected at compile time")
    void unknownParam() {
        var ex = assertThrows(IllegalStateException.class,
                () -> new UrlTemplate("/p/{nope}", OneRequired.class));
        assertTrue(ex.getMessage().contains("unknown"), ex.getMessage());
    }

    @Test
    @DisplayName("interpolation against Void.class params is rejected")
    void slotWithVoidParams() {
        var ex = assertThrows(IllegalStateException.class,
                () -> new UrlTemplate("/p/{x}", Void.class));
        assertTrue(ex.getMessage().contains("Void.class"), ex.getMessage());
    }

    @Test
    @DisplayName("malformed slot — unclosed brace — is rejected")
    void unclosedBrace() {
        var ex = assertThrows(IllegalStateException.class,
                () -> new UrlTemplate("/p/{repo", GitHub.class));
        assertTrue(ex.getMessage().contains("Unclosed"), ex.getMessage());
    }

    @Test
    @DisplayName("malformed slot — stray close brace — is rejected")
    void strayCloseBrace() {
        var ex = assertThrows(IllegalStateException.class,
                () -> new UrlTemplate("/p/repo}", GitHub.class));
        assertTrue(ex.getMessage().contains("Unmatched"), ex.getMessage());
    }

    @Test
    @DisplayName("empty {} slot is rejected")
    void emptySlot() {
        var ex = assertThrows(IllegalStateException.class,
                () -> new UrlTemplate("/p/{}", GitHub.class));
        assertTrue(ex.getMessage().contains("Empty"), ex.getMessage());
    }

    @Test
    @DisplayName("non-record paramsType is rejected")
    void nonRecordParamsType() {
        var ex = assertThrows(IllegalStateException.class,
                () -> new UrlTemplate("/p/{x}", String.class));
        assertTrue(ex.getMessage().contains("record"), ex.getMessage());
    }

    @Test
    @DisplayName("'?' followed by non-':' is rejected")
    void badOptionalSyntax() {
        var ex = assertThrows(IllegalStateException.class,
                () -> new UrlTemplate("/p/{path?bad}", GitHub.class));
        assertTrue(ex.getMessage().contains("default"), ex.getMessage());
    }

    // ---- ProxyApp integration -------------------------------------------

    record GitHubProxy() implements ProxyApp<GitHub, GitHubProxy> {
        public record link() implements AppLink<GitHubProxy> {}
        @Override public String simpleName() { return "github"; }
        @Override public Class<GitHub> paramsType() { return GitHub.class; }
        @Override public String urlTemplate() { return "https://github.com/{repo}/{path?}"; }
    }

    @Test
    @DisplayName("ProxyApp template compiles and renders correctly")
    void proxyAppEndToEnd() {
        var proxy = new GitHubProxy();
        var t = new UrlTemplate(proxy.urlTemplate(), proxy.paramsType());
        assertEquals("https://github.com/acme%2Fproj/README.md",
                t.render(new GitHub("acme/proj", Optional.of("README.md"))));
        assertEquals("github", proxy.simpleName());
    }

    @Test
    @DisplayName("ProxyApp default simpleName uses kebab-case")
    void proxyAppDefaultSimpleName() {
        record DocsProxy() implements ProxyApp<AppModule._None, DocsProxy> {
            @Override public String urlTemplate() { return "https://docs.example.com/"; }
        }
        assertEquals("docs-proxy", new DocsProxy().simpleName());
    }
}
