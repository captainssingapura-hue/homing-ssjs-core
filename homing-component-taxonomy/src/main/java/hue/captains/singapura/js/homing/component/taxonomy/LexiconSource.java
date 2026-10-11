package hue.captains.singapura.js.homing.component.taxonomy;

/**
 * A lexicon as a repository declares it: its words, read whole with those of the modules on the
 * classpath that extend it. A tool told which lexicon to work on by name reads it through this: the
 * class's {@code INSTANCE}, then {@link #read()}.
 */
public interface LexiconSource {
    /** The lexicon, read and refused nothing. */
    Lexicon read();
}
