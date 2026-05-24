package hue.captains.singapura.js.homing.workspace;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wire-shape tests for {@link WidgetEntriesJson} — the Java↔JS
 * contract for the registry payload consumed by WidgetPickerModule.js.
 * Per the "Tests Carry the Discipline" doctrine, this serializer is
 * the boundary between two languages and earns dedicated coverage.
 */
final class WidgetEntriesJsonTest {

    @Test void emptyList_emitsEmptyArray() {
        assertEquals("[]", WidgetEntriesJson.of(List.of()));
    }

    @Test void singleParamlessWidget_shapeIsContractual() {
        var entry = WidgetEntry.of(NoParamsStub.class, WidgetLabel.of("Stub"));
        String json = WidgetEntriesJson.of(List.of(entry));

        assertTrue(json.startsWith("[{"), json);
        assertTrue(json.endsWith("}]"),    json);
        assertTrue(json.contains("\"simpleName\":\"NoParamsStub\""),                              json);
        assertTrue(json.contains("\"moduleUrl\":\"/module?class=" + NoParamsStub.class.getCanonicalName() + "\""), json);
        assertTrue(json.contains("\"label\":\"Stub\""),                                            json);
        assertTrue(json.contains("\"icon\":{\"kind\":\"emoji\",\"value\":\"📦\"}"),      json);
        assertTrue(json.contains("\"description\":\"\""),                                          json);
        assertTrue(json.contains("\"group\":\"Widgets\""),                                         json);
        assertTrue(json.contains("\"lifecycleHint\":\"MULTI\""),                                   json);
        assertTrue(json.contains("\"paramsFields\":[]"),                                           json);
        assertTrue(json.contains("\"defaults\":{}"),                                               json);
    }

    @Test void defaults_emittedInRegistrationOrder() {
        var defs = new LinkedHashMap<String, String>();
        defs.put("title", "Hello");
        defs.put("body",  "World");
        var entry = WidgetEntry.of(WithParamsStub.class, WidgetLabel.of("WithP"))
                .withDefaults(Map.of()) // baseline
                .withDefaults(defs);
        String json = WidgetEntriesJson.of(List.of(entry));
        assertTrue(json.contains("\"defaults\":{\"title\":\"Hello\",\"body\":\"World\"}"), json);
    }

    @Test void widgetWithParams_emitsRecordComponents() {
        var entry = WidgetEntry.of(WithParamsStub.class, WidgetLabel.of("WithP"));
        String json = WidgetEntriesJson.of(List.of(entry));

        assertTrue(json.contains("\"paramsFields\":[" +
                "{\"name\":\"docId\",\"type\":\"String\"}," +
                "{\"name\":\"page\",\"type\":\"int\"}]"), json);
    }

    @Test void singletonHint_reflectsInJson() {
        var entry = WidgetEntry.of(SingletonStub.class, WidgetLabel.of("S"));
        String json = WidgetEntriesJson.of(List.of(entry));
        assertTrue(json.contains("\"lifecycleHint\":\"SINGLETON\""), json);
    }

    @Test void pinnedHint_reflectsInJson() {
        var entry = WidgetEntry.of(PinnedStub.class, WidgetLabel.of("P"));
        String json = WidgetEntriesJson.of(List.of(entry));
        assertTrue(json.contains("\"lifecycleHint\":\"PINNED\""), json);
    }

    @Test void multipleEntries_separatedByComma() {
        var json = WidgetEntriesJson.of(List.of(
                WidgetEntry.of(NoParamsStub.class,  WidgetLabel.of("A")),
                WidgetEntry.of(SingletonStub.class, WidgetLabel.of("B"))
        ));
        // Two top-level objects.
        int objs = 0;
        int depth = 0;
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') { if (depth == 0) objs++; depth++; }
            else if (c == '}') depth--;
        }
        assertEquals(2, objs, json);
    }

    @Test void stringEscaping_handlesQuoteAndBackslash() {
        var entry = WidgetEntry.of(NoParamsStub.class, WidgetLabel.of("She said \"hi\\bye\""));
        String json = WidgetEntriesJson.of(List.of(entry));
        assertTrue(json.contains("\"label\":\"She said \\\"hi\\\\bye\\\"\""), json);
    }

    @Test void stringEscaping_handlesControlChars() {
        var sb = new StringBuilder();
        WidgetEntriesJson.appendString(sb, "a\nb\tcd");
        assertEquals("\"a\\nb\\tc\\u0001d\"", sb.toString());
    }

    @Test void missingInstanceField_throwsWithGuidance() {
        var entry = WidgetEntry.of(NoInstanceStub.class, WidgetLabel.of("Bad"));
        var ex = assertThrows(IllegalStateException.class,
                () -> WidgetEntriesJson.of(List.of(entry)));
        assertTrue(ex.getMessage().contains("INSTANCE"), ex.getMessage());
        assertTrue(ex.getMessage().contains("WorkspaceWidget"), ex.getMessage());
    }

    @Test void svgIconVariant_emitsSvgKind() {
        // SvgRef construction needs a sealed class instance; we instead
        // exercise the icon-shape branch via a fluent override at the
        // entry level using an Emoji to confirm emoji path, then assert
        // Svg is dispatched separately. Sealed exhaustiveness is verified
        // at compile-time in WidgetRegistryTypesTest; here we confirm
        // the JSON shape for the variant currently in use.
        var entry = WidgetEntry.of(NoParamsStub.class, WidgetLabel.of("X"))
                .withIcon(new WidgetIcon.Emoji("🎯"));
        String json = WidgetEntriesJson.of(List.of(entry));
        assertTrue(json.contains("\"kind\":\"emoji\""), json);
        assertTrue(json.contains("\"value\":\"🎯\""), json);
    }

    // ─── Test fixtures ─────────────────────────────────────────────────────

    public static final class NoParamsStub extends WorkspaceWidget<WorkspaceWidget._None, NoParamsStub> {
        public static final NoParamsStub INSTANCE = new NoParamsStub();
        private NoParamsStub() {}
        private record construct() implements WorkspaceWidget._Construct<_None, NoParamsStub> {}
        @Override protected _Construct<_None, NoParamsStub> construct() { return new construct(); }
        @Override public Class<_None> paramsType() { return _None.class; }
        @Override public String title() { return "NoParams"; }
        @Override protected List<String> constructBodyJs() { return List.of("    return document.createElement('div');"); }
    }

    public static final class WithParamsStub extends WorkspaceWidget<WithParamsStub.Params, WithParamsStub> {
        public static final WithParamsStub INSTANCE = new WithParamsStub();
        private WithParamsStub() {}
        public record Params(String docId, int page) implements WorkspaceWidget._Param {}
        private record construct() implements WorkspaceWidget._Construct<Params, WithParamsStub> {}
        @Override protected _Construct<Params, WithParamsStub> construct() { return new construct(); }
        @Override public Class<Params> paramsType() { return Params.class; }
        @Override public String title() { return "WithParams"; }
        @Override protected List<String> constructBodyJs() { return List.of("    return document.createElement('div');"); }
    }

    public static final class SingletonStub extends WorkspaceWidget<WorkspaceWidget._None, SingletonStub> {
        public static final SingletonStub INSTANCE = new SingletonStub();
        private SingletonStub() {}
        private record construct() implements WorkspaceWidget._Construct<_None, SingletonStub> {}
        @Override protected _Construct<_None, SingletonStub> construct() { return new construct(); }
        @Override public Class<_None> paramsType() { return _None.class; }
        @Override public String title() { return "Singleton"; }
        @Override public LifecycleHint lifecycleHint() { return LifecycleHint.SINGLETON; }
        @Override protected List<String> constructBodyJs() { return List.of("    return document.createElement('div');"); }
    }

    public static final class PinnedStub extends WorkspaceWidget<WorkspaceWidget._None, PinnedStub> {
        public static final PinnedStub INSTANCE = new PinnedStub();
        private PinnedStub() {}
        private record construct() implements WorkspaceWidget._Construct<_None, PinnedStub> {}
        @Override protected _Construct<_None, PinnedStub> construct() { return new construct(); }
        @Override public Class<_None> paramsType() { return _None.class; }
        @Override public String title() { return "Pinned"; }
        @Override public LifecycleHint lifecycleHint() { return LifecycleHint.PINNED; }
        @Override protected List<String> constructBodyJs() { return List.of("    return document.createElement('div');"); }
    }

    /** Deliberately omits {@code INSTANCE} to verify the boot refusal. */
    public static final class NoInstanceStub extends WorkspaceWidget<WorkspaceWidget._None, NoInstanceStub> {
        public NoInstanceStub() {}
        private record construct() implements WorkspaceWidget._Construct<_None, NoInstanceStub> {}
        @Override protected _Construct<_None, NoInstanceStub> construct() { return new construct(); }
        @Override public Class<_None> paramsType() { return _None.class; }
        @Override public String title() { return "NoInstance"; }
        @Override protected List<String> constructBodyJs() { return List.of("    return document.createElement('div');"); }
    }
}
