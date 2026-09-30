package hue.captains.singapura.js.homing.studio.base.composed.text;

import hue.captains.singapura.tao.ontology.ValueObject;

import java.util.List;
import java.util.Objects;

/**
 * RFC 0018 / Phase 4 — sealed block-element ADT for {@code TextSegment}
 * prose. Four variants total, each earning its keep via the audit:
 *
 * <ul>
 *   <li>{@link Para}     — paragraph; blank-line-separated</li>
 *   <li>{@link Bullets}  — unordered list ({@code -} prefix per item)</li>
 *   <li>{@link Numbered} — ordered list ({@code N.} prefix per item)</li>
 *   <li>{@link Quote}    — blockquote ({@code >} prefix per line)</li>
 * </ul>
 *
 * <p>Deliberately excluded from this tier:</p>
 * <ul>
 *   <li>Headings inside body — segment {@code title} carries the section
 *       heading; deeper hierarchy splits into more segments.</li>
 *   <li>Code blocks — promote to a future typed {@code CodeSegment}
 *       (separate Doc kind, syntax-highlighted, citable).</li>
 *   <li>Tables / images / SVG — already typed segments
 *       ({@code TableSegment} / {@code ImageSegment} / {@code SvgSegment}).</li>
 * </ul>
 *
 * @since RFC 0018 Phase 4
 */
public sealed interface Block extends ValueObject
        permits Block.Para, Block.Bullets, Block.Numbered, Block.Quote {

    /** Paragraph. The inlines have already been tokenized. */
    record Para(List<Inline> inlines) implements Block {
        public Para {
            Objects.requireNonNull(inlines, "Block.Para.inlines");
            inlines = List.copyOf(inlines);
        }
    }

    /** Unordered list. Each item is a sequence of inlines. */
    record Bullets(List<List<Inline>> items) implements Block {
        public Bullets {
            Objects.requireNonNull(items, "Block.Bullets.items");
            items = items.stream().map(List::copyOf).toList();
            if (items.isEmpty()) {
                throw new IllegalArgumentException("Block.Bullets must have at least one item");
            }
        }
    }

    /** Ordered list. Each item is a sequence of inlines. */
    record Numbered(List<List<Inline>> items) implements Block {
        public Numbered {
            Objects.requireNonNull(items, "Block.Numbered.items");
            items = items.stream().map(List::copyOf).toList();
            if (items.isEmpty()) {
                throw new IllegalArgumentException("Block.Numbered must have at least one item");
            }
        }
    }

    /** Blockquote. Lines were joined; inlines carry the resulting prose. */
    record Quote(List<Inline> inlines) implements Block {
        public Quote {
            Objects.requireNonNull(inlines, "Block.Quote.inlines");
            inlines = List.copyOf(inlines);
        }
    }
}
