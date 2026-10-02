package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.server.ThemeRegistry;
import hue.captains.singapura.js.homing.site.Placed;
import hue.captains.singapura.js.homing.site.Site;
import hue.captains.singapura.js.homing.site.mpa.Brand;
import hue.captains.singapura.js.homing.site.mpa.Mpa;
import hue.captains.singapura.tao.http.action.ActionRegistry;
import io.vertx.ext.web.RoutingContext;

/**
 * The MPA a tree is read with when no site handed one in: every question put
 * to it is refused, and says why - a catalogue that makes its pages with the
 * site's MPA was read into a tree that has none. A tree of pages that need no
 * site never asks it anything.
 */
final class NoMpa implements Mpa {

    static final NoMpa INSTANCE = new NoMpa();

    private NoMpa() {}

    private static IllegalStateException refused() {
        return new IllegalStateException("A catalogue made its pages with the site's MPA, but this tree was read without one:"
                + " read it with CatalogueTree.of(root, mpa) - or CatalogueRouter.at(mount, root, mpa)");
    }

    @Override public Brand brand() { throw refused(); }
    @Override public ThemeRegistry themes() { throw refused(); }
    @Override public String moduleUrl(EsModule<?> module) { throw refused(); }
    @Override public <P extends AppModule._Param, M extends AppModule<P, M>> Placed page(M app, P params) { throw refused(); }
    @Override public ActionRegistry<RoutingContext> registry(Site site) { throw refused(); }

    @Override public String toString() { return "no MPA"; }
}
