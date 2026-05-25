package hue.captains.singapura.js.homing.workspace.party;

import java.util.Objects;

/**
 * Declaration of one Party — the workspace-scoped routing tree for one
 * concern, with its own sealed message ADT and its own Secretary
 * topology rooted at {@link #root()}.
 *
 * <p>This record is a <b>declaration</b>, not a runtime. Workspaces
 * construct a {@code Party} in their setup code to describe what
 * Parties they have; the framework's writer (cycle 2) emits the JS
 * bootstrap that constructs the actual runtime.</p>
 *
 * <h2>Parties stay disjoint</h2>
 *
 * <p>The compiler enforces that messages cannot leak across Parties:
 * each Party is parameterised by its own {@code M}, so a {@code Party<AnimalsMsg>}
 * cannot accept a {@code LayoutMsg}. The framework relies on this; the
 * absence of cross-Party messaging in the {@link Action} ADT
 * complements it at the dispatch level.</p>
 *
 * @param <M> the Party's scoped message ADT type
 * @param name workspace-local identifier for this Party (e.g. {@code "animalSelection"})
 * @param messageType the sealed ADT class — every message in this Party
 *                    is an instance of this class
 * @param root the root Secretary of this Party's tree
 * @since RFC 0028 cycle 1
 */
public record Party<M>(String name, Class<M> messageType, Secretary<?, M> root) {

    public Party {
        Objects.requireNonNull(name,        "Party.name");
        Objects.requireNonNull(messageType, "Party.messageType");
        Objects.requireNonNull(root,        "Party.root");
        if (name.isBlank()) {
            throw new IllegalArgumentException("Party.name must not be blank");
        }
        if (!messageType.equals(root.messageType())) {
            throw new IllegalArgumentException(
                    "Party.messageType (" + messageType.getName()
                    + ") does not match root Secretary's messageType ("
                    + root.messageType().getName() + ")");
        }
    }
}
