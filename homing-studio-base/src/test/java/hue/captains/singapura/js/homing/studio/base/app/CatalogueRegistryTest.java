package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.app.DocReader;

import hue.captains.singapura.js.homing.tree.NodeName;
import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.studio.base.Doc;
import hue.captains.singapura.js.homing.studio.base.DocRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link CatalogueRegistry}'s boot-time validations after
 * RFC 0005-ext2 + RFC 0011 (CRTP catalogue self-type, typed entries).
 */
class CatalogueRegistryTest {

    private static final UUID DOC_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");

    private static final Doc TEST_DOC = new Doc() {
        @Override public UUID   uuid()    { return DOC_ID; }
        @Override public String title()   { return "Test Doc"; }
        @Override public String contents(){ return "# Test"; }
    };

    record RootCatalogue() implements L0_Catalogue<RootCatalogue> {
        public static final RootCatalogue INSTANCE = new RootCatalogue();
        @Override public String name() { return "Root"; }
        @Override public List<? extends L1_Catalogue<RootCatalogue, ?>> subCatalogues() {
            return List.of(LeafCatalogue.INSTANCE);
        }
    }

    record LeafCatalogue() implements L1_Catalogue<RootCatalogue, LeafCatalogue> {
        public static final LeafCatalogue INSTANCE = new LeafCatalogue();
        @Override public RootCatalogue parent() { return RootCatalogue.INSTANCE; }
        @Override public String name()         { return "Leaf"; }
        @Override public List<Entry<LeafCatalogue>> leaves() {
            return List.of(Entry.of(this, DocReader.INSTANCE, new DocReader.Params(TEST_DOC.uuid().toString()), TEST_DOC));
        }
    }

    record BlankNameCatalogue() implements L0_Catalogue<BlankNameCatalogue> {
        @Override public String name() { return "  "; }
    }

    /** A non-L0 catalogue wired as the brand home-app to test the
     *  "home-app must be L0" check. */
    record NonL0AsHomeApp() implements L1_Catalogue<RootCatalogue, NonL0AsHomeApp> {
        public static final NonL0AsHomeApp INSTANCE = new NonL0AsHomeApp();
        @Override public RootCatalogue parent() { return RootCatalogue.INSTANCE; }
        @Override public String name() { return "Pretend-Home"; }
    }

    /** An L1 typed under OrphanRoot but never registered — used to drive the
     *  unregistered-sub-catalogue runtime check. */
    record UnregisteredChild() implements L1_Catalogue<OrphanRoot, UnregisteredChild> {
        public static final UnregisteredChild INSTANCE = new UnregisteredChild();
        @Override public OrphanRoot parent() { return OrphanRoot.INSTANCE; }
        @Override public String name() { return "Unregistered-Child"; }
    }

    /** L0 referencing an L1 child that's typed correctly but isn't in the
     *  registered list — the closure check should reject this at boot. */
    record OrphanRoot() implements L0_Catalogue<OrphanRoot> {
        public static final OrphanRoot INSTANCE = new OrphanRoot();
        @Override public String name() { return "Orphan-Root"; }
        @Override public List<? extends L1_Catalogue<OrphanRoot, ?>> subCatalogues() {
            return List.of(UnregisteredChild.INSTANCE);
        }
    }

    // ----- Tests -----

    private final DocRegistry docs = new DocRegistry(List.of(TEST_DOC));

    @Test
    void valid_simpleTree_constructsCleanly() {
        var brand = new StudioBrand("Test Studio", RootCatalogue.class);
        var registry = new CatalogueRegistry(brand, docs,
                List.of(RootCatalogue.INSTANCE, LeafCatalogue.INSTANCE));

        assertEquals(2, registry.size());
        assertSame(RootCatalogue.INSTANCE, registry.resolve(RootCatalogue.class));
        assertNull(registry.parentOf(RootCatalogue.class));
        assertSame(RootCatalogue.INSTANCE, registry.parentOf(LeafCatalogue.class));
    }

    @Test
    void breadcrumbs_walkUpFromLeafToRoot() {
        var brand = new StudioBrand("Test", RootCatalogue.class);
        var registry = new CatalogueRegistry(brand, docs,
                List.of(RootCatalogue.INSTANCE, LeafCatalogue.INSTANCE));

        var crumbs = registry.breadcrumbs(LeafCatalogue.class);
        assertEquals(2, crumbs.size());
        assertSame(RootCatalogue.INSTANCE, crumbs.get(0));
        assertSame(LeafCatalogue.INSTANCE, crumbs.get(1));
    }

    @Test
    void levelOf_returnsTypedLevel() {
        assertEquals(0, CatalogueRegistry.levelOf(RootCatalogue.INSTANCE));
        assertEquals(1, CatalogueRegistry.levelOf(LeafCatalogue.INSTANCE));
    }

    @Test
    void rejects_blankName() {
        var brand = new StudioBrand("Test", RootCatalogue.class);
        var ex = assertThrows(IllegalStateException.class,
                () -> new CatalogueRegistry(brand, docs,
                        List.of(RootCatalogue.INSTANCE, LeafCatalogue.INSTANCE, new BlankNameCatalogue())));
        assertTrue(ex.getMessage().contains("blank name"));
    }

    @Test
    void rejects_unregisteredSubCatalogue() {
        var brand = new StudioBrand("Test", OrphanRoot.class);
        var ex = assertThrows(IllegalStateException.class,
                () -> new CatalogueRegistry(brand, docs,
                        List.of(OrphanRoot.INSTANCE)));
        assertTrue(ex.getMessage().contains("not in the registered catalogue list"));
    }

    @Test
    void rejects_unregisteredDoc() {
        UUID strangerId = UUID.randomUUID();
        Doc strangerDoc = new Doc() {
            @Override public UUID   uuid()    { return strangerId; }
            @Override public String title()   { return "Stranger"; }
            @Override public String contents(){ return "x"; }
        };
        record StrangerCatalogue(Doc d) implements L0_Catalogue<StrangerCatalogue> {
            @Override public String name() { return "Stranger-Cat"; }
            @Override public List<Entry<StrangerCatalogue>> leaves() {
                return List.of(Entry.of(this, DocReader.INSTANCE, new DocReader.Params(d.uuid().toString()), d));
            }
        }
        var brand = new StudioBrand("Test", StrangerCatalogue.class);
        var ex = assertThrows(IllegalStateException.class,
                () -> new CatalogueRegistry(brand, docs,
                        List.of(new StrangerCatalogue(strangerDoc))));
        assertTrue(ex.getMessage().contains("not in the DocRegistry"));
    }

    @Test
    void rejects_brandHomeApp_notRegistered() {
        var brand = new StudioBrand("Test", RootCatalogue.class);
        var ex = assertThrows(IllegalStateException.class,
                () -> new CatalogueRegistry(brand, docs,
                        List.<Catalogue<?>>of()));
        assertTrue(ex.getMessage().contains("not in the registered catalogue list"));
    }

    @Test
    void rejects_brandHomeApp_notL0() {
        var brand = new StudioBrand("Test", NonL0AsHomeApp.class);
        var ex = assertThrows(IllegalStateException.class,
                () -> new CatalogueRegistry(brand, docs,
                        List.of(RootCatalogue.INSTANCE, NonL0AsHomeApp.INSTANCE)));
        assertTrue(ex.getMessage().contains("must be an L0_Catalogue"));
    }

    // ----- RFC 0051 Law 1, app half: identity is (app, args), not the framing -----

    /** Minimal AppModule so a Navigable can exist in this test. */
    public static final class DemoApp implements AppModule<AppModule._None, DemoApp> {
        public static final DemoApp INSTANCE = new DemoApp();
        private DemoApp() {}
        record appMain() implements AppModule._AppMain<AppModule._None, DemoApp> {}
        @Override public String simpleName() { return "demo-app"; }
        @Override public String title()      { return "Demo App"; }
        @Override public ImportsFor<DemoApp> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<DemoApp> exports() {
            return new ExportsOf<>(this, List.<Exportable<DemoApp>>of(new appMain()));
        }
    }

    private static Navigable<AppModule._None, DemoApp> tile(String name, String summary) {
        return new Navigable<>(DemoApp.INSTANCE, AppModule._None.INSTANCE, name, summary);
    }

    /** The canonical tile, one level down. */
    static final Navigable<AppModule._None, DemoApp> CANON =
            tile("Demo App", "The canonical entry.");
    /** The same (app, args) dressed as a featured tile. RFC 0051 Phase 6 — this
     *  used to be a DIFFERENT AppDoc uuid, because AppDoc seeded identity from
     *  the whole Navigable including its display framing, which is exactly what
     *  the doc-uuid half of Law 1 could not see. With the wrapper gone there is
     *  no second uuid to be fooled by: the leaf IS the binding, and NavKey
     *  compares (app class, params) — so the framing cannot disguise it. */
    static final Navigable<AppModule._None, DemoApp> ECHO =
            tile("Featured: Demo App", "Same app, same args, different framing.");

    record FramingRoot() implements L0_Catalogue<FramingRoot> {
        public static final FramingRoot INSTANCE = new FramingRoot();
        @Override public String name() { return "Framing-Root"; }
        @Override public List<Entry<FramingRoot>> leaves() {
            return List.of(Entry.of(this, ECHO));
        }
        @Override public List<? extends L1_Catalogue<FramingRoot, ?>> subCatalogues() {
            return List.of(FramingChild.INSTANCE);
        }
    }

    record FramingChild() implements L1_Catalogue<FramingRoot, FramingChild> {
        public static final FramingChild INSTANCE = new FramingChild();
        @Override public FramingRoot parent() { return FramingRoot.INSTANCE; }
        @Override public String name()        { return "Framing-Child"; }
        @Override public List<Entry<FramingChild>> leaves() {
            return List.of(Entry.of(this, CANON));
        }
    }

    @Test
    void law1_rejects_sameAppAndArgsFramedTwice() {
        // Guard the premise: the two tiles differ in FRAMING, so this exercises
        // the (app, args) half. Before Phase 6 they also differed in uuid,
        // because AppDoc seeded one from the framing; now there is no uuid at
        // all on an app leaf, which is the stronger form of the same point.
        assertNotEquals(CANON, ECHO, "premise broken: the framings are identical");
        assertEquals(CANON.app(), ECHO.app(), "premise broken: not the same app");
        assertEquals(CANON.params(), ECHO.params(), "premise broken: not the same args");

        var brand = new StudioBrand("Test", FramingRoot.class);
        var registry = new DocRegistry(List.<Doc>of());
        var ex = assertThrows(IllegalStateException.class,
                () -> new CatalogueRegistry(brand, registry,
                        List.of(FramingRoot.INSTANCE, FramingChild.INSTANCE)));
        assertTrue(ex.getMessage().contains("identified by (app, args)"),
                "expected the app-half Law 1 message, got: " + ex.getMessage());
    }

    // ----- RFC 0051 Law 2: siblings claim distinct path segments -----

    /** Two docs that derive the same segment — the shape every value-Doc
     *  (ComposedDoc, RigidDoc, PlanDoc) has by default, since they share a
     *  class. Modelled here with an explicit override so the test says what
     *  it means rather than depending on those classes' derivations. */
    record TwinDoc(UUID uuid, String heading) implements Doc {
        @Override public UUID   uuid()     { return uuid; }
        @Override public String title()    { return heading; }
        @Override public String contents() { return ""; }
    }

    static final TwinDoc TWIN_A = new TwinDoc(UUID.randomUUID(), "First Twin");
    static final TwinDoc TWIN_B = new TwinDoc(UUID.randomUUID(), "Second Twin");

    record TwinCatalogue() implements L0_Catalogue<TwinCatalogue> {
        public static final TwinCatalogue INSTANCE = new TwinCatalogue();
        @Override public String name() { return "Twin-Cat"; }
        @Override public List<Entry<TwinCatalogue>> leaves() {
            return List.of(Entry.of(this, DocReader.INSTANCE, new DocReader.Params(TWIN_A.uuid().toString()),
                    TWIN_A, new NodeName("twin")),
                    Entry.of(this, DocReader.INSTANCE, new DocReader.Params(TWIN_B.uuid().toString()),
                            TWIN_B, new NodeName("twin")));
        }
    }

    /** A sub-catalogue and a leaf colliding — the cross-kind case. One path
     *  segment cannot mean both "descend here" and "open this". */
    record ClashRoot() implements L0_Catalogue<ClashRoot> {
        public static final ClashRoot INSTANCE = new ClashRoot();
        @Override public String name() { return "Clash-Root"; }
        @Override public List<Entry<ClashRoot>> leaves() {
            var d = new TwinDoc(UUID.randomUUID(), "Leaf");
            return List.of(Entry.of(this, DocReader.INSTANCE,
                    new DocReader.Params(d.uuid().toString()), d));
        }
        @Override public List<? extends L1_Catalogue<ClashRoot, ?>> subCatalogues() {
            return List.of(TwinChild.INSTANCE);
        }
    }

    record TwinChild() implements L1_Catalogue<ClashRoot, TwinChild> {
        public static final TwinChild INSTANCE = new TwinChild();
        @Override public ClashRoot parent() { return ClashRoot.INSTANCE; }
        @Override public String name()      { return "Twin-Child"; }
        @Override public NodeName slug()    { return new NodeName("twin"); }
    }

    @Test
    void law2_rejects_twoLeavesClaimingOneSegment() {
        var brand = new StudioBrand("Test", TwinCatalogue.class);
        var reg = new DocRegistry(List.of(TWIN_A, TWIN_B));
        var ex = assertThrows(IllegalStateException.class,
                () -> new CatalogueRegistry(brand, reg, List.of(TwinCatalogue.INSTANCE)));
        assertTrue(ex.getMessage().contains("claim the path segment 'twin'"), ex.getMessage());
    }

    @Test
    void law2_rejects_subCatalogueClashingWithLeaf() {
        var brand = new StudioBrand("Test", ClashRoot.class);
        var reg = new DocRegistry(List.of(ClashRoot.INSTANCE.leaves().stream()
                .map(e -> ((Entry.OfLeaf<?, ?, ?>) e).content()).findFirst().orElseThrow()));
        var ex = assertThrows(IllegalStateException.class,
                () -> new CatalogueRegistry(brand, reg,
                        List.of(ClashRoot.INSTANCE, TwinChild.INSTANCE)));
        assertTrue(ex.getMessage().contains("claim the path segment 'twin'"), ex.getMessage());
    }

    @Test
    void law2_derivesReadableSegmentsFromClassNames() {
        // The derivation is the reason no catalogue in either studio needed a
        // manual slug: camel humps become word breaks, the role suffix goes.
        assertEquals("leaf", LeafCatalogue.INSTANCE.slug().value());
        assertEquals("doc-tree-ontology",
                NodeName.ofType(DocTreeOntologyDoc.class, "Doc").value());
    }

    /** Name-only stand-in for a real studio doc class, to pin the derivation. */
    private static final class DocTreeOntologyDoc {}

    // ----- RFC 0051 Laws 3 and 4: resolvable positions, one root -----

    /** An L1 that is registered and holds a doc, but which no parent lists in
     *  subCatalogues(). Nothing else notices — the closure check only walks
     *  DOWN from parents, so a child nobody claims is never visited. Its doc
     *  therefore has a position that cannot be walked back to a root. */
    record UnclaimedChild() implements L1_Catalogue<RootCatalogue, UnclaimedChild> {
        public static final UnclaimedChild INSTANCE = new UnclaimedChild();
        @Override public RootCatalogue parent() { return RootCatalogue.INSTANCE; }
        @Override public String name()          { return "Unclaimed-Child"; }
        @Override public List<Entry<UnclaimedChild>> leaves() {
            return List.of(Entry.of(this, DocReader.INSTANCE, new DocReader.Params(ORPHANED_DOC.uuid().toString()),
                    ORPHANED_DOC, new NodeName("orphaned")));
        }
    }

    static final UUID ORPHANED_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
    static final Doc ORPHANED_DOC = new Doc() {
        @Override public UUID   uuid()     { return ORPHANED_ID; }
        @Override public String title()    { return "Orphaned"; }
        @Override public String contents() { return ""; }
    };

    @Test
    void law3_rejects_positionThatDoesNotResolveToARoot() {
        var brand = new StudioBrand("Test", RootCatalogue.class);
        var reg = new DocRegistry(List.of(TEST_DOC, ORPHANED_DOC));
        var ex = assertThrows(IllegalStateException.class,
                () -> new CatalogueRegistry(brand, reg,
                        List.of(RootCatalogue.INSTANCE, LeafCatalogue.INSTANCE,
                                UnclaimedChild.INSTANCE)));
        assertTrue(ex.getMessage().contains("no parent lists it in subCatalogues()"), ex.getMessage());
    }

    /** A second, entirely unremarkable L0 — no leaves, nothing wrong with it
     *  except that nothing hosts it. */
    record LoneRoot() implements L0_Catalogue<LoneRoot> {
        public static final LoneRoot INSTANCE = new LoneRoot();
        @Override public String name() { return "Lone-Root"; }
    }

    @Test
    void law4_rejects_secondUnhostedRoot() {
        // LoneRoot is a perfectly valid L0 on its own; the violation is
        // only that nothing hosts it, so the tree would have two entrances.
        var brand = new StudioBrand("Test", RootCatalogue.class);
        var reg = new DocRegistry(List.of(TEST_DOC));
        var ex = assertThrows(IllegalStateException.class,
                () -> new CatalogueRegistry(brand, reg,
                        List.of(RootCatalogue.INSTANCE, LeafCatalogue.INSTANCE,
                                LoneRoot.INSTANCE)));
        assertTrue(ex.getMessage().contains("Expected exactly one un-hosted L0"), ex.getMessage());
    }

    // RFC 0011 note: the previous "rejects_staleParentReference" test
    // (a child whose parent() returns a different L0 INSTANCE than its
    // container) is no longer expressible in this codebase — the CRTP
    // self-bound on L<N>_Catalogue forces every child in subCatalogues()
    // to be typed L<N+1>_Catalogue<ThisCatalogue, ?>, so the wildcard
    // escape hatch the old test relied on doesn't compile.

    // -----------------------------------------------------------------------
    // RFC 0051 — identity and position are separate claims.
    // -----------------------------------------------------------------------

    /** A doc homed by extraDocHomes rather than by being a catalogue leaf —
     *  the tree-leaf shape. Its derived path (home's path + its slug) names a
     *  child the catalogue does not have. */
    private static final UUID GUEST_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");

    private static final Doc GUEST_DOC = new Doc() {
        @Override public UUID   uuid()    { return GUEST_ID; }
        @Override public String title()   { return "Guest Doc"; }
        @Override public String contents(){ return "# Guest"; }
    };

    private CatalogueRegistry withGuest() {
        var brand = new StudioBrand("Test", RootCatalogue.class);
        return new CatalogueRegistry(
                brand,
                new DocRegistry(List.of(TEST_DOC, GUEST_DOC)),
                List.of(RootCatalogue.INSTANCE, LeafCatalogue.INSTANCE),
                null,
                java.util.Map.of(GUEST_ID, LeafCatalogue.INSTANCE));
    }

    @Test
    void aDerivedPathThatResolvesToNothingIsNotIndexed() {
        // pathOf DERIVES a path for any homed doc; it does not check that the
        // catalogue actually has that child. Handing such a path to /goto
        // produced a 404 — and worse, several docs sharing a class-derived
        // slug all derived the SAME dead path.
        var registry = withGuest();
        assertNull(registry.pathForFlat("doc-reader",
                        java.util.Map.of("doc", List.of(GUEST_ID.toString()))),
                "a path nothing answers to must not be handed out as an address");
    }

    @Test
    void butTheDocIsStillFoundByItsFlatAddress() {
        // Identity is unconditional. The chrome resolver looks a doc up by its
        // flat address to find its trail, and an unpositioned doc can still
        // have one (a tree leaf's is enriched from the tree). Guarding this
        // lookup as well blanked those pages.
        var registry = withGuest();
        assertSame(GUEST_DOC, registry.docForFlat("doc-reader",
                java.util.Map.of("doc", List.of(GUEST_ID.toString()))));
    }

    @Test
    void aRealCatalogueLeafStillResolvesBothWays() {
        var registry = withGuest();
        var args = java.util.Map.of("doc", List.of(DOC_ID.toString()));
        assertSame(TEST_DOC, registry.docForFlat("doc-reader", args));
        var path = registry.pathForFlat("doc-reader", args);
        assertNotNull(path, "a genuine catalogue leaf has a position");
        assertInstanceOf(PathResolution.ToLeaf.class, registry.resolve(path));
    }
}
