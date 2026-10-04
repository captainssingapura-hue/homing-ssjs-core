package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PartialModulePathTest {

    @Test
    void toString_returnsBasePath() {
        var path = new PartialModulePath("/module?class=X");
        assertEquals("/module?class=X", path.toString());
    }
}
