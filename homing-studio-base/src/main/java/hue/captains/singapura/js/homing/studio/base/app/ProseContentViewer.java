package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppModule;

/**
 * RFC 0015 Phase 5 — framework-default {@link ContentViewer} for prose
 * Docs (kind == {@code "doc"}). Binds the prose-doc kind to
 * {@link DocReader}.
 *
 * <p>Serves every Doc whose {@code kind()} returns {@code "doc"}:
 * {@code ClasspathMarkdownDoc} subtypes, {@code InlineDoc},
 * {@code ResourceMarkdownDoc}, and {@code ProxyDoc} (which by Phase 4
 * constraint always targets a prose Doc).</p>
 *
 * <p>Registered by {@code DefaultFixtures.contentViewers()}; downstream
 * may override the default registry to substitute a custom prose viewer
 * if needed.</p>
 *
 * @since RFC 0015 Phase 5
 * @deprecated Bound to the deprecated {@link DocReader} viewer for the {@code "doc"}
 *             kind. Remains registered as the framework default for existing .md-backed
 *             Docs. New docs should be authored as {@code ComposedDoc}s, which use a
 *             different content kind and viewer ({@code ComposedWidget}).
 */
@Deprecated
public record ProseContentViewer() implements ContentViewer {

    public static final ProseContentViewer INSTANCE = new ProseContentViewer();

    @Override public String kind() { return "doc"; }

    @Override public AppModule<?, ?> app() { return DocReader.INSTANCE; }

    @Override public String urlFor(String contentId) {
        return "/app?app=doc-reader&doc=" + contentId;
    }

    @Override public String summary() {
        return "Markdown prose renderer — fetches /doc?id=<uuid> and parses with the bundled marked.js.";
    }
}
