// =============================================================================
// CssLinks — the one place a stylesheet link is made (RFC 0064, D25). Both
// owners of the page's sheets append through it: the CSS manager, which loads
// the provided groups once, and ThemeSheets, which loads the derived sheets
// under the theme worn and swaps them on a switch.
//
//   CssLink.append(href) → { link, loaded }   the link, appended to <head>,
//                                              and a promise of it landing
// =============================================================================

class CssLink {
    static append(href) {
        const link = document.createElement("link");
        link.rel = "stylesheet";
        link.href = href;
        const loaded = new Promise((resolve, reject) => {
            link.onload = () => resolve(link);
            link.onerror = () => reject(new Error("Failed to load CSS: " + href));
        });
        document.head.appendChild(link);
        return { link, loaded };
    }
}
