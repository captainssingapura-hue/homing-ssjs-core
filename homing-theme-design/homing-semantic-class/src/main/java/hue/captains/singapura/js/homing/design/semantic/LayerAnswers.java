package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.Casting;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.LinkedHashMap;
import java.util.Map;

/** What a style answers, as a layer holds it ({@link Layer}). */
record LayerAnswers() implements StatelessFunctionalObject {

    static final LayerAnswers INSTANCE = new LayerAnswers();

    /** Each answer the style gives, keyed by its node and target. */
    Map<Layer.Key, Answer<?>> of(DesignStyle<?> style, Layer base) {
        var out = new LinkedHashMap<Layer.Key, Answer<?>>();
        for (Implementation<?> i : style.implementations()) put(style, out, i.node(), i.target(), i.answer(), base);
        for (Coloring<?, ?> c : style.colorings()) put(style, out, c.node(), c.target(), c.answer(), base);
        return out;
    }

    private void put(DesignStyle<?> style, Map<Layer.Key, Answer<?>> out, Casting node, Target target, Answer<?> answer, Layer base) {
        String where = style.token() + ": " + SemanticTokens.INSTANCE.casting(node) + " " + target.token();
        if (answer instanceof Answer.FromBase<?> && base == null) throw new IllegalArgumentException(where + " asks its base, and " + style.token() + " has none");
        if (out.putIfAbsent(new Layer.Key(node, target), answer) != null) throw new IllegalArgumentException(where + " is answered twice");
    }
}
