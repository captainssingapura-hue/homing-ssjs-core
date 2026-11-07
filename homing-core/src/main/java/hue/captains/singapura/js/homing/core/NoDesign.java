package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.js.homing.base.lang.StatelessSingleton;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;
import java.util.Optional;

/** The design of a server given none: it makes nothing. */
public record NoDesign() implements DesignRenderer, StatelessFunctionalObject, StatelessSingleton {

    public static final NoDesign INSTANCE = new NoDesign();

    @Override public Optional<List<String>> script(EsModule<?> module) { return Optional.empty(); }

    @Override public Optional<String> render(DerivedCss<?> sheet, String themeSlug) { return Optional.empty(); }

    @Override public boolean owns(DerivedCss<?> sheet) { return false; }
}
