package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentPartDSL;
import hue.captains.singapura.js.homing.component.taxonomy.FamilyHead;
import hue.captains.singapura.js.homing.component.taxonomy.Flag;
import hue.captains.singapura.js.homing.component.taxonomy.Importance;
import hue.captains.singapura.js.homing.component.taxonomy.L1_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L1_RoleBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L1_StateBranch;
import hue.captains.singapura.js.homing.component.taxonomy.Pseudo;
import hue.captains.singapura.js.homing.component.taxonomy.ReadTaxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.Role;
import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Slot;
import hue.captains.singapura.js.homing.component.taxonomy.StateRoot;
import hue.captains.singapura.js.homing.component.taxonomy.StateType;
import hue.captains.singapura.js.homing.component.taxonomy.StateValue;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomySource;

import java.util.List;
import java.util.Map;

/**
 * A taxonomy with states, for the leaves' classes: a tab that is enabled, hovered, selected and held by the keys, each
 * at a level of its own; and a strip whose lead tab is a tab cast as Lead - a casting, its own class, the tab's states.
 */
public record StatedTaxonomy() implements TaxonomySource {

    public static final StatedTaxonomy INSTANCE = new StatedTaxonomy();

    public record Control() implements L1_ComponentBranch<Root>, FamilyHead {
        public static final Control INSTANCE = new Control();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    public record Leading() implements L1_RoleBranch<RoleRoot> {
        public static final Leading INSTANCE = new Leading();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    public record Lead() implements Role<Leading> {
        public static final Lead INSTANCE = new Lead();
        @Override public Leading parent() { return Leading.INSTANCE; }
    }

    public record Availability() implements L1_StateBranch<StateRoot> {
        public static final Availability INSTANCE = new Availability();
        @Override public StateRoot parent() { return StateRoot.INSTANCE; }
    }

    public record Enabled() implements Flag<Availability>, Importance.Quaternary {
        public static final Enabled INSTANCE = new Enabled();
        @Override public Availability parent() { return Availability.INSTANCE; }
        @Override public boolean rest() { return true; }
        @Override public Map<StateValue, String> words() { return Map.of(StateValue.FALSE, "disabled"); }
    }

    public record Hovered() implements Flag<Availability>, Importance.Quinary, Pseudo {
        public static final Hovered INSTANCE = new Hovered();
        @Override public Availability parent() { return Availability.INSTANCE; }
        @Override public Map<StateValue, String> words() { return Map.of(StateValue.TRUE, "hover"); }
        @Override public String pseudoClass() { return ":hover"; }
    }

    public record Selected() implements Flag<Availability>, Importance.Tertiary {
        public static final Selected INSTANCE = new Selected();
        @Override public Availability parent() { return Availability.INSTANCE; }
        @Override public Map<StateValue, String> words() { return Map.of(StateValue.TRUE, "selected"); }
    }

    public record Keys() implements StateType<Availability>, Importance.Secondary {
        public static final Keys INSTANCE = new Keys();
        private static final List<StateValue> VALUES = List.of(new StateValue("away"), new StateValue("candidate"), new StateValue("held"));
        @Override public Availability parent() { return Availability.INSTANCE; }
        @Override public List<StateValue> values() { return VALUES; }
        @Override public Map<StateValue, String> words() { return Map.of(VALUES.get(1), "candidate", VALUES.get(2), "held"); }
    }

    /** Listed with the keys first, which its levels put last. */
    public record Tab() implements Component<Control> {
        public static final Tab INSTANCE = new Tab();
        @Override public Control parent() { return Control.INSTANCE; }
        @Override public List<StateType<?>> states() { return List.of(Keys.INSTANCE, Enabled.INSTANCE, Hovered.INSTANCE, Selected.INSTANCE); }
    }

    /** Its lead tab: a tab cast as Lead, a leaf for its class, with the tab's states. */
    public record Strip() implements Component<Control> {
        public static final Strip INSTANCE = new Strip();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Control parent() { return Control.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(DSL.part(Tab.INSTANCE).as(Lead.INSTANCE).one()); }
    }

    @Override public Taxonomy read() { return new ReadTaxonomy().read(List.of(Tab.INSTANCE, Strip.INSTANCE)); }
}
