package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.table.TableData;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static hue.captains.singapura.js.homing.studio.base.composed.RelationCell.cell;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the {@code kind:"relation"} content-object wire shape emitted by
 * {@link SegmentJson} — previously untested. Covers both the plain relation
 * (unchanged, no side-car key) and the articulated relation (the sparse
 * {@code articulations} side-car, emitted only when present).
 */
class RelationSegmentJsonTest {

    private static String json(RelationSegment rs) {
        var sb = new StringBuilder();
        SegmentJson.write(sb, rs, "anchor-1", s -> "");
        return sb.toString();
    }

    @Test
    void plainRelationOmitsArticulations() {
        var rs = new RelationSegment(
                List.of("Name", "Status"),
                List.of(List.of("alpha", "ok")),
                Optional.<hue.captains.singapura.js.homing.studio.base.composed.text.Line.Plain>empty());
        var j = json(rs);
        assertTrue(j.contains("\"kind\":\"relation\""), j);
        assertTrue(j.contains("\"headers\":[\"Name\",\"Status\"]"), j);
        assertTrue(j.contains("\"rows\":[[\"alpha\",\"ok\"]]"), j);
        // Backward-compat: a plain relation must not carry the side-car key at all.
        assertFalse(j.contains("articulations"), "plain relation must not emit the side-car key: " + j);
    }

    @Test
    void articulatedRelationEmitsSparseSideCar() {
        var arts = new RelationArticulations(List.of(
                // header cell of column 1, right-aligned
                ArticulatedCell.header(1, Articulation.of().align(TableData.Align.RIGHT).build()),
                // body cell (0,1): success badge + strong emphasis
                ArticulatedCell.at(0, 1, Articulation.of().badge(TableData.Badge.SUCCESS).strong().build())));
        var rs = new RelationSegment(
                List.of("Name", "Status"),
                List.of(List.of("alpha", "ok")),
                Optional.<hue.captains.singapura.js.homing.studio.base.composed.text.Line.Plain>empty(),
                arts);
        var j = json(rs);
        assertTrue(j.contains("\"articulations\":["), j);
        assertTrue(j.contains("{\"row\":-1,\"col\":1,\"align\":\"right\"}"), j);
        assertTrue(j.contains("{\"row\":0,\"col\":1,\"badge\":\"success\",\"emphasis\":\"strong\"}"), j);
    }

    /**
     * The inline-cell DSL factory derives the sparse side-car coordinates from
     * each cell's POSITION — the author tracks no (row, col). Header cells map to
     * row -1; body cells to their 0-based body-row + column index. Plain cells
     * (no marks) contribute nothing to the side-car.
     */
    @Test
    void articulatedFactoryDerivesCoordinatesFromCellPositions() {
        var rs = RelationSegment.articulated(
                List.of(cell("Module"), cell("Build"), cell("Coverage").right()),
                List.of(
                        List.of(cell("core").strong(), cell("passing").success(), cell("94%").right()),
                        List.of(cell("shell"),         cell("failing").error(),   cell("71%").right().muted())),
                Optional.<hue.captains.singapura.js.homing.studio.base.composed.text.Line.Plain>empty());

        // Plain string rows are unchanged — cell text only.
        assertEquals(List.of(List.of("core", "passing", "94%"),
                             List.of("shell", "failing", "71%")), rs.rows());

        // Exactly the marked cells appear in the side-car, at positions derived
        // from where they sit (order: headers first, then rows left-to-right).
        var j = json(rs);
        assertTrue(j.contains("{\"row\":-1,\"col\":2,\"align\":\"right\"}"), j);          // header "Coverage"
        assertTrue(j.contains("{\"row\":0,\"col\":0,\"emphasis\":\"strong\"}"), j);        // "core"
        assertTrue(j.contains("{\"row\":0,\"col\":1,\"badge\":\"success\"}"), j);          // "passing"
        assertTrue(j.contains("{\"row\":0,\"col\":2,\"align\":\"right\"}"), j);            // "94%"
        assertTrue(j.contains("{\"row\":1,\"col\":1,\"badge\":\"error\"}"), j);            // "failing"
        assertTrue(j.contains("{\"row\":1,\"col\":2,\"align\":\"right\",\"emphasis\":\"muted\"}"), j); // "71%"
        // Plain cells contribute no coordinate: the "Module"/"Build" headers and the "shell" cell.
        assertFalse(j.contains("\"row\":-1,\"col\":0"), "plain 'Module' header must not be articulated: " + j);
        assertFalse(j.contains("\"row\":-1,\"col\":1"), "plain 'Build' header must not be articulated: " + j);
        assertFalse(j.contains("\"row\":1,\"col\":0"),  "plain 'shell' cell must not be articulated: " + j);
    }
}
