package hue.captains.singapura.js.homing.ui.icons;

import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.Icon;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.design.Trees;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The vocabulary and the wardrobe are the same list: every {@link Icon}
 * word has a class wearing it on the glyph, no class wears a word twice,
 * and the generated table names each by the word's token.
 */
class IconStylesTest {

    /** The vocabulary, read off the interface: every record nested in {@link Icon}. */
    static List<Class<?>> vocabulary() {
        return java.util.Arrays.stream(Icon.class.getDeclaredClasses()).filter(Class::isRecord).<Class<?>>map(c -> c).toList();
    }

    @Test
    void everyWordHasItsClass_andEveryClassItsWord_once() {
        var words = vocabulary();
        assertEquals(16, words.size(), "the vocabulary: " + words);
        var worn = IconStyles.worded().stream().map(IconStyles.Worded::word).toList();
        assertEquals(words.size(), worn.stream().distinct().count(), "a word worn twice: " + worn);
        for (Class<?> w : words) assertTrue(worn.contains(w), w.getSimpleName() + " has no class in the wardrobe");
        for (var c : IconStyles.worded()) {
            assertEquals("ic_" + Trees.semanticToken(c.word()), c.getClass().getSimpleName(), "a class is named for its word");
            assertEquals(List.of(DesignClass.of(c.word(), Target.Type.Glyph.class)), c.wears(), "worn on the glyph, and nothing else");
        }
        assertEquals(IconStyles.worded().size() + 1, IconStyles.INSTANCE.cssClasses().size(), "the base and the worded ones");
        assertEquals("ic_base", IconStyles.INSTANCE.cssClasses().get(0).getClass().getSimpleName(), "the base first, so a word's class comes after it in the sheet");
    }

    @Test
    void theTableIsGeneratedFromTheWardrobe_byToken() {
        var body = IconsModule.INSTANCE.selfContent(null);
        assertEquals("const ICONS = Object.freeze({ \"check\": ic_check, \"disclose\": ic_disclose, \"close\": ic_close, \"detach\": ic_detach, \"rotate\": ic_rotate, \"flip\": ic_flip,"
                   + " \"add\": ic_add, \"remove\": ic_remove, \"reset\": ic_reset, \"pin\": ic_pin, \"settings\": ic_settings,"
                   + " \"grip\": ic_grip, \"size\": ic_size, \"aspect\": ic_aspect, \"extent\": ic_extent, \"level\": ic_level });", body.get(2));
        assertEquals(List.of("check", "disclose", "close", "detach", "rotate", "flip", "add", "remove", "reset", "pin", "settings", "grip", "size", "aspect", "extent", "level"), IconsModule.names());
        assertTrue(IconStyles.forWord(Icon.Detach.class).isPresent() && IconStyles.forWord(Icon.Detach.class).get().name().equals("detach"));
        var imported = IconsModule.INSTANCE.imports().getAllImports().values().stream().flatMap(mi -> mi.allImports().stream()).map(e -> e.getClass().getSimpleName()).toList();
        for (var c : IconStyles.worded()) assertTrue(imported.contains(c.getClass().getSimpleName()), c.getClass().getSimpleName() + " is in the table but not imported");
    }
}
