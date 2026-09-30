package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.studio.base.composed.ComposedDoc;
import hue.captains.singapura.js.homing.studio.base.composed.ComposedDocJson;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeJsonWriter;
import hue.captains.singapura.js.homing.studio.base.composed.DocTreeV2JsonWriter;
import hue.captains.singapura.js.homing.studio.base.image.ImageDoc;
import hue.captains.singapura.js.homing.studio.base.image.ImageJson;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDoc;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDocNormalizer;
import hue.captains.singapura.js.homing.studio.base.rigid.RigidDocV2;
import hue.captains.singapura.js.homing.studio.base.table.TableDoc;
import hue.captains.singapura.js.homing.studio.base.table.TableJson;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * A doc as the studio's legacy viewers read it: the one {@code Function<Doc, Content>}
 * the {@code /doc}, {@code /open-content} and {@code /doc-tree-content} endpoints serve.
 *
 * <p>A doc whose data is text hands its text over as it always has ({@link Doc#contents()},
 * with its {@link Doc#contentType()}). A doc whose data is structure has no content of its
 * own, so the viewers' JSON is made here, from its data: a composed doc's segments and TOC
 * (its embedded docs at leveled {@code /doc?id=<root>&l1=..} URLs), a rigid doc's tree, a
 * table's cells, an image's bytes inlined. Byte-for-byte what those docs' {@code contents()}
 * returned before the doc model became data; retired with the viewers.</p>
 *
 * <p>A composed doc's embedded docs are addressed from the root the request came in by,
 * so the function is made per position: {@link #rootedAt(String, List)}; {@link #INSTANCE}
 * places each doc at its own root.</p>
 */
public final class LegacyDocWire implements Function<Doc, Content> {

    /** Each doc at its own root - its uuid, no levels. */
    public static final LegacyDocWire INSTANCE = new LegacyDocWire(null, List.of());

    private static final String JSON = "application/json; charset=utf-8";

    private final String rootId;
    private final List<String> pathPrefix;

    private LegacyDocWire(String rootId, List<String> pathPrefix) {
        this.rootId = rootId;
        this.pathPrefix = List.copyOf(pathPrefix);
    }

    /** The wire for a doc reached from {@code rootId} by the levels {@code pathPrefix} ({@code l1..lN}). */
    public static LegacyDocWire rootedAt(String rootId, List<String> pathPrefix) {
        return new LegacyDocWire(Objects.requireNonNull(rootId, "rootId"), Objects.requireNonNull(pathPrefix, "pathPrefix"));
    }

    @Override
    public Content apply(Doc doc) {
        Objects.requireNonNull(doc, "doc");
        String root = rootId != null ? rootId : doc.uuid().toString();
        return switch (doc) {
            case ComposedDoc cd -> new Content(ComposedDocJson.write(cd, root, pathPrefix), JSON);
            // a rigid doc's tree is always rooted at the doc itself: it has no leveled children
            case RigidDoc rd    -> new Content(DocTreeJsonWriter.INSTANCE.write(
                                        RigidDocNormalizer.INSTANCE.toDocTree(rd), rd.uuid().toString()), JSON);
            case RigidDocV2 v2  -> new Content(DocTreeV2JsonWriter.INSTANCE.write(v2.toDocTreeV2(), v2.uuid().toString()), JSON);
            case TableDoc t     -> new Content(TableJson.write(t.data()), JSON);
            case ImageDoc im    -> new Content(ImageJson.write(im), JSON);
            default             -> new Content(doc.contentsRootedAt(root, pathPrefix), doc.contentType());
        };
    }
}
