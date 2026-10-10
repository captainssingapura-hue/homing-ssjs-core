package hue.captains.singapura.js.homing.component.taxonomy;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A taxonomy's leaves are its own and those of the modules on the classpath that extend it: the
 * {@link Sketch} house, extended by the {@link TradingDeskExtension} the tests' classpath names,
 * is read with the desk's leaves and roles, and never with those of a module extending another.
 */
class TaxonomyExtensionsTest {

    @Test
    void theExtensionsOfASource_areThoseOnTheClasspathThatNameIt() {
        assertEquals(List.of(new TradingDeskExtension()), TaxonomyExtensions.INSTANCE.of(SketchSource.class));
        assertEquals(List.of(new ElsewhereExtension()), TaxonomyExtensions.INSTANCE.of(ElsewhereExtension.Elsewhere.class));
    }

    @Test
    void aSourcesOwnLeavesComeFirst_thenTheExtensions() {
        var components = TaxonomyExtensions.INSTANCE.components(SketchSource.class, Sketch.DECLARED);
        assertEquals(Sketch.DECLARED, components.subList(0, Sketch.DECLARED.size()));
        assertEquals(List.of(TradingDesk.OrderTicket.INSTANCE), components.subList(Sketch.DECLARED.size(), components.size()));
        assertEquals(TradingDesk.CATALOGUE, TaxonomyExtensions.INSTANCE.roles(SketchSource.class, List.of()));
    }

    @Test
    void theTreeIsReadAsEver_fromWhatIsThere() {
        Taxonomy t = SketchSource.INSTANCE.read();
        assertTrue(t.children(Sketch.Card.INSTANCE).contains(TradingDesk.OrderTicket.INSTANCE), "the desk's leaf, under the house's branch");
        assertTrue(t.children(Sketch.Button.INSTANCE).contains(TradingDesk.TradeButton.INSTANCE), "and what its parts reach");
        assertTrue(t.children(Sketch.Doing.INSTANCE).contains(TradingDesk.Trading.INSTANCE), "its own branch of the catalogue");
        assertFalse(new ReadTaxonomy().read(Sketch.DECLARED, Sketch.CATALOGUE).components().contains(TradingDesk.OrderTicket.INSTANCE),
                "the house alone has none of it");
    }
}
