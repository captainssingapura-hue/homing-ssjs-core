package hue.captains.singapura.js.homing.libs;

import hue.captains.singapura.js.homing.base.lang.StatelessSingleton;
import hue.captains.singapura.js.homing.core.BundledExternalModule;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.List;

/**
 * Registry of every 3rd-party JavaScript library bundled with this distribution.
 *
 * <h2>Why a manual list and not classpath scanning / annotation processing</h2>
 *
 * <p>Adding a 3rd-party JS library to the classpath is a security-sensitive,
 * supply-chain-touching action. It deserves a single, explicit, grep-able place
 * where the decision is recorded — not a "magic happens at build time" mechanism.
 * The registry stays manual on purpose:</p>
 *
 * <ol>
 *   <li><b>Audit-able.</b> One file, top to bottom, lists every external dependency
 *       shipped in our JAR. Reviewers, security teams, and license auditors get a
 *       complete answer with one {@code git blame}.</li>
 *
 *   <li><b>Build-deterministic.</b> The download set is exactly what's listed here —
 *       not "whatever happens to be on the build classpath today". A test class
 *       accidentally implementing {@code BundledExternalModule} can't ship by
 *       accident.</li>
 *
 *   <li><b>No reflection / annotation-processing machinery.</b> Preserves homing.js's
 *       core promise: small dep footprint, AOT/native-image friendly, no compile-time
 *       code generation. Plain Java records, plain {@code List.of(...)}.</li>
 *
 *   <li><b>Order-controlled.</b> Download order, license-listing order, and any
 *       future UI presentation order all derive from this list — single source of
 *       truth for the sequence.</li>
 *
 *   <li><b>Downstream-friendly.</b> Consumer projects can declare their own bundled
 *       libs by writing their own equivalent registry list. No collision with ours,
 *       no need to participate in our discovery scheme. Each project owns its bundle
 *       set.</li>
 *
 *   <li><b>Failure mode is loud.</b> Forget to add a new lib here? Build doesn't
 *       download it, classpath lookup fails at request time with a clear
 *       {@code ResourceNotFound}. Compare to ServiceLoader/scan: forgotten
 *       META-INF entry → silent omission until someone hits the missing route in
 *       production.</li>
 * </ol>
 *
 * <p>The cost of "one line per library" is trivially small: in five years of
 * adding a handful of bundled deps, the registry will be ~20 lines. The benefits
 * above compound forever.</p>
 *
 * <h2>How the LibDownloader uses it</h2>
 *
 * <p>{@link hue.captains.singapura.js.homing.libs.tools.LibDownloader} reads
 * {@link #all()} via reflection at build time, fetches each entry's
 * {@link BundledExternalModule#sourceUrl()}, verifies against
 * {@link BundledExternalModule#sha512()}, and writes the bytes to
 * {@link BundledExternalModule#resourcePath()} under {@code target/classes/}.</p>
 */
public record HomingLibsRegistry() implements StatelessFunctionalObject, StatelessSingleton {

    public static final HomingLibsRegistry INSTANCE = new HomingLibsRegistry();

    /** Every bundled library shipped by homing-libs. Add new entries here. */
    public List<BundledExternalModule<?>> all() {
        return List.of(
            MarkedJs.INSTANCE,
            ThreeJs.INSTANCE,
            ThreeJsSvgLoader.INSTANCE,
            ToneJs.INSTANCE
    );
    }
}
