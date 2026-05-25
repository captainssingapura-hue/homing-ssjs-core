package hue.captains.singapura.js.homing.studio.base.conformance;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocProvider;
import hue.captains.singapura.js.homing.studio.base.DocRegistry;
import hue.captains.singapura.js.homing.studio.base.Reference;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0004 + RFC 0004-ext1 — build-time gate over a studio's typed Doc surface.
 *
 * <p>Per RFC 0005, subclasses provide an explicit list of {@link DocProvider}s.
 * (Pre-RFC-0005 the base walked SimpleAppResolver for AppModules implementing
 * DocProvider; post-RFC-0005 catalogues are no longer AppModules, so explicit
 * enumeration is the cleaner contract.) Generates dynamic tests that assert:</p>
 * <ol>
 *   <li>every contributed Doc has a non-null {@link UUID};</li>
 *   <li>UUIDs are unique within the closure (registry construction would already
 *       reject collisions; the test pins it as a CI gate);</li>
 *   <li>every Doc's {@link Doc#contents()} succeeds and returns a non-empty body
 *       — catches "added the record but forgot the .md file" or a stale
 *       {@code resourcePath()} after a refactor.</li>
 *   <li><b>RFC 0004-ext1</b>: every link in markdown content is either an in-doc
 *       anchor ({@code #anchor}) or a managed reference ({@code #ref:<name>})
 *       resolving to a declared {@link Reference}. Direct out-of-document URLs
 *       (relative paths, http://, https://, mailto:, etc.) fail the scan.</li>
 * </ol>
 *
 * <p><b>Why this base lives in {@code homing-studio-base}, not in
 * {@code homing-conformance}.</b> It references {@code Doc} / {@code DocProvider}
 * / {@code DocRegistry} / {@code Reference} — substrate types that belong to
 * studio-base. Hosting this base in {@code homing-conformance} would have forced
 * conformance to depend on studio-base, a layering inversion (conformance is
 * cross-cutting, sits below substrate concepts). Studio-base hosting its own
 * conformance base keeps the layering clean: {@code homing-conformance} depends
 * only on {@code homing-core}; studio-base provides bases for its own types.</p>
 *
 * @since RFC 0004 (extended in RFC 0004-ext1; reshaped in RFC 0005; relocated
 *        in b.2j cleanup to fix the conformance-on-substrate inversion)
 */
public abstract class DocConformanceTest {

    /** Markdown link pattern: `[label](url)`. */
    private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]*)\\]\\(([^)]+)\\)");

    /** Managed reference fragment: `#ref:<name>`. */
    private static final Pattern REF_FRAGMENT = Pattern.compile("^#ref:([A-Za-z0-9_\\-]+)$");

    /** Explicit list of doc providers — per RFC 0005, downstream enumerates rather than walks. */
    protected abstract List<DocProvider> docProviders();

    @TestFactory
    public Stream<DynamicTest> docConformance() {
        var docs = new ArrayList<Doc>();
        for (DocProvider provider : docProviders()) {
            docs.addAll(provider.docs());
        }

        List<DynamicTest> tests = new ArrayList<>();

        // (1) Non-null UUIDs.
        for (Doc d : docs) {
            tests.add(DynamicTest.dynamicTest(
                    "uuid not null: " + d.getClass().getName(),
                    () -> assertNotNull(d.uuid(),
                            "Doc " + d.getClass().getName() + " returned null uuid()")));
        }

        // (2) UUID uniqueness — pinned via DocRegistry construction.
        tests.add(DynamicTest.dynamicTest(
                "uuid uniqueness across the doc closure",
                () -> {
                    Set<UUID> seen = new HashSet<>();
                    for (Doc d : docs) {
                        if (d.uuid() != null && !seen.add(d.uuid())) {
                            fail("Doc UUID collision: " + d.uuid()
                                    + " appears more than once in the closure");
                        }
                    }
                    // Belt-and-braces: also exercise the registry's collision check.
                    new DocRegistry(docs);
                }));

        // (3) Contents resolve and are non-empty.
        for (Doc d : docs) {
            tests.add(DynamicTest.dynamicTest(
                    "contents() resolves: " + d.getClass().getSimpleName(),
                    () -> {
                        String body = d.contents();
                        assertNotNull(body, d.getClass().getName() + " contents() returned null");
                        assertFalse(body.isBlank(),
                                d.getClass().getName() + " contents() returned blank");
                    }));
        }

        // (4) RFC 0004-ext1 — managed references in markdown bodies.
        // Per markdown Doc, scan every `[label](url)` link. Allow `#anchor`
        // (in-doc), require `#ref:<name>` to resolve in references(), reject
        // anything else with a migration-pointing message.
        for (Doc d : docs) {
            if (!".md".equalsIgnoreCase(d.fileExtension())) continue;
            tests.add(DynamicTest.dynamicTest(
                    "managed references: " + d.getClass().getSimpleName(),
                    () -> assertManagedReferences(d)));
        }

        return tests.stream();
    }

    /**
     * Scan a Doc's markdown body for links and assert RFC 0004-ext1 compliance.
     * Strips fenced code blocks before scanning — `[label](url)` syntax inside
     * triple-backtick fences is example code, not a real link.
     */
    private static void assertManagedReferences(Doc d) {
        Set<String> declaredNames = new HashSet<>();
        for (Reference r : d.references()) {
            declaredNames.add(r.name());
        }
        String body = stripFencedCode(d.contents());
        Matcher m = MD_LINK.matcher(body);
        List<String> failures = new ArrayList<>();
        while (m.find()) {
            String label = m.group(1);
            String url   = m.group(2).trim();

            if (url.startsWith("#")) {
                Matcher refMatch = REF_FRAGMENT.matcher(url);
                if (refMatch.matches()) {
                    String key = refMatch.group(1);
                    if (!declaredNames.contains(key)) {
                        failures.add("  [" + label + "](" + url + ")"
                                + " — cited #ref:" + key + " is not declared in references()."
                                + " Add a Reference with name=\"" + key + "\" or fix the citation.");
                    }
                }
                // Plain `#anchor` (no `ref:` prefix) — in-doc heading anchor; allowed.
            } else {
                // Out-of-document URL — banned. Includes `http://`, `https://`,
                // `mailto:`, relative paths, bare filenames, etc.
                failures.add("  [" + label + "](" + url + ")"
                        + " — out-of-document link in markdown body."
                        + " RFC 0004-ext1: declare in Doc.references() and cite as #ref:<name>.");
            }
        }
        if (!failures.isEmpty()) {
            fail("Doc " + d.getClass().getName() + " has " + failures.size()
                    + " banned/unresolved link(s) in markdown body:\n"
                    + String.join("\n", failures)
                    + "\n\nSee RFC 0004-ext1 for the migration shape.");
        }
    }

    /** Inline code span on a single line: backtick-wrapped, no newline inside. */
    private static final Pattern INLINE_CODE = Pattern.compile("`[^`\\n]*`");

    /**
     * Strip both fenced code blocks (triple backticks) and inline code spans (single
     * backticks). Markdown link syntax inside either is example content, not a link
     * — the conformance scan ignores it. Inline strips replace each span with
     * whitespace of the same length so byte positions stay roughly aligned for
     * any future diagnostic that wants to point at line/column.
     */
    static String stripFencedCode(String body) {
        StringBuilder out = new StringBuilder(body.length());
        boolean inFence = false;
        for (String line : body.split("\n", -1)) {
            String trimmed = line.stripLeading();
            if (trimmed.startsWith("```")) {
                inFence = !inFence;
                out.append('\n');
                continue;
            }
            if (inFence) {
                out.append('\n');
            } else {
                out.append(stripInlineCode(line)).append('\n');
            }
        }
        return out.toString();
    }

    private static String stripInlineCode(String line) {
        Matcher m = INLINE_CODE.matcher(line);
        StringBuilder sb = new StringBuilder();
        int last = 0;
        while (m.find()) {
            sb.append(line, last, m.start());
            for (int i = m.start(); i < m.end(); i++) sb.append(' ');
            last = m.end();
        }
        sb.append(line, last, line.length());
        return sb.toString();
    }
}
