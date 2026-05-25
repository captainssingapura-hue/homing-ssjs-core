package hue.captains.singapura.js.homing.ssjs.test;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.junit.jupiter.api.AfterEach;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Generic GraalVM-backed test base for any pure JavaScript module that
 * exports one or more top-level objects via {@code var X = {...}} script
 * semantics — the canonical authoring style for ssjs modules.
 *
 * <p>Subclasses get:</p>
 * <ul>
 *   <li>A fresh JS {@link Context} per test (torn down on
 *       {@link #closeContext() @AfterEach}).</li>
 *   <li>{@link #loadModule(String) loadModule}/{@link #loadGlobal(String) loadGlobal}
 *       — read a classpath resource, evaluate it as JS, and return a named
 *       global as a Polyglot {@link Value}.</li>
 *   <li>{@link #obj(Map) obj} / {@link #envelope(String, Map, String) envelope}
 *       — marshalling helpers so JS sees real native objects (a raw Java
 *       {@link Map} comes through as a host object whose members can't be
 *       read with dot access).</li>
 *   <li>{@link #deepCopy(Value) deepCopy} — {@code JSON.parse(JSON.stringify)}
 *       on the running context; adequate for plain data records used as test
 *       fixtures.</li>
 * </ul>
 *
 * <p>This base intentionally knows nothing about any particular module's
 * contract — Secretary-shaped tests extend
 * {@link SecretaryTestBase}, which adds the Step assertions on top.</p>
 *
 * <h2>Usage</h2>
 *
 * <pre>{@code
 * class MyReducerTest extends JsModuleTestBase {
 *     @BeforeEach void load() { loadModule("/path/to/MyReducer.js"); }
 *
 *     @Test void reducesAdd() {
 *         Value reducer = global("MyReducer");
 *         Value next = reducer.getMember("reduce").execute(
 *             obj(Map.of("count", 0)),
 *             obj(Map.of("kind", "INC")));
 *         assertEquals(1, next.getMember("count").asInt());
 *     }
 * }
 * }</pre>
 */
public abstract class JsModuleTestBase {

    /** The live JS context for the current test — null between tests. */
    protected Context js;

    @AfterEach
    void closeContext() {
        if (js != null) {
            js.close();
            js = null;
        }
    }

    // ─── Module loading ────────────────────────────────────────────────────

    /**
     * Boot a fresh JS context (if one isn't already live) and evaluate
     * {@code resourcePath} as JavaScript. The file is read from the test
     * classpath via {@link Class#getResourceAsStream(String)}.
     *
     * <p>May be called multiple times in a single {@code @BeforeEach} to
     * compose modules that share state via the global object (the typical
     * authoring convention for ssjs modules under the framework).</p>
     */
    protected void loadModule(String resourcePath) {
        ensureContext();
        String src = readResource(resourcePath);
        Source source = Source.newBuilder("js", src, sourceName(resourcePath)).buildLiteral();
        js.eval(source);
    }

    /**
     * Convenience: load a module and return the named top-level global as
     * a {@link Value}, asserting that it exists.
     */
    protected Value loadGlobal(String resourcePath, String globalName) {
        loadModule(resourcePath);
        return global(globalName);
    }

    /** Look up an already-evaluated global; fails the test if missing. */
    protected Value global(String name) {
        ensureContext();
        Value v = js.getBindings("js").getMember(name);
        assertNotNull(v, "expected global '" + name + "' to be defined on the JS context");
        return v;
    }

    /**
     * Hook for subclasses that want non-default {@link Context} options
     * (e.g. host access, ESM eval semantics). Defaults to a hermetic JS-only
     * context at ECMAScript 2022.
     */
    protected Context buildContext() {
        return Context.newBuilder("js")
                .allowAllAccess(false)
                .option("js.ecmascript-version", "2022")
                .build();
    }

    // ─── Marshalling ───────────────────────────────────────────────────────

    /** Wrap a Java {@link Map} as a {@link ProxyObject} so JS sees real members. */
    protected Value obj(Map<String, Object> entries) {
        ensureContext();
        return js.asValue(ProxyObject.fromMap(deepWrap(entries)));
    }

    /**
     * Build an envelope of the canonical Party shape:
     * {@code { from, message: { kind, ...payload } }}.
     */
    protected Value envelope(String kind, Map<String, Object> payload, String from) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("kind", kind);
        if (payload != null) message.putAll(payload);
        Map<String, Object> env = new LinkedHashMap<>();
        env.put("from", from);
        env.put("message", message);
        return obj(env);
    }

    /** {@code JSON.parse(JSON.stringify(value))} on the live context. */
    protected Value deepCopy(Value v) {
        ensureContext();
        Value json = js.getBindings("js").getMember("JSON");
        return json.getMember("parse").execute(json.getMember("stringify").execute(v));
    }

    // ─── Internals ─────────────────────────────────────────────────────────

    private void ensureContext() {
        if (js == null) js = buildContext();
    }

    private String readResource(String path) {
        try (InputStream in = getClass().getResourceAsStream(path)) {
            if (in == null) throw new IllegalArgumentException("classpath resource not found: " + path);
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("failed to read " + path, e);
        }
    }

    private String sourceName(String path) {
        int slash = path.lastIndexOf('/');
        return slash < 0 ? path : path.substring(slash + 1);
    }

    /** Recursively rewrap nested Maps as ProxyObjects so JS sees them natively. */
    @SuppressWarnings("unchecked")
    private Map<String, Object> deepWrap(Map<String, Object> in) {
        Map<String, Object> out = new LinkedHashMap<>(in.size());
        for (Map.Entry<String, Object> e : in.entrySet()) {
            Object v = e.getValue();
            if (v instanceof Map<?, ?> m) {
                out.put(e.getKey(), ProxyObject.fromMap(deepWrap((Map<String, Object>) m)));
            } else {
                out.put(e.getKey(), v);
            }
        }
        return out;
    }
}
