package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.semantic.BaseColours;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Neon;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Night;
import hue.captains.singapura.js.homing.design.semantic.BaseColours.Paper;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.semantic.ColorPalette;
import hue.captains.singapura.js.homing.design.semantic.SchemeRegistry;
import hue.captains.singapura.js.homing.design.styles.PaletteDeclaration;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The instance's palettes (RFC 0067, D47, D49): one registry, each jar a sub-registry by its declaration, apart from the
 * styles - each palette held whole over its scheme's tree as the instance's registry of schemes read it, whether a style
 * requires the scheme or not - and held to its own checks.
 */
class PaletteRegistryTest {

    @Test
    void theInstancesPalettes_eachJarASubRegistry_eachSchemeReadByTheReaderFoundForIt() {
        PaletteRegistry registry = new PaletteRegistry(List.of(BaseColours.Palettes.INSTANCE), BaseColours.SCHEMES);
        var read = RegisteredPalettes.INSTANCE;
        assertEquals(List.of(Night.INSTANCE, Paper.INSTANCE, Neon.INSTANCE), read.palettes(registry), "the jar's, in its order");
        assertEquals(List.of(BaseScheme.class, BaseColours.LoudScheme.class), List.copyOf(read.trees(registry).keySet()),
                "the palettes' schemes - no style asked");
        assertEquals(17, read.tree(registry, Neon.INSTANCE).nodes().size(), "neon over the loud scheme's tree");
        assertEquals(Optional.of(Paper.INSTANCE), read.palette(registry, "paper"));
        assertEquals(BaseColours.LOUD_TREE, read.tree(registry, Neon.INSTANCE), "the tree the registry of schemes reads");
    }

    /** A second jar holding night again. */
    record NightAgain() implements PaletteDeclaration {
        @Override public List<? extends ColorPalette<?>> palettes() { return List.of(Night.INSTANCE); }
    }

    @Test
    void refused_nothing_aJarTwice_twoOfOneToken_notWhole_sittingOnOneNotRegistered_ofASchemeNotRegistered() {
        assertTrue(refused(List.of()).contains("it registers no palette"));
        String jarTwice = refused(List.of(BaseColours.Palettes.INSTANCE, BaseColours.Palettes.INSTANCE));
        assertTrue(jarTwice.contains("Palettes is registered twice"), jarTwice);
        String tokenTwice = refused(List.of(BaseColours.Palettes.INSTANCE, new NightAgain()));
        assertTrue(tokenTwice.contains("two palettes are night"), tokenTwice);
        String incomplete = refused(only(Night.INSTANCE, new BaseColours.Unsafe()));
        assertTrue(incomplete.contains("unsafe leaves safe unanswered"), "a palette whole over its scheme: " + incomplete);
        String adrift = refused(only(Night.INSTANCE, Neon.INSTANCE));
        assertTrue(adrift.contains("neon sits on paper, which is not registered"), "the palettes below registered too: " + adrift);
        String unregistered = assertThrows(IllegalArgumentException.class,
                () -> new PaletteRegistry(List.of(BaseColours.Palettes.INSTANCE), new SchemeRegistry(List.of(BaseColours.INSTANCE)))).getMessage();
        assertTrue(unregistered.startsWith("the instance's palettes are refused:") && unregistered.contains("neon is LoudScheme's, which the instance does not register"), unregistered);
    }

    private static String refused(List<PaletteDeclaration> jars) {
        return assertThrows(IllegalArgumentException.class, () -> new PaletteRegistry(jars, BaseColours.SCHEMES)).getMessage();
    }

    private static List<PaletteDeclaration> only(ColorPalette<?>... palettes) { return List.of(() -> List.of(palettes)); }
}
