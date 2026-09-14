package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.studio.base.app.CatalogueAppHost;
import hue.captains.singapura.js.homing.studio.base.app.DocReader;
import hue.captains.singapura.js.homing.studio.base.app.SvgViewer;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedViewer;
import hue.captains.singapura.js.homing.studio.base.image.ImageViewer;
import hue.captains.singapura.js.homing.studio.base.table.TableViewer;
import hue.captains.singapura.js.homing.studio.base.theme.ThemePreview;
import hue.captains.singapura.js.homing.studio.base.theme.ThemesIntro;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanAppHost;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * RFC 0012 — the framework's default harness. Wraps any umbrella with the
 * standard four harness apps ({@link CatalogueAppHost}, {@link PlanAppHost},
 * {@link DocReader}, {@link ThemesIntro}) and a simple Solo/Group chrome
 * switch. Theme registry, default theme, and brand fall through to
 * {@link Fixtures}' defaults.
 *
 * <p>Downstream that wants extra apps writes its own {@code Fixtures<S>}
 * implementation — typically a record that delegates to
 * {@code DefaultFixtures} for the common parts:</p>
 *
 * <pre>{@code
 * public record MyFixtures(Umbrella<Studio<?>> umbrella) implements Fixtures<Studio<?>> {
 *     @Override public List<AppModule<?,?>> harnessApps() {
 *         var defaults = new DefaultFixtures<>(umbrella).harnessApps();
 *         return Stream.concat(defaults.stream(), Stream.of(MyApp.INSTANCE)).toList();
 *     }
 *     @Override public NodeChrome chromeFor(Umbrella<Studio<?>> node) { ... }
 * }
 * }</pre>
 *
 * @param <S> the studio type at the umbrella's leaves; usually {@code Studio<?>}
 */
public record DefaultFixtures<S extends Studio<?>>(
        Umbrella<S> umbrella) implements Fixtures<S>, ValueObject {

    public DefaultFixtures {
        Objects.requireNonNull(umbrella);
    }

    @Override
    public List<AppModule<?, ?>> harnessApps() {
        return List.of(
                CatalogueAppHost.INSTANCE,
                PlanAppHost.INSTANCE,
                DocReader.INSTANCE,
                ThemesIntro.INSTANCE,
                ThemePreview.INSTANCE,
                SvgViewer.INSTANCE,
                ComposedViewer.INSTANCE,
                TableViewer.INSTANCE,
                ImageViewer.INSTANCE
        );
    }

    @Override
    public NodeChrome chromeFor(Umbrella<S> node) {
        return switch (node) {
            case Umbrella.Solo<S> s   -> new NodeChrome("STUDIO",   s.studio().home().icon());
            case Umbrella.Group<S> g  -> new NodeChrome("CATEGORY", "📁");
        };
    }
}
