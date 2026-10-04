package hue.captains.singapura.js.homing.core;

/**
 * A module's path, as an import statement writes it.
 *
 * <p>One path per module. A module varies by no context: its theme and locale
 * are resolved on the client and put on the resources that vary by them — the
 * sheets — never on the module's URL. So every import of a module, static or
 * dynamic, names the same URL, and the browser holds one instance of it.</p>
 */
public record PartialModulePath(String basePath) {

    @Override
    public String toString() {
        return basePath;
    }
}
