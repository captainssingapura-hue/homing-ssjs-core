package hue.captains.singapura.js.homing.component.taxonomy;

/** The {@link Pictures} lexicon as a repository declares it: its own words, then those of the modules that extend it. */
public record PicturesSource() implements LexiconSource {

    public static final PicturesSource INSTANCE = new PicturesSource();

    @Override public Lexicon read() {
        return ReadLexicon.INSTANCE.read(LexiconExtensions.INSTANCE.words(PicturesSource.class, Pictures.DECLARED));
    }
}
