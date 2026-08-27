package hue.captains.singapura.js.homing.studio.base.app;

import hue.captains.singapura.js.homing.studio.base.Doc;

import java.util.ArrayList;
import java.util.Collection;

/**
 * RFC 0051 Phase 6 — the law that lets {@code url()} be replaced without the
 * addressing shifting: {@link DocViewers#addressOf} must render byte-identically
 * to {@code doc.url()} for every registered doc.
 *
 * <p>This is not a nicety. The flat-address index that powers {@code /goto} is
 * KEYED on these strings, and every cross-reference in every studio resolves
 * through it, so a single character of drift breaks all of them at once — and
 * breaks them silently, since a wrong key simply misses and the reference
 * falls back to rendering flat.</p>
 *
 * <p>It lives in main rather than in a test because only a composed studio has
 * a populated {@code DocRegistry}; each downstream repo runs it over its own.
 * Same reason {@code ParamCodecLaw} lives here.</p>
 *
 * @since RFC 0051 Phase 6
 */
public final class DocAddressLaw {

    private DocAddressLaw() {}

    /**
     * Assert {@code DocViewers.addressOf(doc).flat().equals(doc.url())} for
     * every doc given.
     *
     * @throws AssertionError naming every doc that disagreed, with both strings
     */
    public static void assertMatchesUrl(String studioName, Collection<? extends Doc> docs) {
        if (docs == null || docs.isEmpty()) {
            throw new AssertionError(studioName + ": no docs — the law would pass vacuously");
        }
        var failures = new ArrayList<String>();
        for (Doc doc : docs) {
            String expected = doc.url();
            String actual;
            try {
                actual = DocViewers.addressOf(doc).flat();
            } catch (RuntimeException e) {
                failures.add(describe(doc) + ": addressOf threw " + e);
                continue;
            }
            if (!expected.equals(actual)) {
                failures.add(describe(doc) + "\n      url()      = " + expected
                                           + "\n      addressOf  = " + actual);
            }
        }
        if (!failures.isEmpty()) {
            throw new AssertionError(studioName + " — " + failures.size()
                    + " doc(s) whose address would move:\n  " + String.join("\n  ", failures));
        }
    }

    private static String describe(Doc doc) {
        return doc.getClass().getSimpleName() + " \"" + doc.title() + "\"";
    }
}
