package hue.captains.singapura.js.homing.site.demo.gallery;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;
import hue.captains.singapura.js.homing.core.ParamCodec;
import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.js.DomOpsPartyModule;
import hue.captains.singapura.js.homing.core.js.domOpsParty;
import hue.captains.singapura.js.homing.ui.elements.Elements;

import java.util.List;
import java.util.Map;

/**
 * A counter with a typed start. The codec reads and writes {@code ?start=},
 * so a page bound to {@code Params(7)} is minted as {@code /app?app=counter&start=7}
 * and the stamped params the page receives are {@code {start: "7"}}.
 */
public record CounterApp() implements AppModule<CounterApp.Params, CounterApp> {

    public static final CounterApp INSTANCE = new CounterApp();

    public record Params(int start) implements AppModule._Param {}

    record appMain() implements AppModule._AppMain<Params, CounterApp> {}

    public static final ParamCodec<Params> CODEC = new ParamCodec<>() {
        @Override public Decoded<Params> from(Map<String, List<String>> query) {
            String s = QueryString.first(query, "start");
            if (s == null || s.isBlank()) return Decoded.ok(new Params(0));
            try { return Decoded.ok(new Params(Integer.parseInt(s.trim()))); }
            catch (NumberFormatException e) { return Decoded.malformed("start", s, "an integer"); }
        }
        @Override public Map<String, List<String>> to(Params params) {
            return QueryString.of("start", Integer.toString(params.start()));
        }
    };

    @Override public String title()      { return "Counter"; }
    @Override public String simpleName() { return "counter"; }
    @Override public Class<Params> paramsType() { return Params.class; }
    @Override public ParamCodec<Params> paramCodec() { return CODEC; }

    @Override
    public ImportsFor<CounterApp> imports() {
        return ImportsFor.<CounterApp>builder()
                .add(new ModuleImports<>(List.of(new domOpsParty()), DomOpsPartyModule.INSTANCE))
                .add(new ModuleImports<>(List.of(new Elements.Button()), Elements.INSTANCE))
                .add(new ModuleImports<>(List.of(
                        new GalleryStyles.ga_kicker(),
                        new GalleryStyles.ga_title(),
                        new GalleryStyles.ga_lede(),
                        new GalleryStyles.ga_count(),
                        new GalleryStyles.ga_buttons()
                ), GalleryStyles.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<CounterApp> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new appMain()));
    }
}
