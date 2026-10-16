package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorNode;
import hue.captains.singapura.js.homing.design.color.ColorScheme;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;
import java.util.Optional;

/**
 * A colour scheme's tree reader (RFC 0067, appendix Semantic Colours and Flavours): a stateless functional object, one
 * for each scheme, in the scheme's own jar beside its colours, found for the scheme by the instance's one finder
 * ({@link SchemeReaders}, D47) - by default by its name: {@code <Name>Colours}, beside {@code <Name>Scheme}. It knows its layer - the colours its scheme adds, and only those - and the reader of the scheme below it, and
 * reads the normalized tree: the layers overlaid from the base up to its own, typically two - a style's scheme over the
 * base. Closed: nothing but its reader adds to a scheme's colours.
 *
 * @param <S> its scheme
 */
public interface SchemeTreeReader<S extends ColorScheme> extends StatelessFunctionalObject {

    /** The colours its scheme adds, in the order a sheet writes them: its own layer, none of another scheme's. */
    List<? extends ColorNode<S, ?, ?>> layer();

    /** The reader of the scheme below its own, whose layers it is overlaid on; none for a scheme from scratch. */
    default Optional<SchemeTreeReader<? super S>> below() { return Optional.empty(); }

    /** The normalized tree: every layer from the base up to this one, read and refused nothing ({@link ReadColors}). */
    default ColorTree read() { return ReadColors.INSTANCE.read(this); }
}
