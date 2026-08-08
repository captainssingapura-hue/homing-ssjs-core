package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.*;
import hue.captains.singapura.js.homing.core.util.ReadContentFromResources;
import hue.captains.singapura.js.homing.core.util.ResourceReader;
import hue.captains.singapura.js.homing.core.util.SimpleImportsWriterResolver;
import hue.captains.singapura.js.homing.core.util.SvgGroupContentProvider;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class EsModuleGetAction
        implements GetAction<RoutingContext, ModuleQuery, EmptyParam.NoHeaders, JsModuleContent> {

    private final ModuleNameResolver nameResolver;
    private final ResourceReader resourceReader;

    public EsModuleGetAction(ModuleNameResolver nameResolver) {
        this(nameResolver, ResourceReader.INSTANCE);
    }

    public EsModuleGetAction(ModuleNameResolver nameResolver, ResourceReader resourceReader) {
        this.nameResolver = nameResolver;
        this.resourceReader = resourceReader;
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, ModuleQuery> queryStrMarshaller() {
        return ctx -> new ModuleQuery(
                ctx.request().getParam("class"),
                ctx.request().getParam("theme"),
                ctx.request().getParam("locale")
        );
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<JsModuleContent> execute(ModuleQuery query, EmptyParam.NoHeaders headers) {
        if (query.className() == null || query.className().isBlank()) {
            return CompletableFuture.failedFuture(ResourceNotFound.missingClass());
        }
        try {
            EsModule<?> module = resolveModule(query.className());
            return CompletableFuture.completedFuture(
                    new JsModuleContent(render(module, query.theme(), query.locale())));
        } catch (Exception e) {
            return CompletableFuture.failedFuture(ResourceNotFound.forClass(query.className(), e));
        }
    }

    /**
     * The complete served JavaScript for a module, produced in-process by the
     * SAME assembly the HTTP path uses — so a build-time consumer (conformance)
     * validates exactly what is served. {@code theme}/{@code locale} are the
     * request values (both {@code null} for a plain {@code /module?class=}
     * fetch — see {@link #render(EsModule)}).
     *
     * <p>BundledExternalModule short-circuit: a 3rd-party library bundled at
     * build time — ship the classpath bytes verbatim (no imports prefix, no
     * exports suffix, no css/href injection); the bundled file has its own
     * native exports.</p>
     */
    public String render(EsModule<?> module, String theme, String locale) {
        try {
            if (module instanceof BundledExternalModule<?> bundled) {
                return String.join("\n", bundled.content());
            }
            return String.join("\n", createWriter(module, theme, locale).writeModule());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("render failed: " + module.getClass().getName(), e);
        }
    }

    /** In-process render with default (null) theme/locale — a plain served module. */
    public String render(EsModule<?> module) {
        return render(module, null, null);
    }

    @SuppressWarnings("unchecked")
    private <M extends EsModule<M>> M resolveModule(String className) throws Exception {
        Class<?> clazz = Class.forName(className);
        try {
            var instanceField = clazz.getField("INSTANCE");
            return (M) instanceField.get(null);
        } catch (NoSuchFieldException e) {
            return (M) clazz.getDeclaredConstructor().newInstance();
        }
    }

    @SuppressWarnings("unchecked")
    private <M extends EsModule<M>> EsModuleWriter<M> createWriter(
            EsModule<?> resolvedModule, String theme, String locale
    ) throws Exception {
        M module = (M) resolvedModule;
        ContentProvider<M> contentProvider;
        if (module instanceof SvgGroup) {
            @SuppressWarnings("rawtypes")
            SvgGroup svg = (SvgGroup) module;
            contentProvider = (ContentProvider<M>) new SvgGroupContentProvider<>(svg, resourceReader);
        } else if (module instanceof CssGroup) {
            @SuppressWarnings("rawtypes")
            CssGroup css = (CssGroup) module;
            contentProvider = (ContentProvider<M>) new CssGroupContentProvider<>(css, theme, nameResolver);
        } else if (module instanceof SelfContent self) {
            // Generic self-providing module: the type emits its own JS body.
            // Used by DocGroup (in homing-studio-base) and any future self-contained types
            // — homing-server has no compile-time knowledge of which.
            contentProvider = () -> self.selfContent(nameResolver);
        } else {
            contentProvider = new ReadContentFromResources<>(module, theme, resourceReader);
        }

        if (module instanceof DomModule<?> dom && !dom.cssGroups().isEmpty()) {
            contentProvider = withCssManager(contentProvider);
        }

        // RFC 0001 Step 09: auto-inject the href manager when the module
        // imports any AppLink — same scoping rule as CSS injection.
        if (importsAnyAppLink(module)) {
            contentProvider = withHrefManager(contentProvider);
        }

        // Generic manager injection: each ManagerInjector source whose JS this
        // module imports gets `import { <export> as <bind> } from "<manager>"`
        // prepended to the consumer's body. Supports DocGroup (studio-base)
        // and any future opt-in source — no homing-server-side knowledge of which.
        for (ManagerInjector mi : collectManagerInjectors(module)) {
            contentProvider = withManager(contentProvider, mi);
        }

        return new EsModuleWriter<>(module, contentProvider, nameResolver,
                ExportWriter.INSTANCE, new SimpleImportsWriterResolver(nameResolver, theme, locale));
    }

    /** Package-private for testing — returns true iff the module has at least one AppLink import. */
    static boolean importsAnyAppLink(EsModule<?> module) {
        return module.imports().getAllImports().values().stream()
                .anyMatch(mi -> mi.allImports().stream().anyMatch(e -> e instanceof AppLink<?>));
    }

    /** Public for cross-module testing — distinct {@link ManagerInjector}s reachable through this module's imports. */
    public static List<ManagerInjector> collectManagerInjectors(EsModule<?> module) {
        return module.imports().getAllImports().keySet().stream()
                .filter(target -> target instanceof ManagerInjector)
                .map(target -> (ManagerInjector) target)
                .distinct()
                .toList();
    }

    private <M extends EsModule<M>> ContentProvider<M> withCssManager(ContentProvider<M> delegate) {
        String managerPath = nameResolver.resolve(CssClassManager.INSTANCE).basePath();
        String importLine = "import { CssClassManagerInstance as css } from \"" + managerPath + "\";";
        return () -> {
            List<String> combined = new ArrayList<>();
            combined.add(importLine);
            combined.add("");
            combined.addAll(delegate.content());
            return combined;
        };
    }

    private <M extends EsModule<M>> ContentProvider<M> withHrefManager(ContentProvider<M> delegate) {
        String managerPath = nameResolver.resolve(HrefManager.INSTANCE).basePath();
        String importLine = "import { HrefManagerInstance as href } from \"" + managerPath + "\";";
        return () -> {
            List<String> combined = new ArrayList<>();
            combined.add(importLine);
            combined.add("");
            combined.addAll(delegate.content());
            return combined;
        };
    }

    /**
     * Generic ManagerInjector wrapper — prepends one
     * {@code import { <exportName> as <bindName> } from "<managerPath>"} line.
     * Decoupled from any specific manager (Doc, CSS, etc.).
     */
    private <M extends EsModule<M>> ContentProvider<M> withManager(ContentProvider<M> delegate, ManagerInjector mi) {
        String managerPath = nameResolver.resolve(mi.manager()).basePath();
        String importLine = "import { " + mi.managerExportName() + " as " + mi.managerBindName()
                + " } from \"" + managerPath + "\";";
        return () -> {
            List<String> combined = new ArrayList<>();
            combined.add(importLine);
            combined.add("");
            combined.addAll(delegate.content());
            return combined;
        };
    }
}
