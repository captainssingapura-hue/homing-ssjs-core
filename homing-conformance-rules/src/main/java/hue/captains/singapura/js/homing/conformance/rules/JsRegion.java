package hue.captains.singapura.js.homing.conformance.rules;

/**
 * Which segment of a served module a rule reads, and a {@link Finding} points
 * at. The served artifact is structured (RFC 0044) so a rule scopes to the part
 * it cares about — a "no raw href" rule reads the {@link #BODY} and never flags
 * the sanctioned {@code import { … as href }} in the {@link #PROLOGUE}.
 */
public enum JsRegion {
    /** The generated prologue — injected manager imports. */
    PROLOGUE,
    /** The authored body — the {@code .js} file or the {@code SelfContent} output. */
    BODY,
    /** The generated epilogue — exports. */
    EPILOGUE,
    /** The whole served text — prologue + body + epilogue (e.g. a no-CDN-import check). */
    WHOLE
}
