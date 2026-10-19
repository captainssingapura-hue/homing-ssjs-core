package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A hypothetical extension, as another repository would write it against the {@link Sketch}
 * house: a leaf under a house branch with parts of its own, roles filed in a house branch, and a
 * branch of its own, added deep in the house's catalogue, for a role the house has no word for.
 * Nothing in the house names any of it.
 */
final class TradingDesk {

    private TradingDesk() {}

    /** A branch of its own, under the house's Doing: the catalogue is open at any level. */
    record Trading() implements L2_RoleBranch<Sketch.Doing> {
        static final Trading INSTANCE = new Trading();
        @Override public Sketch.Doing parent() { return Sketch.Doing.INSTANCE; }
    }

    /** Which side of a trade the owner is on. */
    record Side() implements Role<Trading> {
        static final Side INSTANCE = new Side();
        @Override public Trading parent() { return Trading.INSTANCE; }
    }

    /** Filed in the house's branch of acts that end a decision. */
    record Buy() implements Role<Sketch.Committing> {
        static final Buy INSTANCE = new Buy();
        @Override public Sketch.Committing parent() { return Sketch.Committing.INSTANCE; }
    }

    record Sell() implements Role<Sketch.Committing> {
        static final Sell INSTANCE = new Sell();
        @Override public Sketch.Committing parent() { return Sketch.Committing.INSTANCE; }
    }

    /** A leaf under the house's Button branch. */
    record TradeButton() implements Component<Sketch.Button>, WithRoles<Trades> {
        static final TradeButton INSTANCE = new TradeButton();
        @Override public Sketch.Button parent() { return Sketch.Button.INSTANCE; }
        @Override public Trades roleSet() { return Trades.INSTANCE; }
    }

    /**
     * A leaf under the house's Text branch, cast as its own Side: the house's badge is cast only in the roles its set gives,
     * and Side, the desk's word, is none of them (D55).
     */
    record SideFlag() implements Component<Sketch.Text>, WithRoles<Sides> {
        static final SideFlag INSTANCE = new SideFlag();
        @Override public Sketch.Text parent() { return Sketch.Text.INSTANCE; }
        @Override public Sides roleSet() { return Sides.INSTANCE; }
    }

    /** A leaf under the house's Card branch, its parts played by the house's components and its own. */
    record OrderTicket() implements Component<Sketch.Card> {
        static final OrderTicket INSTANCE = new OrderTicket();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Sketch.Card parent() { return Sketch.Card.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(Sketch.Heading.INSTANCE).as(Sketch.Title.INSTANCE).one(),
                           DSL.part(SideFlag.INSTANCE).as(Side.INSTANCE).one(),
                           DSL.part(TradeButton.INSTANCE).as(Buy.INSTANCE).one(),
                           DSL.part(TradeButton.INSTANCE).as(Sell.INSTANCE).one(),
                           DSL.part(Sketch.PlainButton.INSTANCE).as(Sketch.Cancel.INSTANCE).optional());
        }
    }

    static final List<Role<?>> CATALOGUE = List.of(Side.INSTANCE, Buy.INSTANCE, Sell.INSTANCE);

    // ── the roles each component may be cast in (RFC 0067, D55) ──────────

    /** What a trade button may be cast as. */
    record Trades() implements RoleSet {
        static final Trades INSTANCE = new Trades();
        @Override public List<Role<?>> roles() { return List.of(Buy.INSTANCE, Sell.INSTANCE); }
    }

    /** What a side flag may be cast as. */
    record Sides() implements RoleSet {
        static final Sides INSTANCE = new Sides();
        @Override public List<Role<?>> roles() { return List.of(Side.INSTANCE); }
    }
}
