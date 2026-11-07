package hue.captains.singapura.js.homing.libs.tools;

import hue.captains.singapura.js.homing.core.BundledExternalModule;
import hue.captains.singapura.js.homing.libs.HomingLibsRegistry;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;

/**
 * Build-time tool: downloads every {@link BundledExternalModule} declared in
 * {@link HomingLibsRegistry}, SHA-512-verifies the bytes against the pinned
 * hash, and copies them to {@code target/classes/} so they ship inside the JAR.
 *
 * <p>Invoked by {@code exec-maven-plugin} in the {@code process-classes} Maven
 * phase. Caches downloads in {@code ~/.m2/repository/.cache/homing-libs/<sha>.bin}
 * — first build needs internet; subsequent builds run fully offline.</p>
 *
 * <h2>Usage</h2>
 * <pre>
 *   java LibDownloader &lt;outputDir&gt; &lt;cacheDir&gt;
 *
 *   outputDir   target/classes/        — where to place lib/&lt;name&gt;@&lt;ver&gt;/...
 *   cacheDir    ~/.m2/.../homing-libs  — sha-keyed download cache
 * </pre>
 *
 * <h2>Bootstrap mode</h2>
 *
 * <p>If a registered library returns {@code sha512()} that's empty, blank, or the
 * sentinel {@code "TODO"}, the downloader downloads, computes the actual hash,
 * prints it, and fails the build with a one-line instruction to paste the hash
 * into the Java declaration. This is the deliberate bootstrap workflow when adding
 * a new library — exactly once per library.</p>
 */
public record LibDownloader() implements StatelessFunctionalObject {

    public static final LibDownloader INSTANCE = new LibDownloader();

    private static final Duration TIMEOUT = Duration.ofSeconds(60);
    private static final String SHA_TODO_SENTINEL = "TODO";

    /** Downloads, verifies and copies every library; {@code args} are the output folder and the cache folder. */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: LibDownloader <outputDir> <cacheDir>");
            System.exit(2);
        }
        Path outputDir = Path.of(args[0]);
        Path cacheDir = Path.of(args[1]);
        Files.createDirectories(cacheDir);

        List<BundledExternalModule<?>> libs = HomingLibsRegistry.INSTANCE.all();
        if (libs.isEmpty()) {
            System.out.println("[homing-libs] no bundled libraries registered — nothing to download.");
            return;
        }

        HttpClient http = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(TIMEOUT)
                .build();

        boolean anyBootstrapNeeded = false;

        for (BundledExternalModule<?> lib : libs) {
            anyBootstrapNeeded |= INSTANCE.fetch(lib, outputDir, cacheDir, http);
        }

        if (anyBootstrapNeeded) {
            throw new IOException(
                    "One or more libraries are in bootstrap mode (sha512 not pinned). " +
                    "See the printed hashes above; paste each into the corresponding Java class and rebuild. " +
                    "Failing the build is intentional — pinned hashes are mandatory for production.");
        }
    }

    /** One library onto the classpath, verified and cached; whether it is in bootstrap mode, and only printed its hash. */
    private boolean fetch(BundledExternalModule<?> lib, Path outputDir, Path cacheDir, HttpClient http)
            throws IOException, InterruptedException {
        String url = lib.sourceUrl();
        String resourcePath = lib.resourcePath();
        String pinnedSha = lib.sha512();
        boolean pinned = pinnedSha != null && !pinnedSha.isBlank() && !SHA_TODO_SENTINEL.equals(pinnedSha);
        Path target = outputDir.resolve(resourcePath);

        // Fast path: target already exists and matches pinned hash → skip
        if (Files.exists(target) && pinned) {
            String have = sha512Hex(target);
            if (have.equalsIgnoreCase(pinnedSha)) {
                System.out.printf("[homing-libs] cached on classpath: %s%n", resourcePath);
                return false;
            }
        }

        // Cache lookup by hash
        Path cached = pinned ? cacheDir.resolve(pinnedSha + ".bin") : null;
        byte[] bytes = bytesOf(lib, cached, http);
        String actualSha = sha512Hex(bytes);

        // Bootstrap mode — sha is empty or sentinel
        if (!pinned) {
            System.out.printf("%n[homing-libs] BOOTSTRAP — paste the following hash into %s.sha512():%n",
                    lib.getClass().getSimpleName());
            System.out.printf("    return \"%s\";%n", actualSha);
            System.out.printf("    (size: %d bytes, source: %s)%n%n", bytes.length, url);
            return true;
        }

        // Pinned mode — verify
        if (!actualSha.equalsIgnoreCase(pinnedSha)) {
            throw new IOException(String.format(
                    "SHA-512 mismatch for %s%n  expected: %s%n  actual:   %s%n  source:   %s%n" +
                    "  → If the upstream content has legitimately changed, update sha512() in the Java class. " +
                    "Otherwise this is a security signal — investigate.",
                    lib.getClass().getSimpleName(), pinnedSha, actualSha, url));
        }

        // Persist to cache + target
        if (cached != null) {
            Files.write(cached, bytes);
        }
        Files.createDirectories(target.getParent());
        Files.write(target, bytes);
        System.out.printf("[homing-libs] OK: %s (%d bytes) ← %s%n", resourcePath, bytes.length, url);
        return false;
    }

    /** A library's bytes: from the cache when it holds them unchanged, else downloaded. */
    private byte[] bytesOf(BundledExternalModule<?> lib, Path cached, HttpClient http) throws IOException, InterruptedException {
        String url = lib.sourceUrl();
        String pinnedSha = lib.sha512();
        if (cached != null && Files.exists(cached) && sha512Hex(cached).equalsIgnoreCase(pinnedSha)) {
            System.out.printf("[homing-libs] cache hit: %s (sha %s…)%n", lib.resourcePath(), pinnedSha.substring(0, 16));
            return Files.readAllBytes(cached);
        }
        System.out.printf("[homing-libs] downloading: %s%n", url);
        return followEsmShReexportIfPresent(http, url, httpGet(http, url));
    }

    /**
     * esm.sh's {@code ?bundle} flag returns a tiny re-export wrapper of the form:
     * <pre>
     *   /* esm.sh - marked@14.1.4 *&#47;
     *   export * from "/marked@14.1.4/es2022/marked.bundle.mjs";
     * </pre>
     * In a browser loaded from esm.sh, the relative path resolves correctly; on
     * our server it resolves to the wrong host and 404s. Detect the wrapper
     * pattern and dereference it once so we cache + ship the real bundle bytes.
     */
    private static final Pattern ESM_SH_REEXPORT = Pattern.compile(
            "(?s)\\A\\s*/\\* esm\\.sh[^*]*\\*/\\s*export \\* from \"([^\"]+)\";\\s*\\z");

    private byte[] followEsmShReexportIfPresent(HttpClient http, String originalUrl, byte[] bytes)
            throws IOException, InterruptedException {
        if (bytes.length > 1024) return bytes;   // bundles are big; wrappers are tiny
        String text = new String(bytes, StandardCharsets.UTF_8);
        Matcher m = ESM_SH_REEXPORT.matcher(text);
        if (!m.matches()) return bytes;
        String innerPath = m.group(1);
        URI base = URI.create(originalUrl);
        URI inner = innerPath.startsWith("http") ? URI.create(innerPath) : base.resolve(innerPath);
        System.out.printf("[homing-libs]   ↳ following esm.sh wrapper to: %s%n", inner);
        return httpGet(http, inner.toString());
    }

    private byte[] httpGet(HttpClient http, String url) throws IOException, InterruptedException {
        HttpResponse<byte[]> resp = http.send(
                HttpRequest.newBuilder(URI.create(url)).timeout(TIMEOUT).GET().build(),
                HttpResponse.BodyHandlers.ofByteArray());
        if (resp.statusCode() != 200) {
            throw new IOException("HTTP " + resp.statusCode() + " from " + url);
        }
        return resp.body();
    }

    private String sha512Hex(Path file) throws IOException {
        try (InputStream in = Files.newInputStream(file)) {
            return sha512Hex(in.readAllBytes());
        }
    }

    private String sha512Hex(byte[] bytes) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-512").digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 not available", e);
        }
    }

}
