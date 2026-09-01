package hue.captains.singapura.js.homing.studio.base.tree;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.AppUrl;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.SelfContent;

import java.util.List;

/**
 * RFC 0053 — the catalogue as a TREE, rendered from the normalized tree rather
 * than from the routing index. URL: {@code /app?app=catalogue-tree}.
 *
 * <p>This is the second listing mode the catalogue is heading for — cards and
 * tree — built standalone first so it can be proved before anything depends on
 * it. It reads {@code /catalogue-parity}, which walks
 * {@link CatalogueNormalizer}'s forest and, for every vertex, compares the path
 * DERIVED from the segment chain against the AUTHENTIC path the live registry
 * gives. Each row links to the latter.</p>
 *
 * <p><b>Nothing here is on the resolution path.</b> {@code /cat} continues to
 * resolve through {@code CatalogueRegistry.childIndex}; the normalized layer feeds
 * display only. So the new derivation can be read, linked and counted against the
 * old one in public, with the risky swap deferred until it has been.</p>
 *
 * <p>Paramless: the whole forest, always. A vertex worth looking at closely has
 * its own address already, which is the point being demonstrated.</p>
 *
 * @since RFC 0053
 */
public record CatalogueTreeView()
        implements AppModule<AppModule._None, CatalogueTreeView>, SelfContent {

    record appMain() implements AppModule._AppMain<AppModule._None, CatalogueTreeView> {}

    public record link() implements AppLink<CatalogueTreeView> {}

    public static final CatalogueTreeView INSTANCE = new CatalogueTreeView();

    public static final ParamCodec<AppModule._None> CODEC =
            ParamCodec.ofEmpty(() -> AppModule._None.INSTANCE);

    /** The canonical URL for the tree listing. */
    public static String url() {
        return AppUrl.flat(INSTANCE, AppModule._None.INSTANCE);
    }

    @Override public Class<AppModule._None> paramsType()    { return AppModule._None.class; }
    @Override public ParamCodec<AppModule._None> paramCodec() { return CODEC; }

    @Override public String simpleName() { return "catalogue-tree"; }
    @Override public String title()      { return "catalogue tree"; }

    @Override
    public ImportsFor<CatalogueTreeView> imports() {
        return ImportsFor.<CatalogueTreeView>builder()
                .add(new ModuleImports<>(
                        List.of(new CatalogueTreeViewRenderer.renderCatalogueTreeView()),
                        CatalogueTreeViewRenderer.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CatalogueTreeView> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }

    @Override
    public List<String> selfContent(ModuleNameResolver nameResolver) {
        return List.of(
                "function appMain(rootElement) {",
                "    rootElement.replaceChildren(renderCatalogueTreeView());",
                "}"
        );
    }
}
