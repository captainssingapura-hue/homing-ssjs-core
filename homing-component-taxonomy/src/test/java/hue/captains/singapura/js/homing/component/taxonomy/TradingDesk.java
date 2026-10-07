package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A hypothetical extension, as another repository would write it against the {@link Sketch}
 * house: a leaf under a house kind with parts of its own, roles filed in a house branch, and a
 * branch of its own, added deep in the house's catalogue, for a role the house has no word for.
 * Nothing in the house names any of it.
 */
final class TradingDesk {

    private TradingDesk() {}

    /** A branch of its own, under the house's Doing: the catalogue is open at any level. */
    record Trading() implements RoleBranch<Sketch.Doing> {
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

    /** A leaf under the house's Button kind. */
    record TradeButton() implements Component<Sketch.Button> {
        static final TradeButton INSTANCE = new TradeButton();
        @Override public Sketch.Button parent() { return Sketch.Button.INSTANCE; }
    }

    /** A leaf under the house's Card kind, its parts played by the house's components and its own. */
    record OrderTicket() implements Component<Sketch.Card> {
        static final OrderTicket INSTANCE = new OrderTicket();
        private static final ComponentPartDSL DSL = ComponentPartDSL.INSTANCE;
        @Override public Sketch.Card parent() { return Sketch.Card.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            return List.of(DSL.part(Sketch.Heading.INSTANCE).as(Sketch.Title.INSTANCE).one(),
                           DSL.part(Sketch.Badge.INSTANCE).as(Side.INSTANCE).one(),
                           DSL.part(TradeButton.INSTANCE).as(Buy.INSTANCE).one(),
                           DSL.part(TradeButton.INSTANCE).as(Sell.INSTANCE).one(),
                           DSL.part(Sketch.PlainButton.INSTANCE).as(Sketch.Cancel.INSTANCE).optional());
        }
    }

    static final List<Role<?>> CATALOGUE = List.of(Side.INSTANCE, Buy.INSTANCE, Sell.INSTANCE);
}
