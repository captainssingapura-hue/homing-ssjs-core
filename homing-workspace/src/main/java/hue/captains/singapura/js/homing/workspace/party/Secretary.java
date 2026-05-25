package hue.captains.singapura.js.homing.workspace.party;

import java.util.List;
import java.util.Objects;

/**
 * Declaration of one Secretary in a Party's tree — name, state shape,
 * pointer to the JS-side behaviour module, and child Secretaries.
 *
 * <p>This record is a <b>declaration</b>, not a runtime. The actual
 * behaviour function {@code (state, envelope) → Step} lives in JS at
 * {@link #jsModulePath()}. The Java side carries the typed shape so
 * the workspace can author its Party topology in Java; a future
 * Java-to-JS writer (cycle 2) emits the bootstrap that wires the JS
 * behaviour into the runtime at the declared position.</p>
 *
 * <p>The generic parameters {@code <S, M>} are documentary: they tell
 * the workspace author what state shape and message ADT this Secretary
 * deals with. They are not used at JS runtime (behaviour is dynamically
 * typed there).</p>
 *
 * @param <S> the Secretary's state type (workspace-defined record)
 * @param <M> the Party's scoped message ADT type
 * @param name local identifier within the Party tree — appended to the
 *             parent's path to form this Secretary's {@link AgentId}
 * @param stateType the state class — recorded for inspection metadata
 * @param messageType the Party's ADT class — recorded for inspection
 * @param jsModulePath where the behaviour and initial state live in JS
 * @param subSecretaries child Secretaries (same M, possibly different S);
 *                      immutable copy
 * @since RFC 0028 cycle 1
 */
public record Secretary<S, M>(
        String name,
        Class<S> stateType,
        Class<M> messageType,
        JsModulePath jsModulePath,
        List<Secretary<?, M>> subSecretaries
) {

    public Secretary {
        Objects.requireNonNull(name,         "Secretary.name");
        Objects.requireNonNull(stateType,    "Secretary.stateType");
        Objects.requireNonNull(messageType,  "Secretary.messageType");
        Objects.requireNonNull(jsModulePath, "Secretary.jsModulePath");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Secretary.name must not be blank");
        }
        subSecretaries = List.copyOf(subSecretaries);
    }

    /** Convenience: a leaf Secretary with no sub-Secretaries. */
    public static <S, M> Secretary<S, M> leaf(
            String name, Class<S> stateType, Class<M> messageType, JsModulePath jsModulePath) {
        return new Secretary<>(name, stateType, messageType, jsModulePath, List.of());
    }
}
