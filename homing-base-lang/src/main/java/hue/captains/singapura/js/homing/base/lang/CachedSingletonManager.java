package hue.captains.singapura.js.homing.base.lang;

import hue.captains.singapura.tao.ontology.Mutable;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@link SingletonManager} that keeps what each read came to, so it asks the manager it wraps about a type once.
 * Stateful by design: what it has read is its state, and it grows as it is asked.
 *
 * <p>It keeps a type's instance, and a type's refusal - so a type refused once is refused in the same words after,
 * even where a second read would say less (a class that failed to initialize says only, the second time, that it could
 * not be). It never keeps "not there yet": a type asked for while it is still being initialized is asked about again
 * when it is asked for again.</p>
 *
 * <p>Two threads asking for a type at the very first may both ask the wrapped manager; the first answer kept is the
 * one both are given, and for an instance it is the same object either way.</p>
 *
 * <p>Holding state, it is no singleton - only a stateless type is one. Whoever wants the cache makes one, keeps it as
 * long as the types it reads - a server's run, say - and hands it to what needs it. It holds every type it has read for
 * as long as it lives.</p>
 */
public final class CachedSingletonManager implements SingletonManager, Mutable {

    private final SingletonManager reader;
    private final ConcurrentMap<Class<?>, Read> reads = new ConcurrentHashMap<>();

    /** A cache over {@code reader}, which is asked about each type once. */
    public CachedSingletonManager(SingletonManager reader) {
        this.reader = Objects.requireNonNull(reader, "reader");
    }

    @Override
    public Read read(Class<?> type) {
        Read kept = reads.get(type);
        if (kept != null) return kept;
        // Asked outside the map, never inside a compute: reading one type may initialize it, and its initialization
        // may ask this manager for another type.
        Read read = reader.read(type);
        if (read instanceof Read.NotYet) return read;
        Read first = reads.putIfAbsent(type, read);
        return first != null ? first : read;
    }
}
