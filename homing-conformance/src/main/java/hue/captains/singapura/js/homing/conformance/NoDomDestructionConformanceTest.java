package hue.captains.singapura.js.homing.conformance;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Base class for "no DOM-destruction" conformance tests — operational
 * enforcement of the
 * <a href="#">Workspace Is the Substrate</a> doctrine's rule that
 * branch-owned DOM must never leave the document tree.
 *
 * <h2>What's banned</h2>
 *
 * <p>Wholesale-wipe patterns that detach every descendant in one stroke:</p>
 * <ul>
 *   <li>{@code .innerHTML = ""}, {@code .innerHTML = ''},
 *       {@code .innerHTML = \`\`} — the canonical violation.</li>
 *   <li>{@code .textContent = ""} / {@code = ''} / {@code = \`\`} — same
 *       structural effect (clears all children, detaches them).</li>
 *   <li>{@code .replaceChildren()} called with zero arguments — clears
 *       children, semantically equivalent to the above.</li>
 * </ul>
 *
 * <p>What's <i>not</i> banned: targeted {@code removeChild(specificNode)},
 * targeted {@code replaceChildren(node1, node2, ...)} with arguments,
 * and assignments of non-empty values to {@code innerHTML} /
 * {@code textContent}. Those don't have the "detach everything in one
 * stroke" property; they're surgical.</p>
 *
 * <h2>Why it matters</h2>
 *
 * <p>{@link DomModule} JS lives inside the framework's substrate layer
 * (workspace chrome, layout primitives, viewers). DOM elements there are
 * typically branch-owned (created via {@code branch.createElement}); the
 * branch keeps them alive in JS even after detach, but browsers reset
 * UI state (scroll position, focus, in-flight CSS animations, audio
 * playback, iframe documents, observer entries) the moment an element
 * leaves the document tree. A single wholesale wipe at the substrate
 * layer therefore costs UI state on every widget × every interaction ×
 * every use, forever.</p>
 *
 * <p>The fix is structural — express layout / content changes as in-place
 * tree mutations (single sync {@code appendChild} between two attached
 * parents) or targeted node removals. The b.2j SplitPane refactor is
 * the canonical worked example.</p>
 *
 * <h2>Allowlist guidance</h2>
 *
 * <p>The allowlist exists for modules that genuinely have a reason to
 * wipe a container they fully own, and whose contents are not branch-owned
 * widget DOM. Document the rationale in the subclass's allowlist comment;
 * an entry without justification is a smell, not an exemption.</p>
 *
 * @since b.2j — operational enforcement of the "never detach branch-owned
 *        DOM" doctrine
 */
public abstract class NoDomDestructionConformanceTest {

    /**
     * The patterns we reject. Each captures one canonical wholesale-wipe
     * spelling. Source lines are scanned individually; line-prefix
     * comments ({@code //}) are stripped before matching so commentary
     * about the ban itself doesn't trip the scanner.
     */
    private static final List<Pattern> DESTRUCTION_PATTERNS = List.of(
            // .innerHTML = "" / '' / ``  (with optional whitespace anywhere)
            Pattern.compile("\\.innerHTML\\s*=\\s*([\"'`])\\s*\\1"),
            // .textContent = "" / '' / ``
            Pattern.compile("\\.textContent\\s*=\\s*([\"'`])\\s*\\1"),
            // .replaceChildren()  — zero-arg form is the wipe
            Pattern.compile("\\.replaceChildren\\s*\\(\\s*\\)")
    );

    /**
     * Strip both {@code //} line comments AND {@code /&#42; ... &#42;/}
     * block comments (which may span multiple lines) from the source
     * before pattern matching. Commentary describing the ban — in
     * framework code, in Javadoc-style blocks, in this very file — must
     * not trip the scanner. String literals containing comment markers
     * are not handled with full fidelity; if a legitimate code line
     * trips the scanner because of one, add the module to the allowlist
     * with a justification.
     *
     * <p>Returns a list of the input lines with all comment text
     * replaced by spaces (preserving line numbers + column positions
     * for diagnostic accuracy).</p>
     */
    private static List<String> stripComments(List<String> lines) {
        var out = new java.util.ArrayList<String>(lines.size());
        boolean inBlock = false;
        for (String raw : lines) {
            var sb = new StringBuilder(raw.length());
            int i = 0;
            while (i < raw.length()) {
                if (inBlock) {
                    // Scan for end of block comment.
                    int end = raw.indexOf("*/", i);
                    if (end < 0) {
                        // Block continues past this line.
                        while (i < raw.length()) { sb.append(' '); i++; }
                    } else {
                        for (int j = i; j < end + 2; j++) sb.append(' ');
                        i = end + 2;
                        inBlock = false;
                    }
                } else if (i + 1 < raw.length() && raw.charAt(i) == '/' && raw.charAt(i + 1) == '/') {
                    // Line comment — blank rest of line.
                    while (i < raw.length()) { sb.append(' '); i++; }
                } else if (i + 1 < raw.length() && raw.charAt(i) == '/' && raw.charAt(i + 1) == '*') {
                    // Enter block comment.
                    inBlock = true;
                    sb.append(' ').append(' ');
                    i += 2;
                } else {
                    sb.append(raw.charAt(i));
                    i++;
                }
            }
            out.add(sb.toString());
        }
        return out;
    }

    /** All DomModule instances whose JS resource should be scanned. */
    protected abstract List<DomModule<?>> domModules();

    /**
     * Module classes exempted from the scan. Override to allowlist
     * specific modules; document the reason inline. An undocumented
     * allowlist entry is a smell.
     */
    protected Set<Class<? extends DomModule<?>>> allowList() {
        return Set.of();
    }

    @TestFactory
    Stream<DynamicTest> noDomDestructionInDomModules() {
        return domModules().stream()
                .filter(m -> !allowList().contains(m.getClass()))
                .map(m -> DynamicTest.dynamicTest(
                        m.getClass().getSimpleName() + " must not detach widget DOM via wholesale wipe",
                        () -> assertNoDomDestruction(m)
                ));
    }

    private void assertNoDomDestruction(DomModule<?> module) {
        String basePath = "homing/js/" + module.getClass().getCanonicalName().replace(".", "/") + ".js";
        List<String> lines;
        try {
            lines = ResourceReader.INSTANCE.getStringsFromResource(basePath);
        } catch (RuntimeException e) {
            return; // no JS resource — nothing to check
        }

        var stripped = stripComments(lines);
        var violations = new java.util.ArrayList<String>();
        for (int i = 0; i < lines.size(); i++) {
            String line = stripped.get(i);
            for (Pattern p : DESTRUCTION_PATTERNS) {
                if (p.matcher(line).find()) {
                    violations.add("  line " + (i + 1) + ": " + lines.get(i).strip() + "  [" + p.pattern() + "]");
                }
            }
        }

        if (!violations.isEmpty()) {
            throw new AssertionError(
                    module.getClass().getSimpleName() + ".js contains banned wholesale-wipe DOM operations. "
                    + "Branch-owned widget DOM must never leave the document tree (see the Workspace Is "
                    + "the Substrate doctrine). Replace wholesale wipes with targeted removeChild of "
                    + "specific subtrees, or in-place tree mutation (single-move appendChild between "
                    + "two attached parents).\n"
                    + String.join("\n", violations)
            );
        }
    }
}
