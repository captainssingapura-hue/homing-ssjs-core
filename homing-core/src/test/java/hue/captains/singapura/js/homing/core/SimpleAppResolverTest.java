package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SimpleAppResolverTest {

    // =====================================================================
    // Fixtures — apps and proxies arranged into specific link topologies
    // =====================================================================

    /** Standalone app with no AppLink imports. */
    record Standalone() implements AppModule<AppModule._None, Standalone> {
        public record link() implements AppLink<Standalone> {}
        static final Standalone INSTANCE = new Standalone();
        @Override public String title() { return "Standalone"; }
        @Override public ImportsFor<Standalone> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<Standalone> exports() { return new ExportsOf<>(this, List.of()); }
    }

    /** App that links to {@link Standalone}. */
    record Hub() implements AppModule<AppModule._None, Hub> {
        public record link() implements AppLink<Hub> {}
        static final Hub INSTANCE = new Hub();
        @Override public String title() { return "Hub"; }
        @Override public ImportsFor<Hub> imports() {
            return ImportsFor.<Hub>builder()
                    .add(new ModuleImports<>(List.of(new Standalone.link()), Standalone.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<Hub> exports() { return new ExportsOf<>(this, List.of()); }
    }

    /** Linear chain: Root → MidA → Leaf */
    record Leaf() implements AppModule<AppModule._None, Leaf> {
        public record link() implements AppLink<Leaf> {}
        static final Leaf INSTANCE = new Leaf();
        @Override public String title() { return "Leaf"; }
        @Override public ImportsFor<Leaf> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<Leaf> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record MidA() implements AppModule<AppModule._None, MidA> {
        public record link() implements AppLink<MidA> {}
        static final MidA INSTANCE = new MidA();
        @Override public String title() { return "MidA"; }
        @Override public ImportsFor<MidA> imports() {
            return ImportsFor.<MidA>builder()
                    .add(new ModuleImports<>(List.of(new Leaf.link()), Leaf.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<MidA> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record MidB() implements AppModule<AppModule._None, MidB> {
        public record link() implements AppLink<MidB> {}
        static final MidB INSTANCE = new MidB();
        @Override public String title() { return "MidB"; }
        @Override public ImportsFor<MidB> imports() {
            return ImportsFor.<MidB>builder()
                    .add(new ModuleImports<>(List.of(new Leaf.link()), Leaf.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<MidB> exports() { return new ExportsOf<>(this, List.of()); }
    }

    /** Root that diamonds out to MidA and MidB, both pointing at Leaf. */
    record Root() implements AppModule<AppModule._None, Root> {
        public record link() implements AppLink<Root> {}
        static final Root INSTANCE = new Root();
        @Override public String title() { return "Root"; }
        @Override public ImportsFor<Root> imports() {
            return ImportsFor.<Root>builder()
                    .add(new ModuleImports<>(List.of(new MidA.link()), MidA.INSTANCE))
                    .add(new ModuleImports<>(List.of(new MidB.link()), MidB.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<Root> exports() { return new ExportsOf<>(this, List.of()); }
    }

    /** Cycle pair: CycleA ↔ CycleB. */
    record CycleA() implements AppModule<AppModule._None, CycleA> {
        public record link() implements AppLink<CycleA> {}
        static final CycleA INSTANCE = new CycleA();
        @Override public String title() { return "CycleA"; }
        @Override public ImportsFor<CycleA> imports() {
            return ImportsFor.<CycleA>builder()
                    .add(new ModuleImports<>(List.of(new CycleB.link()), CycleB.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<CycleA> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record CycleB() implements AppModule<AppModule._None, CycleB> {
        public record link() implements AppLink<CycleB> {}
        static final CycleB INSTANCE = new CycleB();
        @Override public String title() { return "CycleB"; }
        @Override public ImportsFor<CycleB> imports() {
            return ImportsFor.<CycleB>builder()
                    .add(new ModuleImports<>(List.of(new CycleA.link()), CycleA.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<CycleB> exports() { return new ExportsOf<>(this, List.of()); }
    }

    /** Two apps that collide on simpleName(). */
    record Foo() implements AppModule<AppModule._None, Foo> {
        public record link() implements AppLink<Foo> {}
        static final Foo INSTANCE = new Foo();
        @Override public String simpleName() { return "shared"; }
        @Override public String title() { return "Foo"; }
        @Override public ImportsFor<Foo> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<Foo> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record Bar() implements AppModule<AppModule._None, Bar> {
        public record link() implements AppLink<Bar> {}
        static final Bar INSTANCE = new Bar();
        @Override public String simpleName() { return "shared"; }
        @Override public String title() { return "Bar"; }
        @Override public ImportsFor<Bar> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<Bar> exports() { return new ExportsOf<>(this, List.of()); }
    }

    /** App that imports CSS from another EsModule but has NO AppLink imports. */
    record Styles() implements CssGroup<Styles> {
        public record btn() implements CssClass<Styles> {}
        static final Styles INSTANCE = new Styles();
        @Override public List<CssClass<Styles>> cssClasses() { return List.of(new btn()); }
    }

    record OnlyCss() implements AppModule<AppModule._None, OnlyCss> {
        public record link() implements AppLink<OnlyCss> {}
        static final OnlyCss INSTANCE = new OnlyCss();
        @Override public String title() { return "OnlyCss"; }
        @Override public ImportsFor<OnlyCss> imports() {
            return ImportsFor.<OnlyCss>builder()
                    .add(new ModuleImports<>(List.of(new Styles.btn()), Styles.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<OnlyCss> exports() { return new ExportsOf<>(this, List.of()); }
    }

    /** Proxies. */
    record GitHubProxy() implements ProxyApp<AppModule._None, GitHubProxy> {
        public record link() implements AppLink<GitHubProxy> {}
        static final GitHubProxy INSTANCE = new GitHubProxy();
        @Override public String simpleName() { return "github"; }
        @Override public String urlTemplate() { return "https://github.com"; }
    }

    record DocsProxy() implements ProxyApp<AppModule._None, DocsProxy> {
        public record link() implements AppLink<DocsProxy> {}
        static final DocsProxy INSTANCE = new DocsProxy();
        @Override public String simpleName() { return "docs"; }
        @Override public String urlTemplate() { return "https://docs.example.com"; }
    }

    /** App that links to two proxies. */
    record WithProxies() implements AppModule<AppModule._None, WithProxies> {
        public record link() implements AppLink<WithProxies> {}
        static final WithProxies INSTANCE = new WithProxies();
        @Override public String title() { return "WithProxies"; }
        @Override public ImportsFor<WithProxies> imports() {
            return ImportsFor.<WithProxies>builder()
                    .add(new ModuleImports<>(List.of(new GitHubProxy.link()), GitHubProxy.INSTANCE))
                    .add(new ModuleImports<>(List.of(new DocsProxy.link()), DocsProxy.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<WithProxies> exports() { return new ExportsOf<>(this, List.of()); }
    }

    /** Cross-kind name collision: app and proxy share simpleName "clash". */
    record ClashApp() implements AppModule<AppModule._None, ClashApp> {
        public record link() implements AppLink<ClashApp> {}
        static final ClashApp INSTANCE = new ClashApp();
        @Override public String simpleName() { return "clash"; }
        @Override public String title() { return "ClashApp"; }
        @Override public ImportsFor<ClashApp> imports() {
            return ImportsFor.<ClashApp>builder()
                    .add(new ModuleImports<>(List.of(new ClashProxy.link()), ClashProxy.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<ClashApp> exports() { return new ExportsOf<>(this, List.of()); }
    }

    record ClashProxy() implements ProxyApp<AppModule._None, ClashProxy> {
        public record link() implements AppLink<ClashProxy> {}
        static final ClashProxy INSTANCE = new ClashProxy();
        @Override public String simpleName() { return "clash"; }
        @Override public String urlTemplate() { return "https://example.com/x"; }
    }

    // =====================================================================
    // Tests
    // =====================================================================

    @Test
    @DisplayName("standalone entry app registers itself only")
    void standalone() {
        var r = new SimpleAppResolver(List.of(Standalone.INSTANCE));
        assertEquals(1, r.apps().size());
        assertEquals(0, r.proxies().size());
        assertSame(Standalone.INSTANCE, r.resolveApp("standalone"));
        assertNull(r.resolveProxy("standalone"));
    }

    @Test
    @DisplayName("entry app + linked target are both registered")
    void linearTwoStep() {
        var r = new SimpleAppResolver(List.of(Hub.INSTANCE));
        assertEquals(2, r.apps().size());
        assertSame(Hub.INSTANCE,        r.resolveApp("hub"));
        assertSame(Standalone.INSTANCE, r.resolveApp("standalone"));
    }

    @Test
    @DisplayName("linear chain Root → MidA → Leaf registers all three")
    void linearChain() {
        // Root only links to MidA and MidB (each links to Leaf).
        var r = new SimpleAppResolver(List.of(Root.INSTANCE));
        assertEquals(4, r.apps().size());
        assertNotNull(r.resolveApp("root"));
        assertNotNull(r.resolveApp("mid-a"));
        assertNotNull(r.resolveApp("mid-b"));
        assertNotNull(r.resolveApp("leaf"));
    }

    @Test
    @DisplayName("diamond — Leaf reached via two paths registers once")
    void diamond() {
        var r = new SimpleAppResolver(List.of(Root.INSTANCE));
        // Despite being reachable via both MidA and MidB, Leaf appears once.
        long leafCount = r.apps().stream()
                .filter(a -> a == Leaf.INSTANCE)
                .count();
        assertEquals(1, leafCount);
    }

    @Test
    @DisplayName("cycle A ↔ B does not infinite-loop and registers both")
    void cycle() {
        var r = new SimpleAppResolver(List.of(CycleA.INSTANCE));
        assertEquals(2, r.apps().size());
        assertSame(CycleA.INSTANCE, r.resolveApp("cycle-a"));
        assertSame(CycleB.INSTANCE, r.resolveApp("cycle-b"));
    }

    @Test
    @DisplayName("orphan app — not reachable from any entry — is NOT registered")
    void orphan() {
        // Standalone is an entry. Hub is an orphan from Standalone's perspective.
        var r = new SimpleAppResolver(List.of(Standalone.INSTANCE));
        assertNull(r.resolveByClass(Hub.class));
        assertEquals(1, r.apps().size());
    }

    @Test
    @DisplayName("CSS-only imports do not trigger AppModule registration")
    void cssImportNotFollowed() {
        var r = new SimpleAppResolver(List.of(OnlyCss.INSTANCE));
        // OnlyCss has a non-AppLink import (CSS). Styles is NOT an AppModule;
        // but more importantly the walker should not register it via this path.
        assertEquals(1, r.apps().size());
        assertSame(OnlyCss.INSTANCE, r.resolveApp("only-css"));
    }

    @Test
    @DisplayName("simple-name collision among apps fails at construction")
    void collisionApps() {
        // Foo and Bar both override simpleName() to "shared".
        // Both are entry apps so both get registered, then indexByName should detect.
        var ex = assertThrows(IllegalStateException.class,
                () -> new SimpleAppResolver(List.of(Foo.INSTANCE, Bar.INSTANCE)));
        assertTrue(ex.getMessage().contains("collision"), ex.getMessage());
        assertTrue(ex.getMessage().contains("shared"), ex.getMessage());
    }

    @Test
    @DisplayName("proxies are discovered through entry app's AppLink imports")
    void proxiesViaEntryApp() {
        var r = new SimpleAppResolver(List.of(WithProxies.INSTANCE));
        assertEquals(1, r.apps().size());
        assertEquals(2, r.proxies().size());
        assertSame(GitHubProxy.INSTANCE, r.resolveProxy("github"));
        assertSame(DocsProxy.INSTANCE,   r.resolveProxy("docs"));
    }

    @Test
    @DisplayName("resolve(name) returns either app or proxy")
    void resolveAcrossKinds() {
        var r = new SimpleAppResolver(List.of(WithProxies.INSTANCE));
        assertSame(WithProxies.INSTANCE, r.resolve("with-proxies"));
        assertSame(GitHubProxy.INSTANCE, r.resolve("github"));
        assertNull(r.resolve("nope"));
    }

    @Test
    @DisplayName("resolveApp filters out proxies; resolveProxy filters out apps")
    void kindFiltering() {
        var r = new SimpleAppResolver(List.of(WithProxies.INSTANCE));
        assertNotNull(r.resolveApp("with-proxies"));
        assertNull(r.resolveApp("github"));      // it's a proxy, not an app

        assertNotNull(r.resolveProxy("github"));
        assertNull(r.resolveProxy("with-proxies")); // it's an app, not a proxy
    }

    @Test
    @DisplayName("resolveByClass works for both kinds")
    void resolveByClass() {
        var r = new SimpleAppResolver(List.of(WithProxies.INSTANCE));
        assertSame(WithProxies.INSTANCE, r.resolveByClass(WithProxies.class));
        assertSame(GitHubProxy.INSTANCE, r.resolveByClass(GitHubProxy.class));
        assertNull(r.resolveByClass(Hub.class));   // not registered
    }

    @Test
    @DisplayName("cross-kind name collision (app + proxy share name) fails at construction")
    void crossKindCollision() {
        var ex = assertThrows(IllegalStateException.class,
                () -> new SimpleAppResolver(List.of(ClashApp.INSTANCE)));
        assertTrue(ex.getMessage().contains("clash"), ex.getMessage());
        assertTrue(ex.getMessage().toLowerCase().contains("collision")
                   || ex.getMessage().toLowerCase().contains("both"),
                ex.getMessage());
    }

    @Test
    @DisplayName("all() returns apps + proxies in a single collection")
    void allCombined() {
        var r = new SimpleAppResolver(List.of(WithProxies.INSTANCE));
        assertEquals(3, r.all().size());  // 1 app + 2 proxies
    }

    @Test
    @DisplayName("multiple entry apps with overlapping closures register everything once")
    void multipleEntries() {
        // Root reaches MidA, MidB, Leaf. Hub reaches Standalone.
        var r = new SimpleAppResolver(List.of(Root.INSTANCE, Hub.INSTANCE));
        assertEquals(6, r.apps().size());  // Root, MidA, MidB, Leaf, Hub, Standalone
    }
}
