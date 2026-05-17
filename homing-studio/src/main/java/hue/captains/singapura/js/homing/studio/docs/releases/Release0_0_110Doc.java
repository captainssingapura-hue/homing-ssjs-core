package hue.captains.singapura.js.homing.studio.docs.releases;

import hue.captains.singapura.js.homing.studio.base.ClasspathMarkdownDoc;
import hue.captains.singapura.js.homing.studio.base.DocReference;
import hue.captains.singapura.js.homing.studio.base.Reference;
import hue.captains.singapura.js.homing.studio.docs.blocks.MdadKitDoc;
import hue.captains.singapura.js.homing.studio.docs.casestudies.WhyWeDitchedHtmlCaseStudy;
import hue.captains.singapura.js.homing.studio.docs.defects.Defect0005Doc;
import hue.captains.singapura.js.homing.studio.docs.defects.Defect0006Doc;
import hue.captains.singapura.js.homing.studio.docs.doctrines.TypedContentVocabularyDoc;
import hue.captains.singapura.js.homing.studio.docs.guides.ReleaseChecklistDoc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0017Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0018Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0019Doc;
import hue.captains.singapura.js.homing.studio.docs.rfcs.Rfc0020Doc;

import java.util.List;
import java.util.UUID;

/**
 * Release notes for 0.0.110 (binary — sixth release, predecessor 0.0.101).
 *
 * <p>One headline doctrine and four RFCs that together replace the framework's
 * markdown-with-HTML-escape-hatch prose model with a typed content vocabulary.
 * Five new sealed segment kinds, three new first-class Doc subtypes
 * (SvgDoc/TableDoc/ImageDoc), a strict additive {@code .mdad+} grammar derived
 * from auditing existing prose, and a self-proof case study written entirely
 * in the new vocabulary. Two new conformance tests close registration-drift
 * 404 holes (Defect 0005). Purely additive — no breaking changes for
 * downstream studios.</p>
 */
public record Release0_0_110Doc() implements ClasspathMarkdownDoc {
    private static final UUID ID = UUID.fromString("d9f4c2a7-6b18-4e3f-8a51-7c9e2d4b5f80");
    public static final Release0_0_110Doc INSTANCE = new Release0_0_110Doc();

    @Override public UUID   uuid()    { return ID; }
    @Override public String title()   { return "0.0.110 — Typed Content Vocabulary"; }
    @Override public String summary() { return "Typed Content Vocabulary doctrine + four RFCs (0017 Themable Content, 0018 Slim Markdown, 0019 ComposedDoc, 0020 Visual Asset Docs) land as one arc. ComposedDoc + a sealed 5-variant Segment ADT (Text / Markdown / Svg / Table / Image), TextSegment with an audit-driven .mdad+ parser, and three new first-class Doc kinds. The self-proof: a case study written entirely in the new vocabulary. Two registration-drift conformance tests close a 404 class. One new skill (create-homing-content-tree), three skill updates. Purely additive; no migration required."; }
    @Override public String category(){ return "RELEASE"; }

    @Override public List<Reference> references() {
        return List.of(
                new DocReference("doc-tcv",      TypedContentVocabularyDoc.INSTANCE),
                new DocReference("rfc-17",       Rfc0017Doc.INSTANCE),
                new DocReference("rfc-18",       Rfc0018Doc.INSTANCE),
                new DocReference("rfc-19",       Rfc0019Doc.INSTANCE),
                new DocReference("rfc-20",       Rfc0020Doc.INSTANCE),
                new DocReference("def-5",        Defect0005Doc.INSTANCE),
                new DocReference("def-6",        Defect0006Doc.INSTANCE),
                new DocReference("cs-why-html",  WhyWeDitchedHtmlCaseStudy.INSTANCE),
                new DocReference("mdad-kit",     MdadKitDoc.INSTANCE),
                new DocReference("checklist",    ReleaseChecklistDoc.INSTANCE),
                new DocReference("rel-0-0-101",  Release0_0_101Doc.INSTANCE)
        );
    }
}
