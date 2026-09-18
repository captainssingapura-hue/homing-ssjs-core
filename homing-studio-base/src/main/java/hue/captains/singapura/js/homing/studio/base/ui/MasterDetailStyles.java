package hue.captains.singapura.js.homing.studio.base.ui;

import hue.captains.singapura.js.homing.core.CssClass;
import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.core.Wearable;

import java.util.List;

import static hue.captains.singapura.js.homing.design.DesignClass.of;
import static hue.captains.singapura.js.homing.design.Target.*;
import static hue.captains.singapura.js.homing.design.Structure.*;


/**
 * The master/detail split: a nav column beside a detail body. Structure
 * only; the nav wears the rail along its trailing edge.
 */
public record MasterDetailStyles() implements CssGroup<MasterDetailStyles> {
    public static final MasterDetailStyles INSTANCE = new MasterDetailStyles();

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
    public record md_nav() implements CssClass<MasterDetailStyles> {
        @Override public List<? extends Wearable> wears() { return List.of(of(Rail.class, Color.Edge.class), of(Rail.class, Shape.Rule.class)); }
        @Override public String body() { return """
            flex: 0 0 auto;
            width: max-content;
            min-width: 190px;
            max-width: 340px;
            min-height: 0;
            overflow-y: auto;
            padding: 8px 16px 8px 0;
            outline: none;
            """; }
    }
    public record md_body() implements CssClass<MasterDetailStyles> {
        @Override public String body() { return """
            flex: 1 1 0;
            min-width: 0;
            min-height: 0;
            overflow-y: auto;
            padding: 8px 0 8px 16px;
            """; }
    }

    @Override
    public List<CssClass<MasterDetailStyles>> cssClasses() {
        return List.of(new md_split(), new md_nav(), new md_body());
    }
}
