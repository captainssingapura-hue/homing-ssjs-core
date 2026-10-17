package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.component.taxonomy.ComponentModule;
import hue.captains.singapura.js.homing.design.semantic.BaseScheme;
import hue.captains.singapura.js.homing.design.styles.ink.InkStyles;
import hue.captains.singapura.js.homing.design.styles.loudly.LoudlyStyles;
import hue.captains.singapura.js.homing.design.styles.plain.PlainStyles;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Ink;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Loudly;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/** The tests' style managers (D50): plain the base, from the tests' component module; ink and loudly over it, each from its module. */
public record StyledManagers() implements StatelessFunctionalObject {

    public static final StyledManagers INSTANCE = new StyledManagers();

    private static final List<ComponentModule> DEFINITIONS = List.of(StyledComponents.INSTANCE);

    /** The base modules: the tests' components, each with its look in plain. */
    public List<BaseModule<BaseScheme, Plain>> base() {
        return CollectModules.INSTANCE.base(Plain.INSTANCE, DEFINITIONS, List.of(PlainStyles.INSTANCE));
    }

    /** Plain alone. */
    public StyleManager plain() { return new StyleRegistryBuilder<>(base()).build(); }

    /** Plain, and ink over it. */
    public StyleManager plainAndInk() {
        return new StyleRegistryBuilder<>(base()).style(Ink.INSTANCE, CollectModules.INSTANCE.style(Ink.INSTANCE, DEFINITIONS, List.of(InkStyles.INSTANCE))).build();
    }

    /** Plain, and ink and loudly over it. */
    public StyleManager all() {
        return new StyleRegistryBuilder<>(base())
                .style(Ink.INSTANCE, CollectModules.INSTANCE.style(Ink.INSTANCE, DEFINITIONS, List.of(InkStyles.INSTANCE)))
                .style(Loudly.INSTANCE, CollectModules.INSTANCE.style(Loudly.INSTANCE, DEFINITIONS, List.of(LoudlyStyles.INSTANCE)))
                .build();
    }
}
