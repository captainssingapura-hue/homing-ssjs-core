package hue.captains.singapura.js.homing.workspace.party;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AgentIdTest {

    @Test
    void emptySegmentsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new AgentId(List.of()));
    }

    @Test
    void nullSegmentsRejected() {
        assertThrows(NullPointerException.class, () -> new AgentId(null));
    }

    @Test
    void invalidCharactersRejected() {
        assertThrows(IllegalArgumentException.class, () -> AgentId.of("bad/segment"));
        assertThrows(IllegalArgumentException.class, () -> AgentId.of("with space"));
        assertThrows(IllegalArgumentException.class, () -> AgentId.of(""));
    }

    @Test
    void validCharactersAccepted() {
        AgentId.of("workspace");
        AgentId.of("ribbon-tab");
        AgentId.of("widget_3");
        AgentId.of("ABC123_-x");
    }

    @Test
    void asPathJoinsWithSlash() {
        assertEquals("workspace/ribbon/animalSelector",
                AgentId.of("workspace", "ribbon", "animalSelector").asPath());
    }

    @Test
    void childAppendsSegment() {
        AgentId root = AgentId.of("workspace");
        AgentId child = root.child("ribbon");
        assertEquals("workspace/ribbon", child.asPath());
        // parent is unchanged (immutability)
        assertEquals("workspace", root.asPath());
    }

    @Test
    void parentOrNullStripsLastSegment() {
        AgentId leaf = AgentId.of("workspace", "ribbon", "animalSelector");
        assertEquals(AgentId.of("workspace", "ribbon"), leaf.parentOrNull());
        assertEquals(AgentId.of("workspace"), leaf.parentOrNull().parentOrNull());
        assertNull(leaf.parentOrNull().parentOrNull().parentOrNull());   // root has no parent
    }

    @Test
    void isStrictAncestorOfDetectsDescendants() {
        AgentId ws = AgentId.of("workspace");
        AgentId selector = AgentId.of("workspace", "ribbon", "animalSelector");
        AgentId sibling = AgentId.of("other", "ribbon", "animalSelector");

        assertTrue(ws.isStrictAncestorOf(selector));
        assertFalse(ws.isStrictAncestorOf(ws));            // strict — not reflexive
        assertFalse(selector.isStrictAncestorOf(ws));      // reverse direction
        assertFalse(ws.isStrictAncestorOf(sibling));       // different root
    }

    @Test
    void equalityIsByValue() {
        assertEquals(AgentId.of("workspace", "ribbon"), AgentId.of("workspace", "ribbon"));
        assertEquals(AgentId.of("workspace", "ribbon").hashCode(),
                     AgentId.of("workspace", "ribbon").hashCode());
    }

    @Test
    void segmentsListIsImmutableCopy() {
        var raw = new java.util.ArrayList<>(List.of("workspace", "ribbon"));
        AgentId id = new AgentId(raw);
        raw.add("mutated");
        assertEquals(2, id.segments().size());   // original change doesn't leak
    }
}
