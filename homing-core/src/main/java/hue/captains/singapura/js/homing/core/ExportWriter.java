package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.js.homing.base.lang.StatelessSingleton;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;
import java.util.stream.Collectors;

public record ExportWriter() implements StatelessFunctionalObject, StatelessSingleton {
    public static final ExportWriter INSTANCE = new ExportWriter();
    /** The export statement, naming each export by its class's simple name; no lines when there is no export. */
    public <M extends EsModule<M>> List<String> writeExports(ExportsOf<M> exports){
        if(exports.exports().isEmpty()) return List.of();
        return List.of("export {" + exports.exports().stream().map(e->e.getClass().getSimpleName()).collect(Collectors.joining(", ")) + "};");
    }
}
