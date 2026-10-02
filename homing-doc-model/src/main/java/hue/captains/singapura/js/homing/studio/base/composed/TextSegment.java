package hue.captains.singapura.js.homing.studio.base.composed;

import hue.captains.singapura.js.homing.studio.base.composed.text.Block;
import hue.captains.singapura.js.homing.studio.base.composed.text.TextParser;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * RFC 0018 / Phase 4 — strict-typed prose segment.
 *
 * <p>Where {@link MarkdownSegment} parses arbitrary CommonMark on the
 * client (escape hatches and all), TextSegment is its disciplined
 * sibling: bodies must parse cleanly as the {@code .mdad+} grammar
 * (paragraphs + inline emphasis + lists + blockquotes + typed
 * references — the additive vocabulary the audit said tech docs
 * actually need). Anything else fails at construction time with a
 * precise line + column.</p>
 *
 * <p>Parsing happens server-side; the parsed {@link Block} list is
 * shipped on the wire as JSON, so the client renderer is a tiny
 * AST-walk (no markdown library, no second parser).</p>
 *
 * <h3>What's in the grammar (per the Phase 4 audit)</h3>
 * <ul>
 *   <li>Paragraphs — blank-line-separated</li>
 *   <li>Bullet lists ({@code - item})</li>
 *   <li>Numbered lists ({@code N. item})</li>
 *   <li>Blockquotes ({@code > line})</li>
 *   <li>Inline {@code **bold**}, {@code *italic*}, {@code `code`}</li>
 *   <li>Typed references {@code [label](#ref:name)} (RFC 0004-ext1)</li>
 * </ul>
 *
 * <h3>What's deliberately NOT in the grammar</h3>
 * <ul>
 *   <li>Headings inside body — {@code title} carries the section heading;
 *       deeper hierarchy splits into more segments.</li>
 *   <li>Code blocks — promote to a future typed {@code CodeSegment}.</li>
 *   <li>Tables / images / SVG — already typed segments.</li>
 *   <li>Raw HTML — angle brackets are literal text; the grammar simply
 *       doesn't recognise markup tokens.</li>
 * </ul>
 *
 * @param body  the prose source — must parse as the {@code .mdad+} grammar
 * @param title optional segment title; contributes to TOC when present
 *
 * @since RFC 0018 Phase 4
 * @deprecated Signpost only (no removal yet): free-form prose segments are being
 *     superseded by strict, directly-constructed block segments built on
 *     {@link hue.captains.singapura.js.homing.studio.base.composed.text.Line} —
 *     as {@link SimpleListSegment} already is for lists. A paragraph/quote
 *     successor is planned; prefer the typed building blocks going forward.
 *     Still fully functional in the meantime.
 */
@Deprecated
public record TextSegment(String body, Optional<String> title) implements Listable {

    public TextSegment {
        Objects.requireNonNull(body,  "TextSegment.body");
        Objects.requireNonNull(title, "TextSegment.title (use Optional.empty)");
        // Validate at construction — fail fast on bad bodies. The thrown
        // ParseException carries line + column.
        TextParser.parse(body);
    }

    /** Convenience — no title. */
    public TextSegment(String body) {
        this(body, Optional.empty());
    }

    /** The parsed AST. Re-parses on each call (cheap for these sizes). */
    public List<Block> parsed() {
        return TextParser.parse(body);
    }
}
