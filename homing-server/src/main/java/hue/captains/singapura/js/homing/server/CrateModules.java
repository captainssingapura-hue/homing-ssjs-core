package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.Crate;
import hue.captains.singapura.js.homing.core.CrateEntry;
import hue.captains.singapura.js.homing.core.EsModule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/** What a tree of crates serves. */
public record CrateModules() implements StatelessFunctionalObject {

    public static final CrateModules INSTANCE = new CrateModules();

    /** The modules of {@code roots} and of every crate they require, by canonical name; the first declared keeps a name. */
    public Map<String, EsModule<?>> byName(Collection<? extends Crate> roots) {
        var byName = new LinkedHashMap<String, EsModule<?>>();
        var seen = new LinkedHashMap<String, Crate>();
        Deque<Crate> queue = new ArrayDeque<>(roots);
        while (!queue.isEmpty()) {
            Crate c = queue.removeFirst();
            if (seen.putIfAbsent(c.name(), c) != null) continue;
            for (CrateEntry e : c.entries()) byName.putIfAbsent(e.module().getClass().getCanonicalName(), e.module());
            queue.addAll(c.requires());
        }
        return byName;
    }
}
