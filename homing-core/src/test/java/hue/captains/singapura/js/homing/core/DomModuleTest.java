package hue.captains.singapura.js.homing.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomModuleTest {

    record Styles() implements CssGroup<Styles> {
        static final Styles INSTANCE = new Styles();
        record btn() implements CssClass<Styles> {}

        @Override public List<CssClass<Styles>> cssClasses() { return List.of(new btn()); }
    }

    record OtherModule() implements EsModule<OtherModule> {
        static final OtherModule INSTANCE = new OtherModule();
        record helper() implements Exportable._Constant<OtherModule> {}

        @Override public ImportsFor<OtherModule> imports() { return ImportsFor.noImports(); }
        @Override public ExportsOf<OtherModule> exports() {
            return new ExportsOf<>(INSTANCE, List.of(new helper()));
        }
    }

    record MyDom() implements DomModule<MyDom> {
        static final MyDom INSTANCE = new MyDom();
        record render() implements Exportable._Constant<MyDom> {}

        @Override
        public ImportsFor<MyDom> imports() {
            return ImportsFor.<MyDom>builder()
                    .add(new ModuleImports<>(List.of(new Styles.btn()), Styles.INSTANCE))
                    .add(new ModuleImports<>(List.of(new OtherModule.helper()), OtherModule.INSTANCE))
                    .build();
        }

        @Override
        public ExportsOf<MyDom> exports() {
            return new ExportsOf<>(INSTANCE, List.of(new render()));
        }
    }

    record PureDom() implements DomModule<PureDom> {
        static final PureDom INSTANCE = new PureDom();
        record go() implements Exportable._Constant<PureDom> {}

        @Override
        public ImportsFor<PureDom> imports() {
            return ImportsFor.<PureDom>builder()
                    .add(new ModuleImports<>(List.of(new OtherModule.helper()), OtherModule.INSTANCE))
                    .build();
        }

        @Override
        public ExportsOf<PureDom> exports() {
            return new ExportsOf<>(INSTANCE, List.of(new go()));
        }
    }

    @Test
    void cssGroups_derivesCssGroupsFromImports() {
        var cssGroups = MyDom.INSTANCE.cssGroups();

        assertEquals(1, cssGroups.size());
        assertSame(Styles.INSTANCE, cssGroups.getFirst());
    }

    @Test
    void cssGroups_emptyWhenNoCssImports() {
        var cssGroups = PureDom.INSTANCE.cssGroups();
        assertTrue(cssGroups.isEmpty());
    }

    @Test
    void cssGroups_excludesNonCssModules() {
        var allImportSources = MyDom.INSTANCE.imports().getAllImports().keySet();
        var cssGroups = MyDom.INSTANCE.cssGroups();

        // OtherModule is in imports but not in cssGroups
        assertTrue(allImportSources.contains(OtherModule.INSTANCE));
        assertEquals(1, cssGroups.size(), "Only CssGroup sources should be included");
    }
}
