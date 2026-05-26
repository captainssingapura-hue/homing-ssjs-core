package hue.captains.singapura.js.homing.studio.base.export;

import hue.captains.singapura.js.homing.core.DomModule;
import hue.captains.singapura.js.homing.core.Exportable;
import hue.captains.singapura.js.homing.core.ExportsOf;
import hue.captains.singapura.js.homing.core.ImportsFor;

import java.util.List;

/**
 * Shared client-side HTML export utility — provides {@code exportPageAsHtml(filename)}
 * for any rendered doc page.
 *
 * <h2>Problem</h2>
 *
 * <p>Chrome's "Save as → Webpage, Single File" (MHTML) format has two properties
 * that break in-page navigation for dynamically-rendered doc pages:</p>
 * <ul>
 *   <li>All {@code <script>} elements and event-handler attributes are stripped —
 *       no JavaScript runs when the file is opened.</li>
 *   <li>Every {@code <a href>} is serialised as the browser's fully-resolved
 *       property value. A link set via {@code setAttribute("href", "#anchor")}
 *       becomes {@code href="http://localhost:8080/app?…#anchor"} in the file;
 *       clicking it navigates to the live server rather than scrolling the page.</li>
 * </ul>
 *
 * <h2>Solution</h2>
 *
 * <p>This module produces a standalone {@code .html} file entirely in the browser,
 * without a server round-trip:</p>
 * <ol>
 *   <li>All linked stylesheets are fetched in parallel and inlined as a single
 *       {@code <style>} block — the file renders correctly with no server.</li>
 *   <li>{@code <script>} elements are removed from the clone.</li>
 *   <li>Page-local fragment hrefs are correct by construction: the DOM content
 *       attribute holds the raw value set by {@code HrefManager} ({@code "#anchor"}),
 *       and {@code innerHTML} serialisation uses content attributes, not the resolved
 *       IDL property — so the output naturally contains bare {@code #anchor} hrefs
 *       that work offline without any rewriting.</li>
 *   <li>The result is downloaded via a {@code Blob} URL — no server endpoint needed.</li>
 * </ol>
 *
 * <h2>Usage</h2>
 *
 * <p>Any viewer module that wants an export button imports
 * {@link exportPageAsHtml} and calls
 * {@code exportPageAsHtml(filename)} (returns a {@code Promise<void>}):</p>
 *
 * <pre>{@code
 * exportPageAsHtml('my-doc.html');
 * }</pre>
 *
 * @since HTML-export feature (MHTML replacement)
 */
public record HtmlExportModule() implements DomModule<HtmlExportModule> {

    /**
     * Async function exported to callers.
     * Signature: {@code exportPageAsHtml(filename: string) → Promise<void>}
     */
    public record exportPageAsHtml() implements Exportable._Constant<HtmlExportModule> {}

    public static final HtmlExportModule INSTANCE = new HtmlExportModule();

    @Override
    public ImportsFor<HtmlExportModule> imports() {
        // No framework imports — uses only browser globals (fetch, Blob, URL, XMLSerializer).
        return ImportsFor.<HtmlExportModule>builder().build();
    }

    @Override
    public ExportsOf<HtmlExportModule> exports() {
        return new ExportsOf<>(INSTANCE, List.of(new exportPageAsHtml()));
    }
}
