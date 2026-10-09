package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentPartDSL;
import hue.captains.singapura.js.homing.component.taxonomy.L1_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L1_RoleBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L2_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L2_RoleBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ReadTaxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.Role;
import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Slot;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.TaxonomySource;

import java.util.List;

/**
 * The taxonomy the resolution's cases are written against: two buttons under a button branch, a
 * floating pane, and a dialog that casts all three - a plain button as Cancel, a danger button as
 * Go, the pane as its Window. Go and Cancel are Committing, under Doing; Window is Shaping, which
 * is functional.
 */
public record ResolvingTaxonomy() implements TaxonomySource {

    public static final ResolvingTaxonomy INSTANCE = new ResolvingTaxonomy();

    public record Control() implements L1_ComponentBranch<Root> {
        public static final Control INSTANCE = new Control();
        @Override public Root parent() { return Root.INSTANCE; }
    }
    public record Button() implements L2_ComponentBranch<Control> {
        public static final Button INSTANCE = new Button();
        @Override public Control parent() { return Control.INSTANCE; }
    }
    public record Container() implements L1_ComponentBranch<Root> {
        public static final Container INSTANCE = new Container();
        @Override public Root parent() { return Root.INSTANCE; }
    }
    public record PlainButton() implements Component<Button> {
        public static final PlainButton INSTANCE = new PlainButton();
        @Override public Button parent() { return Button.INSTANCE; }
    }
    public record DangerButton() implements Component<Button> {
        public static final DangerButton INSTANCE = new DangerButton();
        @Override public Button parent() { return Button.INSTANCE; }
    }
    public record FloatingPane() implements Component<Container> {
        public static final FloatingPane INSTANCE = new FloatingPane();
        @Override public Container parent() { return Container.INSTANCE; }
    }
    public record Dialog() implements Component<Container> {
        public static final Dialog INSTANCE = new Dialog();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(FloatingPane.INSTANCE).as(Window.INSTANCE).one(),
                           DSL.part(DangerButton.INSTANCE).as(Go.INSTANCE).optional(),
                           DSL.part(PlainButton.INSTANCE).as(Cancel.INSTANCE).one());
        }
    }

    public record Doing() implements L1_RoleBranch<RoleRoot> {
        public static final Doing INSTANCE = new Doing();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }
    public record Committing() implements L2_RoleBranch<Doing> {
        public static final Committing INSTANCE = new Committing();
        @Override public Doing parent() { return Doing.INSTANCE; }
    }
    public record Shaping() implements L1_RoleBranch<RoleRoot> {
        public static final Shaping INSTANCE = new Shaping();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
        @Override public boolean functional() { return true; }
    }
    public record Go() implements Role<Committing> {
        public static final Go INSTANCE = new Go();
        @Override public Committing parent() { return Committing.INSTANCE; }
    }
    public record Cancel() implements Role<Committing> {
        public static final Cancel INSTANCE = new Cancel();
        @Override public Committing parent() { return Committing.INSTANCE; }
    }
    public record Window() implements Role<Shaping> {
        public static final Window INSTANCE = new Window();
        @Override public Shaping parent() { return Shaping.INSTANCE; }
    }

    @Override public Taxonomy read() {
        return new ReadTaxonomy().read(List.of(Dialog.INSTANCE), List.of(Go.INSTANCE, Cancel.INSTANCE, Window.INSTANCE));
    }
}
