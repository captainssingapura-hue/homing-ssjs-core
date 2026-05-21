package hue.captains.singapura.js.homing.core.js;

import java.util.ArrayList;
import java.util.List;

/**
 * RFC 0024 — emits the bootstrap JS that constructs a {@link PartyTree}
 * shape under {@code domOpsParty}. Analogous to the framework's existing
 * {@code EsModuleWriter} / {@code ExportWriter}: a declarative Java
 * record becomes generated JS.
 *
 * <h2>Output shape</h2>
 *
 * <p>For a tree like:</p>
 *
 * <pre>{@code
 * new PartyTree(List.of(
 *     FrameworkBranch.flat("header", PartyChief.INSTANCE, List.of(
 *         new ElementDecl("brand",       "div"),
 *         new ElementDecl("breadcrumb",  "nav"),
 *         new ElementDecl("themePicker", "div")
 *     )),
 *     new AppBodyBranch("main", ShellChief.INSTANCE)
 * ))
 * }</pre>
 *
 * <p>the writer emits (roughly):</p>
 *
 * <pre>{@code
 * var partyChief = Object.freeze({ toString: function(){ return 'partyChief'; } });
 * var shellChief = Object.freeze({ toString: function(){ return 'shellChief'; } });
 *
 * var header = domOpsParty.createBranch('header');
 * header.activate(partyChief);
 * var brand = header.createElement('brand', 'div');
 * var breadcrumb = header.createElement('breadcrumb', 'nav');
 * var themePicker = header.createElement('themePicker', 'div');
 *
 * var main = domOpsParty.createBranch('main');
 * main.activate(shellChief);
 * }</pre>
 *
 * <p>The output is the <i>structural</i> bootstrap only — chrome content
 * (textContent on the brand element, fetched breadcrumb data, theme
 * picker behaviour) is the consumer's responsibility, expressed as
 * separate JS lines that reference the variables this writer brings into
 * scope.</p>
 *
 * <h2>Variable naming</h2>
 *
 * <p>Each {@link PartyTree.L1Branch#name()} and each {@link PartyTree.ElementDecl#name()}
 * becomes a JS variable of the same name. The writer assumes these
 * names are valid JS identifiers and do not collide across the tree;
 * the consumer is responsible for choosing names that work in scope.
 * Nested {@link PartyTree.FrameworkBranch} children use their own
 * {@code name} as the variable.</p>
 *
 * <h2>FreshWidgetChief is not emitted</h2>
 *
 * <p>{@link PartyTree.OwnerRef.FreshWidgetChief} is meaningless at the
 * static-tree level — its semantics are "mint a fresh owner at mount
 * time." The writer rejects it for declared L1 branches; consumers that
 * need per-mount widget owners construct them in their own JS at the
 * mount call site, not via PartyTree.</p>
 *
 * @since RFC 0024 Phase P1a
 */
public record PartyTreeWriter(PartyTree tree) {

    /**
     * Emits the bootstrap JS lines for this tree. The lines do not include
     * any leading import; the consumer is responsible for ensuring
     * {@code domOpsParty} is in scope (via an {@code ImportsFor} entry
     * on {@link DomOpsPartyModule#INSTANCE} listing
     * {@link DomOpsPartyModule.domOpsParty}).
     */
    public List<String> writeBootstrap() {
        var lines = new ArrayList<String>();
        emitChiefSentinels(lines);
        for (PartyTree.L1Branch b : tree.branches()) {
            lines.add("");
            emitL1(b, lines);
        }
        return lines;
    }

    /**
     * Mints the sentinel owner objects ({@code partyChief}, {@code shellChief})
     * used by L1 branches. {@code FreshWidgetChief} is per-mount and is
     * NOT minted here.
     */
    private void emitChiefSentinels(List<String> lines) {
        boolean needsParty = false;
        boolean needsShell = false;
        for (PartyTree.L1Branch b : tree.branches()) {
            switch (b.owner()) {
                case PartyTree.OwnerRef.PartyChief p -> needsParty = true;
                case PartyTree.OwnerRef.ShellChief s -> needsShell = true;
                case PartyTree.OwnerRef.FreshWidgetChief f -> throw new IllegalStateException(
                        "FreshWidgetChief is not valid for a declared L1 branch — branch \""
                      + b.name() + "\". FreshWidgetChief is minted per mount, not at tree bootstrap.");
            }
        }
        // Note: we also need to check nested FrameworkBranch.children for
        // the same owner kinds. For P1a chrome is flat (no children); add the
        // recursive check when a real nested-chrome use case shows up.
        if (needsParty) {
            lines.add("var partyChief = Object.freeze({ toString: function(){ return 'partyChief'; } });");
        }
        if (needsShell) {
            lines.add("var shellChief = Object.freeze({ toString: function(){ return 'shellChief'; } });");
        }
    }

    /** Emits {@code createBranch} + {@code activate} (+ contents) for one L1 branch. */
    private void emitL1(PartyTree.L1Branch b, List<String> lines) {
        switch (b) {
            case PartyTree.FrameworkBranch fb -> emitFrameworkBranch(fb, "domOpsParty", lines);
            case PartyTree.AppBodyBranch ab -> {
                lines.add("var " + ab.name() + " = domOpsParty.createBranch('" + ab.name() + "');");
                lines.add(ab.name() + ".activate(" + ownerJs(ab.owner()) + ");");
            }
        }
    }

    /** Emits a FrameworkBranch (recursively for nested children). */
    private void emitFrameworkBranch(PartyTree.FrameworkBranch fb, String parentJsVar, List<String> lines) {
        lines.add("var " + fb.name() + " = " + parentJsVar + ".createBranch('" + fb.name() + "');");
        lines.add(fb.name() + ".activate(" + ownerJs(fb.owner()) + ");");
        for (PartyTree.ElementDecl el : fb.elements()) {
            lines.add("var " + el.name() + " = " + fb.name() + ".createElement('"
                    + el.name() + "', '" + el.tagName() + "');");
        }
        for (PartyTree.FrameworkBranch child : fb.children()) {
            emitFrameworkBranch(child, fb.name(), lines);
        }
    }

    /** Resolves an OwnerRef to its JS-variable name. */
    private static String ownerJs(PartyTree.OwnerRef owner) {
        return switch (owner) {
            case PartyTree.OwnerRef.PartyChief p -> "partyChief";
            case PartyTree.OwnerRef.ShellChief s -> "shellChief";
            case PartyTree.OwnerRef.FreshWidgetChief f -> throw new IllegalStateException(
                    "FreshWidgetChief is not a bootstrap-time owner.");
        };
    }
}
