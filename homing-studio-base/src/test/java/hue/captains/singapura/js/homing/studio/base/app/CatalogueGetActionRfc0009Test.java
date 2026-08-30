package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0009 validation — verifies that {@link CatalogueGetAction} serialises
 * each sub-catalogue typed {@code badge()} into the resolved row {@code badge}
 * JSON field, and prefixes each breadcrumb crumb's {@code name} with the
 * catalogue's {@code icon()} glyph when non-empty. Backward-compat: a
 * catalogue that doesn't override either method emits the framework default
 * (badge = {@code "CATALOGUE"}, name unprefixed).
 */
class CatalogueGetActionRfc0009Test {

    record HomeCatalogue() implements L0_Catalogue<HomeCatalogue> {
        public static final HomeCatalogue INSTANCE = new HomeCatalogue();
        @Override public String name() { return "Home"; }
        @Override public String icon() { return "🏠"; }
        @Override public List<? extends L1_Catalogue<HomeCatalogue, ?>> subCatalogues() {
            return List.of(BrandedChildCatalogue.INSTANCE, PlainChildCatalogue.INSTANCE);
        }
    }

    /** Overrides both badge() and icon() — RFC 0009 happy path. */
    record BrandedChildCatalogue() implements L1_Catalogue<HomeCatalogue, BrandedChildCatalogue> {
        public static final BrandedChildCatalogue INSTANCE = new BrandedChildCatalogue();
        @Override public HomeCatalogue parent() { return HomeCatalogue.INSTANCE; }
        @Override public String name()  { return "Studios"; }
        @Override public String badge() { return "STUDIO"; }
        @Override public String icon()  { return "🎬"; }
    }

    /** Overrides neither — falls back to defaults. */
    record PlainChildCatalogue() implements L1_Catalogue<HomeCatalogue, PlainChildCatalogue> {
        public static final PlainChildCatalogue INSTANCE = new PlainChildCatalogue();
        @Override public HomeCatalogue parent() { return HomeCatalogue.INSTANCE; }
        @Override public String name() { return "Plain"; }
    }

    private static final DocRegistry NO_DOCS = new DocRegistry(List.of());

    private CatalogueRegistry registry() {
        var brand = new StudioBrand("Test", HomeCatalogue.class);
        return new CatalogueRegistry(brand, NO_DOCS,
                List.of(HomeCatalogue.INSTANCE,
                        BrandedChildCatalogue.INSTANCE,
                        PlainChildCatalogue.INSTANCE));
    }

    private String serialize(Catalogue<?> c) throws Exception {
        var action = new CatalogueGetAction(registry());
        var body = action.execute(new CatalogueGetAction.Query(c.getClass().getName(), null),
                                   new EmptyParam.NoHeaders()).get().body();
        return body;
    }

    @Test
    void card_carriesOverriddenBadge() throws Exception {
        // Serialising the HOME catalogue produces card entries for its two children.
        // RFC 0053: the badge rides in the row the tree resolves, not in a tile.
        // Same behaviour, one field along - the per-instance override is what is
        // under test, not where the serializer happens to put it.
        String body = serialize(HomeCatalogue.INSTANCE);

        assertTrue(body.contains("\"badge\":\"STUDIO\""),
                "Branded child badge() should flow into its resolved row. Body: " + body);
        assertTrue(body.contains("\"badge\":\"CATALOGUE\""),
                "Plain child should fall back to the default \"CATALOGUE\" badge. Body: " + body);
    }

    @Test
    void breadcrumb_prefixesWithIconWhenPresent() throws Exception {
        // Serialising the BRANDED CHILD produces a breadcrumb chain:
        //   [HomeCatalogue (icon: 🏠), BrandedChildCatalogue (icon: 🎬)]
        // Both crumbs should have their name prefixed with the icon glyph.
        String body = serialize(BrandedChildCatalogue.INSTANCE);

        assertTrue(body.contains("\"name\":\"🏠 Home\""),
                "Home crumb should carry the icon prefix. Body: " + body);
        assertTrue(body.contains("\"name\":\"🎬 Studios\""),
                "Branded child's crumb should carry the icon prefix. Body: " + body);
    }

    @Test
    void breadcrumb_leavesNameUntouchedWhenIconEmpty() throws Exception {
        // Plain child has no icon — its crumb name should be unmodified.
        String body = serialize(PlainChildCatalogue.INSTANCE);

        assertTrue(body.contains("\"name\":\"Plain\""),
                "Plain crumb (no icon override) should carry just the name. Body: " + body);
        assertFalse(body.contains("\" Plain\""),
                "Plain crumb should not have a leading-space artefact. Body: " + body);
    }
}
