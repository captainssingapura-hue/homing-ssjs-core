package hue.captains.singapura.js.homing.conformance.rules;

import hue.captains.singapura.js.homing.core.StandardJsModuleType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * RFC 0044 — the global {@link MaxEffectiveLinesRule}: a served module is capped
 * at {@value MaxEffectiveLinesRule#LIMIT} <b>effective</b> lines (non-blank,
 * non-comment). Its finding is count-independent so the baseline is stable.
 */
class MaxEffectiveLinesRuleTest {

    private static ServedModule mod(List<String> lines) {
        return new ServedModule("demo.M", StandardJsModuleType.CONSUMER, JsSource.of(lines.toArray(String[]::new)));
    }

    private static List<String> repeat(String line, int n) {
        var out = new ArrayList<String>();
        for (int i = 0; i < n; i++) out.add(line);
        return out;
    }

    @Test
    void atOrUnderTheLimitIsClean() {
        assertTrue(MaxEffectiveLinesRule.INSTANCE.check(mod(repeat("const x = 1;", MaxEffectiveLinesRule.LIMIT))).isEmpty());
    }

    @Test
    void overTheLimitIsFlaggedWithACountIndependentMessage() {
        var justOver = MaxEffectiveLinesRule.INSTANCE.check(mod(repeat("const x = 1;", MaxEffectiveLinesRule.LIMIT + 1)));
        var wayOver  = MaxEffectiveLinesRule.INSTANCE.check(mod(repeat("const x = 1;", 500)));
        assertFalse(justOver.isEmpty());
        assertFalse(wayOver.isEmpty());
        // Same message regardless of the exact count → a stable baseline fingerprint.
        assertEquals(justOver.get(0).message(), wayOver.get(0).message(),
                "the finding message must not depend on the exact line count");
    }

    @Test
    void blankAndCommentLinesDoNotCount() {
        var lines = new ArrayList<String>();
        lines.addAll(repeat("const x = 1;", MaxEffectiveLinesRule.LIMIT));   // exactly the limit of code
        lines.addAll(repeat("", 40));                                        // blanks — ignored
        lines.addAll(repeat("// a comment", 40));                            // comments — ignored
        Collections.shuffle(lines, new java.util.Random(1));
        assertEquals(List.of(), MaxEffectiveLinesRule.INSTANCE.check(mod(lines)),
                "blank + comment lines must not push a limit-sized module over");
    }
}
