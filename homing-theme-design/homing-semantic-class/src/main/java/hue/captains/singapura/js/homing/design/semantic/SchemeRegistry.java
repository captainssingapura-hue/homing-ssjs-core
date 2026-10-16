package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;

/**
 * The instance's colour schemes (RFC 0067, appendix Semantic Colours and Flavours, D49): one declaration for each,
 * registered explicitly at bootstrap as one flat list - as the components are, by module. A pure record of its
 * declarations: made only when every rule it is held to finds nothing ({@link SchemeRule#RULES}, each applied apart),
 * refused otherwise with every problem at once. Its trees are read from it ({@link SchemeTrees}): the style manager
 * takes its styles', and the palettes' registry its palettes'; a style or palette of a scheme it does not register is
 * refused where it is registered. Nothing is found, and nothing read off a type: a scheme not registered is no scheme
 * of the instance's. A scheme registered and used by nothing is no problem.
 *
 * @param declarations each scheme's declaration, in the order registered
 */
public record SchemeRegistry(List<SchemeDeclaration<?>> declarations) implements ValueObject {

    public SchemeRegistry {
        declarations = List.copyOf(declarations);
        Validation found = Validation.Valid.INSTANCE;
        for (SchemeRule rule : SchemeRule.RULES) found = found.and(rule.check(declarations));
        if (found instanceof Validation.Invalid invalid)
            throw new IllegalArgumentException("the instance's schemes are refused:\n  " + String.join("\n  ", invalid.problems()));
    }
}
