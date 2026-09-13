package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * The query this module was served with — {@code theme} and {@code locale} —
 * read from {@code import.meta.url}, and a helper that carries it onto a bare
 * module URL.
 *
 * <p><b>Why it exists.</b> A module's imports inherit the query its own request
 * carried, so everything under the chrome is keyed by
 * {@code …&theme=T&locale=L}. A widget's {@code moduleUrl} is minted bare, and
 * a bare request starts a <i>second</i> import chain: a second instance of
 * every module the chrome already loaded. For a stateless module that is
 * duplicated code; for {@code DomOpsPartyModule} it is a second, empty root.
 * RFC 0063 found this by building the party monitor and watching it draw a
 * tree with nothing in it.</p>
 *
 * <p><b>Why it is alone.</b> {@code import.meta} does not parse in a classic
 * script, which is how the test harness loads raw module files. Isolating the
 * one read here keeps every consumer testable; a consumer stubs
 * {@code withServingContext} the way it stubs its importer.</p>
 *
 * @since RFC 0063
 */
public record ServingContextModule() implements DomModule<ServingContextModule> {

    public static final ServingContextModule INSTANCE = new ServingContextModule();

    /** {@code servingContext() → { theme, locale }}. */
    public record servingContext() implements Exportable._Constant<ServingContextModule> {}

    /** {@code withServingContext(url) → url} with the context appended where absent. */
    public record withServingContext() implements Exportable._Constant<ServingContextModule> {}

    @Override
    public ImportsFor<ServingContextModule> imports() {
        return ImportsFor.<ServingContextModule>builder().build();
    }

    @Override
    public ExportsOf<ServingContextModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new servingContext(), new withServingContext()));
    }
}
