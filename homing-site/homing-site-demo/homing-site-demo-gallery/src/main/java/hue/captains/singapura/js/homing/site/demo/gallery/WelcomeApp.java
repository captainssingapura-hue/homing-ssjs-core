package hue.captains.singapura.js.homing.site.demo.gallery;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.ui.elements.Elements;

import java.util.List;

/**
 * The front page: what the gallery is, where its pages are, and which theme
 * the page wears right now — re-read whenever the theme changes, from here
 * or from another tab.
 *
 * <p>A plain {@code AppModule}: {@code appMain(el)} and nothing else. It
 * becomes a page under the chrome by being handed to {@code StandardMpa.page},
 * not by extending anything. Its cards are the shared {@code Card} from
 * homing-ui-elements; the page owns only the grid they sit in.</p>
 */
public record WelcomeApp() implements AppModule<AppModule._None, WelcomeApp> {

    public static final WelcomeApp INSTANCE = new WelcomeApp();

    record appMain() implements AppModule._AppMain<AppModule._None, WelcomeApp> {}

    @Override public String title()      { return "Welcome"; }
    @Override public String simpleName() { return "welcome"; }

    @Override
    public ImportsFor<WelcomeApp> imports() {
        return ImportsFor.<WelcomeApp>builder()
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.Card()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new GalleryStyles.ga_kicker(),
                        new GalleryStyles.ga_title(),
                        new GalleryStyles.ga_lede(),
                        new GalleryStyles.ga_cards()
                ), GalleryStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<WelcomeApp> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }
}
