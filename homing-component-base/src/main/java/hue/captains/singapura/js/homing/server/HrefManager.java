package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.*;

import java.util.List;

/**
 * Framework-level EsModule that provides typed {@code href} operations.
 *
 * <p>Auto-injected into any DomModule that imports an {@link hue.captains.singapura.js.homing.core.AppLink},
 * mirroring how {@link CssClassManager} is auto-injected for modules with CSS groups.
 * The injected identifier is {@code href}; its API is the six methods documented
 * in RFC 0001 §4.0.1: {@code toAttr}, {@code set}, {@code create}, {@code openNew},
 * {@code navigate}, {@code fragment}.</p>
 *
 * <p>Introduced in RFC 0001 Step 09. Step 10 will enforce — via a conformance
 * scanner — that this is the <em>only</em> way href operations are constructed
 * in user JS.</p>
 */
public record HrefManager() implements EsModule<HrefManager> {

    public static final HrefManager INSTANCE = new HrefManager();

    public record HrefManagerInstance() implements Exportable._Constant<HrefManager> {}

    @Override
    public ImportsFor<HrefManager> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<HrefManager> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new HrefManagerInstance()));
    }
}
