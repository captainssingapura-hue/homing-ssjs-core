package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.ParamCodecLaw;
import hue.captains.singapura.js.homing.studio.base.StudioBaseCrate;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedViewer;
import hue.captains.singapura.js.homing.studio.base.image.ImageViewer;
import hue.captains.singapura.js.homing.studio.base.table.TableViewer;
import hue.captains.singapura.js.homing.studio.base.tracker.PlanAppHost;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * RFC 0051 Phase 2 — the round-trip law over every coded app this module
 * ships. {@code TreeAppHost} has its own test (it also owns the escaping
 * regression that motivated the codec); everything else lives here.
 *
 * <p>The samples are the point. A generator would produce the values a
 * codec already handles; the interesting ones are the values that used to
 * break the string concatenation this replaced — an {@code &}, a space, an
 * {@code =} inside a value — and the ones a codec is likeliest to forget:
 * an absent optional and an empty-but-present one. Those two are different
 * facts, and {@code QueryString.put} treats them differently on purpose.</p>
 *
 * <p>{@link #everyCodedAppInThisCrateIsCovered()} is what keeps this list
 * honest. Adding a codec to an app in this module fails that test until the
 * app appears below, so the law extends itself rather than depending on
 * whoever adds the next codec remembering this file exists.</p>
 */
class ParamCodecLawsTest {

    /** Apps whose samples are declared in this class — read by the coverage gate. */
    private static final Set<String> COVERED = Set.of(
            "catalogue", "plan", "doc-reader", "composed-viewer",
            "svg-viewer", "table-viewer", "image-viewer", "doc-tree-viewer",
            // Covered by TreeAppHostCodecTest, in this module's tree package.
            "tree");

    // -----------------------------------------------------------------------
    // Two-component codecs — a required id plus an optional companion.
    // -----------------------------------------------------------------------

    @Test
    void catalogueAppHost() {
        ParamCodecLaw.assertRoundTrips("CatalogueAppHost", CatalogueAppHost.CODEC, List.of(
                new CatalogueAppHost.Params("hue.captains.Studio", null),
                // Absent and empty are different facts; a codec that maps one
                // onto the other passes casual inspection and fails here.
                new CatalogueAppHost.Params("hue.captains.Studio", ""),
                new CatalogueAppHost.Params("hue.captains.Studio", "umbrella"),
                new CatalogueAppHost.Params("a&b=c", "ctx with spaces")));
    }

    @Test
    void planAppHost() {
        ParamCodecLaw.assertRoundTrips("PlanAppHost", PlanAppHost.CODEC, List.of(
                new PlanAppHost.Params("hue.captains.Rfc0051PlanData", null),
                new PlanAppHost.Params("hue.captains.Rfc0051PlanData", ""),
                // ?phase= rides across the /goto hop, so it has to survive
                // encoding as faithfully as the identity key does.
                new PlanAppHost.Params("hue.captains.Rfc0051PlanData", "5"),
                new PlanAppHost.Params("p&q", "phase 2 & 3")));
    }

    // -----------------------------------------------------------------------
    // Single-component codecs — one required identity key. An empty value is
    // NOT a sample here: these codecs reject it as missing, which is correct
    // and is asserted separately below.
    // -----------------------------------------------------------------------

    @Test
    void docReader() {
        ParamCodecLaw.assertRoundTrips("DocReader", DocReader.CODEC, List.of(
                new DocReader.Params("a1c9e4d7-5f28-4b6a-9e03-7d4c2b8f5a16"),
                new DocReader.Params("id with spaces & symbols")));
    }

    @Test
    void composedViewer() {
        ParamCodecLaw.assertRoundTrips("ComposedViewer", ComposedViewer.CODEC, List.of(
                new ComposedViewer.Params("c37ebd98-0000-4000-8000-000000000001"),
                new ComposedViewer.Params("a&b=c")));
    }

    @Test
    void svgViewer() {
        ParamCodecLaw.assertRoundTrips("SvgViewer", SvgViewer.CODEC, List.of(
                new SvgViewer.Params("19fca08d-6984-3000-ba9a-cd2dd4ab7575"),
                new SvgViewer.Params("a&b=c")));
    }

    @Test
    void tableViewer() {
        ParamCodecLaw.assertRoundTrips("TableViewer", TableViewer.CODEC, List.of(
                new TableViewer.Params("11111111-2222-3333-4444-555555555555"),
                new TableViewer.Params("a&b=c")));
    }

    @Test
    void imageViewer() {
        ParamCodecLaw.assertRoundTrips("ImageViewer", ImageViewer.CODEC, List.of(
                new ImageViewer.Params("66666666-7777-8888-9999-000000000000"),
                new ImageViewer.Params("a&b=c")));
    }

    @Test
    void docTreeViewer() {
        ParamCodecLaw.assertRoundTrips("DocTreeViewer", DocTreeViewer.CODEC, List.of(
                new DocTreeViewer.Params("abcdef01-2345-6789-abcd-ef0123456789"),
                new DocTreeViewer.Params("a&b=c")));
    }

    // -----------------------------------------------------------------------
    // The other half of the contract: a missing identity is NAMED, so a
    // handler can tell 400 from 404 rather than guessing from an empty result.
    // -----------------------------------------------------------------------

    @Test
    void aMissingIdentityKeyIsNamed() {
        assertMissingKey("CatalogueAppHost", CatalogueAppHost.CODEC.fromQueryString("context=umbrella"), "id");
        assertMissingKey("PlanAppHost",      PlanAppHost.CODEC.fromQueryString("phase=5"),               "id");
        assertMissingKey("DocReader",        DocReader.CODEC.fromQueryString(""),                        "doc");
        assertMissingKey("ComposedViewer",   ComposedViewer.CODEC.fromQueryString("theme=forest"),       "id");
        assertMissingKey("SvgViewer",        SvgViewer.CODEC.fromQueryString("theme=forest"),            "id");
        assertMissingKey("TableViewer",      TableViewer.CODEC.fromQueryString(""),                      "id");
        assertMissingKey("ImageViewer",      ImageViewer.CODEC.fromQueryString(""),                      "id");
        assertMissingKey("DocTreeViewer",    DocTreeViewer.CODEC.fromQueryString(""),                    "id");
    }

    private static <P extends AppModule._Param> void assertMissingKey(
            String who, ParamCodec.Decoded<P> decoded, String expectedKey) {

        if (!(decoded instanceof ParamCodec.Decoded.Missing<P> missing)) {
            throw new AssertionError(who + ": expected a named Missing, got " + describe(decoded));
        }
        assertEquals(expectedKey, missing.key(), who + " named the wrong missing key");
    }

    private static String describe(ParamCodec.Decoded<?> decoded) {
        return decoded == null ? "null" : decoded.describe();
    }

    // -----------------------------------------------------------------------
    // Coverage gate.
    // -----------------------------------------------------------------------

    /**
     * Every app in this module's crate that declares a codec must appear in
     * {@link #COVERED}. This is what makes the law self-extending: a new
     * codec without samples fails here, naming the app, rather than passing
     * silently because nobody thought to write its test.
     */
    @Test
    void everyCodedAppInThisCrateIsCovered() {
        var uncovered = new TreeSet<String>();
        for (var entry : StudioBaseCrate.INSTANCE.entries()) {
            if (entry.module() instanceof AppModule<?, ?> app
                    && !(app.paramCodec() instanceof ParamCodec.None)
                    && !COVERED.contains(app.simpleName())) {
                uncovered.add(app.simpleName());
            }
        }
        assertEquals(Set.of(), uncovered,
                "these apps declare a ParamCodec but no round-trip samples — add them to "
              + ParamCodecLawsTest.class.getSimpleName());
    }
}
