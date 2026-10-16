package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.design.semantic.BaseColours;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.semantic.Derives;
import hue.captains.singapura.js.homing.design.semantic.Implementation;
import hue.captains.singapura.js.homing.design.styles.StyleDeclaration;
import hue.captains.singapura.js.homing.design.styles.ink.InkStyles;
import hue.captains.singapura.js.homing.design.styles.loudly.LoudlyStyles;
import hue.captains.singapura.js.homing.design.styles.plain.PlainStyles;
import hue.captains.singapura.js.homing.design.styles.shabby.ShabbyStyles;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Ink;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Loudly;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Shabby;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An instance's styles, built in stages (RFC 0067, D50): the base modules - the components, each with its look in the
 * base, its design's castings in its bundle - then each style over the base with its module; each step refusing at once
 * what it can tell. Looks are collected from the jars' declarations as they stand, bundled by the component whose design
 * each is of.
 */
class StyleRegistryTest {

    private static final List<ComponentModule> DEFINITIONS = List.of(StyledComponents.INSTANCE);
    private static final List<BaseModule<BaseScheme, Plain>> BASE = CollectModules.INSTANCE.base(Plain.INSTANCE, DEFINITIONS, List.of(PlainStyles.INSTANCE));
    private static final StyleModule<BaseScheme, Ink> INK = CollectModules.INSTANCE.style(Ink.INSTANCE, DEFINITIONS, List.of(InkStyles.INSTANCE));
    private static final StyleModule<BaseColours.LoudScheme, Loudly> LOUDLY = CollectModules.INSTANCE.style(Loudly.INSTANCE, DEFINITIONS, List.of(LoudlyStyles.INSTANCE));

    private static StyleManager manager() {
        return new StyleRegistryBuilder<>(BASE).style(Ink.INSTANCE, INK).style(Loudly.INSTANCE, LOUDLY).build();
    }

    @Test
    void anInstance_inStages_theBaseModulesThenEachStyleOverIt() {
        StyleManager m = manager();
        assertEquals(List.of("plain", "ink", "loudly"), m.styles().stream().map(s -> s.token()).toList(), "the base first, then each in order");
        assertEquals(Plain.INSTANCE, m.base());
        assertEquals(4, m.taxonomy().components().size(), "the components, from the base modules");
        assertEquals(5, m.implementations(Plain.INSTANCE).size(), "the base layer: every casting");
        assertEquals(1, m.implementations(Ink.INSTANCE).size());
        StyleBundle<?, ?> strip = BASE.get(0).bundles().stream().filter(b -> b.component() == StatedTaxonomy.Strip.INSTANCE).findFirst().orElseThrow();
        assertEquals(List.of("strip", "tab_lead"), strip.looks().stream().map(l -> m.casting(l).orElseThrow().token()).toList(),
                "the strip's bundle: its own look, and its tab cast as its lead - its design's");
        assertEquals(List.of(INK), m.modules(Ink.INSTANCE));
    }

    @Test
    void aCasting_itsOwn_elseItsBaseLayers_aDelegationFollowed() {
        StyleManager m = manager();
        assertEquals("ink's own", m.resolve(Ink.INSTANCE, new Casting(StatedTaxonomy.Tab.INSTANCE)).says());
        assertEquals("plain's, ink answering absence", m.resolve(Ink.INSTANCE, new Casting(StatedTaxonomy.Strip.INSTANCE)).says());
        assertEquals("plain's, ink answering absence, as plain's Tab",
                m.resolve(Ink.INSTANCE, new Casting(StatedTaxonomy.Tab.INSTANCE, StatedTaxonomy.Lead.INSTANCE)).says(), "its base layer's delegation, followed");
    }

    @Test
    void aStylesSheet_oneClassForEveryCasting_aDelegationUnderItsOwnClass() {
        StyleManager m = manager();
        String sheet = m.sheet(Ink.INSTANCE);
        assertTrue(sheet.contains("@property --extent"), "the degrees registered");
        assertTrue(sheet.contains("/* tab: ink's own */\n.styled-tab {"), sheet);
        assertTrue(sheet.contains("/* tab_lead: plain's, ink answering absence, as plain's Tab */\n"), "the lead tab: plain's tab's file");
        assertTrue(sheet.contains(".styled-tab_lead {") && sheet.contains("&.styled-tab_lead--held {"), "under the lead's own class, its states its own");
        assertTrue(sheet.contains("@keyframes meter-sweep {"), "the meter's keyframes, named for it");
        assertTrue(!sheet.contains("$"), "every mark filled in");
        for (Casting c : m.taxonomy().castings()) assertTrue(sheet.contains("." + LeafClasses.INSTANCE.styled(c) + " {"), c.token());
        var families = m.families(Ink.INSTANCE);
        assertTrue(families.get("control").contains(".styled-tab {") && families.get("gauge").contains(".styled-meter {"), "the family files, aggregated");
    }

    /** Plain's looks without the meter's. */
    record NoMeter() implements StyleDeclaration {
        @Override public List<Provided<?, ?>> provided() {
            return List.of(new Provided<>(Plain.INSTANCE, List.of(new hue.captains.singapura.js.homing.design.styles.plain.Tab(),
                    new hue.captains.singapura.js.homing.design.styles.plain.TabLead(), new hue.captains.singapura.js.homing.design.styles.plain.Strip(),
                    new hue.captains.singapura.js.homing.design.styles.plain.Badge())));
        }
    }

    /** Plain's strip without its tab cast as its lead. */
    record NoLead() implements StyleDeclaration {
        @Override public List<Provided<?, ?>> provided() {
            return List.of(new Provided<>(Plain.INSTANCE, List.of(new hue.captains.singapura.js.homing.design.styles.plain.Tab(),
                    new hue.captains.singapura.js.homing.design.styles.plain.Strip(), new hue.captains.singapura.js.homing.design.styles.plain.Meter(),
                    new hue.captains.singapura.js.homing.design.styles.plain.Badge())));
        }
    }

    @Test
    void theBaseModules_refused_noneAComponentWithNoLook_aDesignHalfLooked_twoLooksOfOneCasting() {
        assertTrue(refused(() -> new StyleRegistryBuilder<BaseScheme, Plain>(List.of())).contains("it registers no base module"));
        String noMeter = refused(() -> new StyleRegistryBuilder<>(CollectModules.INSTANCE.base(Plain.INSTANCE, DEFINITIONS, List.of(new NoMeter()))));
        assertTrue(noMeter.contains("StyledComponents gives meter no look in the base: every component needs one"), noMeter);
        String noLead = refused(() -> new StyleRegistryBuilder<>(CollectModules.INSTANCE.base(Plain.INSTANCE, DEFINITIONS, List.of(new NoLead()))));
        assertTrue(noLead.contains("strip's bundle in the base has no look of tab_lead: the base gives every casting of a component's design"), noLead);
        String twice = refused(() -> new StyleRegistryBuilder<>(CollectModules.INSTANCE.base(Shabby.INSTANCE, DEFINITIONS, List.of(ShabbyStyles.INSTANCE))));
        assertTrue(twice.contains("tab's bundle in shabby has two looks of tab"), twice);
        assertTrue(twice.contains("tab is given two looks in shabby"), twice);
        String derived = refused(() -> new StyleRegistryBuilder<>(List.of(new CollectModules.CollectedBase<>(Ink.INSTANCE, StyledComponents.INSTANCE, List.of()))));
        assertTrue(derived.contains("the base, ink, is written over plain: a base derives from nothing"), derived);
    }

    /** A style over another base than the instance's. */
    record Worn() implements Derives<BaseScheme, Shabby> {
        public static final Worn INSTANCE = new Worn();
        @Override public BaseScheme scheme() { return BaseScheme.INSTANCE; }
        @Override public Shabby base() { return Shabby.INSTANCE; }
        @Override public List<Implementation<?>> implementations() { return List.of(); }
    }

    /** The tab and the strip alone: no meter, no badge. */
    record TabsAlone() implements ComponentModule {
        @Override public List<? extends hue.captains.singapura.js.homing.component.taxonomy.Component<?>> components() {
            return List.of(StatedTaxonomy.Tab.INSTANCE, StatedTaxonomy.Strip.INSTANCE);
        }
    }

    @Test
    void aStyle_refused_theBase_twice_overAnotherBase_twoBundlesForOneComponent_outsideTheComponentsRegistered() {
        var builder = new StyleRegistryBuilder<>(BASE);
        assertTrue(refused(() -> builder.style(Plain.INSTANCE, BASE.get(0))).contains("plain is the base: it comes in the base modules"));
        assertTrue(refused(() -> builder.style(Ink.INSTANCE, INK).style(Ink.INSTANCE, INK)).contains("ink is registered twice"));
        String worn = refused(() -> builder.style(Worn.INSTANCE, new CollectModules.CollectedStyle<>(Worn.INSTANCE, List.of())));
        assertTrue(worn.contains("worn is written over shabby, and the base is plain: a style is written over the instance's base"), worn);
        String bundles = refused(() -> builder.style(Ink.INSTANCE, INK, INK));
        assertTrue(bundles.contains("ink has two bundles for tab, in CollectedStyle and CollectedStyle: one bundle of a component in a style"), bundles);
        var tabsAlone = List.<ComponentModule>of(new TabsAlone());
        var base = CollectModules.INSTANCE.base(Plain.INSTANCE, tabsAlone, List.of(new hue.captains.singapura.js.homing.design.styles.StyleDeclaration() {
            @Override public List<Provided<?, ?>> provided() {
                return List.of(new Provided<>(Plain.INSTANCE, List.of(new hue.captains.singapura.js.homing.design.styles.plain.Tab(),
                        new hue.captains.singapura.js.homing.design.styles.plain.TabLead(), new hue.captains.singapura.js.homing.design.styles.plain.Strip())));
            }
        }));
        String outside = refused(() -> new StyleRegistryBuilder<>(base).style(Loudly.INSTANCE, LOUDLY));
        assertTrue(outside.contains("loudly: CollectedStyle bundles meter, which no base module lists: a style styles the components registered"), outside);
        String collected = refused(() -> CollectModules.INSTANCE.style(Loudly.INSTANCE, tabsAlone, List.of(LoudlyStyles.INSTANCE)));
        assertTrue(collected.contains("implements meter, of no component's design given"), "a look of no component, refused as it is collected: " + collected);
    }

    @Test
    void someJarsAlone_eachLookOfTheirDesigns_theRestSetAside_theHouseStillStrict() {
        var tabsAlone = List.<ComponentModule>of(new TabsAlone());
        String strict = refused(() -> CollectModules.INSTANCE.base(Plain.INSTANCE, tabsAlone, List.of(PlainStyles.INSTANCE)));
        assertTrue(strict.contains("implements meter, of no component's design given"), "collected for the whole, a look of no component given is refused: " + strict);
        StyleManager some = new StyleRegistryBuilder<>(CollectModules.INSTANCE.baseWithin(Plain.INSTANCE, tabsAlone, List.of(PlainStyles.INSTANCE))).build();
        assertEquals(List.of("tab", "strip", "tab_lead"), some.taxonomy().castings().stream().map(Casting::token).toList(), "the tab and the strip, and the strip's lead");
        assertEquals(3, some.implementations(Plain.INSTANCE).size(), "the meter's and the badge's looks set aside: no casting of this instance");
    }

    private static String refused(Runnable step) {
        return assertThrows(IllegalArgumentException.class, step::run).getMessage();
    }
}
