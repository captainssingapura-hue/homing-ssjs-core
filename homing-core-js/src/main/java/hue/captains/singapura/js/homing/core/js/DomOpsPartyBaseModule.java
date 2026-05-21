package hue.captains.singapura.js.homing.core.js;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * Internal base class of the DomOpsParty branch-tree library (RFC 0024).
 *
 * <p>Vendored from {@code es-el-manager/src/party/_DomOpsPartyBase.js}.
 * Not part of the public API — components should import from
 * {@link DomOpsPartyModule} instead. This module exists only so the
 * framework can wire {@code _DomOpsPartyBase} into the import graph of
 * {@link DomOpsPartyModule} (where the 19 level classes extend it).</p>
 *
 * <p>The JS source carries every level type's shared behaviour:
 * element ownership, branch registration, recursive {@code dissolve()}
 * teardown, owner-WeakRef activation gate, and name validation.</p>
 *
 * <p>The leading underscore in {@code _DomOpsPartyBase} is preserved from
 * the upstream source as a Java identifier; it's a legal record name and
 * keeps the JS export symbol identical to the vendored bundle.</p>
 *
 * @since RFC 0024 Phase 1
 */
public record DomOpsPartyBaseModule() implements DomModule<DomOpsPartyBaseModule> {

    public static final DomOpsPartyBaseModule INSTANCE = new DomOpsPartyBaseModule();

    /** The {@code _DomOpsPartyBase} JS class. */
    @SuppressWarnings("checkstyle:TypeName")
    public record _DomOpsPartyBase() implements Exportable._Class<DomOpsPartyBaseModule> {}

    @Override
    public ImportsFor<DomOpsPartyBaseModule> imports() {
        return ImportsFor.noImports();
    }

    @Override
    public ExportsOf<DomOpsPartyBaseModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new _DomOpsPartyBase()));
    }
}
