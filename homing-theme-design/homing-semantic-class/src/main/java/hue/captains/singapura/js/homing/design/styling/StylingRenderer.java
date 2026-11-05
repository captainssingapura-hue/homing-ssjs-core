package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.core.DerivedCss;
import hue.captains.singapura.js.homing.core.DesignRenderer;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.tao.ontology.Mutable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * One styling as a server is given it: the modules and the sheets made from it, each handed the styling it holds. A
 * site builds one with its styling, and a conformance run with the styling it grades. A sheet walks every casting, so
 * each is made once under each slug and kept here for as long as the renderer lives.
 */
public final class StylingRenderer implements DesignRenderer, Mutable {

    /** A sheet under a slug, as it is kept. */
    private record Asked(DerivedCss<?> sheet, String slug) {}

    private final Styling styling;
    private final Map<Asked, String> sheets = new ConcurrentHashMap<>();

    /** The renderer of what is made from this styling. */
    public StylingRenderer(Styling styling) {
        this.styling = Objects.requireNonNull(styling, "StylingRenderer.styling");
    }

    @Override public Optional<List<String>> script(EsModule<?> module) {
        return module instanceof StyledModule styled ? Optional.of(styled.selfContent(styling)) : Optional.empty();
    }

    @Override public Optional<String> render(DerivedCss<?> sheet, String slug) {
        return sheet instanceof StyledSheet styled
                ? Optional.of(sheets.computeIfAbsent(new Asked(sheet, slug), asked -> styled.css(styling, slug)))
                : Optional.empty();
    }

    @Override public boolean owns(DerivedCss<?> sheet) { return sheet instanceof StyledSheet; }

    @Override public Optional<String> fallback(DerivedCss<?> sheet) {
        return sheet instanceof StyledSheet styled ? Optional.of(styled.fallback(styling)) : Optional.empty();
    }
}
