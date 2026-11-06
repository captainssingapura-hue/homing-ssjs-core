package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.table.TableData;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A cell is a value: a mark gives a new cell, and the one marked stays as it was. */
class RelationCellTest {

    @Test
    void aCellWithNoMarksArticulatesNothing() {
        assertTrue(new RelationCell("plain").articulation().isEmpty());
    }

    @Test
    void aMarkLeavesTheCellItWasGivenAlone() {
        var plain = new RelationCell("94%");
        var marked = plain.right();
        assertTrue(plain.articulation().isEmpty());
        assertEquals(Optional.of(TableData.Align.RIGHT), marked.articulation().align());
    }

    @Test
    void twoCellsMarkedFromOneShareNothing() {
        var shared = new RelationCell("x");
        var good = shared.success();
        var bad = shared.error();
        assertEquals(Optional.of(TableData.Badge.SUCCESS), good.articulation().badge());
        assertEquals(Optional.of(TableData.Badge.ERROR), bad.articulation().badge());
    }

    @Test
    void marksOfDifferentKindsAddUpInAnyOrder() {
        assertEquals(new RelationCell("x").right().muted().warning(), new RelationCell("x").warning().muted().right());
        var articulation = new RelationCell("x").right().muted().warning().articulation();
        assertEquals(Optional.of(TableData.Badge.WARNING), articulation.badge());
        assertEquals(Optional.of(TableData.Align.RIGHT), articulation.align());
        assertEquals(Optional.of(Articulation.Emphasis.MUTED), articulation.emphasis());
    }

    @Test
    void aLaterMarkOfTheSameKindReplacesTheEarlierOne() {
        assertEquals(new RelationCell("x").muted(), new RelationCell("x").strong().muted());
    }

    @Test
    void aNullMarkTakesTheMarkOff() {
        assertEquals(new RelationCell("x"), new RelationCell("x").success().badge(null));
    }
}
