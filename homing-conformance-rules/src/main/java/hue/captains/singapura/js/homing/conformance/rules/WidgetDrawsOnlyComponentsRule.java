package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Designed by construction (the widget doctrine): a widget draws nothing but components - the
 * components it constructs, the sub-widgets it hosts - so it is designed with no design of its own.
 * It makes no element, holds and touches none, wears no class and writes no style or text of its own:
 * a simple component makes its own element with the single-use maker its owner hands it ({@code
 * branch.elementMaker(name)}), a composed one mints its own tree on the sub-branch it is given, and a
 * widget's arrangement is a designed container's.
 *
 * <p>For modules of the {@code WIDGET} type, which is inferred from a module's export and never
 * declared. Flags, in the served text with its comments stripped, a line that makes an element (any
 * {@code createElement}, a companion factory's {@code create}, markup, a clone, the {@code document}),
 * that places, queries or removes one, or that touches its classes, attributes, style or text, or a
 * component's element ({@code .el}). What a widget may do is make sub-branches, construct components
 * and sub-widgets, use their API, and join parties.</p>
 */
public record WidgetDrawsOnlyComponentsRule() implements JsRule {

    public static final WidgetDrawsOnlyComponentsRule INSTANCE = new WidgetDrawsOnlyComponentsRule();

    /** What a widget may not do, each with what it says when it is done. */
    private record Signal(Pattern pattern, String says) {}

    private static final List<Signal> SIGNALS = List.of(
            new Signal(Pattern.compile("\\bcreateElement(?:NS)?\\b"), "makes an element"),
            new Signal(Pattern.compile("\\b[A-Z][A-Za-z0-9]*Factory\\.create\\b"), "makes an element by a companion: hand the component its maker"),
            new Signal(Pattern.compile("\\bcreate(?:TextNode|DocumentFragment)\\b"), "makes a node"),
            new Signal(Pattern.compile("\\.cloneNode\\b"), "clones an element"),
            new Signal(Pattern.compile("\\.(?:inner|outer)HTML\\b|\\.insertAdjacent(?:HTML|Element|Text)\\b"), "writes markup"),
            new Signal(Pattern.compile("\\bdocument\\b"), "reaches the document"),
            new Signal(Pattern.compile("\\.(?:appendChild|removeChild|replaceChild|replaceChildren|insertBefore|replaceWith)\\b|\\.(?:append|prepend|before|after)\\("), "places an element"),
            new Signal(Pattern.compile("\\.querySelector(?:All)?\\b|\\bgetElement(?:ById|sBy[A-Za-z]+)\\b"), "queries the DOM"),
            new Signal(Pattern.compile("\\.classList\\b|\\.className\\b|\\bcss\\.(?:addClass|removeClass|toggleClass|setClass|cls)\\b"), "wears a class of its own"),
            new Signal(Pattern.compile("\\.(?:setAttribute|removeAttribute|toggleAttribute)\\b|\\.dataset\\b"), "writes an attribute"),
            new Signal(Pattern.compile("\\.style\\b"), "writes a style"),
            new Signal(Pattern.compile("\\.(?:textContent|innerText)\\b"), "writes text of its own: words go in text components"),
            new Signal(Pattern.compile("\\.el\\b"), "touches a component's element"));

    @Override public RuleId      id()     { return new RuleId("widget-draws-only-components"); }
    @Override public String      intent() { return "A widget draws nothing but components: it makes, holds and touches no element, and wears no class and writes no style or text of its own."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("widget-is-an-operational-unit"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        List<String> raw = module.lines();
        List<String> stripped = JsText.stripComments(raw);
        for (int i = 0; i < stripped.size(); i++) {
            for (Signal s : SIGNALS) {
                Matcher m = s.pattern().matcher(stripped.get(i));
                if (m.find()) {
                    findings.add(new Finding(module.moduleClass(), id(),
                            "a widget draws only components - it " + s.says() + " (" + m.group() + "): " + raw.get(i).trim(), i));
                    break;
                }
            }
        }
        return List.copyOf(findings);
    }
}
