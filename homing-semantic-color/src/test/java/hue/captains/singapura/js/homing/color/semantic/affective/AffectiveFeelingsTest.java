package hue.captains.singapura.js.homing.color.semantic.affective;

import hue.captains.singapura.js.homing.color.semantic.SemanticColor;
import hue.captains.singapura.js.homing.color.semantic.group.Affective;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Pins the Affective group's landmark feelings: each is a {@link SemanticColor}
 * leaf under {@link Affective}, with a distinct name and a self-description.
 */
class AffectiveFeelingsTest {

    private static final List<SemanticColor> FEELINGS = List.of(
            Serene.INSTANCE, Cheerful.INSTANCE, Tense.INSTANCE, Somber.INSTANCE, Neutral.INSTANCE);

    @Test
    void feelingsAreSemanticColorsWithDistinctNames() {
        long distinct = FEELINGS.stream().map(SemanticColor::name).distinct().count();
        assertEquals(FEELINGS.size(), distinct);
        assertEquals("serene", Serene.INSTANCE.name());
        assertEquals("tense", Tense.INSTANCE.name());
    }

    @Test
    void feelingsAreLeavesUnderAffective() {
        assertSame(Affective.INSTANCE, Serene.INSTANCE.parent());
        assertSame(Affective.INSTANCE, Cheerful.INSTANCE.parent());
        assertSame(Affective.INSTANCE, Tense.INSTANCE.parent());
        assertSame(Affective.INSTANCE, Somber.INSTANCE.parent());
        assertSame(Affective.INSTANCE, Neutral.INSTANCE.parent());
        assertFalse(Serene.INSTANCE.desc().summary().text().isBlank());
    }
}
