package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.semantic.SchemeRegistry;
import hue.captains.singapura.js.homing.design.semantic.Validation;
import hue.captains.singapura.js.homing.design.semantic.Validations;
import hue.captains.singapura.js.homing.design.styles.PaletteDeclaration;
import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * The instance's palettes (RFC 0067, D47, D49): one registry for the whole instance, each jar a sub-registry of its own -
 * its declaration ({@link PaletteDeclaration}) - registered at bootstrap, the default palette the first jar's first,
 * over the instance's schemes ({@link SchemeRegistry}). It stands apart from the styles: neither a style nor a scheme
 * knows a palette, and a palette of a scheme no style requires may be registered - nothing wears it. A pure record of
 * its jars and the schemes: made only when every rule it is held to finds nothing ({@link PaletteRule#RULES}, each
 * applied apart), refused otherwise with every problem at once. What is worked out of it - its palettes, one by its
 * token, the tree each is written over - is {@link RegisteredPalettes}'.
 *
 * @param jars    the jars of palettes, each by its declaration, the default first
 * @param schemes the instance's schemes, each palette written over its scheme's tree
 */
public record PaletteRegistry(List<PaletteDeclaration> jars, SchemeRegistry schemes) implements ValueObject {

    public PaletteRegistry {
        jars = List.copyOf(jars);
        Objects.requireNonNull(schemes, "PaletteRegistry.schemes");
        final List<PaletteDeclaration> registered = jars;
        Validation found = Validations.INSTANCE.all(PaletteRule.RULES, rule -> rule.check(registered, schemes));
        if (found instanceof Validation.Invalid invalid)
            throw new IllegalArgumentException("the instance's palettes are refused:\n  " + String.join("\n  ", invalid.problems()));
    }
}
