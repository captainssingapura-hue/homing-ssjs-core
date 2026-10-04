package hue.captains.singapura.js.homing.core.util;

import hue.captains.singapura.js.homing.core.ContentProvider;
import hue.captains.singapura.js.homing.core.EsModule;

import java.util.List;

/** A module's JS body, read from its classpath resource {@code homing/js/<package path>/<Name>.js}. */
public record ReadContentFromResources<M extends EsModule>(M module, ResourceReader resourceReader) implements ContentProvider<M> {

    public ReadContentFromResources(M module) {
        this(module, ResourceReader.INSTANCE);
    }

    @Override
    public List<String> content() {
        final String basePath = "homing/js/" + module.getClass().getCanonicalName().replace(".", "/");
        return resourceReader.getStringsFromResource(basePath + ".js");
    }
}
