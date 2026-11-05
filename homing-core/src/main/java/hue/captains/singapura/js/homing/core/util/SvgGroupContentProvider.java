package hue.captains.singapura.js.homing.core.util;

import hue.captains.singapura.js.homing.core.ContentProvider;
import hue.captains.singapura.js.homing.core.SvgBeing;
import hue.captains.singapura.js.homing.core.SvgGroup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Auto-generates JS content for a SvgGroup.
 * Produces a SvgBeing class and frozen marker instances for each being in the group.
 */
public record SvgGroupContentProvider<G extends SvgGroup<G>>(G group, ResourceReader resourceReader) implements ContentProvider<G> {

    public SvgGroupContentProvider(G group) {
        this(group, ResourceReader.CLASSPATH);
    }

    @Override
    public List<String> content() {
        final String dirPath = "homing/svg/" + group.getClass().getCanonicalName().replace(".", "/");
        return group.svgBeings().stream()
                .flatMap(svg->svgDeclaration(dirPath, svg))
                .toList();
    }

    Stream<String> svgDeclaration(String dirPath, SvgBeing<G> svg){
        return Stream.of(
                Stream.of("const " + svg.getClass().getSimpleName() + " = `" ),
                resourceReader.getStringsFromResource(dirPath + "/" + svg.getClass().getSimpleName() + ".svg").stream(),
                Stream.of("`;")
        ).flatMap(Function.identity());
    }
}
