package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * RFC 0045 — SVG styling goes through CSS like everything else. Setting a
 * <b>presentation attribute</b> ({@code setAttribute('fill', …)},
 * {@code setAttribute('stroke', …)}, …) bakes the value into the element and
 * bypasses the theme exactly as {@code element.style} does — and it is invisible
 * to {@link NoInlineStyleRule}, which only sees {@code .style.x =} and
 * {@code setAttribute('style', …)}. This rule closes that gap: a styling
 * attribute must be a CSS property on a typed class, not a per-element attribute.
 *
 * <p>Only <b>styling</b> attributes match; <b>geometry</b> attributes ({@code x},
 * {@code y}, {@code width}, {@code d}, {@code viewBox}, {@code transform}, …) are
 * structure, have no CSS-class equivalent in practice, and are never flagged.
 * Comment-stripped and ungated, like the other value-level rules.</p>
 */
public record NoPresentationAttributeRule() implements JsRule {

    public static final NoPresentationAttributeRule INSTANCE = new NoPresentationAttributeRule();

    // setAttribute('fill'|'stroke'|... ) — a styling attribute as the first arg.
    // Geometry (x/y/width/height/cx/cy/r/d/viewBox/points/transform/preserveAspectRatio)
    // is deliberately excluded.
    private static final Pattern PRESENTATION_ATTR = Pattern.compile(
            "\\.setAttribute\\(\\s*['\"]("
                    + "fill|fill-opacity|fill-rule|stroke|stroke-width|stroke-dasharray|stroke-dashoffset|"
                    + "stroke-linecap|stroke-linejoin|stroke-opacity|stroke-miterlimit|opacity|color|"
                    + "font-size|font-family|font-weight|font-style|text-anchor|dominant-baseline|"
                    + "letter-spacing|paint-order|shape-rendering"
                    + ")['\"]");

    @Override public RuleId      id()     { return new RuleId("no-presentation-attribute"); }
    @Override public String      intent() { return "SVG styling must go through CSS classes, not presentation attributes - no setAttribute('fill'|'stroke'|...); geometry attributes are exempt."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("typed-css-not-inline"); }

    @Override
    public List<Finding> check(ServedModule module) {
        var findings = new ArrayList<Finding>();
        List<String> lines = JsText.stripComments(module.lines());
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (PRESENTATION_ATTR.matcher(line).find()) {
                findings.add(new Finding(module.moduleClass(), id(),
                        "SVG presentation attribute (style via a CSS class, not setAttribute): " + line.trim(), i));
            }
        }
        return List.copyOf(findings);
    }
}
