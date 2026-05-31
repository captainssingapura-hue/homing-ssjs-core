/**
 * RFC 0035 — Substrate-Free Contracts for MultiTabPane.
 *
 * <p>The Java contract surface for MTP. Second concrete application of
 * the {@code A Good Design Doesn't Pick Up Its Substrate} meta-doctrine,
 * following the event-management package
 * ({@code homing-workspace/events/contract}). Same shape: typed
 * identifiers + value records + a sealed sum for the mutation
 * vocabulary + an interface for the public method set, all
 * substrate-free; the JS implementation conforms structurally.</p>
 *
 * <h2>Contract surface</h2>
 *
 * <ul>
 *   <li><b>Identifiers:</b> {@link SlotId}, {@link TabId}</li>
 *   <li><b>Value record:</b> {@link TabDescriptor}</li>
 *   <li><b>Sealed sum:</b> {@link MtpMutationEvent} with 8 variants
 *       ({@code TabActivated}, {@code TabAdded}, {@code TabRemoved},
 *       {@code TabMoved}, {@code TabAttached},
 *       {@code WorkspaceActiveChanged}, {@code Split}, {@code Merge})</li>
 *   <li><b>Interface:</b> {@link MultiTabPaneContract} — public method
 *       set + callback option names that the JS class implements</li>
 * </ul>
 *
 * <h2>V1 compromises</h2>
 *
 * <p>{@code paneId} and {@code orientation} are typed as {@code String}
 * in this V1 — the dependency direction (studio-base does not depend
 * on homing-workspace) prevents reusing the typed {@code PaneId} and
 * {@code Orientation} from {@code homing-workspace/state}. Follow-up:
 * move both to a shared lower module so this contract can adopt them.</p>
 *
 * <h2>Conformance gate</h2>
 *
 * <p>{@code MultiTabPaneConformanceTest} loads {@code
 * MultiTabPaneModule.js} in a GraalVM Polyglot context and asserts:
 * the class is defined; every public method declared in {@link
 * MultiTabPaneContract} exists with matching arity; every callback
 * option in {@link MultiTabPaneContract#CALLBACK_OPTION_NAMES} is
 * accepted at construction time.</p>
 *
 * @since RFC 0035 — second domain application of the meta-doctrine.
 */
package hue.captains.singapura.js.homing.studio.base.ui.layout.contract;
