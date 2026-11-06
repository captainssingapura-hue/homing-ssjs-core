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
}
