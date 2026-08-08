package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RFC 0044 Phase 7 — the strict counterpart of {@link NoDomDestructionRule}, for
 * modules classified {@link hue.captains.singapura.js.homing.core.JsModuleType#PURE_LOGIC}
 * or {@link hue.captains.singapura.js.homing.core.JsModuleType#SECRETARY}: a
 * headless logic/data module (reducer, codec, store, worker, registry) must
 * touch <b>no</b> DOM at all. Where a DOM owner is merely forbidden the
 * wholesale wipe, these modules are forbidden any DOM reference whatsoever —
 * the classification is a promise the module keeps to the rest of the system.
 *
 * <p>Flags the unambiguous DOM signals in the served text (comments stripped
 * first): the {@code document} global, element mutation ({@code innerHTML},
 * {@code appendChild}, {@code replaceChildren}, …), factories
 * ({@code createElement}, …), queries ({@code querySelector},
 * {@code getElementById}, …), and attribute/class/style access. Deliberately
 * <i>not</i> flagged are the ambiguous, non-DOM-exclusive members —
 * {@code addEventListener} (WebSocket, worker, AbortSignal) and the worker
 * globals ({@code self}, {@code postMessage}) — so a legitimate store or worker
 * is not tripped by them.</p>
 */
public record NoDomAccessRule() implements JsRule {

    public static final NoDomAccessRule INSTANCE = new NoDomAccessRule();

    /** Each entry: a short token name and the pattern that spots it. */
    private static final List<Pattern> DOM_SIGNALS = List.of(
            Pattern.compile("\\bdocument\\b"),
            Pattern.compile("\\.innerHTML\\b"),
            Pattern.compile("\\.outerHTML\\b"),
            Pattern.compile("\\.textContent\\b"),
            Pattern.compile("\\.innerText\\b"),
            Pattern.compile("\\.appendChild\\b"),
            Pattern.compile("\\.removeChild\\b"),
            Pattern.compile("\\.replaceChild(?:ren)?\\b"),
            Pattern.compile("\\.insertBefore\\b"),
            Pattern.compile("\\bcreateElement\\b"),
            Pattern.compile("\\bcreateTextNode\\b"),
            Pattern.compile("\\bcreateDocumentFragment\\b"),
            Pattern.compile("\\.querySelector(?:All)?\\b"),
            Pattern.compile("\\bgetElementById\\b"),
            Pattern.compile("\\bgetElementsBy[A-Za-z]+\\b"),
            Pattern.compile("\\.classList\\b"),
            Pattern.compile("\\.setAttribute\\b"),
            Pattern.compile("\\.getAttribute\\b"),
            Pattern.compile("\\.style\\."));

    @Override public RuleId      id()     { return new RuleId("no-dom-access"); }
    @Override public String      intent() { return "A no-DOM module (secretary / pure logic) must not reference the DOM at all."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("logic-modules-touch-no-dom"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        List<String> raw = module.lines();
        List<String> stripped = JsText.stripComments(raw);
        for (int i = 0; i < stripped.size(); i++) {
            for (Pattern p : DOM_SIGNALS) {
                Matcher m = p.matcher(stripped.get(i));
                if (m.find()) {
                    findings.add(new Finding(module.moduleClass(), id(),
                            "forbidden DOM access (" + m.group() + "): " + raw.get(i).trim(), i));
                    break;
                }
            }
        }
        return List.copyOf(findings);
    }
}
