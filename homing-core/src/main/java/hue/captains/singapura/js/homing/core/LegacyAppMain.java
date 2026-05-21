package hue.captains.singapura.js.homing.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Grandfather clause for {@link AppModule} types that pre-date RFC 0024's
 * Component contract. Mark an AppModule with {@code @LegacyAppMain} when
 * its {@code appMain} owns DOM directly via raw {@code document.createElement}
 * / {@code addEventListener} / {@code requestAnimationFrame} — i.e. when it
 * has not yet been migrated to mount through a typed {@code Branch} handle.
 *
 * <p><b>What the marker does:</b> the future {@code ComponentConformanceTest}
 * (RFC 0024 Phase 6) treats marked AppModules as <i>known debt</i> rather
 * than <i>unknown drift</i>:</p>
 *
 * <ul>
 *   <li>Marker present + raw DOM ops → <b>WARN</b> with the count + a
 *       pointer at RFC 0024. The build still passes.</li>
 *   <li>Marker absent + raw DOM ops outside a Component.mount body →
 *       <b>ERROR</b>. The author has either (a) migrated the app to the
 *       Component contract or (b) introduced new raw-op debt without
 *       admitting to it.</li>
 *   <li>No raw DOM ops, no marker, Component-shaped → <b>PASS</b>. The
 *       new code is held to the contract.</li>
 * </ul>
 *
 * <p><b>How to remove the marker:</b> migrate the AppModule to the
 * Component contract — its {@code appMain} body routes through a
 * {@code Branch} handle for every side effect (DOM ops, listeners,
 * animation frames, timers). Drop the annotation. The conformance gate
 * then verifies the migration is clean.</p>
 *
 * <p><b>Why an annotation rather than a marker interface:</b> a marker
 * interface would propagate through subtype relationships and force any
 * AppModule subtype hierarchy to opt in or out; the annotation attaches
 * to the exact type that needs it. Closer to {@code @Deprecated} /
 * {@code @SuppressWarnings} — flag-shaped, not type-shaped.</p>
 *
 * @since RFC 0024 Phase 5
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LegacyAppMain {

    /**
     * Optional — a brief rationale for keeping the AppModule in its
     * pre-Component shape. Helps reviewers see why migration was deferred
     * for this app specifically (e.g. "stable; no in-page lifecycle
     * pressure", "viewer base; migration pairs with DocViewer split"). The
     * conformance gate includes this string in its WARN message so the
     * audit trail is human-readable.
     *
     * @return rationale string; empty when no specific reason supplied
     */
    String reason() default "";
}
