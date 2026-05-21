package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.LegacyAppMain;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.SelfContent;

import java.util.List;

/**
 * Single shared {@link AppModule} that serves any registered {@link Catalogue}.
 *
 * <p>Per <a href="../../../../../../../../../../docs/rfcs/Rfc0005Doc.md">RFC 0005</a>
 * D1, catalogues are served through one {@code CatalogueAppHost} rather than one
 * AppModule per catalogue. URL contract:</p>
 *
 * <pre>/app?app=catalogue&id=&lt;class-fqn&gt;</pre>
 *
 * <p>The selfContent emits a JS body that fetches the resolved catalogue payload
 * from {@link CatalogueGetAction} ({@code /catalogue?id=<id>}) and invokes
 * {@link CatalogueHostRenderer}. Server-side resolution + client-side fetch =
 * one AppHost serving every registered catalogue.</p>
 *
 * @since RFC 0005
 */
@LegacyAppMain(reason = "Studio chrome / catalogue tile-grid; migration tracked as RFC 0024 successor work to the canonical spike.")
public record CatalogueAppHost() implements AppModule<CatalogueAppHost.Params, CatalogueAppHost>, SelfContent {

    record appMain() implements AppModule._AppMain<CatalogueAppHost.Params, CatalogueAppHost> {}

    public record link() implements AppLink<CatalogueAppHost> {}

    /**
     * @param id      class FQN of the {@link Catalogue} to render (server
     *                resolves via {@link CatalogueRegistry})
     * @param context optional framework-managed scoping tag — forwarded to
     *                {@link CatalogueGetAction} so the same catalogue class
     *                can render context-scoped variants (RFC 0014 per-studio
     *                diagnostics). {@code null} / missing = the catalogue's
     *                unscoped page.
     */
    public record Params(String id, String context) implements AppModule._Param {}

    public static final CatalogueAppHost INSTANCE = new CatalogueAppHost();

    /**
     * Build the canonical URL serving the given {@link Catalogue}. Used by any consumer
     * (other AppModules, downstream apps) that needs to link to a catalogue without
     * hand-building the path.
     */
    public static String urlFor(Class<? extends Catalogue<?>> catalogueClass) {
        return "/app?app=" + INSTANCE.simpleName() + "&id=" + catalogueClass.getName();
    }

    @Override public Class<Params> paramsType() { return Params.class; }

    @Override public String simpleName() { return "catalogue"; }

    /** Page-kind label. {@code AppHtmlGetAction} appends the downstream brand;
     *  the renderer refines it to {@code "<catalogue-name> · <brand>"} on load. */
    @Override public String title() { return "catalogue"; }

    @Override
    public ImportsFor<CatalogueAppHost> imports() {
        return ImportsFor.<CatalogueAppHost>builder()
                .add(new ModuleImports<>(List.of(new CatalogueHostRenderer.renderCatalogueHost()),
                        CatalogueHostRenderer.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CatalogueAppHost> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        return List.of(
                "function appMain(rootElement) {",
                "    rootElement.replaceChildren(renderCatalogueHost({",
                "        catalogueId: params.id,",
                "        context:     params.context",
                "    }));",
                "}"
        );
    }
}
