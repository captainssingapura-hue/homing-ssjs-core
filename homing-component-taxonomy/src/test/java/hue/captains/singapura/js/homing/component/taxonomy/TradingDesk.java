package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A hypothetical extension, as another repository would write it against the {@link Sketch}
 * house: a leaf under a house kind with parts of its own, roles filed in a house family, and a
 * family of its own for a role the house has no word for. Nothing in the house names any of it.
 */
final class TradingDesk {

    private TradingDesk() {}

    /** A family of its own. */
    record Trading() implements RoleFamily<AnyRole> {
        static final Trading INSTANCE = new Trading();
        @Override public AnyRole parent() { return AnyRole.INSTANCE; }
    }

    /** Which side of a trade the owner is on. */
    record Side() implements Role<Trading> {
        static final Side INSTANCE = new Side();
        @Override public Trading family() { return Trading.INSTANCE; }
    }

    /** Filed in the house's family of acts that end a decision. */
    record Buy() implements Role<Sketch.Committing> {
        static final Buy INSTANCE = new Buy();
        @Override public Sketch.Committing family() { return Sketch.Committing.INSTANCE; }
    }

    record Sell() implements Role<Sketch.Committing> {
        static final Sell INSTANCE = new Sell();
        @Override public Sketch.Committing family() { return Sketch.Committing.INSTANCE; }
    }

    /** A leaf under the house's Button kind. */
    record TradeButton() implements Component<Sketch.Button> {
        static final TradeButton INSTANCE = new TradeButton();
        @Override public Sketch.Button parent() { return Sketch.Button.INSTANCE; }
    }

    /** A leaf under the house's Card kind, its parts played by the house's components and its own. */
    record OrderTicket() implements Component<Sketch.Card> {
        static final OrderTicket INSTANCE = new OrderTicket();
        @Override public Sketch.Card parent() { return Sketch.Card.INSTANCE; }
        @Override public List<Slot<?>> parts() {
            final ComponentPartDSL dsl = ComponentPartDSL.INSTANCE;
            return List.of(dsl.part(Sketch.Heading.INSTANCE).as(Sketch.Title.INSTANCE).one(),
                           dsl.part(Sketch.Badge.INSTANCE).as(Side.INSTANCE).one(),
                           dsl.part(TradeButton.INSTANCE).as(Buy.INSTANCE).one(),
                           dsl.part(TradeButton.INSTANCE).as(Sell.INSTANCE).one(),
                           dsl.part(Sketch.PlainButton.INSTANCE).as(Sketch.Cancel.INSTANCE).optional());
        }
    }

    static final List<Role<?>> CATALOGUE = List.of(Side.INSTANCE, Buy.INSTANCE, Sell.INSTANCE);
}
