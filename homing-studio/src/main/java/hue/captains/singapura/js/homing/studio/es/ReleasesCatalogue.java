package hue.captains.singapura.js.homing.studio.es;

import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocProvider;
import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L1_Catalogue;
import hue.captains.singapura.js.homing.studio.docs.releases.Release0_0_100Doc;
import hue.captains.singapura.js.homing.studio.docs.releases.Release0_0_101Doc;
import hue.captains.singapura.js.homing.studio.docs.releases.Release0_0_110Doc;
import hue.captains.singapura.js.homing.studio.docs.releases.Release0_0_11Doc;

import java.util.List;

/**
 * Sub-catalogue listing every released version of the framework, newest first.
 *
 * <p>Each entry is a typed {@code Doc} carrying the release notes — UUIDs are
 * stable across renames, content is plain markdown, summaries flow from the
 * Doc itself. The pattern set here is the precedent for every subsequent
 * release: one {@code Release<X_Y_Z>Doc} record + matching {@code .md} file
 * on the classpath, prepended to the list below.</p>
 *
 * <p>Versions before 0.0.11 shipped without formal release notes; they're
 * not retroactively documented. The first explicit release on record is
 * 0.0.11.</p>
 */
public record ReleasesCatalogue()
        implements L1_Catalogue<StudioCatalogue, ReleasesCatalogue>, DocProvider {

    public static final ReleasesCatalogue INSTANCE = new ReleasesCatalogue();

    @Override public StudioCatalogue parent() { return StudioCatalogue.INSTANCE; }
    @Override public String name()    { return "Releases"; }
    @Override public String summary() { return "Release notes for every shipped version of Homing — newest first. Each release lists what changed, what shipped, what's compatible, and what's next."; }
    @Override public String badge()   { return "RELEASE"; }
    @Override public String icon()    { return "🏷️"; }

    @Override public List<Entry<ReleasesCatalogue>> leaves() {
        // Newest first. Prepend new releases here.
        return List.of(
                Entry.of(this, Release0_0_110Doc.INSTANCE),
                Entry.of(this, Release0_0_101Doc.INSTANCE),
                Entry.of(this, Release0_0_100Doc.INSTANCE),
                Entry.of(this, Release0_0_11Doc.INSTANCE)
        );
    }

    /** {@link DocProvider} contribution — releases-catalogue docs feed
     *  the studio's DocRegistry so DocReader can serve them by UUID. */
    @Override public List<Doc> docs() {
        return List.of(
                Release0_0_110Doc.INSTANCE,
                Release0_0_101Doc.INSTANCE,
                Release0_0_100Doc.INSTANCE,
                Release0_0_11Doc.INSTANCE
        );
    }
}
