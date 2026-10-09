package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.Target;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One identity's answers, by the class they answer: a style, over its base when it has one, or a
 * palette, which has none. What resolution walks a layer at a time.
 *
 * @param token   the style's or the palette's token
 * @param answers what it answers, by node and target
 * @param base    the layer beneath, or null at the bottom of the chain
 */
record Layer(String token, Map<Key, Answer<?>> answers, Layer base) {

    /** A class answered: a node and a target. */
    record Key(Casting node, Target target) {}

    Layer { answers = Collections.unmodifiableMap(new LinkedHashMap<>(answers)); }

    /** What this layer says for a node on a target, or null. */
    Answer<?> answer(Casting node, Target target) { return answers.get(new Key(node, target)); }

    /** The layers from this one down, this one first. */
    List<Layer> chain() {
        var out = new java.util.ArrayList<Layer>();
        for (Layer l = this; l != null; l = l.base()) out.add(l);
        return List.copyOf(out);
    }

    /** One identity's answers, indexed: refused when a class is answered twice, or asks a base there is none of. */
    static Layer index(String token, List<? extends Implementation<?>> implementations, Layer base) {
        var out = new LinkedHashMap<Key, Answer<?>>();
        for (Implementation<?> i : implementations) {
            if (i.answer() instanceof Answer.FromBase<?> && base == null)
                throw new IllegalArgumentException(token + ": " + SemanticTokens.INSTANCE.casting(i.node()) + " " + i.target().token()
                        + " asks its base, and " + token + " has none");
            if (out.putIfAbsent(new Key(i.node(), i.target()), i.answer()) != null)
                throw new IllegalArgumentException(token + ": " + SemanticTokens.INSTANCE.casting(i.node()) + " " + i.target().token() + " is answered twice");
        }
        return new Layer(token, out, base);
    }
}
