package hue.captains.singapura.js.homing.studio.base;

import hue.captains.singapura.js.homing.server.EmptyParam;
import hue.captains.singapura.js.homing.server.ResourceNotFound;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * RFC 0004: DocGetAction takes a {@link DocRegistry} and serves Doc bytes by UUID.
 * Path traversal / extension whitelist concerns are gone — only registered Docs are
 * reachable, and the input is parsed as a UUID before any lookup.
 */
class DocGetActionTest {

    private static final UUID SENTINEL_ID = UUID.fromString("a4d8f2b1-1111-2222-3333-444455556666");

    private static final Doc SENTINEL = new Doc() {
        @Override public UUID   uuid()    { return SENTINEL_ID; }
        @Override public String title()   { return "Sentinel"; }
        @Override public String contents(){ return "# Sentinel Doc\n\nthe wiring works end-to-end\n"; }
    };

    private final DocRegistry registry = new DocRegistry(List.of(SENTINEL));
    private final DocGetAction action  = new DocGetAction(registry);

    @Test
    void execute_returnsBytesForRegisteredUuid() throws Exception {
        var result = action.execute(
                new DocGetAction.Query(SENTINEL_ID.toString(), java.util.List.of()),
                new EmptyParam.NoHeaders()).get();

        assertNotNull(result);
        assertEquals("text/markdown; charset=utf-8", result.contentType());
        assertTrue(result.body().contains("# Sentinel Doc"));
        assertTrue(result.body().contains("the wiring works end-to-end"));
    }

    @Test
    void execute_failsOnUnknownUuid() {
        var future = action.execute(
                new DocGetAction.Query(UUID.randomUUID().toString(), java.util.List.of()),
                new EmptyParam.NoHeaders());
        var ex = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(ResourceNotFound.class, ex.getCause());
    }

    @Test
    void execute_failsOnMalformedUuid() {
        var future = action.execute(
                new DocGetAction.Query("not-a-uuid", java.util.List.of()),
                new EmptyParam.NoHeaders());
        var ex = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(ResourceNotFound.class, ex.getCause());
    }

    @Test
    void execute_failsOnBlankId() {
        var future = action.execute(
                new DocGetAction.Query("", java.util.List.of()),
                new EmptyParam.NoHeaders());
        var ex = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(ResourceNotFound.class, ex.getCause());
    }

    @Test
    void execute_failsOnNullId() {
        var future = action.execute(
                new DocGetAction.Query(null, java.util.List.of()),
                new EmptyParam.NoHeaders());
        var ex = assertThrows(ExecutionException.class, future::get);
        assertInstanceOf(ResourceNotFound.class, ex.getCause());
    }
}
