package hue.captains.singapura.js.homing.core.js;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Locks the JS the writer emits for representative tree shapes.
 * Behavioural contract: bootstrap output is structural only — no chrome
 * content, no widget-mount calls, just createBranch / activate /
 * createElement.
 */
final class PartyTreeWriterTest {

    @Test
    void flat_chrome_plus_widget_slot() {
        var tree = new PartyTree(List.of(
                PartyTree.FrameworkBranch.flat(
                        "header",
                        PartyTree.OwnerRef.PartyChief.INSTANCE,
                        List.of(
                                new PartyTree.ElementDecl("brand",       "div"),
                                new PartyTree.ElementDecl("breadcrumb",  "nav"),
                                new PartyTree.ElementDecl("themePicker", "div")
                        )),
                new PartyTree.AppBodyBranch("main", PartyTree.OwnerRef.ShellChief.INSTANCE)
        ));

        var actual = String.join("\n", new PartyTreeWriter(tree).writeBootstrap());
        var expected = """
                var partyChief = Object.freeze({ toString: function(){ return 'partyChief'; } });
                var shellChief = Object.freeze({ toString: function(){ return 'shellChief'; } });

                var header = domOpsParty.createBranch('header');
                header.activate(partyChief);
                var brand = header.createElement('brand', 'div');
                var breadcrumb = header.createElement('breadcrumb', 'nav');
                var themePicker = header.createElement('themePicker', 'div');

                var main = domOpsParty.createBranch('main');
                main.activate(shellChief);""";
        assertEquals(expected, actual);
    }

    @Test
    void no_party_chief_sentinel_if_unused() {
        var tree = new PartyTree(List.of(
                new PartyTree.AppBodyBranch("main", PartyTree.OwnerRef.ShellChief.INSTANCE)
        ));
        var out = String.join("\n", new PartyTreeWriter(tree).writeBootstrap());
        assertTrue(out.contains("shellChief"), "shellChief should be minted");
        assertTrue(!out.contains("partyChief"), "partyChief should NOT be minted when unused");
    }

    @Test
    void nested_framework_branches_recurse() {
        var tree = new PartyTree(List.of(
                new PartyTree.FrameworkBranch(
                        "header",
                        PartyTree.OwnerRef.PartyChief.INSTANCE,
                        List.of(new PartyTree.ElementDecl("brand", "div")),
                        List.of(PartyTree.FrameworkBranch.flat(
                                "subnav",
                                PartyTree.OwnerRef.PartyChief.INSTANCE,
                                List.of(new PartyTree.ElementDecl("home", "a"))
                        ))
                )
        ));
        var out = String.join("\n", new PartyTreeWriter(tree).writeBootstrap());
        assertTrue(out.contains("var header = domOpsParty.createBranch('header');"));
        assertTrue(out.contains("var subnav = header.createBranch('subnav');"));
        assertTrue(out.contains("var home = subnav.createElement('home', 'a');"));
    }

    @Test
    void fresh_widget_chief_rejected_at_l1() {
        var tree = new PartyTree(List.of(
                new PartyTree.AppBodyBranch("widget", PartyTree.OwnerRef.FreshWidgetChief.INSTANCE)
        ));
        assertThrows(IllegalStateException.class,
                () -> new PartyTreeWriter(tree).writeBootstrap(),
                "FreshWidgetChief is per-mount, not bootstrap-time");
    }
}
