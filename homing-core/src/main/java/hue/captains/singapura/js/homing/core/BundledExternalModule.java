package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.js.homing.core.util.ResourceReader;

import hue.captains.singapura.js.homing.core.util.ResourceReaders;
import java.util.List;

/**
 * A 3rd-party JavaScript library bundled at build time and served self-contained.
 *
 * <p>Unlike {@link ExternalModule} (which loads from a CDN at browser request time
 * via a hand-written wrapper JS file), a {@code BundledExternalModule} ships the
 * library bytes inside the homing.js classpath. The browser's {@code import}
 * statement is satisfied entirely by the homing.js server — no external network
 * call is ever made at runtime.</p>
 *
 * <h2>Two halves of the contract</h2>
 *
 * <ol>
 *   <li><b>Java side (this declaration)</b> — a typed manifest of what names
 *       the bundled JS file exports. Used by the framework's import-writer so
 *       user code gets type-safe {@code import { Scene } from <ThreeJs>} resolution.
 *       The framework does <i>not</i> emit {@code export} statements for these —
 *       the bundled JS file already declares them natively.</li>
 *
 *   <li><b>JS side (the bundled file)</b> — a real ES module obtained from a CDN
 *       at build time, with all transitive imports inlined ({@code esm.sh} {@code ?bundle}
 *       flag is recommended), checksum-verified, and copied onto the classpath
 *       at {@link #resourcePath()}.</li>
 * </ol>
 *
 * <h2>Server-side behavior</h2>
 *
 * <p>{@code EsModuleGetAction} short-circuits when it sees a {@code BundledExternalModule}:
 * it ships {@link #content()} bytes verbatim, skipping the writer machinery entirely
 * (no imports prefix, no exports suffix, no css/href injection). The bundled file
 * is the entire response.</p>
 *
 * <h2>When NOT to use this</h2>
 *
 * <p>Libraries that aren't ESM (UMD-only, CommonJS-only, or globals-polluting like
 * jQuery) need a hand-written wrapper JS file that adapts the surface to ES module
 * conventions. Use {@link ExternalModule} for those — increasingly rare in the modern
 * ecosystem (2025+).</p>
 *
 * @param <M> the self-type for typed exports
 */
public interface BundledExternalModule<M extends BundledExternalModule<M>> extends EsModule<M> {

    /**
     * Origin URL the bundle was fetched from at build time. Pinned to a specific
     * version. {@code esm.sh}'s {@code ?bundle} query flag inlines transitive deps
     * into a single self-contained file and is the recommended source.
     */
    String sourceUrl();

    /**
     * Resource path on the classpath where the bundled file lives, relative to
     * the classpath root. Must include the version segment for cache-busting and
     * immutable HTTP caching, e.g. {@code "lib/three@0.170.0/three.module.js"}.
     */
    String resourcePath();

    /**
     * SHA-512 hex digest of the expected bundle bytes. The build's downloader
     * verifies the fetched file against this hash and fails loudly on mismatch,
     * preventing silent CDN content drift (re-bundling, version retags, MITM).
     *
     * <p>Must be non-empty for production use. Returning empty causes the downloader
     * to print the computed hash and fail the build with instructions to pin it
     * — a deliberate one-shot bootstrap step when adding a new library.</p>
     */
    String sha512();

    /**
     * Self-served content: classpath bytes at {@link #resourcePath()}.
     * The default reads from {@code homing.devRoot} if set (live-reload during
     * development), falling back to the runtime classpath.
     */
    default List<String> content() {
        return ResourceReaders.INSTANCE.fromSystemProperty().getStringsFromResource(resourcePath());
    }

    /** No imports — bundled files are self-contained. */
    @Override default ImportsFor<M> imports() { return new ImportsFor<>(); }
}
