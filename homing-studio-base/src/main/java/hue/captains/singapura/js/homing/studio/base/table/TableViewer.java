package hue.captains.singapura.js.homing.studio.base.table;

import hue.captains.singapura.js.homing.core.AppLink;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Importable;
import hue.captains.singapura.js.homing.core.LegacyAppMain;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.studio.base.app.DocViewer;

import java.util.List;

/**
 * RFC 0020 — viewer AppModule for {@link TableDoc}. URL contract:
 *
 * <pre>/app?app=table-viewer&id=&lt;tabledoc-uuid&gt;</pre>
 *
 * <p>Extends {@link DocViewer} so the framework's standard chrome
 * (Header + brand + breadcrumb + theme picker) is composed automatically
 * (V11 axiom). The body delegates to
 * {@link TableViewerRenderer}'s {@code renderTable} JS function, which
 * fetches the table JSON and builds the {@code <table>}.</p>
 *
 * @since RFC 0020
 */
@LegacyAppMain(reason = "Chrome via DocViewer; body (113 JS lines) builds <table> from typed cells. Clean migration candidate.")
public final class TableViewer extends DocViewer<TableViewer.Params, TableViewer> {

    public static final TableViewer INSTANCE = new TableViewer();

    private TableViewer() {}

    /** @param id UUID of the TableDoc to render. */
    public record Params(String id) implements AppModule._Param {}

    private record appMain() implements AppModule._AppMain<Params, TableViewer> {}
    public record link() implements AppLink<TableViewer> {}

    @Override public String simpleName() { return "table-viewer"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public String title() { return "table"; }

    @Override
    protected AppModule._AppMain<Params, TableViewer> appMain() {
        return new appMain();
    }

    @Override
    protected List<ModuleImports<? extends Importable>> bodyImports() {
        return List.of(
                new ModuleImports<>(
                        List.of(new TableViewerRenderer.renderTable()),
                        TableViewerRenderer.INSTANCE)
        );
    }

    @Override
    protected List<String> bodyJs() {
        return List.of(
                "    renderTable({ docId: params.id, host: main });"
        );
    }
}
