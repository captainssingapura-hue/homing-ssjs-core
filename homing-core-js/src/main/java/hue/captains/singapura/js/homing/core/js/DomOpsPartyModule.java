package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;
import hue.captains.singapura.js.homing.core.ModuleImports;

import java.util.List;

/**
 * Public face of the DomOpsParty branch-tree DOM-ownership library
 * (RFC 0024). Vendored from {@code es-el-manager/src/party/DomOpsParty.js}.
 *
 * <p>Exports the {@code domOpsParty} singleton plus all 19 concrete level
 * types ({@code DomOpsParty}, {@code DomOpsPartyL1} … {@code
 * DomOpsPartyL18}). Each level overrides {@code createBranch} to return
 * the next deeper level, giving the type system a static guarantee that
 * branches never exceed depth 18.</p>
 *
 * <p>Components obtain a sub-branch from a parent branch and call
 * {@code branch.createElement(name, tagName)} to mint DOM elements —
 * never {@code document.createElement} directly. {@code branch.dissolve()}
 * is the single-call teardown for the whole subtree.</p>
 *
 * <h2>Import wiring</h2>
 *
 * <p>The vendored JS depends on {@code _DomOpsPartyBase} (declared in
 * {@link DomOpsPartyBaseModule}); this module declares that as an
 * {@link ImportsFor} entry so the framework's {@code EsModuleWriter}
 * generates the cross-file {@code import} statement. The original
 * {@code import { _DomOpsPartyBase } from './_DomOpsPartyBase.js';}
 * line was therefore stripped from the resource file.</p>
 *
 * @since RFC 0024 Phase 1
 */
public record DomOpsPartyModule() implements DomModule<DomOpsPartyModule> {

    public static final DomOpsPartyModule INSTANCE = new DomOpsPartyModule();

    // ── Exported level classes (deepest-first, mirroring the JS file). ───────
    public record DomOpsPartyL18() implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL17() implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL16() implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL15() implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL14() implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL13() implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL12() implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL11() implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL10() implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL9()  implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL8()  implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL7()  implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL6()  implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL5()  implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL4()  implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL3()  implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL2()  implements Exportable._Class<DomOpsPartyModule> {}
    public record DomOpsPartyL1()  implements Exportable._Class<DomOpsPartyModule> {}

    /** Root party class (depth 0). Instantiated once as the JS-side
     *  {@code domOpsParty} singleton — see the sibling top-level
     *  {@code domOpsParty} record for the singleton export marker. */
    public record DomOpsParty() implements Exportable._Class<DomOpsPartyModule> {}

    // The {@code domOpsParty} singleton export marker is intentionally a
    // sibling top-level record (NOT nested here) to avoid a Windows
    // filesystem case-collision with {@code DomOpsParty} — both would
    // compile to {@code DomOpsPartyModule$<Name>.class} files whose
    // filenames differ only by case, and NTFS case-insensitive default
    // would have one overwrite the other in the JAR. The JS-side export
    // identifier {@code domOpsParty} is preserved via the top-level
    // record's simple name.

    @Override
    public ImportsFor<DomOpsPartyModule> imports() {
        return ImportsFor.<DomOpsPartyModule>builder()
                .add(new ModuleImports<>(
                        List.of(new DomOpsPartyBaseModule._DomOpsPartyBase()),
                        DomOpsPartyBaseModule.INSTANCE))
                .build();
    }

    @Override
    public ExportsOf<DomOpsPartyModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(
                new DomOpsPartyL18(), new DomOpsPartyL17(), new DomOpsPartyL16(),
                new DomOpsPartyL15(), new DomOpsPartyL14(), new DomOpsPartyL13(),
                new DomOpsPartyL12(), new DomOpsPartyL11(), new DomOpsPartyL10(),
                new DomOpsPartyL9(),  new DomOpsPartyL8(),  new DomOpsPartyL7(),
                new DomOpsPartyL6(),  new DomOpsPartyL5(),  new DomOpsPartyL4(),
                new DomOpsPartyL3(),  new DomOpsPartyL2(),  new DomOpsPartyL1(),
                new DomOpsParty(),
                new domOpsParty()
        ));
    }
}
