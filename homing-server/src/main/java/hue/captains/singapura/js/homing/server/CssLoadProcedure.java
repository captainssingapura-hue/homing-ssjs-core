package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.*;

import java.util.List;

/**
 * RFC 0064 — the CSS manager's affiliate: how sheets arrive. One procedure
 * for a first load and a theme switch alike: plan the waves, append every
 * node's sheet under {@code media="not all"} wave by wave — a wave starts
 * only when the previous has landed, a node in flight is shared rather than
 * appended twice — then flip everything in one synchronous pass, and for a
 * switch retire the outgoing theme. A failed fetch aborts with nothing
 * applied. Reaches the DOM only through the link factory the manager
 * injects, so it tests without a document.
 */
public record CssLoadProcedure() implements EsModule<CssLoadProcedure> {

    public static final CssLoadProcedure INSTANCE = new CssLoadProcedure();

    public record createCssLoadProcedure() implements Exportable._Constant<CssLoadProcedure> {}

    @Override
    public ImportsFor<CssLoadProcedure> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<CssLoadProcedure> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new createCssLoadProcedure()));
    }
}
