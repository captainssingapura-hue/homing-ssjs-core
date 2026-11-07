package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.Target;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One style's answers, by the class they answer - every target's, colour's included - over its base when it has one.
 * What resolution walks a layer at a time.
 *
 * @param token   the style's token
 * @param answers what it answers, by node and target
 * @param base    the layer beneath, or null at the bottom of the chain
 */
record Layer(String token, Map<Key, Answer<?>> answers, Layer base) {

    /** A class answered: a node and a target. */
    record Key(Casting node, Target target) {}

    Layer { answers = Collections.unmodifiableMap(new LinkedHashMap<>(answers)); }

    /** A style's answers, indexed - its implementations, then its colorings: refused when a class is answered twice, or asks a base there is none of. */
    Layer(DesignStyle<?> style, Layer base) {
        this(style.token(), LayerAnswers.INSTANCE.of(style, base), base);
    }

    /** What this layer says for a node on a target, or null. */
    Answer<?> answer(Casting node, Target target) { return answers.get(new Key(node, target)); }

    /** The layers from this one down, this one first. */
    List<Layer> chain() {
        var out = new ArrayList<Layer>();
        for (Layer l = this; l != null; l = l.base()) out.add(l);
        return List.copyOf(out);
    }
}
