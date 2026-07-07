package hue.captains.singapura.js.homing.color.semantic;

import hue.captains.singapura.js.homing.color.semantic.affective.Cheerful;
import org.junit.jupiter.api.Test;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SemanticColorTest {

    @Test
    void degreeIsAProperFractionBelowOne() {
        assertNotNull(Degree.of(2, 5));
        assertEquals(Degree.ZERO, Degree.of(0, 7));                 // 0/n normalizes to 0
        assertThrows(IllegalArgumentException.class, () -> Degree.of(1, 1));   // = 1 rejected
        assertThrows(IllegalArgumentException.class, () -> Degree.of(3, 2));   // > 1 rejected
        assertThrows(IllegalArgumentException.class, () -> Degree.of(-1, 3));  // < 0 rejected
        assertThrows(IllegalArgumentException.class, () -> Degree.of(1, 0));   // bad denominator
    }

    @Test
    void degreeReducesForValueEquality() {
        assertEquals(Degree.of(1, 2), Degree.of(2, 4));
        assertEquals(0.5, Degree.of(2, 4).asDouble());
    }

    @Test
    void commonScalePointsAreTheNiceFractions() {
        assertEquals(Degree.of(1, 4), Degree.QUARTER);
        assertEquals(Degree.of(1, 3), Degree.ONE_THIRD);
        assertEquals(Degree.of(1, 2), Degree.HALF);
        assertEquals(Degree.of(2, 3), Degree.TWO_THIRDS);
        assertEquals(Degree.of(3, 4), Degree.THREE_QUARTERS);
        assertEquals(Degree.of(0, 1), Degree.ZERO);
    }

    @Test
    void resolverSeamWiresThroughDegree() {
        SemanticColorResolver r = PlaceholderResolver.INSTANCE;
        PhysicalColor c = r.resolve(Cheerful.INSTANCE, Degree.of(3, 5));
        assertEquals("cheerful@3/5", c.value());

        // curried form — a resolved scale sampled at a degree
        Function<Degree, PhysicalColor> scale = r.resolve(Cheerful.INSTANCE);
        assertEquals("cheerful@1/4", scale.apply(Degree.of(1, 4)).value());
    }
}
