package hue.captains.singapura.js.homing.studio.es;

import hue.captains.singapura.js.homing.studio.base.app.Entry;
import hue.captains.singapura.js.homing.studio.base.app.L2_Catalogue;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0001Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0005Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0005Ext1Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0005Ext2Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0009Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0010Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0011Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0012Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0013Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0014Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0015Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0016Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0021Doc;

import java.util.List;

/**
 * L2 sub-catalogue of {@link RfcsCatalogue} — RFCs that shape how the framework's
 * apps, catalogues, and plans fit together. The structural spine of Homing.
 */
public record ArchitectureRfcsCatalogue()
        implements L2_Catalogue<RfcsCatalogue, ArchitectureRfcsCatalogue> {

    public static final ArchitectureRfcsCatalogue INSTANCE = new ArchitectureRfcsCatalogue();

    @Override public RfcsCatalogue parent() { return RfcsCatalogue.INSTANCE; }
    @Override public String name()    { return "Architecture"; }
    @Override public String summary() { return "How apps register, how catalogues form a typed tree, how plans get tracked — the structural decisions that hold the framework together."; }

    @Override public List<Entry<ArchitectureRfcsCatalogue>> leaves() {
        return List.of(
                Entry.of(this, Rfc0001Doc.INSTANCE),
                Entry.of(this, Rfc0005Doc.INSTANCE),
                Entry.of(this, Rfc0005Ext1Doc.INSTANCE),
                Entry.of(this, Rfc0005Ext2Doc.INSTANCE),
                Entry.of(this, Rfc0009Doc.INSTANCE),
                Entry.of(this, Rfc0010Doc.INSTANCE),
                Entry.of(this, Rfc0011Doc.INSTANCE),
                Entry.of(this, Rfc0012Doc.INSTANCE),
                Entry.of(this, Rfc0013Doc.INSTANCE),
                Entry.of(this, Rfc0014Doc.INSTANCE),
                Entry.of(this, Rfc0015Doc.INSTANCE),
                Entry.of(this, Rfc0016Doc.INSTANCE),
                Entry.of(this, Rfc0021Doc.INSTANCE)
        );
    }
}
