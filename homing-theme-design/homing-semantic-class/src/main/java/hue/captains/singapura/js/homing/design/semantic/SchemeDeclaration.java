package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorLeaf;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * A colour scheme's declaration (RFC 0067, appendix Semantic Colours and Flavours, D49): a stateless functional object,
 * one for each scheme, in the scheme's own jar beside its colours - as a component module is a jar's flat list of its
 * components. It says its scheme and lists its leaves, a flat list of the colours its scheme adds and only those; the
 * branches above them come in with them, each said by its leaf's {@code parent()}, as a component's branch comes in
 * with it. An instance registers its declarations explicitly, in one list ({@link SchemeRegistry}), which reads each
 * scheme's full tree: its own layer over the layers of the schemes below it. Closed: nothing but its declaration adds to
 * a scheme's colours. Nothing of it is found, or read off a type, at run time.
 *
 * @param <S> its scheme
 */
public interface SchemeDeclaration<S extends ColorScheme> extends StatelessFunctionalObject {

    /** Its scheme: the scheme's one object. */
    S scheme();

    /** The colours its scheme adds, a flat list in the order a sheet writes them: its own leaves, none of another scheme's. */
    List<? extends ColorLeaf<S, ?, ?>> leaves();
}
