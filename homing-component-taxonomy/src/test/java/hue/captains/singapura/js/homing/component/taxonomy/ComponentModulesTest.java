package hue.captains.singapura.js.homing.component.taxonomy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A components taxonomy read from a flat list of component modules, registered explicitly: each a jar's flat list of
 * its components, their parts coming in transitively, the branches, roles and states from them; a component one jar's.
 */
class ComponentModulesTest {

    /** The sketch house's jar: every component it declares. */
    record SketchModule() implements ComponentModule {
        @Override public List<? extends Component<?>> components() { return Sketch.DECLARED; }
    }

    /** The trading desk's jar: its ticket alone - the trade button, its part, comes in with it. */
    record DeskModule() implements ComponentModule {
        @Override public List<? extends Component<?>> components() { return List.of(TradingDesk.OrderTicket.INSTANCE); }
    }

    /** A jar that lists the sketch's plain button as its own too. */
    record Grabbing() implements ComponentModule {
        @Override public List<? extends Component<?>> components() { return List.of(Sketch.PlainButton.INSTANCE); }
    }

    private static Taxonomy read(List<? extends ComponentModule> modules) {
        return ComponentModules.INSTANCE.read(modules, Sketch.CATALOGUE, List.of());
    }

    @Test
    void theModulesRegistered_theirComponents_andTheirPartsTransitively() {
        Taxonomy sketch = read(List.of(new SketchModule()));
        assertFalse(sketch.components().contains(TradingDesk.OrderTicket.INSTANCE), "only what is registered");
        Taxonomy both = read(List.of(new SketchModule(), new DeskModule()));
        assertTrue(both.children(Sketch.Card.INSTANCE).contains(TradingDesk.OrderTicket.INSTANCE), "the desk's ticket, under the house's branch");
        assertTrue(both.components().contains(TradingDesk.TradeButton.INSTANCE), "its trade button, never listed: a part, brought in with it");
        assertTrue(both.children(Sketch.Doing.INSTANCE).contains(TradingDesk.Trading.INSTANCE), "the role its part is cast in, filed in the catalogue");
        assertEquals(List.of(), both.states().types(), "no state its components have: a type of state comes in only with a component that has it");
        var bringers = ComponentModules.INSTANCE.bringers(List.of(new SketchModule(), new DeskModule()), both);
        assertEquals(DeskModule.class, bringers.get(TradingDesk.TradeButton.INSTANCE).getClass(), "brought by the module whose component holds it as a part");
        assertTrue(ComponentModules.INSTANCE.lister(List.of(new SketchModule(), new DeskModule()), TradingDesk.TradeButton.INSTANCE).isEmpty(),
                "and listed by none");
    }

    @Test
    void aModuleRegisteredTwice_orAComponentListedTwice_isRefused() {
        String refused = assertThrows(IllegalArgumentException.class,
                () -> read(List.of(new SketchModule(), new DeskModule(), new DeskModule(), new Grabbing()))).getMessage();
        assertTrue(refused.contains("DeskModule is registered twice"), refused);
        assertTrue(refused.contains("plain-button is listed by SketchModule and by Grabbing: a component is one jar's"), refused);
    }
}
