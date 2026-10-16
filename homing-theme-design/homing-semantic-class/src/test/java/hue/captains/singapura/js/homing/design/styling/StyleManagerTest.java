package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.component.taxonomy.ReadTaxonomy;
import hue.captains.singapura.js.homing.component.taxonomy.Taxonomy;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours;
import hue.captains.singapura.js.homing.design.semantic.SchemeTreeReader;
import hue.captains.singapura.js.homing.design.styles.ink.InkStyles;
import hue.captains.singapura.js.homing.design.styles.loudly.LoudlyStyles;
import hue.captains.singapura.js.homing.design.styles.plain.PlainStyles;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The central style manager (RFC 0067, appendix The Style Jars): registered in two stages - each style with its jars,
 * then the instance's list - held to the cross-jar rules, and a style's sheet aggregated from its implementations, a
 * casting it leaves absent its base layer's, a delegation served under the delegating casting's class.
 */
class StyleManagerTest {

    private static final Taxonomy TAXONOMY = StyledTaxonomy.INSTANCE.read();
    /** The schemes' readers: the base's, and the loud scheme's over it. */
    private static final List<SchemeTreeReader<?>> SCHEMES = List.of(ResolvingColours.INSTANCE, ResolvingColours.LoudColours.INSTANCE);
    private static final StyleRegistration PLAIN = StyleRegistration.of(StyledStyles.Plain.INSTANCE, PlainStyles.INSTANCE);
    private static final StyleRegistration INK = StyleRegistration.of(StyledStyles.Ink.INSTANCE, InkStyles.INSTANCE);
    private static final StyleRegistration LOUDLY = StyleRegistration.of(StyledStyles.Loudly.INSTANCE, LoudlyStyles.INSTANCE);

    @Test
    void anInstance_ofItsRegistrations_heldToTheRules() {
        StyleManager manager = StyleManager.of(TAXONOMY, SCHEMES, List.of(PLAIN, INK, LOUDLY));
        assertEquals(List.of("plain", "ink", "loudly"), manager.styles().stream().map(s -> s.token()).toList(), "the default first");
        assertEquals(5, manager.implementations(StyledStyles.Plain.INSTANCE).size(), "the base layer: every casting");
        assertEquals(1, manager.implementations(StyledStyles.Ink.INSTANCE).size());
    }

    @Test
    void aCasting_itsOwn_elseItsBaseLayers_aDelegationFollowed() {
        StyleManager manager = StyleManager.of(TAXONOMY, SCHEMES, List.of(PLAIN, INK, LOUDLY));
        var ink = StyledStyles.Ink.INSTANCE;
        assertEquals("ink's own", manager.resolve(ink, new Casting(StatedTaxonomy.Tab.INSTANCE)).says());
        assertEquals("plain's, ink answering absence", manager.resolve(ink, new Casting(StatedTaxonomy.Strip.INSTANCE)).says());
        assertEquals("plain's, ink answering absence, as plain's Tab",
                manager.resolve(ink, new Casting(StatedTaxonomy.Tab.INSTANCE, StatedTaxonomy.Lead.INSTANCE)).says(), "its base layer's delegation, followed");
    }

    @Test
    void aStylesSheet_oneClassForEveryCasting_aDelegationUnderItsOwnClass() {
        String sheet = StyleManager.of(TAXONOMY, SCHEMES, List.of(PLAIN, INK, LOUDLY)).sheet(StyledStyles.Ink.INSTANCE);
        assertTrue(sheet.contains("@property --extent"), "the degrees registered");
        assertTrue(sheet.contains("/* tab: ink's own */\n.styled-tab {"), sheet);
        assertTrue(sheet.contains("/* tab_lead: plain's, ink answering absence, as plain's Tab */\n"), "the lead tab: plain's tab's file");
        assertTrue(sheet.contains(".styled-tab_lead {") && sheet.contains("&.styled-tab_lead--held {"), "under the lead's own class, its states its own");
        assertTrue(sheet.contains("@keyframes meter-sweep {"), "the meter's keyframes, named for it");
        assertTrue(!sheet.contains("$"), "every mark filled in");
        for (Casting c : TAXONOMY.castings()) assertTrue(sheet.contains("." + LeafClasses.INSTANCE.styled(c) + " {"), c.token());
        var families = StyleManager.of(TAXONOMY, SCHEMES, List.of(PLAIN, INK, LOUDLY)).families(StyledStyles.Ink.INSTANCE);
        assertTrue(families.get("control").contains(".styled-tab {") && families.get("gauge").contains(".styled-meter {"), "the family files, aggregated");
    }

    @Test
    void refused_noBaseLayer_aJarProvidingNothingForItsStyle_aStyleProvidedForNotRegistered_twoOfOneCasting() {
        String noBase = assertThrows(IllegalArgumentException.class, () -> StyleManager.of(TAXONOMY, SCHEMES, List.of(INK))).getMessage();
        assertTrue(noBase.contains("ink's base layer, plain, is not registered"), noBase);
        String wrongJar = assertThrows(IllegalArgumentException.class, () -> StyleManager.of(TAXONOMY, SCHEMES,
                List.of(PLAIN, StyleRegistration.of(StyledStyles.Ink.INSTANCE, LoudlyStyles.INSTANCE)))).getMessage();
        assertTrue(wrongJar.contains("LoudlyStyles is registered for ink, and provides nothing for it"), wrongJar);
        assertTrue(wrongJar.contains("provides for loudly, which the instance does not register"), wrongJar);
        String twice = assertThrows(IllegalArgumentException.class, () -> StyleManager.of(TAXONOMY, SCHEMES,
                List.of(PLAIN, StyleRegistration.of(StyledStyles.Ink.INSTANCE, InkStyles.INSTANCE, new InkAgain())))).getMessage();
        assertTrue(twice.contains("ink has 2 implementations of tab"), twice);
    }

    @Test
    void theRuleSet_isTheInstances() {
        StyleManager none = StyleManager.of(TAXONOMY, SCHEMES, List.of(INK), List.of());
        assertEquals(List.of("ink"), none.styles().stream().map(s -> s.token()).toList(), "held to no rule, it stands");
        StyleRule onlyPlain = m -> m.styles().stream().anyMatch(s -> s.token().equals("plain")) ? List.of() : List.of("plain first");
        assertTrue(assertThrows(IllegalArgumentException.class, () -> StyleManager.of(TAXONOMY, SCHEMES, List.of(INK), List.of(onlyPlain)))
                .getMessage().contains("plain first"), "a rule of the instance's own");
    }

    @Test
    void theSchemes_areTheRegisteredStyles_eachReadByTheOneReaderRegisteredForIt() {
        StyleManager manager = StyleManager.of(TAXONOMY, SCHEMES, List.of(PLAIN, INK, LOUDLY));
        assertEquals(17, manager.tree(StyledStyles.Loudly.INSTANCE).nodes().size(), "loudly over the loud scheme's tree: the base's layer and its glare");
        assertEquals(16, manager.tree(StyledStyles.Ink.INSTANCE).nodes().size(), "ink over the base's alone");
        String unread = assertThrows(IllegalArgumentException.class,
                () -> StyleManager.of(TAXONOMY, List.of(ResolvingColours.INSTANCE), List.of(PLAIN, INK, LOUDLY))).getMessage();
        assertTrue(unread.contains("loudly requires Loud, which no registered reader reads"), unread);
        String unworn = assertThrows(IllegalArgumentException.class, () -> StyleManager.of(TAXONOMY, SCHEMES, List.of(PLAIN, INK))).getMessage();
        assertTrue(unworn.contains("Loud is read, and no registered style requires it"), unworn);
        String twice = assertThrows(IllegalArgumentException.class,
                () -> StyleManager.of(TAXONOMY, List.of(ResolvingColours.INSTANCE, new ResolvingColours()), List.of(PLAIN, INK))).getMessage();
        assertTrue(twice.contains("BaseScheme is read by two readers"), twice);
    }

    @Test
    void aStyle_isAFunctionOfTheComponents_theirRegistryItsDomain() {
        Taxonomy tabsAlone = new ReadTaxonomy().read(List.of(StatedTaxonomy.Tab.INSTANCE, StatedTaxonomy.Strip.INSTANCE));
        String outside = assertThrows(IllegalArgumentException.class, () -> StyleManager.of(tabsAlone, SCHEMES, List.of(PLAIN, INK, LOUDLY))).getMessage();
        assertTrue(outside.contains("plain: PlainStyles implements meter, which no component registered has: a style is a function of the components, "
                + "their registry its domain"), outside);
        assertTrue(outside.contains("plain: PlainStyles implements badge, which no component registered has"), "the extension's badge, left out: " + outside);
        assertTrue(outside.contains("loudly: LoudlyStyles implements meter"), outside);
        assertTrue(!outside.contains("implements tab,"), "what the registry has, the styles may draw");
        StyleManager whole = StyleManager.of(TAXONOMY, SCHEMES, List.of(PLAIN, INK, LOUDLY));
        assertEquals(List.of(), new StyleRule.WithinDomain().check(whole), "every casting implemented, within the domain");
    }

    /** A second jar for ink, implementing its tab again. */
    record InkAgain() implements hue.captains.singapura.js.homing.design.styles.StyleDeclaration {
        @Override public List<Provided<?, ?>> provided() {
            return List.of(new Provided<>(StyledStyles.Ink.INSTANCE, List.of(new hue.captains.singapura.js.homing.design.styles.ink.Tab())));
        }
    }
}
