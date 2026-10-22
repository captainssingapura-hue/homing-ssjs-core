package hue.captains.singapura.js.homing.component.taxonomy;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * What a jar brings to a components taxonomy: a flat list of its components - those it implements, or realizes inline
 * and gives its look to. Nothing more is listed: a component's parts come in with it, transitively, and the branches
 * above them, the roles they are cast in, the types of state they have, all come from the components
 * themselves. An instance registers a flat list of modules explicitly at bootstrap ({@link ComponentModules}); a
 * component is one module's - listed by no two.
 */
public interface ComponentModule extends StatelessFunctionalObject {

    /** The jar's components, in the order it lists them; their parts need not be listed. */
    List<? extends Component<?>> components();
}
