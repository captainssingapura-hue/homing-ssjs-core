package hue.captains.singapura.js.homing.studio.base;

/**
 * A doc whose own content is markdown text: {@link #contents()} is the markdown. Extended by
 * every markdown kind - {@link MarkdownDoc}, {@link ClasspathMarkdownDoc},
 * {@link ResourceMarkdownDoc}, {@link InlineDoc} - so a function that reads markdown can ask
 * for a markdown doc by its type, where before a markdown doc was known only by a string, its
 * file extension. It adds nothing to {@link Doc}; it says what the doc is.
 */
public interface MarkdownSource extends Doc {
}
