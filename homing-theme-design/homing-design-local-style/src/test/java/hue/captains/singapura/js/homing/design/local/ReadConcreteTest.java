package hue.captains.singapura.js.homing.design.local;

import hue.captains.singapura.js.homing.design.local.Styled.Bold;
import hue.captains.singapura.js.homing.design.local.Styled.BoldMark;
import hue.captains.singapura.js.homing.design.local.Styled.DirectMark;
import hue.captains.singapura.js.homing.design.local.Styled.Edge;
import hue.captains.singapura.js.homing.design.local.Styled.Mark;
import hue.captains.singapura.js.homing.design.local.Styled.Plain;
import hue.captains.singapura.js.homing.design.local.Styled.PlainEdge;
import hue.captains.singapura.js.homing.design.local.Styled.RawEdge;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** A concrete leaf's leaf and local style, read off its declaration; one that names no local style refused in its own terms. */
class ReadConcreteTest {

    @Test
    void aConcreteLeafsLeafAndLocalStyle_areReadOffItsDeclaration() {
        assertEquals(Edge.INSTANCE, new PlainEdge().virtual(), "the leaf, bound by the leaf's own interface");
        assertEquals(Plain.INSTANCE, new PlainEdge().localStyle(), "the local style, passed down through the leaf's interface");
        assertEquals(Mark.INSTANCE, new BoldMark().virtual());
        assertEquals(Bold.INSTANCE, new BoldMark().localStyle());
        assertEquals(Mark.INSTANCE, new DirectMark().virtual(), "all three named directly reads alike");
        assertEquals(Plain.INSTANCE, new DirectMark().localStyle());
        assertEquals("plain", Plain.INSTANCE.token(), "a local style is named from its type");
    }

    @Test
    void aConcreteLeafThatNamesNoLocalStyle_isRefusedInItsOwnTerms() {
        assertEquals(Edge.INSTANCE, new RawEdge().virtual(), "the leaf is the leaf's interface's to bind");
        var e = assertThrows(IllegalStateException.class, () -> new RawEdge().localStyle());
        assertTrue(e.getMessage().contains("names no local style") && e.getMessage().contains("raw"), e.getMessage());
    }

    @Test
    void aLocalStyle_listsItsConcreteLeaves_eachNamingItBack() {
        for (var c : Plain.INSTANCE.concretes()) assertEquals(Plain.INSTANCE, c.localStyle(), c + " names the local style that lists it");
        for (var c : Bold.INSTANCE.concretes()) assertEquals(Bold.INSTANCE, c.localStyle(), c + " names the local style that lists it");
    }
}
