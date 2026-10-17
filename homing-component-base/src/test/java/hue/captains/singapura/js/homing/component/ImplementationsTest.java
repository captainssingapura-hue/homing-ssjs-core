package hue.captains.singapura.js.homing.component;

import hue.captains.singapura.js.homing.component.taxonomy.Component;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentPartDSL;
import hue.captains.singapura.js.homing.component.taxonomy.ImplFor;
import hue.captains.singapura.js.homing.component.taxonomy.ImplForBranch;
import hue.captains.singapura.js.homing.component.taxonomy.ImplForLeavesOf;
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
 * lineage, and imports that are not the parts - read through a part nothing implements; and a
 * branch whose every leaf one class realizes, imported for the leaves it stands for.
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
    /** A button whose face is an icon its owner makes and hands in; a strip that holds one. */
    public record IconButton() implements Component<Button> {
        public static final IconButton INSTANCE = new IconButton();
        @Override public Button parent() { return Button.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(ComponentPartDSL.INSTANCE.part(Icon.INSTANCE).as(Symbol.INSTANCE).one()); }
    }
    public record Strip() implements Component<Container> {
        public static final Strip INSTANCE = new Strip();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(ComponentPartDSL.INSTANCE.part(IconButton.INSTANCE).as(Act.INSTANCE).optional()); }
    }
    /** Declared, but not to the reader. */
    public record Stray() implements Component<Control> {
        public static final Stray INSTANCE = new Stray();
        @Override public Control parent() { return Control.INSTANCE; }
    }
    /** Marks, alike but for their look: one class realizes every one; a badge shows the star, a picker any mark. */
    public record Mark() implements L2_ComponentBranch<Control> {
        public static final Mark INSTANCE = new Mark();
        @Override public Control parent() { return Control.INSTANCE; }
    }
    public record Star() implements Component<Mark> {
        public static final Star INSTANCE = new Star();
        @Override public Mark parent() { return Mark.INSTANCE; }
    }
    public record Moon() implements Component<Mark> {
        public static final Moon INSTANCE = new Moon();
        @Override public Mark parent() { return Mark.INSTANCE; }
    }
    public record Badge() implements Component<Container> {
        public static final Badge INSTANCE = new Badge();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(ComponentPartDSL.INSTANCE.part(Star.INSTANCE).as(Symbol.INSTANCE).one()); }
    }
    public record Picker() implements Component<Container> {
        public static final Picker INSTANCE = new Picker();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(ComponentPartDSL.INSTANCE.anyOf(Mark.INSTANCE).as(Symbol.INSTANCE).optional()); }
    }
    /** Seals, one of which holds a part of its own: not alike but for the look. */
    public record Seal() implements L2_ComponentBranch<Control> {
        public static final Seal INSTANCE = new Seal();
        @Override public Control parent() { return Control.INSTANCE; }
    }
    public record Knot() implements Component<Seal> {
        public static final Knot INSTANCE = new Knot();
        @Override public Seal parent() { return Seal.INSTANCE; }
        @Override public List<Slot<?>> parts() { return List.of(ComponentPartDSL.INSTANCE.part(Icon.INSTANCE).as(Symbol.INSTANCE).one()); }
    }
    public record Doing() implements L1_RoleBranch<RoleRoot> {
        public static final Doing INSTANCE = new Doing();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }
    public record Act() implements Role<Doing> { public static final Act INSTANCE = new Act(); @Override public Doing parent() { return Doing.INSTANCE; } }
    public record Acts() implements Role<Doing> { public static final Acts INSTANCE = new Acts(); @Override public Doing parent() { return Doing.INSTANCE; } }
    public record Symbol() implements Role<Doing> { public static final Symbol INSTANCE = new Symbol(); @Override public Doing parent() { return Doing.INSTANCE; } }

    private static final Taxonomy HOUSE = new ReadTaxonomy().read(
            List.of(PlainButton.INSTANCE, DangerButton.INSTANCE, Icon.INSTANCE, Bar.INSTANCE, Dialog.INSTANCE, IconButton.INSTANCE, Strip.INSTANCE,
                    Star.INSTANCE, Moon.INSTANCE, Badge.INSTANCE, Picker.INSTANCE, Knot.INSTANCE),
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
        public record PlainButtonFactory() implements Exportable._Constant<PlainButtonModule> {}
        @Override public ImportsFor<PlainButtonModule> imports() { return ImportsFor.<PlainButtonModule>builder().add(new ModuleImports<>(List.of(new ButtonModule.Button()), ButtonModule.INSTANCE)).build(); }
        @Override public ExportsOf<PlainButtonModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new PlainButton(), new PlainButtonFactory())); }
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

    public record IconButtonModule() implements DomModule<IconButtonModule>, ImplFor<ImplementationsTest.IconButton> {
        public static final IconButtonModule INSTANCE = new IconButtonModule();
        public record IconButton() implements ElementComponent<IconButtonModule> { @Override public String tag() { return "button"; } }
        @Override public ImportsFor<IconButtonModule> imports() {
            return ImportsFor.<IconButtonModule>builder()
                    .add(new ModuleImports<>(List.of(new ButtonModule.Button()), ButtonModule.INSTANCE))
                    .add(new ModuleImports<>(List.of(new IconModule.Icon()), IconModule.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<IconButtonModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new IconButton())); }
    }
    /** The strip makes the icon its button holds, and hands it in. */
    public record StripModule() implements DomModule<StripModule>, ImplFor<ImplementationsTest.Strip> {
        public static final StripModule INSTANCE = new StripModule();
        public record Strip() implements BranchComponent<StripModule> {}
        @Override public ImportsFor<StripModule> imports() {
            return ImportsFor.<StripModule>builder()
                    .add(new ModuleImports<>(List.of(new IconButtonModule.IconButton()), IconButtonModule.INSTANCE))
                    .add(new ModuleImports<>(List.of(new IconModule.Icon()), IconModule.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<StripModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Strip())); }
    }
    /** Code shared by whoever imports it, implementing nothing: the bar's buttons, made in one place. */
    public record BarHelperModule() implements DomModule<BarHelperModule> {
        public static final BarHelperModule INSTANCE = new BarHelperModule();
        public record BarHelper() implements Exportable._Class<BarHelperModule> {}
        @Override public ImportsFor<BarHelperModule> imports() { return ImportsFor.<BarHelperModule>builder().add(new ModuleImports<>(List.of(new PlainButtonModule.PlainButton()), PlainButtonModule.INSTANCE)).build(); }
        @Override public ExportsOf<BarHelperModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new BarHelper())); }
    }
    /** The dialog that realizes its bar through the helper. */
    public record HelpedDialogModule() implements DomModule<HelpedDialogModule>, ImplFor<ImplementationsTest.Dialog> {
        public static final HelpedDialogModule INSTANCE = new HelpedDialogModule();
        public record Dialog() implements BranchComponent<HelpedDialogModule> {}
        @Override public ImportsFor<HelpedDialogModule> imports() {
            return ImportsFor.<HelpedDialogModule>builder()
                    .add(new ModuleImports<>(List.of(new BarHelperModule.BarHelper()), BarHelperModule.INSTANCE))
                    .add(new ModuleImports<>(List.of(new IconModule.Icon()), IconModule.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<HelpedDialogModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Dialog())); }
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

    public record MarkModule() implements DomModule<MarkModule>, ImplForLeavesOf<ImplementationsTest.Mark> {
        public static final MarkModule INSTANCE = new MarkModule();
        public record Mark() implements ElementComponent<MarkModule> { @Override public String tag() { return "span"; } }
        @Override public ImportsFor<MarkModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<MarkModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new Mark())); }
    }

    /** The badge and the picker, each importing the marks' one class: the star the badge's slot names, any mark for the picker. */
    private record BadgeModule() implements DomModule<BadgeModule>, ImplFor<ImplementationsTest.Badge> {
        public record Badge() implements BranchComponent<BadgeModule> {}
        @Override public ImportsFor<BadgeModule> imports() {
            return ImportsFor.<BadgeModule>builder().add(new ModuleImports<>(List.of(new MarkModule.Mark()), MarkModule.INSTANCE)).build();
        }
        @Override public ExportsOf<BadgeModule> exports() { return new ExportsOf<>(this, List.of(new Badge())); }
    }

    private record PickerModule() implements DomModule<PickerModule>, ImplFor<ImplementationsTest.Picker> {
        public record Picker() implements BranchComponent<PickerModule> {}
        @Override public ImportsFor<PickerModule> imports() {
            return ImportsFor.<PickerModule>builder().add(new ModuleImports<>(List.of(new MarkModule.Mark()), MarkModule.INSTANCE)).build();
        }
        @Override public ExportsOf<PickerModule> exports() { return new ExportsOf<>(this, List.of(new Picker())); }
    }

    /** The badge without the marks' class; the icon with it, though it holds no mark. */
    private record BareBadge() implements DomModule<BareBadge>, ImplFor<ImplementationsTest.Badge> {
        public record Badge() implements BranchComponent<BareBadge> {}
        @Override public ImportsFor<BareBadge> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<BareBadge> exports() { return new ExportsOf<>(this, List.of(new Badge())); }
    }

    private record MarkedIcon() implements DomModule<MarkedIcon>, ImplFor<ImplementationsTest.Icon> {
        public record Icon() implements ElementComponent<MarkedIcon> { @Override public String tag() { return "span"; } }
        @Override public ImportsFor<MarkedIcon> imports() {
            return ImportsFor.<MarkedIcon>builder().add(new ModuleImports<>(List.of(new MarkModule.Mark()), MarkModule.INSTANCE)).build();
        }
        @Override public ExportsOf<MarkedIcon> exports() { return new ExportsOf<>(this, List.of(new Icon())); }
    }

    /** A room the marks are mounted in, implementing nothing: its importer's code; and a picker that mounts through it. */
    public record MarkRoomModule() implements DomModule<MarkRoomModule> {
        public static final MarkRoomModule INSTANCE = new MarkRoomModule();
        public record MarkRoom() implements Exportable._Class<MarkRoomModule> {}
        @Override public ImportsFor<MarkRoomModule> imports() {
            return ImportsFor.<MarkRoomModule>builder().add(new ModuleImports<>(List.of(new MarkModule.Mark()), MarkModule.INSTANCE)).build();
        }
        @Override public ExportsOf<MarkRoomModule> exports() { return new ExportsOf<>(INSTANCE, List.of(new MarkRoom())); }
    }

    private record RoomPicker() implements DomModule<RoomPicker>, ImplFor<ImplementationsTest.Picker> {
        public record Picker() implements BranchComponent<RoomPicker> {}
        @Override public ImportsFor<RoomPicker> imports() {
            return ImportsFor.<RoomPicker>builder().add(new ModuleImports<>(List.of(new MarkRoomModule.MarkRoom()), MarkRoomModule.INSTANCE)).build();
        }
        @Override public ExportsOf<RoomPicker> exports() { return new ExportsOf<>(this, List.of(new Picker())); }
    }

    private record StarModule() implements DomModule<StarModule>, ImplFor<ImplementationsTest.Star> {
        public record Star() implements ElementComponent<StarModule> { @Override public String tag() { return "span"; } }
        @Override public ImportsFor<StarModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<StarModule> exports() { return new ExportsOf<>(this, List.of(new Star())); }
    }

    private record GlyphsModule() implements DomModule<GlyphsModule>, ImplForLeavesOf<ImplementationsTest.Mark> {
        public record Glyph() implements ElementComponent<GlyphsModule> { @Override public String tag() { return "span"; } }
        @Override public ImportsFor<GlyphsModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<GlyphsModule> exports() { return new ExportsOf<>(this, List.of(new Glyph())); }
    }

    private record SealModule() implements DomModule<SealModule>, ImplForLeavesOf<ImplementationsTest.Seal> {
        public record Seal() implements ElementComponent<SealModule> { @Override public String tag() { return "span"; } }
        @Override public ImportsFor<SealModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<SealModule> exports() { return new ExportsOf<>(this, List.of(new Seal())); }
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
                        "DangerButtonModule implements DangerButton but exports DangerButton, DangerButtonBuilder: one module, one class - the component, named as its leaf - and at most its companion, named for it",
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
    void aPartsOwnPartsTheOwnerMayMakeAndHandIn() {
        assertEquals(List.of(), check(ButtonModule.INSTANCE, IconModule.INSTANCE, IconButtonModule.INSTANCE, StripModule.INSTANCE),
                "the strip imports the icon its button holds: it makes it, the button holds whichever it is handed");
        assertEquals(List.of("StripModule (Strip) imports DangerButton (DangerButtonModule), which none of its parts declares"),
                check(ButtonModule.INSTANCE, IconModule.INSTANCE, IconButtonModule.INSTANCE, new GreedyStrip()).stream()
                        .map(p -> p.replace("GreedyStrip", "StripModule")).toList(),
                "but nothing its parts do not hold");
    }

    /** The strip with a button none of its parts holds. */
    private record GreedyStrip() implements DomModule<GreedyStrip>, ImplFor<ImplementationsTest.Strip> {
        public record Strip() implements BranchComponent<GreedyStrip> {}
        @Override public ImportsFor<GreedyStrip> imports() {
            return ImportsFor.<GreedyStrip>builder()
                    .add(new ModuleImports<>(List.of(new IconButtonModule.IconButton()), IconButtonModule.INSTANCE))
                    .add(new ModuleImports<>(List.of(new DangerButtonModule.DangerButton()), DangerButtonModule.INSTANCE))
                    .build();
        }
        @Override public ExportsOf<GreedyStrip> exports() { return new ExportsOf<>(this, List.of(new Strip())); }
    }

    @Test
    void aModuleThatImplementsNothing_isItsImportersCode() {
        assertEquals(List.of(), check(ButtonModule.INSTANCE, PlainButtonModule.INSTANCE, IconModule.INSTANCE, BarHelperModule.INSTANCE, HelpedDialogModule.INSTANCE),
                "the dialog's bar is realized in a helper it imports: the helper's plain button is the dialog's");
    }

    /** A companion is the factory named for the component, and a constant: anything else beside the class is refused. */
    @Test
    void aCompanion_namedForItsComponent_isTheOnlyThingBesideIt() {
        assertEquals(List.of("WrongCompanion implements Icon but exports Icon, Maker: one module, one class - the component, named as its leaf - and at most its companion, named for it"),
                check(new WrongCompanion()));
    }

    private record WrongCompanion() implements DomModule<WrongCompanion>, ImplFor<ImplementationsTest.Icon> {
        public record Icon() implements ElementComponent<WrongCompanion> { @Override public String tag() { return "span"; } }
        public record Maker() implements Exportable._Constant<WrongCompanion> {}
        @Override public ImportsFor<WrongCompanion> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<WrongCompanion> exports() { return new ExportsOf<>(this, List.of(new Icon(), new Maker())); }
    }

    @Test
    void anchoredNowhere_twice_offTheTree_outOfTheLineage() {
        assertEquals(List.of(
                        "Icon is implemented twice: by IconModule and OddIconModule",
                        "OddIconModule implements Icon but exports Glyph: one module, one class - the component, named as its leaf - and at most its companion, named for it",
                        "OddIconModule (Icon) imports the base of Button, a branch it is not under",
                        "AdriftModule exports the component Adrift but is anchored nowhere: a component's module implements its leaf (ImplFor)",
                        "StrayModule implements Stray, which the taxonomy does not declare"),
                check(ButtonModule.INSTANCE, IconModule.INSTANCE, OddIconModule.INSTANCE, AdriftModule.INSTANCE, StrayModule.INSTANCE));
    }

    /**
     * One class realizes every mark: the badge imports it for the star its slot names, the picker for
     * any mark; a module that holds no mark imports it for nothing, and one that names none of them
     * never imports what its part calls for.
     */
    @Test
    void aBranchRealizedWhole_standsForEachLeafItsImportersPartsCallFor() {
        assertEquals(List.of(), check(MarkModule.INSTANCE, new BadgeModule(), new PickerModule()),
                "the star the badge's slot names, and any mark the picker shows: each a leaf of the one class");
        assertEquals(List.of("BareBadge (Badge) never imports Star, which its part symbol calls for",
                             "MarkedIcon (Icon) imports every leaf of Mark (MarkModule), but none of its parts holds one"),
                check(MarkModule.INSTANCE, new BareBadge(), new MarkedIcon()));
        assertEquals(List.of(), check(MarkModule.INSTANCE, MarkRoomModule.INSTANCE, new RoomPicker()),
                "through a room that implements nothing: the room is the picker's code, the marks it mounts the picker's");
    }

    /** A leaf of a branch realized whole is the one class's alone; the class is named as the branch; its leaves hold no parts. */
    @Test
    void aBranchRealizedWhole_once_namedAsTheBranch_itsLeavesAlikeButForTheirLook() {
        assertEquals(List.of("Star is implemented twice: by MarkModule, every leaf of its branch, and StarModule"),
                check(MarkModule.INSTANCE, new StarModule()));
        assertEquals(List.of("GlyphsModule implements every leaf of Mark but exports Glyph: one module, one class - the component, named as the branch, made with the leaf it realizes"),
                check(new GlyphsModule()));
        assertEquals(List.of("SealModule implements every leaf of Seal, but Knot holds parts: the leaves of one class are alike but for their look"),
                check(new SealModule()));
    }
}
