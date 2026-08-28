package hue.captains.singapura.js.homing.conformance.rules;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0051 (D8) — the flat {@code /app?app=…} address is minted in exactly one
 * place, {@code AppUrl}, and nowhere else in Java.
 *
 * <p>Before D8 there were six hand-rolled spellings of the same idea: {@code
 * Navigable.url()} reflecting over the Params record, four content Doc kinds
 * each concatenating {@code "/app?app=x&id=" + uuid}, and two host {@code
 * urlFor} helpers. They agreed only because the values they were handed — UUIDs
 * and class names — happen to contain nothing that needs escaping. The first
 * value that did would have broken them one at a time, and the emitter that
 * produced the broken link would be whichever one the caller happened to
 * reach.</p>
 *
 * <p>Minting through {@code AppUrl} also routes every address through the app's
 * own {@code ParamCodec}, which makes the address and the parse that reads it
 * back one statement: {@code from(to(p)) == p} is asserted by {@code
 * ParamCodecLaw}. Concatenation carried no such guarantee, which is why this is
 * a rule and not a style preference.</p>
 *
 * <h2>Scope, and why it is drawn here</h2>
 *
 * <ul>
 *   <li><b>Main sources only.</b> A test that asserts a rendered URL <em>should</em>
 *       name the literal — that is the test doing its job.</li>
 *   <li><b>Java only.</b> The four client-side fallbacks in JS cannot call a Java
 *       minter. They are reached only when the server supplied no url, and
 *       retiring them means the server always supplying one — a different piece
 *       of work from this rule.</li>
 *   <li><b>This reactor.</b> Downstream studios have no occurrence today; the
 *       minter is framework property, so the guard sits with it.</li>
 * </ul>
 */
class NoFlatAddressLiteralTest {

    /** The minter itself. */
    private static final String MINTER = "AppUrl.java";

    /**
     * The one exemption, and it is a minter too — in the other language.
     *
     * <p>{@code NavWriter} does not build an address; it GENERATES the
     * client-side function that builds one, {@code _homingBuildAppUrl}, into
     * the module JS whenever an app declares {@code AppLink} targets. It
     * cannot call {@code AppUrl}, because the code it writes runs in a
     * browser. So the invariant this rule defends is "one minter per
     * language", not "one minter" — and stating that plainly is better than a
     * pattern that quietly tolerates any literal appearing inside a string
     * that looks generated.</p>
     *
     * <p>This was expected to retire alongside {@code ParamsWriter}, as the
     * other half of RFC 0001's generated-client machinery. It did not.
     * ParamsWriter answered "what are my params", which the server now knows
     * and stamps, so it was deleted outright. NavWriter answers "what is the
     * URL of another app", which the server is not asked and which a page
     * still needs at click time — a different question that happens to share
     * an origin. So the exemption is standing, not pending, and the rule
     * reads "one minter per language" for as long as a page mints links of
     * its own.</p>
     */
    private static final String JS_MINTER = "NavWriter.java";

    private static final String LITERAL = "\"/app?app=";

    @Test
    void onlyAppUrlMintsTheFlatAddress() {
        Path root = reactorRoot();
        var offenders = new ArrayList<String>();

        for (Path java : mainSources(root)) {
            String name = java.getFileName().toString();
            if (name.equals(MINTER) || name.equals(JS_MINTER)) continue;
            List<String> lines;
            try {
                lines = Files.readAllLines(java, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            boolean inBlockComment = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                String code = stripComment(line, inBlockComment);
                inBlockComment = tracksInto(line, inBlockComment);
                if (code.contains(LITERAL)) {
                    offenders.add(root.relativize(java) + ":" + (i + 1) + "  " + lines.get(i).strip());
                }
            }
        }

        assertEquals(List.of(), offenders,
                "the flat /app?app= address is minted only by AppUrl — call AppUrl.flat "
              + "(the codec-taking overload where an AppModule is reachable)");
    }

    /**
     * The scan must actually have looked at something. Without this the test
     * passes vacuously the moment the layout moves, which is the failure mode
     * of every source-walking check.
     */
    @Test
    void theScanReachesTheMinterItself() {
        Path root = reactorRoot();
        var minters = mainSources(root).stream()
                .filter(p -> p.getFileName().toString().equals(MINTER))
                .toList();
        assertEquals(1, minters.size(), "expected exactly one AppUrl.java under " + root);

        String text;
        try {
            text = Files.readString(minters.get(0), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        assertTrue(text.contains(LITERAL),
                "AppUrl.java must contain the literal this rule exempts it for — "
              + "if it does not, the rule is guarding a spelling nothing uses");
    }

    /**
     * Comments are not minting. Several actions document the JSON they emit,
     * quoting the address shape verbatim — {@code "url": "/app?app=doc-reader&doc=<uuid>"}
     * — and that documentation is worth more than a rule that forbids naming
     * the thing being described. Only code counts.
     */
    private static String stripComment(String line, boolean inBlockComment) {
        if (inBlockComment) {
            int end = line.indexOf("*/");
            return end < 0 ? "" : line.substring(end + 2);
        }
        int block = line.indexOf("/*");
        int eol   = line.indexOf("//");
        if (block >= 0 && (eol < 0 || block < eol)) {
            int end = line.indexOf("*/", block + 2);
            return end < 0 ? line.substring(0, block)
                           : line.substring(0, block) + line.substring(end + 2);
        }
        return eol >= 0 ? line.substring(0, eol) : line;
    }

    /** Whether the line leaves us inside a block comment. */
    private static boolean tracksInto(String line, boolean inBlockComment) {
        int at = 0;
        boolean in = inBlockComment;
        while (at < line.length()) {
            if (in) {
                int end = line.indexOf("*/", at);
                if (end < 0) return true;
                in = false; at = end + 2;
            } else {
                int start = line.indexOf("/*", at);
                if (start < 0) return false;
                in = true; at = start + 2;
            }
        }
        return in;
    }

    /** The directory holding the module list — found by walking up from this module. */
    private static Path reactorRoot() {
        Path at = Path.of("").toAbsolutePath();
        for (int up = 0; up < 4 && at != null; up++, at = at.getParent()) {
            if (Files.isDirectory(at.resolve("homing-core"))) return at;
        }
        throw new IllegalStateException(
                "reactor root not found above " + Path.of("").toAbsolutePath());
    }

    private static List<Path> mainSources(Path root) {
        try (Stream<Path> all = Files.walk(root)) {
            return all.filter(Files::isRegularFile)
                      .filter(p -> p.getFileName().toString().endsWith(".java"))
                      .filter(p -> p.toString().replace(java.io.File.separatorChar, '/').contains("/src/main/java/"))
                      .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
