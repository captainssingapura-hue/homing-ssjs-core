package hue.captains.singapura.js.homing.workspace;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests Carry the Discipline (RFC 0024 doctrine) — Mechanism 1 type
 * contracts. Every typed wrapper's invariant is exercised at the
 * boundary it advertises:
 *
 * <ul>
 *   <li>{@link WidgetLabel} — non-null, non-blank.</li>
 *   <li>{@link WidgetDescription} — non-null only; blank allowed; EMPTY sentinel.</li>
 *   <li>{@link WidgetGroup} — non-null, non-blank; DEFAULT sentinel.</li>
 *   <li>{@link WidgetIcon} — sealed; {@code Emoji} non-blank; {@code Svg} non-null.</li>
 *   <li>{@link WidgetEntry} — five-field null-check; {@code of()} defaults; fluent {@code with*}.</li>
 *   <li>{@link LifecycleHint} — three variants in fixed order so JS code-gen
 *       can rely on {@code ordinal}.</li>
 * </ul>
 */
final class WidgetRegistryTypesTest {

    // ------------------------------------------------------------------
    // WidgetLabel
    // ------------------------------------------------------------------

    @Test void widgetLabel_rejectsNull() {
        assertThrows(NullPointerException.class, () -> new WidgetLabel(null));
        assertThrows(NullPointerException.class, () -> WidgetLabel.of(null));
    }

    @Test void widgetLabel_rejectsBlank() {
        assertThrows(IllegalArgumentException.class, () -> WidgetLabel.of(""));
        assertThrows(IllegalArgumentException.class, () -> WidgetLabel.of("  "));
        assertThrows(IllegalArgumentException.class, () -> WidgetLabel.of("\t\n"));
    }

    @Test void widgetLabel_holdsText() {
        assertEquals("My Widget", WidgetLabel.of("My Widget").text());
    }

    // ------------------------------------------------------------------
    // WidgetDescription
    // ------------------------------------------------------------------

    @Test void widgetDescription_rejectsNull() {
        assertThrows(NullPointerException.class, () -> new WidgetDescription(null));
        assertThrows(NullPointerException.class, () -> WidgetDescription.of(null));
    }

    @Test void widgetDescription_allowsBlank() {
        assertEquals("", WidgetDescription.of("").text());
        assertEquals("   ", WidgetDescription.of("   ").text());
    }

    @Test void widgetDescription_emptySentinel() {
        assertNotNull(WidgetDescription.EMPTY);
        assertEquals("", WidgetDescription.EMPTY.text());
    }

    // ------------------------------------------------------------------
    // WidgetGroup
    // ------------------------------------------------------------------

    @Test void widgetGroup_rejectsNullAndBlank() {
        assertThrows(NullPointerException.class, () -> WidgetGroup.of(null));
        assertThrows(IllegalArgumentException.class, () -> WidgetGroup.of(""));
        assertThrows(IllegalArgumentException.class, () -> WidgetGroup.of("   "));
    }

    @Test void widgetGroup_defaultSentinel() {
        assertNotNull(WidgetGroup.DEFAULT);
        assertEquals("Widgets", WidgetGroup.DEFAULT.name());
    }

    // ------------------------------------------------------------------
    // WidgetIcon (sealed)
    // ------------------------------------------------------------------

    @Test void widgetIconEmoji_rejectsNullAndBlank() {
        assertThrows(NullPointerException.class, () -> new WidgetIcon.Emoji(null));
        assertThrows(IllegalArgumentException.class, () -> new WidgetIcon.Emoji(""));
        assertThrows(IllegalArgumentException.class, () -> new WidgetIcon.Emoji("   "));
    }

    @Test void widgetIconEmoji_defaultSentinel() {
        assertEquals("📦", WidgetIcon.Emoji.DEFAULT.glyph());
    }

    @Test void widgetIconSvg_rejectsNull() {
        assertThrows(NullPointerException.class, () -> new WidgetIcon.Svg(null));
    }

    @Test void widgetIcon_sealedExhaustive() {
        // The sealed switch below is the type system's enforcement that
        // every variant gets a JS code-gen branch. If a new variant is
        // added without amending this switch, the test fails to compile.
        WidgetIcon[] icons = { WidgetIcon.Emoji.DEFAULT };
        for (WidgetIcon icon : icons) {
            String tag = switch (icon) {
                case WidgetIcon.Emoji e -> "emoji:" + e.glyph();
                case WidgetIcon.Svg s   -> "svg";
            };
            assertTrue(tag.startsWith("emoji:") || tag.equals("svg"));
        }
    }

    // ------------------------------------------------------------------
    // WidgetEntry
    // ------------------------------------------------------------------

    @Test void widgetEntry_nullFieldsRejected() {
        WidgetLabel label = WidgetLabel.of("X");
        Class<? extends WorkspaceWidget<?, ?>> cls = StubWidget.class;
        assertThrows(NullPointerException.class,
                () -> new WidgetEntry(null,  label, WidgetIcon.Emoji.DEFAULT, WidgetDescription.EMPTY, WidgetGroup.DEFAULT, Map.of()));
        assertThrows(NullPointerException.class,
                () -> new WidgetEntry(cls, null, WidgetIcon.Emoji.DEFAULT, WidgetDescription.EMPTY, WidgetGroup.DEFAULT, Map.of()));
        assertThrows(NullPointerException.class,
                () -> new WidgetEntry(cls, label, null,                    WidgetDescription.EMPTY, WidgetGroup.DEFAULT, Map.of()));
        assertThrows(NullPointerException.class,
                () -> new WidgetEntry(cls, label, WidgetIcon.Emoji.DEFAULT, null,                    WidgetGroup.DEFAULT, Map.of()));
        assertThrows(NullPointerException.class,
                () -> new WidgetEntry(cls, label, WidgetIcon.Emoji.DEFAULT, WidgetDescription.EMPTY, null,                Map.of()));
        assertThrows(NullPointerException.class,
                () -> new WidgetEntry(cls, label, WidgetIcon.Emoji.DEFAULT, WidgetDescription.EMPTY, WidgetGroup.DEFAULT, null));
    }

    @Test void widgetEntry_ofUsesDefaults() {
        WidgetEntry e = WidgetEntry.of(StubWidget.class, WidgetLabel.of("Stub"));
        assertSame(StubWidget.class,        e.widgetClass());
        assertEquals("Stub",                e.label().text());
        assertSame(WidgetIcon.Emoji.DEFAULT, e.icon());
        assertSame(WidgetDescription.EMPTY,  e.description());
        assertSame(WidgetGroup.DEFAULT,      e.group());
        assertTrue(e.defaults().isEmpty(),  "of() must yield an empty defaults map");
    }

    @Test void widgetEntry_withDefaultsCopiesAndPreservesOrder() {
        var src = new LinkedHashMap<String, String>();
        src.put("first",  "1");
        src.put("second", "2");
        var e = WidgetEntry.of(StubWidget.class, WidgetLabel.of("X")).withDefaults(src);
        assertEquals(List.of("first", "second"), List.copyOf(e.defaults().keySet()),
                "iteration order preserved (form rendering depends on it)");
        // Mutating the source after construction must not affect the entry.
        src.put("third", "3");
        assertEquals(2, e.defaults().size(), "WidgetEntry took a defensive copy");
        // Mutating the entry's map must fail.
        assertThrows(UnsupportedOperationException.class, () -> e.defaults().put("k", "v"));
    }

    @Test void widgetEntry_withMethodsAreImmutable() {
        WidgetEntry base = WidgetEntry.of(StubWidget.class, WidgetLabel.of("Stub"));

        WidgetIcon newIcon = new WidgetIcon.Emoji("📄");
        WidgetEntry e1 = base.withIcon(newIcon);
        assertNotSame(base, e1);
        assertSame(newIcon, e1.icon());
        assertSame(WidgetIcon.Emoji.DEFAULT, base.icon(), "with* must not mutate the source");

        WidgetEntry e2 = base.withDescription(WidgetDescription.of("blurb"));
        assertEquals("blurb", e2.description().text());

        WidgetEntry e3 = base.withGroup(WidgetGroup.of("Tools"));
        assertEquals("Tools", e3.group().name());

        WidgetEntry e4 = base.withLabel(WidgetLabel.of("Renamed"));
        assertEquals("Renamed", e4.label().text());
    }

    // ------------------------------------------------------------------
    // LifecycleHint
    // ------------------------------------------------------------------

    @Test void lifecycleHint_fixedOrder() {
        // JS code-gen may key off ordinal; pin the order.
        LifecycleHint[] all = LifecycleHint.values();
        assertEquals(3, all.length);
        assertEquals(LifecycleHint.MULTI,     all[0]);
        assertEquals(LifecycleHint.SINGLETON, all[1]);
        assertEquals(LifecycleHint.PINNED,    all[2]);
    }

    // ------------------------------------------------------------------
    // WorkspaceShell.validateWidgetEntries (no need to instantiate full shell —
    // exercise the rules via a lightweight harness that calls the same
    // validation logic shape.)
    // ------------------------------------------------------------------

    @Test void widgetEntry_classBound_compileTime() {
        // Demonstrates: only WorkspaceWidget subclasses pass the bound.
        // (Negative cases would be compile errors, hence not testable
        // at runtime — the compile-time refusal IS the test.)
        Class<? extends WorkspaceWidget<?, ?>> ok = StubWidget.class;
        assertNotNull(ok);
    }

    // ------------------------------------------------------------------
    // Test fixture — minimal WorkspaceWidget for class-handle tests.
    // ------------------------------------------------------------------

    static final class StubWidget extends WorkspaceWidget<WorkspaceWidget._None, StubWidget> {
        private record construct() implements WorkspaceWidget._Construct<_None, StubWidget> {}
        @Override protected _Construct<_None, StubWidget> construct() { return new construct(); }
        @Override public Class<_None> paramsType() { return _None.class; }
        @Override public String title() { return "Stub"; }
        @Override protected List<String> constructBodyJs() {
            return List.of("    return document.createElement('div');");
        }
    }
}
