package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/** The ways a served resource is not found, each with what is logged and what is answered. */
public record ResourceNotFounds() implements StatelessFunctionalObject {

    public static final ResourceNotFounds INSTANCE = new ResourceNotFounds();

    /** The error for a request without its {@code class} query parameter. */
    public ResourceNotFound missingClass() {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, "Missing 'class' query parameter"),
                new ResourceNotFound._ExternalError("class", "Required query parameter 'class' was not provided")
        );
    }

    /** The error for a class that is found but is not an {@code expectedType}. */
    public ResourceNotFound wrongType(String className, String expectedType) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, className + " is not a " + expectedType),
                new ResourceNotFound._ExternalError(className, "The requested class is not a " + expectedType)
        );
    }

    /** The error for a class that failed to load; {@code cause} stays in the internal error. */
    public ResourceNotFound forClass(String className, Exception cause) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(cause, "Failed to load " + className),
                new ResourceNotFound._ExternalError(className, "The requested resource could not be found")
        );
    }
}
