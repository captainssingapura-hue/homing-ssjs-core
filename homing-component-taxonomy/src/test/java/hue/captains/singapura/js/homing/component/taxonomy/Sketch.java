package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A hypothetical house, declared through the language - nothing here is the real one; it is
 * written to exercise the declaration.
 *
 * <p>A role catalogue of three top branches and six beneath them, and eleven roles at two
 * depths, one of them named by no component. A taxonomy of six branches at two levels and thirteen components: buttons, texts and a
 * mark to play parts; a plain card with no parts; a profile card, a confirmation, a watchlist
 * and a gauge whose parts say every shape of cardinality; and a shelf whose every part is
 * optional. Title is played by a heading in one place and a caption in another.</p>
 */
final class Sketch {

    private Sketch() {}

    // ── the role catalogue: branches at any level, roles at the leaves ───────

    /** What a part tells about its owner. */
    record Saying() implements L1_RoleBranch<RoleRoot> {
        static final Saying INSTANCE = new Saying();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    /** What a part lets the user do to its owner. */
    record Doing() implements L1_RoleBranch<RoleRoot> {
        static final Doing INSTANCE = new Doing();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    /** How a part makes up its owner's room. */
    record Shaping() implements L1_RoleBranch<RoleRoot> {
        static final Shaping INSTANCE = new Shaping();
        @Override public RoleRoot parent() { return RoleRoot.INSTANCE; }
    }

    record Naming() implements L2_RoleBranch<Saying> {
        static final Naming INSTANCE = new Naming();
        @Override public Saying parent() { return Saying.INSTANCE; }
    }

    record Telling() implements L2_RoleBranch<Saying> {
        static final Telling INSTANCE = new Telling();
        @Override public Saying parent() { return Saying.INSTANCE; }
    }

    record Scaling() implements L2_RoleBranch<Saying> {
        static final Scaling INSTANCE = new Scaling();
        @Override public Saying parent() { return Saying.INSTANCE; }
    }

    record Going() implements L2_RoleBranch<Doing> {
        static final Going INSTANCE = new Going();
        @Override public Doing parent() { return Doing.INSTANCE; }
    }

    record Managing() implements L2_RoleBranch<Doing> {
        static final Managing INSTANCE = new Managing();
        @Override public Doing parent() { return Doing.INSTANCE; }
    }

    /** The acts that end a decision. */
    record Committing() implements L2_RoleBranch<Doing> {
        static final Committing INSTANCE = new Committing();
        @Override public Doing parent() { return Doing.INSTANCE; }
    }

    /** Names a container. */
    record Title() implements Role<Naming> {
        static final Title INSTANCE = new Title();
        @Override public Naming parent() { return Naming.INSTANCE; }
    }

    /** Names a control or an item. */
    record Name() implements Role<Naming> {
        static final Name INSTANCE = new Name();
        @Override public Naming parent() { return Naming.INSTANCE; }
    }

    /** Marks the owner with a short word. */
    record Tag() implements Role<Telling> {
        static final Tag INSTANCE = new Tag();
        @Override public Telling parent() { return Telling.INSTANCE; }
    }

    /** States the owner's value. */
    record Value() implements Role<Telling> {
        static final Value INSTANCE = new Value();
        @Override public Telling parent() { return Telling.INSTANCE; }
    }

    /** Says briefly what the owner is about - catalogued, and named by no component here. */
    record Summary() implements Role<Telling> {
        static final Summary INSTANCE = new Summary();
        @Override public Telling parent() { return Telling.INSTANCE; }
    }

    /** Marks a step of the owner's scale. */
    record Notch() implements Role<Scaling> {
        static final Notch INSTANCE = new Notch();
        @Override public Scaling parent() { return Scaling.INSTANCE; }
    }

    /** Goes to what the owner stands for. */
    record Open() implements Role<Going> {
        static final Open INSTANCE = new Open();
        @Override public Going parent() { return Going.INSTANCE; }
    }

    /** Adds a member. */
    record Add() implements Role<Managing> {
        static final Add INSTANCE = new Add();
        @Override public Managing parent() { return Managing.INSTANCE; }
    }

    /** Does what was decided. */
    record Confirm() implements Role<Committing> {
        static final Confirm INSTANCE = new Confirm();
        @Override public Committing parent() { return Committing.INSTANCE; }
    }

    /** Leaves without deciding. */
    record Cancel() implements Role<Committing> {
        static final Cancel INSTANCE = new Cancel();
        @Override public Committing parent() { return Committing.INSTANCE; }
    }

    /** One of the things the owner lists - filed straight under its top branch, beside no other. */
    record Entry() implements Role<Shaping> {
        static final Entry INSTANCE = new Entry();
        @Override public Shaping parent() { return Shaping.INSTANCE; }
    }

    /** Every role the catalogue files. */
    static final List<Role<?>> CATALOGUE = List.of(Title.INSTANCE, Name.INSTANCE, Tag.INSTANCE, Value.INSTANCE,
            Summary.INSTANCE, Open.INSTANCE, Add.INSTANCE, Confirm.INSTANCE, Cancel.INSTANCE, Entry.INSTANCE, Notch.INSTANCE);

    // ── the taxonomy: kinds ────────────────────────────────────────────────

    record Control() implements L1_ComponentBranch<Root> {
        static final Control INSTANCE = new Control();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Container() implements L1_ComponentBranch<Root> {
        static final Container INSTANCE = new Container();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Text() implements L1_ComponentBranch<Root> {
        static final Text INSTANCE = new Text();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Mark() implements L1_ComponentBranch<Root> {
        static final Mark INSTANCE = new Mark();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Button() implements L2_ComponentBranch<Control> {
        static final Button INSTANCE = new Button();
        @Override public Control parent() { return Control.INSTANCE; }
    }

    record Card() implements L2_ComponentBranch<Container> {
        static final Card INSTANCE = new Card();
        @Override public Container parent() { return Container.INSTANCE; }
    }

    // ── the taxonomy: what plays parts ─────────────────────────────────────

    record Caption() implements Component<Text> {
        static final Caption INSTANCE = new Caption();
        @Override public Text parent() { return Text.INSTANCE; }
    }

    record Heading() implements Component<Text> {
        static final Heading INSTANCE = new Heading();
        @Override public Text parent() { return Text.INSTANCE; }
    }

    record Badge() implements Component<Text> {
        static final Badge INSTANCE = new Badge();
        @Override public Text parent() { return Text.INSTANCE; }
    }

    record Tick() implements Component<Mark> {
        static final Tick INSTANCE = new Tick();
        @Override public Mark parent() { return Mark.INSTANCE; }
    }

    record PlainButton() implements Component<Button> {
        static final PlainButton INSTANCE = new PlainButton();
        @Override public Button parent() { return Button.INSTANCE; }
    }

    record DangerButton() implements Component<Button> {
        static final DangerButton INSTANCE = new DangerButton();
        @Override public Button parent() { return Button.INSTANCE; }
    }

    // ── the taxonomy: what has parts ───────────────────────────────────────

    /** A button that names itself: its name, a caption, always one. */
    record AlternatingButton() implements Component<Button> {
        static final AlternatingButton INSTANCE = new AlternatingButton();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Button parent() { return Button.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(Caption.INSTANCE).as(Name.INSTANCE).one());
        }
    }

    /** The basic container: no parts, what it holds is its user's. */
    record PlainCard() implements Component<Card> {
        static final PlainCard INSTANCE = new PlainCard();
        @Override public Card parent() { return Card.INSTANCE; }
    }

    /** A card for a case: a title and a name, any tags, a way to open it. */
    record ProfileCard() implements Component<Card> {
        static final ProfileCard INSTANCE = new ProfileCard();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Card parent() { return Card.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(Heading.INSTANCE).as(Title.INSTANCE).one(),
                           DSL.part(Caption.INSTANCE).as(Name.INSTANCE).one(),
                           DSL.part(Badge.INSTANCE).as(Tag.INSTANCE).any(),
                           DSL.part(PlainButton.INSTANCE).as(Open.INSTANCE).optional());
        }
    }

    /** A card whose every part is optional: a branch missing its plain leaf, by the look of it. */
    record Shelf() implements Component<Card> {
        static final Shelf INSTANCE = new Shelf();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Card parent() { return Card.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(Badge.INSTANCE).as(Tag.INSTANCE).any(),
                           DSL.part(PlainButton.INSTANCE).as(Open.INSTANCE).optional());
        }
    }

    /** A decision put to the user: its title a caption, here; a confirm, and a cancel if it may be left. */
    record Confirmation() implements Component<Container> {
        static final Confirmation INSTANCE = new Confirmation();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(Caption.INSTANCE).as(Title.INSTANCE).one(),
                           DSL.part(PlainButton.INSTANCE).as(Confirm.INSTANCE).one(),
                           DSL.part(PlainButton.INSTANCE).as(Cancel.INSTANCE).optional());
        }
    }

    /** Profiles listed, at least one, and a way to add another. */
    record Watchlist() implements Component<Container> {
        static final Watchlist INSTANCE = new Watchlist();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(ProfileCard.INSTANCE).as(Entry.INSTANCE).atLeast(1),
                           DSL.part(PlainButton.INSTANCE).as(Add.INSTANCE).optional());
        }
    }

    /** A value on a scale of two to twelve notches, read out. */
    record Gauge() implements Component<Control> {
        static final Gauge INSTANCE = new Gauge();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Control parent() { return Control.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(Tick.INSTANCE).as(Notch.INSTANCE).between(2, 12),
                           DSL.part(Caption.INSTANCE).as(Value.INSTANCE).one());
        }
    }

    /** What the house declares; the rest is reached through the parents and the parts. */
    static final List<Component<?>> DECLARED = List.of(PlainButton.INSTANCE, DangerButton.INSTANCE, AlternatingButton.INSTANCE,
            PlainCard.INSTANCE, ProfileCard.INSTANCE, Shelf.INSTANCE, Confirmation.INSTANCE, Watchlist.INSTANCE, Gauge.INSTANCE);
}
