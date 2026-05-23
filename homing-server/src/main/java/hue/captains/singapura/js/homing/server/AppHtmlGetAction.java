package hue.captains.singapura.js.homing.server;

import hue.captains.singapura.js.homing.core.AppModule;
import hue.captains.singapura.js.homing.core.ClickTarget;
import hue.captains.singapura.js.homing.core.Cue;
import hue.captains.singapura.js.homing.core.KeyCombo;
import hue.captains.singapura.js.homing.core.MembraneCue;
import hue.captains.singapura.js.homing.core.ModuleNameResolver;
import hue.captains.singapura.js.homing.core.NoiseCue;
import hue.captains.singapura.js.homing.core.NoteHit;
import hue.captains.singapura.js.homing.core.OscCue;
import hue.captains.singapura.js.homing.core.SimpleAppResolver;
import hue.captains.singapura.js.homing.core.Theme;
import hue.captains.singapura.js.homing.core.ThemeAudio;
import hue.captains.singapura.js.homing.core.ToneNotation;
import hue.captains.singapura.tao.http.action.GetAction;
import hue.captains.singapura.tao.http.action.ParamMarshaller;
import io.vertx.ext.web.RoutingContext;

import java.util.concurrent.CompletableFuture;

/**
 * GET {@code /app?app=<simpleName>}    — the RFC 0001 contract; resolves via {@link SimpleAppResolver}.
 * <br>
 * GET {@code /app?class=<canonical>}   — legacy fallback; uses reflection.
 *
 * <p>If a {@link SimpleAppResolver} is configured, {@code ?app=} is supported.
 * If not, only {@code ?class=} works (legacy mode for servers built before Step 07).</p>
 *
 * <p>Proxy-app names are explicitly rejected: a proxy is a URL builder, not a
 * navigable target. Requests for {@code ?app=&lt;proxy-name&gt;} return 404.</p>
 */
public class AppHtmlGetAction
        implements GetAction<RoutingContext, AppQuery, EmptyParam.NoHeaders, HtmlPageContent> {

    private final ModuleNameResolver nameResolver;
    private final SimpleAppResolver appResolver;   // may be null in legacy-only mode
    private final ThemeRegistry themeRegistry;     // RFC 0002-ext1 — for the theme picker widget
    private final AppMeta meta;                    // downstream-supplied brand label

    public AppHtmlGetAction(ModuleNameResolver nameResolver) {
        this(nameResolver, null, ThemeRegistry.EMPTY, AppMeta.DEFAULT);
    }

    public AppHtmlGetAction(ModuleNameResolver nameResolver, SimpleAppResolver appResolver) {
        this(nameResolver, appResolver, ThemeRegistry.EMPTY, AppMeta.DEFAULT);
    }

    public AppHtmlGetAction(ModuleNameResolver nameResolver, SimpleAppResolver appResolver, ThemeRegistry themeRegistry) {
        this(nameResolver, appResolver, themeRegistry, AppMeta.DEFAULT);
    }

    public AppHtmlGetAction(ModuleNameResolver nameResolver, SimpleAppResolver appResolver,
                             ThemeRegistry themeRegistry, AppMeta meta) {
        this.nameResolver = nameResolver;
        this.appResolver = appResolver;
        this.themeRegistry = themeRegistry != null ? themeRegistry : ThemeRegistry.EMPTY;
        this.meta = meta != null ? meta : AppMeta.DEFAULT;
    }

    @Override
    public ParamMarshaller._QueryString<RoutingContext, AppQuery> queryStrMarshaller() {
        return ctx -> new AppQuery(
                ctx.request().getParam("app"),
                ctx.request().getParam("class"),
                ctx.request().getParam("theme"),
                ctx.request().getParam("locale")
        );
    }

    @Override
    public ParamMarshaller._Header<RoutingContext, EmptyParam.NoHeaders> headerMarshaller() {
        return ctx -> new EmptyParam.NoHeaders();
    }

    @Override
    public CompletableFuture<HtmlPageContent> execute(AppQuery query, EmptyParam.NoHeaders headers) {
        if (!query.hasSimpleName() && !query.hasClassName()) {
            return CompletableFuture.failedFuture(ResourceNotFound.missingClass());
        }

        AppModule<?, ?> app;
        try {
            if (query.hasSimpleName()) {
                if (appResolver == null) {
                    return CompletableFuture.failedFuture(notFound(query.simpleName(),
                            "?app= dispatch requires a SimpleAppResolver — server was constructed without one"));
                }
                if (appResolver.resolveProxy(query.simpleName()) != null) {
                    return CompletableFuture.failedFuture(notFound(query.simpleName(),
                            "Proxy apps are not routable; they are URL builders for typed external links"));
                }
                app = appResolver.resolveApp(query.simpleName());
                if (app == null) {
                    return CompletableFuture.failedFuture(notFound(query.simpleName(),
                            "No app registered with this simple name"));
                }
            } else {
                // Legacy ?class= path — kept for backwards compatibility during Step 11 migration.
                Class<?> clazz = Class.forName(query.className());
                Object instance;
                try {
                    var instanceField = clazz.getField("INSTANCE");
                    instance = instanceField.get(null);
                } catch (NoSuchFieldException e) {
                    instance = clazz.getDeclaredConstructor().newInstance();
                }
                if (!(instance instanceof AppModule<?, ?> a)) {
                    return CompletableFuture.failedFuture(
                            ResourceNotFound.wrongType(query.className(), "AppModule"));
                }
                app = a;
            }
        } catch (Exception e) {
            String resource = query.hasSimpleName() ? query.simpleName() : query.className();
            return CompletableFuture.failedFuture(ResourceNotFound.forClass(resource, e));
        }

        // If the URL didn't carry ?theme=, fall back to the first theme in
        // the registry so downstream (theme-vars / theme-globals fetches,
        // module URLs, the picker's selected option) all see a concrete
        // slug instead of null. The URL itself is untouched — the default
        // is applied in-flight, transparently.
        String effectiveTheme = query.theme();
        if (effectiveTheme == null && !themeRegistry.themes().isEmpty()) {
            effectiveTheme = themeRegistry.themes().get(0).slug();
        }

        String baseModuleUrl = nameResolver.resolve(app).basePath();
        String themeJs  = effectiveTheme != null ? "\"" + effectiveTheme + "\"" : "null";
        String localeJs = query.locale() != null ? "\"" + query.locale() + "\"" : "null";
        String themePickerHtml = renderThemePicker(effectiveTheme);
        String backdropHtml    = renderBackdrop(effectiveTheme);
        String audioHtml       = renderAudioRuntime(effectiveTheme);

        // Backdrop-interactivity opt-out body class. When the active theme
        // installs page-wide pointer-events handling (Maple Bridge / Jazz
        // Drums / Retro 90s) but the app has opted out (workspace-style
        // apps that own the viewport), add a class the theme respects
        // to disable its universal pointer-events restriction for this
        // app's body. The visual backdrop stays — only event handling
        // is suppressed.
        String bodyClass = "";
        if (!app.acceptsBackdropInteractivity()) {
            final String themeSlug = effectiveTheme;
            Theme themeObj = themeRegistry.themes().stream()
                    .filter(t -> t.slug().equals(themeSlug))
                    .findFirst().orElse(null);
            if (themeObj != null && themeObj.backdropInteractivity()) {
                bodyClass = " class=\"homing-bg-passive\"";
            }
        }

        String html = """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <title>%s</title>
                    <!-- Inline SVG favicon — silences the browser's automatic
                         /favicon.ico request without a static asset round-trip.
                         Amber H matches the studio brand's accent. Upgraded to
                         the studio's brand logo (when registered) by the small
                         /brand-fetch script below. -->
                    <link rel="icon" id="__homing_favicon__" type="image/svg+xml" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 64 64'><text x='50%%' y='55%%' text-anchor='middle' dominant-baseline='central' font-family='Georgia,serif' font-weight='700' font-size='44' fill='%%23F4B942'>H</text></svg>">
                    <script>
                        // Upgrade the favicon to the studio's brand logo when
                        // /brand is registered AND carries a non-empty `logo`.
                        // Degrades silently — studios without StudioBrand wiring,
                        // or with no logo, keep the framework-default amber H.
                        fetch("/brand").then(function (r) { return r.ok ? r.json() : null; })
                            .then(function (brand) {
                                if (!brand || !brand.logo) return;
                                var link = document.getElementById("__homing_favicon__");
                                if (!link) return;
                                // btoa needs binary; unescape(encodeURIComponent(...))
                                // is the canonical UTF-8-safe encoding bridge.
                                link.href = "data:image/svg+xml;base64,"
                                          + btoa(unescape(encodeURIComponent(brand.logo)));
                            })
                            .catch(function () { /* fetch failed — keep the default */ });
                    </script>
                </head>
                <body%s>
                    %s
                    <div id="app"></div>
                    %s
                    %s
                    <script type="module">
                        // RFC 0002: theme is opt-in. If the URL didn't carry ?theme=, we
                        // forward nothing and the server resolves to its registered default.
                        // Browser prefers-color-scheme is intentionally ignored — themes are
                        // explicit, not auto-derived.
                        const theme = %s;
                        const locale = %s || navigator.language;
                        if (theme) document.documentElement.style.colorScheme = theme;
                        let moduleUrl = "%s" + "&locale=" + encodeURIComponent(locale);
                        if (theme) moduleUrl += "&theme=" + encodeURIComponent(theme);
                        const { appMain } = await import(moduleUrl);
                        appMain(document.getElementById("app"));
                    </script>
                </body>
                </html>
                """.formatted(htmlEscape(app.title() + " · " + meta.label()),
                              bodyClass,
                              backdropHtml, themePickerHtml, audioHtml, themeJs, localeJs, baseModuleUrl);

        return CompletableFuture.completedFuture(new HtmlPageContent(html));
    }

    /**
     * RFC 0002-ext1 — fixed-position theme switcher widget. Lists every theme
     * registered in the deployment's {@link ThemeRegistry}. On change, navigates
     * to the same URL with an updated {@code ?theme=<slug>} parameter. When the
     * registry has 0 or 1 themes, the widget renders nothing.
     */
    private String renderThemePicker(String currentTheme) {
        var themes = themeRegistry.themes();
        if (themes.size() < 2) return "";   // no point switching if there's only one

        StringBuilder options = new StringBuilder();
        for (var theme : themes) {
            String slug   = theme.slug();
            String label  = theme.label();
            String selected = slug.equals(currentTheme) ? " selected" : "";
            options.append("<option value=\"").append(htmlEscape(slug)).append("\"").append(selected)
                   .append(">").append(htmlEscape(label)).append("</option>");
        }

        // Renders into an invisible "slot" div the StudioElements Header picks
        // up at render time (see StudioElements.js — Header looks for
        // #__theme_picker_slot__ and reparents it into the sticky header bar).
        // Until the slot is reparented, `display:none` keeps it from flashing
        // as a stray element on first paint. Once reparented, an inline style
        // override resets `display` to flex.
        //
        // The picker uses the inverted-surface tokens so it visually merges
        // with the header's dark band — no hardcoded palette, theme-aware.
        return """
                <div id="__theme_picker_slot__" style="display:none; align-items:center; gap:6px; margin-left:auto; font:13px system-ui,sans-serif;">
                    <label style="color:var(--color-text-on-inverted-muted);">Theme:</label>
                    <select id="__theme_picker__" style="font:inherit; border:1px solid rgba(255,255,255,0.15); background:transparent; color:var(--color-text-on-inverted); cursor:pointer; padding:2px 6px; border-radius:4px;">
                        %s
                    </select>
                </div>
                <style>
                    /* OS-rendered popup list of <option>s falls back to the
                       theme's raised surface so it stays legible regardless
                       of the dark header band. */
                    #__theme_picker__ option {
                        background: var(--color-surface-raised);
                        color: var(--color-text-primary);
                    }
                </style>
                <script>
                    (function () {
                        var sel = document.getElementById('__theme_picker__');
                        if (!sel) return;
                        sel.addEventListener('change', function () {
                            var params = new URLSearchParams(window.location.search);
                            if (sel.value) params.set('theme', sel.value);
                            else params.delete('theme');
                            window.location.search = params.toString();
                        });
                    })();
                </script>
                """.formatted(options.toString());
    }

    /**
     * Per-theme atmospheric backdrop. When the active {@link hue.captains.singapura.js.homing.core.Theme}
     * declares a non-null {@link hue.captains.singapura.js.homing.core.Theme#backdrop()}, resolve the
     * referenced SVG markup and inline it as the first child of {@code <body>},
     * wrapped in {@code <div class="theme-backdrop">}.
     *
     * <p>Theme CSS positions the wrapper (typically fixed cover behind the
     * chrome). Because the SVG is real DOM (not a {@code background-image}
     * sandbox), its individual elements participate in the host document's
     * CSS cascade — themes can attach {@code :hover}, transitions, and
     * animation triggers to specific classed elements inside the SVG.</p>
     *
     * <p>Themes without a backdrop (the default for the framework's other six
     * themes) return null and this method emits the empty string.</p>
     */
    private String renderBackdrop(String currentTheme) {
        if (currentTheme == null) return "";
        var theme = themeRegistry.themes().stream()
                .filter(t -> currentTheme.equals(t.slug()))
                .findFirst().orElse(null);
        if (theme == null) return "";
        var ref = theme.backdrop();
        if (ref == null) return "";
        var svg = ref.resolve().orElse("");
        if (svg.isBlank()) return "";
        return "<div class=\"theme-backdrop\" aria-hidden=\"true\">" + svg + "</div>";
    }

    /**
     * RFC 0007 — Per-theme audio runtime. When the active theme declares
     * a non-null {@link hue.captains.singapura.js.homing.core.Theme#audio()},
     * inject:
     * <ol>
     *   <li>The typed cue bindings serialised inline as a JS object literal
     *       (keys are ClickTarget classTokens; values are typed cue records).</li>
     *   <li>A small runtime that lazy-imports Tone.js on first user gesture,
     *       attaches a single delegated click listener, and plays the bound
     *       cue when an element carrying a target's classToken is clicked.</li>
     *   <li>A mute toggle button next to the theme picker, persisted via
     *       {@code localStorage}.</li>
     * </ol>
     *
     * <p>Themes without audio emit the empty string — Tone.js stays out of
     * the page bundle entirely on those pages.</p>
     */
    private String renderAudioRuntime(String currentTheme) {
        if (currentTheme == null) return "";
        var theme = themeRegistry.themes().stream()
                .filter(t -> currentTheme.equals(t.slug()))
                .findFirst().orElse(null);
        if (theme == null) return "";
        ThemeAudio<?> audio = theme.audio();
        if (audio == null || audio.bindings().isEmpty()) return "";

        // Resolve the ToneJs module URL. The framework's name resolver
        // gives us the canonical path; we strip any leading query so
        // it's reusable as an import specifier.
        String toneClass = "hue.captains.singapura.js.homing.libs.ToneJs";
        String toneUrl = "/module?class=" + toneClass;

        // Emit cue bindings as a JS object literal — typed-record-to-JS-data
        // by exhaustive sealed switch. No string parsing on the runtime path.
        StringBuilder bindingsJs = new StringBuilder("{");
        boolean firstBinding = true;
        for (var entry : audio.bindings().entrySet()) {
            if (!firstBinding) bindingsJs.append(",");
            firstBinding = false;
            ClickTarget<?> target = entry.getKey();
            Cue cue = entry.getValue();
            bindingsJs.append("\n        ")
                      .append(jsQuote(target.classToken()))
                      .append(": ")
                      .append(emitCueJs(cue));
        }
        bindingsJs.append("\n    }");

        // Emit hover bindings — { "st-card": <cue>, ... }. Same shape as
        // AUDIO_BINDINGS but a different event triggers them (mouseover
        // entry instead of click). Themes opt in; default empty.
        StringBuilder hoverBindingsJs = new StringBuilder("{");
        boolean firstHover = true;
        for (var entry : audio.hoverBindings().entrySet()) {
            if (!firstHover) hoverBindingsJs.append(",");
            firstHover = false;
            ClickTarget<?> target = entry.getKey();
            Cue cue = entry.getValue();
            hoverBindingsJs.append("\n        ")
                           .append(jsQuote(target.classToken()))
                           .append(": ")
                           .append(emitCueJs(cue));
        }
        hoverBindingsJs.append("\n    }");

        // Emit key bindings — { "KeyA": "drum-kick", "KeyS": "drum-snare", ... }.
        // Mapping is event.code → classToken so the runtime can dispatch keydown
        // through the same playCue path as a click. RFC 0008 Phase 2.
        StringBuilder keyBindingsJs = new StringBuilder("{");
        StringBuilder keyLabelsJs   = new StringBuilder("{");
        boolean firstKey = true;
        // The compiler can't prove the wildcard ClickTarget<?> from
        // ThemeAudio<?>.keyBindings() shares its type parameter with the
        // class tokens used by bindings(), but at runtime they share the
        // same theme instance so the classToken keys line up. We just
        // emit each binding's eventCode + classToken + displayLabel.
        for (var e : audio.keyBindings().entrySet()) {
            KeyCombo key = e.getKey();
            ClickTarget<?> target = e.getValue();
            if (!firstKey) { keyBindingsJs.append(","); keyLabelsJs.append(","); }
            firstKey = false;
            keyBindingsJs.append("\n        ")
                         .append(jsQuote(key.eventCode())).append(": ")
                         .append(jsQuote(target.classToken()));
            keyLabelsJs.append("\n        ")
                       .append(jsQuote(key.eventCode())).append(": ")
                       .append("{ label: ").append(jsQuote(key.displayLabel()))
                       .append(", target: ").append(jsQuote(target.classToken())).append(" }");
        }
        keyBindingsJs.append("\n    }");
        keyLabelsJs.append("\n    }");
        String themeSlugJs = jsQuote(currentTheme);

        // Emit the shared vocal palette — 11 Tone.js note strings the
        // runtime cycles through when baking a cue with paletteMode=VOCAL.
        StringBuilder paletteJsBuilder = new StringBuilder("[");
        boolean firstPaletteNote = true;
        for (var note : hue.captains.singapura.js.homing.core.VocalPalette.NOTES) {
            if (!firstPaletteNote) paletteJsBuilder.append(", ");
            firstPaletteNote = false;
            paletteJsBuilder.append(jsQuote(ToneNotation.forNote(note)));
        }
        paletteJsBuilder.append("]");
        String paletteJs = paletteJsBuilder.toString();

        // Emit the chord palette — 6 chords, each a list of Tone.js note
        // strings. Cues with paletteMode=CHORD render one buffer per chord
        // (all notes simultaneously). Selection semantics match VOCAL:
        // hash on element identity for hover, random for click.
        StringBuilder chordPaletteJsBuilder = new StringBuilder("[");
        boolean firstChord = true;
        for (var chord : hue.captains.singapura.js.homing.core.ChordPalette.CHORDS) {
            if (!firstChord) chordPaletteJsBuilder.append(", ");
            firstChord = false;
            chordPaletteJsBuilder.append("[");
            boolean firstNoteInChord = true;
            for (var note : chord) {
                if (!firstNoteInChord) chordPaletteJsBuilder.append(", ");
                firstNoteInChord = false;
                chordPaletteJsBuilder.append(jsQuote(ToneNotation.forNote(note)));
            }
            chordPaletteJsBuilder.append("]");
        }
        chordPaletteJsBuilder.append("]");
        String chordPaletteJs = chordPaletteJsBuilder.toString();

        // RFC 0008 extension — chord progressions for auto-play guitar.
        // Themes opt in via ThemeAudio.progressions() + progressionVoice().
        // Both empty/null = no autoplay subsystem ships meaningful data;
        // the runtime guards itself with .length checks so the dead JS is
        // harmless overhead (~80 bytes).
        StringBuilder progressionsJsBuilder = new StringBuilder("[");
        boolean firstProgression = true;
        for (var p : audio.progressions()) {
            if (!firstProgression) progressionsJsBuilder.append(",");
            firstProgression = false;
            progressionsJsBuilder.append("\n        { name: ").append(jsQuote(p.name()))
                                 .append(", chordIndices: [");
            boolean firstIdx = true;
            for (int idx : p.chordIndices()) {
                if (!firstIdx) progressionsJsBuilder.append(",");
                firstIdx = false;
                progressionsJsBuilder.append(idx);
            }
            progressionsJsBuilder.append("], secondsPerChord: ").append(p.secondsPerChord())
                                 .append(", moodColor: ")
                                 .append(p.moodColor() == null ? "null" : jsQuote(p.moodColor()))
                                 .append(" }");
        }
        progressionsJsBuilder.append("\n    ]");
        String progressionsJs = progressionsJsBuilder.toString();

        // Progression voice — null when the theme doesn't opt in.
        Cue progressionVoice = audio.progressionVoice();
        String progressionVoiceJs = (progressionVoice == null) ? "null" : emitCueJs(progressionVoice);

        return """
                <script type="module">
                    // RFC 0007 (cues) + RFC 0008 Phase 2 (keyboard play + per-theme prefs)
                    // theme-audio runtime. Inlined to avoid a separate request for
                    // ~120 lines of JS. The cue + key bindings are emitted by
                    // AppHtmlGetAction from typed Java records.
                    const AUDIO_BINDINGS = __AUDIO_BINDINGS__;
                    const HOVER_BINDINGS = __HOVER_BINDINGS__;   // token → cue, triggered on mouseover entry
                    const KEY_BINDINGS   = __KEY_BINDINGS__;     // event.code → classToken
                    const KEY_LABELS     = __KEY_LABELS__;       // event.code → { label, target } for the panel
                    const VOCAL_PALETTE  = __VOCAL_PALETTE__;    // 11 Tone.js note strings — C3..B5
                    const CHORD_PALETTE  = __CHORD_PALETTE__;    // 6 chord arrays — diatonic C major
                    const PROGRESSIONS   = __PROGRESSIONS__;     // RFC 0008 ext — list of {name, chordIndices, secondsPerChord, moodColor}
                    const PROGRESSION_VOICE = __PROGRESSION_VOICE__; // null when theme didn't opt in
                    const TONE_URL       = "__TONE_URL__";
                    const THEME_SLUG     = __THEME_SLUG__;
                    // Session-only random salt — re-rolls each tab load. Used to
                    // map elements to palette pitches; same element + same salt
                    // always picks the same variant within a session.
                    const SESSION_SALT   = (Math.random() * 2147483647) | 0;
                    const DURATION_MAP   = { "1n":"1n","2n":"2n","4n":"4n","8n":"8n","16n":"16n","32n":"32n","64n":"64n" };

                    // Per-theme localStorage prefs (RFC 0008 §4.4). Each theme gets
                    // its own mute + play-mode state; switching themes preserves them
                    // independently. Older global `homing-audio-muted` is silently
                    // dropped — first-time-each-theme starts unmuted.
                    const PREF_MUTED     = "homing-theme:" + THEME_SLUG + ":muted";
                    const PREF_PLAY_MODE = "homing-theme:" + THEME_SLUG + ":play-mode";

                    let tonePromise = null;
                    let audioReady = false;
                    let clickBuffers = null;       // Map<classToken, AudioBuffer[]> — click cues (variants per token)
                    let hoverBuffers = null;       // Map<classToken, AudioBuffer[]> — hover cues
                    let audioContext = null;       // raw Web Audio context — borrowed from Tone
                    const inFlightUntil = new Map();
                    // Per-element hover debounce (RFC 0008 fast-sweep fix). Different
                    // cards never throttle each other; the same card gets a tiny
                    // safety window against cursor-jitter duplicate mouseover events.
                    const hoverInFlight = new WeakMap();

                    // Themes that use universal `body, body * { pointer-events: none }`
                    // plumbing (Maple Bridge for moon hover, Retro 90s for icon hover)
                    // block click events on every element not explicitly restored.
                    // Every audio-bound class token needs pointer-events: auto so
                    // clicks reach it. Restoring the parent isn't enough when the
                    // element is a <g> with unclassed children (e.g. <g class="mb-temple">
                    // contains 8 rects/paths/circles) — the click target is a child,
                    // which has its own `body *` pointer-events: none unless we
                    // explicitly restore descendants too. Inject one unlayered
                    // <style> covering both the element and its descendants —
                    // unlayered rules outrank any @layer at the same origin level.
                    const peStyle = document.createElement("style");
                    // Union of click + hover tokens — both kinds need pointer-events
                    // restored so the universal `body * { pointer-events: none }`
                    // plumbing (used by Maple Bridge / Retro 90s for backdrop
                    // hover) doesn't block them. Set semantics dedup naturally.
                    const peTokens = Array.from(new Set([
                        ...Object.keys(AUDIO_BINDINGS),
                        ...Object.keys(HOVER_BINDINGS)
                    ]));
                    const peRules = peTokens
                            .map(t => "body ." + t + ", body ." + t + " * { pointer-events: auto; cursor: pointer; }")
                            .join("\\n");
                    // Default visual play feedback — a subtle compress-and-bounce
                    // that reads as "this was struck." Themes override per-class
                    // for instrument-specific feels (drum compress, cymbal shimmer).
                    // transform: scale composes with the independent `scale:`
                    // property used by hover effects, so hover-while-playing
                    // multiplies cleanly.
                    const playedDefaults =
                        "@keyframes homing-audio-played { " +
                        "  0%   { transform: scale(1); } " +
                        "  30%  { transform: scale(0.94); } " +
                        "  60%  { transform: scale(1.03); } " +
                        "  100% { transform: scale(1); } " +
                        "} " +
                        // Only CLICK-bound tokens get the played animation;
                        // hover triggers skipPulse so this rule isn't relevant
                        // for hover-only bindings.
                        Object.keys(AUDIO_BINDINGS)
                            .map(t => "body ." + t + ".played { animation: homing-audio-played 220ms ease-out; transform-origin: center; transform-box: fill-box; }")
                            .join("\\n");
                    peStyle.textContent = peRules + "\\n" + playedDefaults;
                    document.head.appendChild(peStyle);

                    function muted()    { return localStorage.getItem(PREF_MUTED) === "1"; }
                    function playMode() { return localStorage.getItem(PREF_PLAY_MODE) === "1"; }

                    // RFC 0008 ext — auto-play guitar state (only relevant when
                    // PROGRESSIONS is non-empty AND PROGRESSION_VOICE is non-null).
                    const PREF_AUTOPLAY = "homing-theme:" + THEME_SLUG + ":autoplay";
                    const PREF_ROOT_KEY = "homing-theme:" + THEME_SLUG + ":root-key";
                    const DIATONIC_OFFSETS = [0, 2, 4, 5, 7, 9, 11];     // C D E F G A B
                    const DIATONIC_LABELS  = ["C", "D", "E", "F", "G", "A", "B"];
                    function autoplay()       { return localStorage.getItem(PREF_AUTOPLAY) === "1"; }
                    function setAutoplay(v)   { localStorage.setItem(PREF_AUTOPLAY, v ? "1" : "0"); }
                    function rootKey()        { return parseInt(localStorage.getItem(PREF_ROOT_KEY) || "0", 10); }
                    function setRootKey(v)    { localStorage.setItem(PREF_ROOT_KEY, String(v)); }
                    let progressionBuffers = null;     // AudioBuffer[] — one per CHORD_PALETTE entry
                    let autoplayTimer      = null;
                    let currentProgression = null;
                    let chordIndex         = 0;
                    let moodOverlay        = null;     // div, injected lazily

                    function ensureMoodOverlay() {
                        if (moodOverlay) return moodOverlay;
                        moodOverlay = document.createElement("div");
                        moodOverlay.id = "__theme_mood_overlay__";
                        // Behind chrome, above the theme backdrop. Fixed-position
                        // wash, low opacity — "hint not the main flavor."
                        moodOverlay.style.cssText =
                            "position:fixed;inset:0;pointer-events:none;" +
                            "background-color:transparent;opacity:0;" +
                            "transition:background-color 1.5s ease, opacity 1.5s ease;" +
                            "z-index:9999;mix-blend-mode:multiply;";
                        document.body.appendChild(moodOverlay);
                        return moodOverlay;
                    }
                    function applyMoodColor(color) {
                        const ov = ensureMoodOverlay();
                        if (color) {
                            ov.style.backgroundColor = color;
                            ov.style.opacity = "0.20";
                        } else {
                            ov.style.opacity = "0";
                        }
                    }

                    function setGuitarVisualState(state) {
                        const g = document.querySelector(".jd-auto-guitar");
                        if (!g) return;
                        g.classList.remove("autoplaying", "muted-autoplay");
                        if (state === "playing") g.classList.add("autoplaying");
                        if (state === "muted-playing") g.classList.add("autoplaying", "muted-autoplay");
                    }

                    function pickRandomProgression() {
                        return PROGRESSIONS[Math.floor(Math.random() * PROGRESSIONS.length)];
                    }

                    function playProgressionChord(chordIdx) {
                        if (muted() || !progressionBuffers || !audioContext) return;
                        const buffer = progressionBuffers[chordIdx];
                        if (!buffer) return;
                        const source = audioContext.createBufferSource();
                        source.buffer = buffer;
                        // Web Audio playbackRate = 2^(semitones/12) — cheap transposition.
                        const semitones = DIATONIC_OFFSETS[rootKey()] || 0;
                        source.playbackRate.value = Math.pow(2, semitones / 12);
                        source.connect(audioContext.destination);
                        source.start();
                    }

                    function scheduleNextChord() {
                        if (!autoplay()) return;       // toggled off mid-cycle
                        if (!currentProgression || chordIndex >= currentProgression.chordIndices.length) {
                            // Cycle complete (or first call) — pick new random
                            // progression and rotate the mood (D3: per-cycle).
                            currentProgression = pickRandomProgression();
                            chordIndex = 0;
                            applyMoodColor(currentProgression.moodColor);
                        }
                        playProgressionChord(currentProgression.chordIndices[chordIndex]);
                        // Even when muted, update visual state so the user sees
                        // the dimmed-playing glow on the guitar.
                        setGuitarVisualState(muted() ? "muted-playing" : "playing");
                        chordIndex++;
                        autoplayTimer = setTimeout(scheduleNextChord,
                                                   currentProgression.secondsPerChord * 1000);
                    }

                    async function startAutoplay() {
                        // Random root per toggle (D2). Persist so reload keeps it
                        // until the user manually slides.
                        setRootKey(Math.floor(Math.random() * DIATONIC_OFFSETS.length));
                        updateKeySliderUi();
                        await ensureAudio();
                        currentProgression = null;     // forces pickRandomProgression on first tick
                        chordIndex = 0;
                        scheduleNextChord();
                    }
                    function stopAutoplay() {
                        if (autoplayTimer) { clearTimeout(autoplayTimer); autoplayTimer = null; }
                        currentProgression = null;
                        applyMoodColor(null);
                        setGuitarVisualState("idle");
                    }
                    async function toggleAutoplay() {
                        const wasOn = autoplay();
                        setAutoplay(!wasOn);
                        if (wasOn) stopAutoplay();
                        else       await startAutoplay();
                    }

                    function updateKeySliderUi() {
                        const label  = document.getElementById("__key_slider_label__");
                        const slider = document.getElementById("__key_slider__");
                        if (label)  label.textContent = "Key: " + DIATONIC_LABELS[rootKey()];
                        if (slider) slider.value = String(rootKey());
                    }

                    // Approximate seconds per Tone.js duration string, at default 120 BPM.
                    // Used to size the offline-render duration per cue — we render long
                    // enough to capture each note's held portion + its release tail.
                    function durationSec(d) {
                        switch (d) {
                            case "1n":  return 2.0;
                            case "2n":  return 1.0;
                            case "4n":  return 0.5;
                            case "8n":  return 0.25;
                            case "16n": return 0.125;
                            case "32n": return 0.0625;
                            case "64n": return 0.03125;
                            default:    return 0.5;
                        }
                    }

                    // Generous offline-render duration: longest note's
                    // (offset + held + release) + a small tail buffer.
                    // For multi-partial cues (bells, chords), this covers the
                    // slowest-decaying voice.
                    function cueRenderDuration(cue) {
                        let maxEnd = 0;
                        const env = cue.env || { release: 0.5 };
                        for (const hit of cue.notes) {
                            const held = durationSec(DURATION_MAP[hit.duration] || hit.duration);
                            const end = (hit.offsetMs / 1000) + held + (env.release || 0.5) + 0.05;
                            if (end > maxEnd) maxEnd = end;
                        }
                        // CHORD palette renders as a 4-step arpeggio (0.18 s
                        // stride). Extend the buffer to fit all four attacks
                        // plus the last note's full release tail.
                        if (cue.paletteMode === "CHORD") {
                            const arpeggioSpan = 4 * 0.18;   // ≈ 0.72 s
                            maxEnd = maxEnd + arpeggioSpan;
                        }
                        return Math.max(maxEnd, 0.1);
                    }

                    // Build a Tone synth voice for the given cue inside the current
                    // active Tone context — used during Tone.Offline rendering, where
                    // every Tone constructor attaches to the offline destination.
                    // When cue.distortion > 0, route through a Tone.Distortion node
                    // before destination for the gritty electric-guitar feel.
                    function makeSynth(cue, tone) {
                        let synth;
                        switch (cue.kind) {
                            case "OSC":      synth = new tone.Synth({ oscillator: { type: cue.type }, envelope: cue.env, volume: cue.volumeDb }); break;
                            case "MEMBRANE": synth = new tone.MembraneSynth({ pitchDecay: cue.pitchDecay, octaves: cue.octaves, envelope: cue.env, volume: cue.volumeDb }); break;
                            case "NOISE":    synth = new tone.NoiseSynth({ envelope: cue.env, volume: cue.volumeDb }); break;
                        }
                        if (cue.distortion && cue.distortion > 0) {
                            // Tone.Distortion (waveshaper) feeding the offline destination.
                            // Both nodes auto-route into the active offline context's
                            // destination graph — same place .toDestination() would.
                            const dist = new tone.Distortion(cue.distortion).toDestination();
                            synth.connect(dist);
                        } else {
                            synth.toDestination();
                        }
                        return synth;
                    }

                    // Render ONE buffer for a cue at an optional palette entry.
                    //   paletteEntry === null:
                    //       Use cue.notes verbatim (single-buffer case).
                    //   paletteEntry is a string (Tone.js note):
                    //       Override every NoteHit's pitch with this single pitch
                    //       (VOCAL palette case — same envelope, different pitch).
                    //   paletteEntry is an array of strings (chord):
                    //       Play all chord pitches simultaneously, using the
                    //       cue's first NoteHit as the duration template
                    //       (CHORD palette case — full chord per buffer).
                    async function bakeOneBuffer(cue, tone, paletteEntry) {
                        const dur = cueRenderDuration(cue);
                        const buf = await tone.Offline(() => {
                            if (Array.isArray(paletteEntry)) {
                                // CHORD — broken-chord arpeggio. Notes ascend
                                // one at a time with a small stagger, then we
                                // descend back through the middle note for an
                                // up-down feel (1-3-5-3 for a triad). Each note
                                // gets its own synth voice so they overlap
                                // musically as the envelope rings out.
                                const template = cue.notes[0];
                                const noteDuration = DURATION_MAP[template.duration] || template.duration;
                                const ARPEGGIO_STEP = 0.18;     // seconds between attacks
                                // Build the arpeggio order — for a triad
                                // [root, third, fifth] play [0, 1, 2, 1] so
                                // each chord lasts ~4 steps (~720 ms) and
                                // feels rhythmic rather than blocky.
                                const arpOrder = paletteEntry.length === 3
                                    ? [0, 1, 2, 1]
                                    : Array.from({ length: paletteEntry.length }, (_, i) => i);
                                for (let i = 0; i < arpOrder.length; i++) {
                                    const synth = makeSynth(cue, tone);
                                    synth.triggerAttackRelease(
                                        paletteEntry[arpOrder[i]],
                                        noteDuration,
                                        i * ARPEGGIO_STEP);
                                }
                                return;
                            }
                            for (const hit of cue.notes) {
                                const synth = makeSynth(cue, tone);
                                const startSec = hit.offsetMs / 1000;
                                if (cue.kind === "NOISE") {
                                    synth.triggerAttackRelease(DURATION_MAP[hit.duration] || hit.duration, startSec);
                                } else {
                                    const pitch = paletteEntry !== null ? paletteEntry : hit.note;
                                    synth.triggerAttackRelease(pitch, DURATION_MAP[hit.duration] || hit.duration, startSec);
                                }
                            }
                        }, dur);
                        return buf.get();
                    }

                    // Bake a cue to one OR MORE AudioBuffers. Branches on
                    // paletteMode: VOCAL → one buffer per pitch in VOCAL_PALETTE;
                    // CHORD → one buffer per chord in CHORD_PALETTE;
                    // NONE (or NOISE regardless) → single buffer at declared pitches.
                    // Returns an array — length 1 for single, 11 for VOCAL, 6 for CHORD.
                    async function bakeCue(cue, tone) {
                        if (cue.kind === "NOISE") {
                            return [await bakeOneBuffer(cue, tone, null)];
                        }
                        if (cue.paletteMode === "VOCAL") {
                            return Promise.all(
                                VOCAL_PALETTE.map(pitch => bakeOneBuffer(cue, tone, pitch))
                            );
                        }
                        if (cue.paletteMode === "CHORD") {
                            return Promise.all(
                                CHORD_PALETTE.map(chord => bakeOneBuffer(cue, tone, chord))
                            );
                        }
                        return [await bakeOneBuffer(cue, tone, null)];
                    }

                    // Trigger a pre-baked cue. Each trigger creates a fresh
                    // AudioBufferSourceNode — the natural one-shot polyphony
                    // primitive of Web Audio. Source nodes auto-detach after
                    // playback ends; no manual disposal needed. Rapid retriggers
                    // overlap cleanly (the buffer plays N times concurrently, each
                    // in its own node, summed at the destination).
                    // Hash an element's textContent + session salt to a stable
                    // index in [0, n). Used by hover triggers: same element +
                    // same salt → same palette pitch every time within a
                    // session; salt re-rolls per reload so the assignment
                    // refreshes between visits.
                    function hashIndex(el, n) {
                        if (n <= 1) return 0;
                        const text = (el.textContent || "").substring(0, 64);
                        let h = SESSION_SALT;
                        for (let i = 0; i < text.length; i++) {
                            h = ((h << 5) - h + text.charCodeAt(i)) | 0;
                        }
                        return Math.abs(h) % n;
                    }

                    function playCue(token, buffers, opts) {
                        const variants = buffers ? buffers.get(token) : null;
                        if (!variants || variants.length === 0 || !audioContext) return;
                        // Variant selection — three modes:
                        //   1. Single-variant cue → that one buffer.
                        //   2. Multi-variant cue + hover trigger → hash on element.
                        //   3. Multi-variant cue + click trigger → random (humanization).
                        let buffer;
                        if (variants.length === 1) {
                            buffer = variants[0];
                        } else if (opts && opts.hoverElement) {
                            buffer = variants[hashIndex(opts.hoverElement, variants.length)];
                        } else {
                            buffer = variants[Math.floor(Math.random() * variants.length)];
                        }
                        const source = audioContext.createBufferSource();
                        source.buffer = buffer;
                        source.connect(audioContext.destination);
                        source.start();
                        // Visual pulse for click/keyboard triggers; skip on hover
                        // because the existing CSS hover-grow already provides
                        // visual feedback — doubling them looks busy.
                        if (!opts || !opts.skipPulse) pulse(token);
                        // Tiny debounce — prevents accidental double-fire from
                        // a single click release event. Short enough to let fast
                        // drumming work (30 ms allows ~30 hits/second).
                        inFlightUntil.set(token, Date.now() + 30);
                    }

                    // Visual play feedback — add transient `.played` class to
                    // every element carrying this token, force reflow to restart
                    // the CSS animation, clear after 220 ms. WeakMap tracks the
                    // pending removal per element so rapid retriggers don't
                    // cancel each other mid-animation. RFC 0008 Phase 2.
                    const pulseTimers = new WeakMap();
                    function pulse(token) {
                        const els = document.querySelectorAll("." + CSS.escape(token));
                        els.forEach((el) => {
                            const prev = pulseTimers.get(el);
                            if (prev) clearTimeout(prev);
                            el.classList.remove("played");
                            void el.offsetWidth;  // force reflow to reset CSS animation
                            el.classList.add("played");
                            pulseTimers.set(el, setTimeout(() => {
                                el.classList.remove("played");
                                pulseTimers.delete(el);
                            }, 220));
                        });
                    }

                    async function bakeMap(bindings, tone) {
                        const out = new Map();
                        const tokens = Object.keys(bindings);
                        const buffers = await Promise.all(tokens.map(t => bakeCue(bindings[t], tone)));
                        tokens.forEach((t, i) => out.set(t, buffers[i]));
                        return out;
                    }

                    async function ensureAudio() {
                        if (!tonePromise) tonePromise = import(TONE_URL);
                        const tone = await tonePromise;
                        if (!audioReady) {
                            await tone.start();
                            audioReady = true;
                            // Borrow the raw AudioContext for direct AudioBufferSourceNode
                            // creation on trigger. Tone manages its own context lifecycle;
                            // we just read from it.
                            audioContext = tone.getContext().rawContext;
                            // Bake every click + hover cue in parallel. Each Tone.Offline
                            // call renders its own short audio buffer. With ~5–15 cues per
                            // theme and ~50–500 ms render time each, the total bake
                            // completes in under a second on modest hardware.
                            const [c, h] = await Promise.all([
                                bakeMap(AUDIO_BINDINGS, tone),
                                bakeMap(HOVER_BINDINGS, tone)
                            ]);
                            clickBuffers = c;
                            hoverBuffers = h;
                            // RFC 0008 ext — bake chord progression buffers if the
                            // theme opted in. Returns AudioBuffer[] (one per chord
                            // in CHORD_PALETTE) — bakeCue handles paletteMode=CHORD
                            // expansion.
                            if (PROGRESSION_VOICE) {
                                progressionBuffers = await bakeCue(PROGRESSION_VOICE, tone);
                            }
                        }
                        return tone;
                    }

                    // Combined selector — one CSS string matching any audio-bound class.
                    // Element.closest() walks up the DOM tree matching this selector,
                    // which is more robust than a manual parentNode loop (handles
                    // SVG element boundaries, document fragments, etc., uniformly).
                    const AUDIO_SELECTOR = Object.keys(AUDIO_BINDINGS).map(t => "." + t).join(",");

                    document.body.addEventListener("click", async (e) => {
                        if (!e.target || !e.target.closest) return;
                        // RFC 0008 ext — guitar toggle. The guitar element itself
                        // is the toggle; mute does NOT gate the state change (the
                        // user can configure autoplay while muted, and the
                        // scheduler honours mute by skipping chord playback).
                        const guitar = e.target.closest(".jd-auto-guitar");
                        if (guitar && PROGRESSIONS.length > 0 && PROGRESSION_VOICE) {
                            await toggleAutoplay();
                            return;
                        }
                        if (muted()) return;
                        const matched = e.target.closest(AUDIO_SELECTOR);
                        if (!matched) return;
                        // Find which binding's class is on the matched element. With a
                        // combined selector, multiple bindings could theoretically match
                        // (one element with multiple bound classes); first match wins.
                        let token = null;
                        for (const t of Object.keys(AUDIO_BINDINGS)) {
                            if (matched.classList.contains(t)) { token = t; break; }
                        }
                        if (!token) return;
                        const now = Date.now();
                        if ((inFlightUntil.get(token) || 0) > now) return;
                        // ensureAudio() bakes every cue on first call; subsequent
                        // calls short-circuit. playCue() reads from clickBuffers.
                        await ensureAudio();
                        playCue(token, clickBuffers);
                    });

                    // RFC 0008 Phase 2 — keyboard play. Only fires when play mode
                    // is explicitly enabled (per-theme toggle, persisted to
                    // localStorage). Modifier-key presses (Ctrl/Cmd/Alt + letter)
                    // pass through so browser shortcuts and assistive tech keep
                    // working. Tab / Escape / arrows / function keys are never
                    // bindable — see KeyCombo.java exclusion list.
                    document.addEventListener("keydown", async (e) => {
                        if (!playMode() || muted()) return;
                        if (e.ctrlKey || e.metaKey || e.altKey) return;
                        if (e.repeat) return;  // key-held = single trigger, not auto-repeat
                        const token = KEY_BINDINGS[e.code];
                        if (!token) return;
                        e.preventDefault();
                        const now = Date.now();
                        if ((inFlightUntil.get(token) || 0) > now) return;
                        await ensureAudio();
                        playCue(token, clickBuffers);
                    });

                    // Hover cues — RFC 0008 hover extension. mouseover bubbles
                    // (mouseenter doesn't, so delegation requires mouseover).
                    // The relatedTarget contained-by-current-target check filters
                    // movement WITHIN an already-entered element down to a single
                    // "entered" event.
                    //
                    // Throttling is PER-ELEMENT, not global: different cards never
                    // throttle each other, so fast cursor sweeps fire a sound on
                    // each card crossed. The same card has an 80 ms debounce to
                    // suppress duplicate mouseover events caused by browser
                    // cursor-jitter or subpixel re-entries.
                    const HOVER_SELECTOR = Object.keys(HOVER_BINDINGS).map(t => "." + t).join(",");
                    if (HOVER_SELECTOR.length > 0) {
                        document.body.addEventListener("mouseover", async (e) => {
                            if (muted()) return;
                            if (!e.target || !e.target.closest) return;
                            const matched = e.target.closest(HOVER_SELECTOR);
                            if (!matched) return;
                            // Filter "moved within element" — the relatedTarget is
                            // where the cursor came from. If it's already inside
                            // the matched element, this is in-bounds movement, not
                            // a fresh entry.
                            const related = e.relatedTarget;
                            if (related && matched.contains(related)) return;
                            // Per-element debounce — different cards always fire;
                            // same card needs an 80 ms gap to re-fire.
                            const now = Date.now();
                            if ((hoverInFlight.get(matched) || 0) > now) return;
                            hoverInFlight.set(matched, now + 80);
                            let token = null;
                            for (const t of Object.keys(HOVER_BINDINGS)) {
                                if (matched.classList.contains(t)) { token = t; break; }
                            }
                            if (!token) return;
                            await ensureAudio();
                            // skipPulse — the existing CSS hover-grow already
                            // provides visual feedback; adding the .played pulse
                            // would compete with it.
                            // hoverElement — passed so variants are selected via
                            // hash on the specific card / list-item / TOC element,
                            // making the same element always play its own pitch
                            // within a session.
                            playCue(token, hoverBuffers, {
                                skipPulse: true,
                                hoverElement: matched
                            });
                        });
                    }

                    // Theme control panel — RFC 0008 Phase 2. Two buttons next
                    // to the theme picker: mute toggle (always present when a
                    // theme has audio), play-mode toggle (only present when the
                    // theme declares keyBindings). The play button's title
                    // attribute shows the binding map as a tooltip — discoverable
                    // without a full popover UI.
                    function makeBtn(label, title) {
                        const b = document.createElement("button");
                        b.type = "button";
                        b.textContent = label;
                        b.title = title;
                        b.style.cssText = "font:inherit;border:1px solid rgba(255,255,255,0.15);background:transparent;color:var(--color-text-on-inverted);cursor:pointer;padding:2px 8px;border-radius:4px;margin-left:6px;transition:background 120ms ease;";
                        return b;
                    }

                    function buildKeyTooltip() {
                        let lines = ["Play mode — keyboard plays sounds.", "Press the key to fire its cue."];
                        for (const code of Object.keys(KEY_LABELS)) {
                            const k = KEY_LABELS[code];
                            lines.push("  " + k.label + " → " + k.target);
                        }
                        return lines.join("\\n");
                    }

                    function installControlPanel() {
                        const slot = document.getElementById("__theme_picker_slot__");
                        if (!slot) return;

                        // Mute toggle.
                        const muteBtn = makeBtn("🔊", "Toggle audio");
                        muteBtn.id = "__audio_toggle__";
                        muteBtn.setAttribute("aria-label", "Toggle audio");
                        const updateMute = () => { muteBtn.textContent = muted() ? "🔇" : "🔊"; };
                        updateMute();
                        muteBtn.addEventListener("click", (e) => {
                            e.stopPropagation();
                            localStorage.setItem(PREF_MUTED, muted() ? "0" : "1");
                            updateMute();
                        });
                        slot.appendChild(muteBtn);

                        // Play-mode toggle — only present when the theme declares
                        // keyBindings. When ON, keydowns fire bound cues.
                        if (Object.keys(KEY_BINDINGS).length > 0) {
                            const playBtn = makeBtn("▷", buildKeyTooltip());
                            playBtn.id = "__play_mode_toggle__";
                            playBtn.setAttribute("aria-label", "Toggle keyboard play mode");
                            const updatePlay = () => {
                                playBtn.textContent = playMode() ? "▶" : "▷";
                                playBtn.style.background = playMode() ? "rgba(255,230,80,0.25)" : "transparent";
                                playBtn.style.borderColor = playMode() ? "rgba(255,230,80,0.6)" : "rgba(255,255,255,0.15)";
                            };
                            updatePlay();
                            playBtn.addEventListener("click", (e) => {
                                e.stopPropagation();
                                localStorage.setItem(PREF_PLAY_MODE, playMode() ? "0" : "1");
                                updatePlay();
                            });
                            slot.appendChild(playBtn);
                        }

                        // RFC 0008 ext — Key slider. Only present when the theme
                        // ships progressions (auto-play opt-in). 7 diatonic root
                        // positions; the runtime applies the offset to chord
                        // playback via AudioBufferSourceNode.playbackRate.
                        if (PROGRESSIONS.length > 0 && PROGRESSION_VOICE) {
                            const wrap = document.createElement("div");
                            wrap.style.cssText = "display:flex;align-items:center;gap:6px;margin-left:6px;color:var(--color-text-on-inverted);";
                            const label = document.createElement("span");
                            label.id = "__key_slider_label__";
                            label.style.cssText = "font:13px system-ui,sans-serif;min-width:48px;";
                            const slider = document.createElement("input");
                            slider.id = "__key_slider__";
                            slider.type = "range";
                            slider.min = "0";
                            slider.max = String(DIATONIC_OFFSETS.length - 1);
                            slider.step = "1";
                            slider.value = String(rootKey());
                            slider.style.cssText = "width:80px;cursor:pointer;";
                            slider.title = "Transpose auto-play chord progression";
                            slider.addEventListener("input", (e) => {
                                e.stopPropagation();
                                setRootKey(parseInt(slider.value, 10));
                                updateKeySliderUi();
                            });
                            wrap.appendChild(label);
                            wrap.appendChild(slider);
                            slot.appendChild(wrap);
                            updateKeySliderUi();
                        }

                        // Resume auto-play state from a prior session — the user
                        // toggled it on, then reloaded. Honour the persisted state
                        // but wait for a user gesture (click anywhere on the page)
                        // before actually starting audio — Web Audio policy.
                        if (autoplay() && PROGRESSIONS.length > 0 && PROGRESSION_VOICE) {
                            const resumeOnGesture = () => {
                                document.removeEventListener("click", resumeOnGesture, true);
                                if (autoplay()) startAutoplay();
                            };
                            document.addEventListener("click", resumeOnGesture, true);
                            setGuitarVisualState(muted() ? "muted-playing" : "playing");
                        }
                    }
                    if (document.readyState === "loading") {
                        document.addEventListener("DOMContentLoaded", installControlPanel);
                    } else {
                        installControlPanel();
                    }
                </script>
                """
                // Named-placeholder substitution. Plain String.replace() makes
                // `%` an ordinary character throughout the JS source — no more
                // format-specifier collisions with CSS keyframes (0%, 30%, …),
                // modulo operators (h % n), or any other `%` we might want to
                // emit. The placeholder tokens are `__NAME__` style so they
                // can't collide with anything in the typed cue JS, the Tone
                // module URL, or theme slugs. One pass per placeholder; later
                // replacements never re-scan earlier outputs.
                .replace("__AUDIO_BINDINGS__", bindingsJs.toString())
                .replace("__HOVER_BINDINGS__", hoverBindingsJs.toString())
                .replace("__KEY_BINDINGS__",   keyBindingsJs.toString())
                .replace("__KEY_LABELS__",     keyLabelsJs.toString())
                .replace("__VOCAL_PALETTE__",  paletteJs)
                .replace("__CHORD_PALETTE__",  chordPaletteJs)
                .replace("__PROGRESSIONS__",   progressionsJs)
                .replace("__PROGRESSION_VOICE__", progressionVoiceJs)
                .replace("__TONE_URL__",       toneUrl)
                .replace("__THEME_SLUG__",     themeSlugJs);
    }

    /** Emit one {@link Cue} as a JS object literal. Sealed-switch exhaustive
     *  over the three permits; field access is by record component, not by
     *  reflection-on-name. */
    private static String emitCueJs(Cue cue) {
        return switch (cue) {
            case OscCue o      -> emitOscCue(o);
            case MembraneCue m -> emitMembraneCue(m);
            case NoiseCue n    -> emitNoiseCue(n);
        };
    }

    private static String emitOscCue(OscCue c) {
        return "{ kind: \"OSC\", type: " + jsQuote(ToneNotation.forOsc(c.type())) +
                ", env: " + emitEnvelope(c.env()) +
                ", volumeDb: " + c.volumeDb() +
                ", paletteMode: " + jsQuote(c.paletteMode().name()) +
                ", distortion: " + c.distortion() +
                ", notes: " + emitNotes(c.notes()) + " }";
    }

    private static String emitMembraneCue(MembraneCue c) {
        return "{ kind: \"MEMBRANE\", pitchDecay: " + c.pitchDecay() +
                ", octaves: " + c.octaves() +
                ", env: " + emitEnvelope(c.env()) +
                ", volumeDb: " + c.volumeDb() +
                ", paletteMode: " + jsQuote(c.paletteMode().name()) +
                ", notes: " + emitNotes(c.notes()) + " }";
    }

    private static String emitNoiseCue(NoiseCue c) {
        // NoiseCue's paletteMode is structurally present but unused at
        // bake time — pitch shift on un-pitched noise is imperceptible.
        // Field omitted from JS for a cleaner cue object.
        return "{ kind: \"NOISE\", env: " + emitEnvelope(c.env()) +
                ", volumeDb: " + c.volumeDb() +
                ", notes: " + emitNotes(c.notes()) + " }";
    }

    private static String emitEnvelope(hue.captains.singapura.js.homing.core.Envelope e) {
        return "{ attack: " + e.attack() + ", decay: " + e.decay() +
               ", sustain: " + e.sustain() + ", release: " + e.release() + " }";
    }

    private static String emitNotes(java.util.List<NoteHit> notes) {
        StringBuilder sb = new StringBuilder("[");
        boolean first = true;
        for (var hit : notes) {
            if (!first) sb.append(", ");
            first = false;
            sb.append("{ note: ").append(jsQuote(ToneNotation.forNote(hit.note())))
              .append(", duration: ").append(jsQuote(ToneNotation.forDuration(hit.duration())))
              .append(", offsetMs: ").append(hit.offsetMs())
              .append(" }");
        }
        sb.append("]");
        return sb.toString();
    }

    /** JS string literal — backslash-escape quotes + backslashes. The runtime
     *  never parses these; they're emitted from typed-enum constants and
     *  ClickTarget records, so the content is always safe ASCII. */
    private static String jsQuote(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }

    private static String htmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static ResourceNotFound notFound(String resource, String reason) {
        return new ResourceNotFound(
                new ResourceNotFound._InternalError(null, reason + ": " + resource),
                new ResourceNotFound._ExternalError(resource, reason)
        );
    }
}
