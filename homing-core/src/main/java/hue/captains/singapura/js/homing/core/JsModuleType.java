package hue.captains.singapura.js.homing.core;

/**
 * The <b>domain</b> classification of a served JS module — the axis the RFC 0044
 * rule policy is polymorphic over, and what a {@link CrateEntry} declares.
 *
 * <p>An <b>open</b> interface, deliberately: the framework ships its fixed set as
 * {@link StandardJsModuleType} (an enum — a sealed branch, so framework code can
 * exhaustive-{@code switch} it), while a downstream component library defines its
 * <b>own</b> types by implementing this interface alongside its modules. The
 * framework policy dispatches the sealed branch with a switch and downstream
 * types via a dictionary lookup (see {@code DefaultJsRulePolicy.extendedWith}).
 * This is <i>extend, don't patch</i>: a library adds new types + rule sets; it
 * never edits the framework's.</p>
 *
 * <p>Because a type is a plain object that co-exists with the code it classifies,
 * it is a proper map key (implementations — enums or records — carry value
 * identity) and a stable studio label. Names Are Types: a module's kind is a
 * typed value, never a raw {@code String}.</p>
 */
public interface JsModuleType {

    /** URL-safe, stable identity — unique across the framework + any extensions. */
    String slug();

    /** Human-readable label, for the studio. */
    String label();
}
