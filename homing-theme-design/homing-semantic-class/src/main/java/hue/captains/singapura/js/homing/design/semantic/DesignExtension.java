package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.component.taxonomy.ConcreteComponent;

import java.util.List;

/**
 * What a Maven module adds of designs (RFC 0067, appendix Virtual Leaves and Designs): its concrete leaves,
 * of every design it ships. They are gathered into designs by the design each one names, so one module may
 * ship several - a game's classic flavour and its glass one. Nothing in the taxonomy changes.
 *
 * <p>Found on the classpath by {@link java.util.ServiceLoader}: a module names its extension in {@code
 * META-INF/services/hue.captains.singapura.js.homing.design.semantic.DesignExtension}, and a public record
 * with no components is one the loader can make.</p>
 */
public interface DesignExtension {

    /** The concrete leaves the module declares, in the order it lists them. */
    List<ConcreteComponent<?, ?, ?>> concretes();
}
