package hue.captains.singapura.js.homing.workspace;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boundary tests for the workspace-chrome wire shapes — RibbonItem and
 * FooterItem sealed types + their JSON serializers. Per the Tests Carry
 * the Discipline doctrine, the Java↔JS wire shape earns dedicated
 * coverage so a future restructure doesn't silently drift.
 */
final class WorkspaceLayoutJsonTest {

    // ── Sealed-type construction guards ────────────────────────────────────

    @Test void ribbonButton_rejectsNullAndBlankActionId() {
        assertThrows(NullPointerException.class,
                () -> new RibbonItem.Button(null, "tip", "id"));
        assertThrows(NullPointerException.class,
                () -> new RibbonItem.Button(WidgetIcon.Emoji.DEFAULT, null, "id"));
        assertThrows(NullPointerException.class,
                () -> new RibbonItem.Button(WidgetIcon.Emoji.DEFAULT, "tip", null));
        assertThrows(IllegalArgumentException.class,
                () -> new RibbonItem.Button(WidgetIcon.Emoji.DEFAULT, "tip", ""));
        assertThrows(IllegalArgumentException.class,
                () -> new RibbonItem.Button(WidgetIcon.Emoji.DEFAULT, "tip", "  "));
    }

    @Test void ribbonLabel_rejectsNull() {
        assertThrows(NullPointerException.class, () -> new RibbonItem.Label(null));
    }

    @Test void footerButton_rejectsNullAndBlankActionId() {
        assertThrows(NullPointerException.class,
                () -> new FooterItem.Button(null, "tip", "id"));
        assertThrows(NullPointerException.class,
                () -> new FooterItem.Button(WidgetIcon.Emoji.DEFAULT, null, "id"));
        assertThrows(NullPointerException.class,
                () -> new FooterItem.Button(WidgetIcon.Emoji.DEFAULT, "tip", null));
        assertThrows(IllegalArgumentException.class,
                () -> new FooterItem.Button(WidgetIcon.Emoji.DEFAULT, "tip", ""));
    }

    @Test void footerLabel_rejectsNull() {
        assertThrows(NullPointerException.class, () -> new FooterItem.Label(null));
    }

    // ── Ribbon JSON ────────────────────────────────────────────────────────

    @Test void ribbonItems_emptyEmitsEmptyArray() {
        assertEquals("[]", WorkspaceLayoutJson.ribbonItems(List.of()));
    }

    @Test void ribbonItems_buttonShape() {
        String json = WorkspaceLayoutJson.ribbonItems(List.of(
                new RibbonItem.Button(new WidgetIcon.Emoji("🔄"), "Refresh", "do-refresh")
        ));
        assertEquals(
                "[{\"kind\":\"Button\","
              + "\"icon\":{\"kind\":\"emoji\",\"value\":\"🔄\"},"
              + "\"tooltip\":\"Refresh\","
              + "\"actionId\":\"do-refresh\"}]",
                json);
    }

    @Test void ribbonItems_separatorShape() {
        assertEquals("[{\"kind\":\"Separator\"}]",
                WorkspaceLayoutJson.ribbonItems(List.of(new RibbonItem.Separator())));
    }

    @Test void ribbonItems_labelShape() {
        assertEquals("[{\"kind\":\"Label\",\"text\":\"Hello\"}]",
                WorkspaceLayoutJson.ribbonItems(List.of(new RibbonItem.Label("Hello"))));
    }

    @Test void ribbonItems_mixedSequence_preservesOrder() {
        String json = WorkspaceLayoutJson.ribbonItems(List.of(
                new RibbonItem.Label("Mode:"),
                new RibbonItem.Separator(),
                new RibbonItem.Button(new WidgetIcon.Emoji("▶"), "Play", "play"),
                new RibbonItem.Button(new WidgetIcon.Emoji("⏸"), "Pause", "pause")
        ));
        // Iteration order preserved → ribbon left-to-right matches Java list order.
        int idxLabel  = json.indexOf("\"text\":\"Mode:\"");
        int idxSep    = json.indexOf("\"kind\":\"Separator\"");
        int idxPlay   = json.indexOf("\"actionId\":\"play\"");
        int idxPause  = json.indexOf("\"actionId\":\"pause\"");
        assertTrue(idxLabel < idxSep,   "Label before Separator");
        assertTrue(idxSep   < idxPlay,  "Separator before Play");
        assertTrue(idxPlay  < idxPause, "Play before Pause");
    }

    // ── Footer JSON ────────────────────────────────────────────────────────

    @Test void footerItems_emptyEmitsEmptyArray() {
        // An empty array is the meaningful "suppress footer" signal.
        assertEquals("[]", WorkspaceLayoutJson.footerItems(List.of()));
    }

    @Test void footerItems_labelShape() {
        assertEquals("[{\"kind\":\"Label\",\"text\":\"Ready\"}]",
                WorkspaceLayoutJson.footerItems(List.of(new FooterItem.Label("Ready"))));
    }

    @Test void footerItems_separatorShape() {
        assertEquals("[{\"kind\":\"Separator\"}]",
                WorkspaceLayoutJson.footerItems(List.of(new FooterItem.Separator())));
    }

    @Test void footerItems_buttonShape() {
        String json = WorkspaceLayoutJson.footerItems(List.of(
                new FooterItem.Button(new WidgetIcon.Emoji("🔍"), "Zoom in", "zoom-in")
        ));
        assertTrue(json.contains("\"kind\":\"Button\""), json);
        assertTrue(json.contains("\"icon\":{\"kind\":\"emoji\",\"value\":\"🔍\"}"), json);
        assertTrue(json.contains("\"actionId\":\"zoom-in\""), json);
    }

    @Test void footerItems_mixedSequence_preservesOrder() {
        String json = WorkspaceLayoutJson.footerItems(List.of(
                new FooterItem.Label("Status:"),
                new FooterItem.Label("OK"),
                new FooterItem.Separator(),
                new FooterItem.Button(new WidgetIcon.Emoji("🔍"), "Zoom", "zoom")
        ));
        int idxStatus = json.indexOf("\"text\":\"Status:\"");
        int idxOk     = json.indexOf("\"text\":\"OK\"");
        int idxSep    = json.indexOf("\"kind\":\"Separator\"");
        int idxZoom   = json.indexOf("\"actionId\":\"zoom\"");
        assertTrue(idxStatus < idxOk,   "Status: before OK");
        assertTrue(idxOk     < idxSep,  "OK before Separator");
        assertTrue(idxSep    < idxZoom, "Separator before Zoom");
    }

    // ── quoteString ────────────────────────────────────────────────────────

    @Test void quoteString_wrapsAndEscapes() {
        assertEquals("\"hello\"",                WorkspaceLayoutJson.quoteString("hello"));
        assertEquals("\"\"",                     WorkspaceLayoutJson.quoteString(""));
        assertEquals("\"a\\\"b\\\\c\\nd\"",      WorkspaceLayoutJson.quoteString("a\"b\\c\nd"));
    }
}
