package hue.captains.singapura.js.homing.studio.base.ui.layout.contract;

import java.util.Objects;

/**
 * The structural shape of a tab passed to {@code MultiTabPane.addTab()}.
 * MTP requires {@code id} and {@code title}; chrome-specific extensions
 * ({@code widgetKind}, {@code widgetInstanceUuid}, {@code render},
 * {@code setActive}, {@code onClose}) sit alongside but are not contract
 * concerns at this layer.
 *
 * <p>The Java record names the minimum contract; the JS-side tab object
 * is a plain JS object with these fields plus whatever chrome-specific
 * machinery the host attaches.</p>
 *
 * @param id    host-supplied opaque identifier (unique within the pane)
 * @param title display string shown on the strip chip
 *
 * @since RFC 0035 — MTP contract package
 */
public record TabDescriptor(TabId id, String title) {

    public TabDescriptor {
        Objects.requireNonNull(id,    "TabDescriptor.id");
        Objects.requireNonNull(title, "TabDescriptor.title");
    }
}
