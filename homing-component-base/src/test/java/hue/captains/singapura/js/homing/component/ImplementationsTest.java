package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentPartDSL;
import hue.captains.singapura.js.homing.component.taxonomy.ImplFor;
import hue.captains.singapura.js.homing.component.taxonomy.ImplForBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L1_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L1_RoleBranch;
import hue.captains.singapura.js.homing.component.taxonomy.L2_ComponentBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ReadTaxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.Role;
import hue.captains.singapura.js.homing.component.taxonomy.RoleRoot;
import hue.captains.singapura.js.homing.component.taxonomy.Root;
import hue.captains.singapura.js.homing.component.taxonomy.Slot;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * A small house, its implementations anchored at it, and what the gate refuses: a component
 * anchored nowhere, a leaf implemented twice, a module of two classes, a base imported out of its
 * lineage, and imports that are not the parts - read through a part nothing implements.
 */
class ImplementationsTest {

    // ── the house: a button branch, an icon, a bar of buttons, a dialog with a bar and an icon ──
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
    public record Icon() implements Component<Control> {
        public static final Icon INSTANCE = new Icon();
        @Override public Control parent() { return Control.INSTANCE; }
    }
    /** Nothing implements the bar: a dialog realizes it inline, so the bar's buttons are the dialog's to import. */
    public record Bar() implements Component<Container> {
        public static final Bar INSTANCE = new Bar();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(ComponentPartDSL.INSTANCE.part(PlainButton.INSTANCE).as(Act.INSTANCE).atLeast(1)); }
    }
    public record Dialog() implements Component<Container> {
        public static final Dialog INSTANCE = new Dialog();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(ComponentPartDSL.INSTANCE.part(Bar.INSTANCE).as(Acts.INSTANCE).optional(),
                           ComponentPartDSL.INSTANCE.part(Icon.INSTANCE).as(Symbol.INSTANCE).one());
        }
    }
    /** Declared, but not to the reader. */
    public record Stray() implements Component<Control> {
        public static final Stray INSTANCE = new Stray();
        @Override public Control parent() { return Control.INSTANCE; }
    }
    public record Doing() implements L1_RoleBranch<RoleRoot> {
        public static final Doing INSTANCE = new Doing();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }
    public record Act() implements Role<Doing> { public static final Act INSTANCE = new Act(); @Override public Doing parent() { return Doing.INSTANCE; } }
    public record Acts() implements Role<Doing> { public static final Acts INSTANCE = new Acts(); @Override public Doing parent() { return Doing.INSTANCE; } }
    public record Symbol() implements Role<Doing> { public static final Symbol INSTANCE = new Symbol(); @Override public Doing parent() { return Doing.INSTANCE; } }

    private static final Taxonomy HOUSE = new ReadTaxonomy().read(
            List.of(PlainButton.INSTANCE, DangerButton.INSTANCE, Icon.INSTANCE, Bar.INSTANCE, Dialog.INSTANCE),
            List.of(Act.INSTANCE, Acts.INSTANCE, Symbol.INSTANCE));

    // ── the implementations ──────────────────────────────────────────────
    public record ButtonModule() implements DomModule<ButtonModule>, ImplForBranch<ImplementationsTest.Button> {
        public static final ButtonModule INSTANCE = new ButtonModule();
        public record Button() implements Exportable._Class<ButtonModule> {}
        @Override public ImportsFor<ButtonModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<ButtonModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Button())); }
    }
    public record PlainButtonModule() implements DomModule<PlainButtonModule>, ImplFor<ImplementationsTest.PlainButton> {
        public static final PlainButtonModule INSTANCE = new PlainButtonModule();
        public record PlainButton() implements ElementComponent<PlainButtonModule> { @Override public String tag() { return "button"; } }
        @Override public ImportsFor<PlainButtonModule> imports() { return ImportsFor.<PlainButtonModule>builder().add(new ModuleImports<>(List.of(new ButtonModule.Button()), ButtonModule.INSTANCE)).build(); }
        @Override public ExportsOf<PlainButtonModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PlainButton())); }
    }
    public record IconModule() implements DomModule<IconModule>, ImplFor<ImplementationsTest.Icon> {
        public static final IconModule INSTANCE = new IconModule();
        public record Icon() implements ElementComponent<IconModule> { @Override public String tag() { return "span"; } }
        @Override public ImportsFor<IconModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<IconModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Icon())); }
    }
    public record DialogModule() implements DomModule<DialogModule>, ImplFor<ImplementationsTest.Dialog> {
        public static final DialogModule INSTANCE = new DialogModule();
        public record Dialog() implements BranchComponent<DialogModule> {}
        @Override public ImportsFor<DialogModule> imports() {
            return ImportsFor.<DialogModule>builder()
                    .add(new ModuleImports<>(List.of(new PlainButtonModule.PlainButton()), PlainButtonModule.INSTANCE))
                    .add(new ModuleImports<>(List.of(new IconModule.Icon()), IconModule.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<DialogModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Dialog())); }
    }

    // ── the refused ──────────────────────────────────────────────────────
    /** A dialog that forgets its icon and takes a button no part of it declares. */
    public record LooseDialogModule() implements DomModule<LooseDialogModule>, ImplFor<ImplementationsTest.Dialog> {
        public static final LooseDialogModule INSTANCE = new LooseDialogModule();
        public record Dialog() implements BranchComponent<LooseDialogModule> {}
        @Override public ImportsFor<LooseDialogModule> imports() {
            return ImportsFor.<LooseDialogModule>builder()
                    .add(new ModuleImports<>(List.of(new PlainButtonModule.PlainButton()), PlainButtonModule.INSTANCE))
                    .add(new ModuleImports<>(List.of(new DangerButtonModule.DangerButton()), DangerButtonModule.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<LooseDialogModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Dialog())); }
    }
    /** Two classes, and a base imported from a branch the icon is not under... of a leaf it is not. */
    public record DangerButtonModule() implements DomModule<DangerButtonModule>, ImplFor<ImplementationsTest.DangerButton> {
        public static final DangerButtonModule INSTANCE = new DangerButtonModule();
        public record DangerButton() implements ElementComponent<DangerButtonModule> { @Override public String tag() { return "button"; } }
        public record DangerButtonBuilder() implements Exportable._Constant<DangerButtonModule> {}
        @Override public ImportsFor<DangerButtonModule> imports() { return ImportsFor.<DangerButtonModule>builder().add(new ModuleImports<>(List.of(new ButtonModule.Button()), ButtonModule.INSTANCE)).build(); }
        @Override public ExportsOf<DangerButtonModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new DangerButton(), new DangerButtonBuilder())); }
    }
    public record OddIconModule() implements DomModule<OddIconModule>, ImplFor<ImplementationsTest.Icon> {
        public static final OddIconModule INSTANCE = new OddIconModule();
        public record Glyph() implements ElementComponent<OddIconModule> { @Override public String tag() { return "span"; } }
        @Override public ImportsFor<OddIconModule> imports() { return ImportsFor.<OddIconModule>builder().add(new ModuleImports<>(List.of(new ButtonModule.Button()), ButtonModule.INSTANCE)).build(); }
        @Override public ExportsOf<OddIconModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Glyph())); }
    }
    public record AdriftModule() implements DomModule<AdriftModule> {
        public static final AdriftModule INSTANCE = new AdriftModule();
        public record Adrift() implements BranchComponent<AdriftModule> {}
        @Override public ImportsFor<AdriftModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<AdriftModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Adrift())); }
    }
    public record StrayModule() implements DomModule<StrayModule>, ImplFor<ImplementationsTest.Stray> {
        public static final StrayModule INSTANCE = new StrayModule();
        public record Stray() implements ElementComponent<StrayModule> { @Override public String tag() { return "span"; } }
        @Override public ImportsFor<StrayModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<StrayModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Stray())); }
    }

    private record Shipped(String name, List<EsModule<?>> modules) implements Crate {
        @Override public List<CrateEntry> entries() { return modules.stream().map(CrateEntry::of).toList(); }
    }

    private static List<String> check(EsModule<?>... modules) {
        return Implementations.INSTANCE.validate(HOUSE, List.of(new Shipped("house", List.of(modules))));
    }

    @Test
    void anchored_once_oneClassEach_composedAsDeclared() {
        assertEquals(List.of(), check(ButtonModule.INSTANCE, PlainButtonModule.INSTANCE, IconModule.INSTANCE, DialogModule.INSTANCE),
                "the dialog imports the plain button its unimplemented bar calls for, and the icon its own part does");
    }

    @Test
    void theParts_noMoreAndNoFewer() {
        assertEquals(List.of(
                        "DangerButtonModule implements DangerButton but exports DangerButton, DangerButtonBuilder: one module, one class - the component, named as its leaf",
                        "LooseDialogModule (Dialog) imports DangerButton (DangerButtonModule), which none of its parts declares",
                        "LooseDialogModule (Dialog) never imports Icon, which its part symbol calls for"),
                check(ButtonModule.INSTANCE, PlainButtonModule.INSTANCE, DangerButtonModule.INSTANCE, IconModule.INSTANCE, LooseDialogModule.INSTANCE));
    }

    @Test
    void throughAPartNothingImplements_theCallIsSaid() {
        assertEquals(List.of("NoButtons (Dialog) never imports PlainButton, which its part acts (an unrealized Bar) → act calls for"),
                check(ButtonModule.INSTANCE, PlainButtonModule.INSTANCE, IconModule.INSTANCE, new NoButtons()));
    }

    /** The dialog with its button import taken away. */
    private record NoButtons() implements DomModule<NoButtons>, ImplFor<ImplementationsTest.Dialog> {
        public record Dialog() implements BranchComponent<NoButtons> {}
        @Override public ImportsFor<NoButtons> imports() {
            return ImportsFor.<NoButtons>builder().add(new ModuleImports<>(List.of(new IconModule.Icon()), IconModule.INSTANCE)).build();
        }
        @Override public ExportsOf<NoButtons> exports() { return new ExportsOf<>(this, List.of(new Dialog())); }
    }

    @Test
    void anchoredNowhere_twice_offTheTree_outOfTheLineage() {
        assertEquals(List.of(
                        "Icon is implemented twice: by IconModule and OddIconModule",
                        "OddIconModule implements Icon but exports Glyph: one module, one class - the component, named as its leaf",
                        "OddIconModule (Icon) imports the base of Button, a branch it is not under",
                        "AdriftModule exports the component Adrift but is anchored nowhere: a component's module implements its leaf (ImplFor)",
                        "StrayModule implements Stray, which the taxonomy does not declare"),
                check(ButtonModule.INSTANCE, IconModule.INSTANCE, OddIconModule.INSTANCE, AdriftModule.INSTANCE, StrayModule.INSTANCE));
    }
}
