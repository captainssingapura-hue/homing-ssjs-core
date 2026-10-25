package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.QueryString;
import hue.captains.singapura.js.homing.core.SimpleAppResolver;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An app is found by its simple name in the registry, never by its class's name: {@code /app?class=} is retired
 * (NoReflectionAtRunTime), and answers not found, saying {@code ?app=} is the way.
 */
class AppByNameTest {

    private final AppHtmlGetAction action = new AppHtmlGetAction(new QueryParamResolver(),
            new SimpleAppResolver(List.of(AppHtmlStampTest.PlainApp.INSTANCE)));

    @Test
    void byItsClassName_notFound_sayingByItsSimpleName() {
        var q = new AppQuery(null, null, null, QueryString.parse("class=" + AppHtmlStampTest.PlainApp.class.getName()));
        var failed = assertThrows(ExecutionException.class, () -> action.execute(q, new EmptyParam.NoHeaders()).get());
        var notFound = assertInstanceOf(ResourceNotFound.class, failed.getCause());
        assertEquals(404, notFound.statusCode());
        assertEquals("No app named: use ?app=<simple name>", notFound.externalError().furtherExplanation());
    }

    @Test
    void bySimpleName_asBefore() throws Exception {
        var q = new AppQuery("plain", null, null, QueryString.parse("app=plain"));
        assertTrue(action.execute(q, new EmptyParam.NoHeaders()).get().body().contains("<!DOCTYPE html>"));
    }
}
