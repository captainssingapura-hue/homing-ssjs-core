package hue.captains.singapura.js.homing.studio.base.ui;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.CssImportsFor;

import java.util.List;

/**
 * Layout for the master/detail pattern — a tree on the left, whatever the
 * selected row is about on the right.
 *
 * <p>Its own group rather than part of
 * {@link hue.captains.singapura.js.homing.studio.base.css.StudioStyles}, because
 * it travels with {@link MasterDetail} and both of its consumers — the catalogue
 * listing and the theme picker — take the component, not the sheet.</p>
 *
 * <p><b>The nav is sized by its content, not by a ratio.</b> An earlier
 * catalogue split divided the space by the golden ratio, which was a reasonable
 * answer to not knowing how wide a tree wanted to be. It is the wrong answer
 * once you can ask: a ratio makes the nav's width a function of the window,
 * so the same tree is cramped on a narrow screen and swimming on a wide one.
 * Content sizing makes it a function of the longest label, which is the only
 * thing that actually determines how much room the tree needs.</p>
 */
public record MasterDetailStyles() implements CssGroup<MasterDetailStyles> {

    public static final MasterDetailStyles INSTANCE = new MasterDetailStyles();

    /** The pair, side by side. */
    public record md_split() implements CssClass<MasterDetailStyles> {
        @Override public String body() { return """
            display: flex;
            flex-direction: row;
            align-items: stretch;
            flex: 1 1 auto;
            min-height: 0;
            height: 100%;
            gap: 0;
            """; }
    }

    /**
     * The tree. Width is max(designed floor, longest entry) + fixed padding,
     * said declaratively: max-content sizes to the longest row, min-width keeps
     * a short list from looking pinched, max-width stops one long name from
     * eating the pane. No measuring, no reflow pass, no JS.
     */
    public record md_nav() implements CssClass<MasterDetailStyles> {
        @Override public String body() { return """
            flex: 0 0 auto;
            width: max-content;
            min-width: 190px;
            max-width: 340px;
            min-height: 0;
            overflow-y: auto;
            padding: var(--space-2) var(--space-4) var(--space-2) 0;
            border-right: 1px solid var(--color-border);
            outline: none;
            """; }
    }

    /** Everything the selected row is about. */
    public record md_body() implements CssClass<MasterDetailStyles> {
        @Override public String body() { return """
            flex: 1 1 0;
            min-width: 0;
            min-height: 0;
            overflow-y: auto;
            padding: var(--space-2) 0 var(--space-2) var(--space-4);
            """; }
    }

    @Override
    public CssImportsFor<MasterDetailStyles> cssImports() {
        return CssImportsFor.none(this);
    }

    @Override
    public List<CssClass<MasterDetailStyles>> cssClasses() {
        return List.of(new md_split(), new md_nav(), new md_body());
    }
}
