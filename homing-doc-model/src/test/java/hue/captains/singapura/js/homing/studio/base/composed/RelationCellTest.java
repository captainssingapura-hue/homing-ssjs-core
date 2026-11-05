package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.table.TableData;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static hue.captains.singapura.js.homing.studio.base.composed.RelationCell.cell;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A cell is a value: a mark gives a new cell, and the one marked stays as it was. */
class RelationCellTest {

    @Test
    void aCellWithNoMarksArticulatesNothing() {
        assertTrue(cell("plain").articulation().isEmpty());
    }

    @Test
    void aMarkLeavesTheCellItWasGivenAlone() {
        var plain = cell("94%");
        var marked = plain.right();
        assertTrue(plain.articulation().isEmpty());
        assertEquals(Optional.of(TableData.Align.RIGHT), marked.articulation().align());
    }

    @Test
    void twoCellsMarkedFromOneShareNothing() {
        var shared = cell("x");
        var good = shared.success();
        var bad = shared.error();
        assertEquals(Optional.of(TableData.Badge.SUCCESS), good.articulation().badge());
        assertEquals(Optional.of(TableData.Badge.ERROR), bad.articulation().badge());
    }

    @Test
    void marksOfDifferentKindsAddUpInAnyOrder() {
        assertEquals(cell("x").right().muted().warning(), cell("x").warning().muted().right());
        var articulation = cell("x").right().muted().warning().articulation();
        assertEquals(Optional.of(TableData.Badge.WARNING), articulation.badge());
        assertEquals(Optional.of(TableData.Align.RIGHT), articulation.align());
        assertEquals(Optional.of(Articulation.Emphasis.MUTED), articulation.emphasis());
    }

    @Test
    void aLaterMarkOfTheSameKindReplacesTheEarlierOne() {
        assertEquals(cell("x").muted(), cell("x").strong().muted());
    }

    @Test
    void aNullMarkTakesTheMarkOff() {
        assertEquals(cell("x"), cell("x").success().badge(null));
    }
}
