package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.tao.http.action.ExternalError;
import hue.captains.singapura.tao.http.action.HttpReturnableException;
import hue.captains.singapura.tao.http.action.InternalError;

import java.util.Objects;

public final class ResourceNotFound extends RuntimeException implements HttpReturnableException<ResourceNotFound._ExternalError, ResourceNotFound._InternalError> {

    private final _InternalError internalError;
    private final _ExternalError externalError;

    public ResourceNotFound(_InternalError internalError, _ExternalError externalError) {
        this.internalError = internalError;
        this.externalError = externalError;
    }

    @Override
    public int statusCode() {
        return 404;
    }

    @Override
    public _ExternalError externalError() {
        return this.externalError;
    }

    @Override
    public _InternalError internalError() {
        return this.internalError;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceNotFound that = (ResourceNotFound) o;
        return Objects.equals(internalError, that.internalError) && Objects.equals(externalError, that.externalError);
    }

    @Override
    public int hashCode() {
        return Objects.hash(internalError, externalError);
    }

    public record _InternalError(Exception ex, String desc) implements InternalError {}

    public record _ExternalError(String resource, String furtherExplanation) implements ExternalError {}

    /** The error for a request without its {@code class} query parameter. */
    public static ResourceNotFound missingClass() {
        return new ResourceNotFound(
                new _InternalError(null, "Missing 'class' query parameter"),
                new _ExternalError("class", "Required query parameter 'class' was not provided")
        );
    }

    /** The error for a class that is found but is not an {@code expectedType}. */
    public static ResourceNotFound wrongType(String className, String expectedType) {
        return new ResourceNotFound(
                new _InternalError(null, className + " is not a " + expectedType),
                new _ExternalError(className, "The requested class is not a " + expectedType)
        );
    }

    /** The error for a class that failed to load; {@code cause} stays in the internal error. */
    public static ResourceNotFound forClass(String className, Exception cause) {
        return new ResourceNotFound(
                new _InternalError(cause, "Failed to load " + className),
                new _ExternalError(className, "The requested resource could not be found")
        );
    }
}
