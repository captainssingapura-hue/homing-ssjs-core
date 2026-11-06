package hue.captains.singapura.js.homing.core.util;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.regex.Pattern;

/** The reader a process starts with. */
public record ResourceReaders() implements StatelessFunctionalObject {

    public static final ResourceReaders INSTANCE = new ResourceReaders();

    /** A reader over the roots {@code homing.devRoot} names, or {@link ResourceReader#CLASSPATH} when it names none. */
    public ResourceReader fromSystemProperty() {
        String devRoot = System.getProperty("homing.devRoot");
        if (devRoot == null || devRoot.isBlank()) return ResourceReader.CLASSPATH;
        return new ResourceReader(Arrays.stream(devRoot.split(Pattern.quote(File.pathSeparator)))
                .map(String::trim).filter(s -> !s.isEmpty()).map(Path::of).toList());
    }
}
