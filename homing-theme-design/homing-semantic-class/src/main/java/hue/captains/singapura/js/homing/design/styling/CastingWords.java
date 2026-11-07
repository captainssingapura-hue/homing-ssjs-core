package hue.captains.singapura.js.homing.design.styling;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/** The words a casting's rule is read against ({@link CastingRule}, {@link CastingColours}). */
enum CastingWords {
    /** The pseudo-elements a casting's rule may draw, each by name (D29): parts of its own element the browser makes. */
    PSEUDO_ELEMENTS(List.of("::before", "::after", "::placeholder", "::marker", "::selection",
        "::backdrop", "::file-selector-button", "::picker(select)", "::picker-icon", "::checkmark",
        "::-webkit-slider-thumb", "::-webkit-slider-runnable-track", "::-moz-range-thumb", "::-moz-range-track",
        "::-moz-range-progress")),
    /** The reader's settings a casting's rule may answer by an {@code @media} nested in it (D29). */
    READER_SETTINGS(List.of("(prefers-reduced-motion: reduce)", "(prefers-reduced-transparency: reduce)",
        "(prefers-contrast: more)", "(prefers-contrast: less)", "(forced-colors: active)")),
    NAMED(Set.of(
        "aliceblue", "antiquewhite", "aqua", "aquamarine", "azure", "beige", "bisque", "black", "blanchedalmond", "blue",
        "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse", "chocolate", "coral", "cornflowerblue", "cornsilk",
        "crimson", "cyan", "darkblue", "darkcyan", "darkgoldenrod", "darkgray", "darkgreen", "darkgrey", "darkkhaki",
        "darkmagenta", "darkolivegreen", "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen",
        "darkslateblue", "darkslategray", "darkslategrey", "darkturquoise", "darkviolet", "deeppink", "deepskyblue",
        "dimgray", "dimgrey", "dodgerblue", "firebrick", "floralwhite", "forestgreen", "fuchsia", "gainsboro",
        "ghostwhite", "gold", "goldenrod", "gray", "green", "greenyellow", "grey", "honeydew", "hotpink", "indianred",
        "indigo", "ivory", "khaki", "lavender", "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral",
        "lightcyan", "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink", "lightsalmon",
        "lightseagreen", "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue", "lightyellow", "lime",
        "limegreen", "linen", "magenta", "maroon", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple",
        "mediumseagreen", "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue",
        "mintcream", "mistyrose", "moccasin", "navajowhite", "navy", "oldlace", "olive", "olivedrab", "orange",
        "orangered", "orchid", "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip", "peachpuff",
        "peru", "pink", "plum", "powderblue", "purple", "rebeccapurple", "red", "rosybrown", "royalblue", "saddlebrown",
        "salmon", "sandybrown", "seagreen", "seashell", "sienna", "silver", "skyblue", "slateblue", "slategray",
        "slategrey", "snow", "springgreen", "steelblue", "tan", "teal", "thistle", "tomato", "turquoise", "violet",
        "wheat", "white", "whitesmoke", "yellow", "yellowgreen")),
    SYSTEM(Set.of(
        "accentcolor", "accentcolortext", "activetext", "buttonborder", "buttonface", "buttontext", "canvas", "canvastext",
        "field", "fieldtext", "graytext", "highlight", "highlighttext", "linktext", "mark", "marktext", "selecteditem",
        "selecteditemtext", "visitedtext")),
    LOCAL(Set.of("--fixed-", "--it-", "--along-", "--current-"));

    private final Collection<String> all;

    CastingWords(Collection<String> all) { this.all = all; }

    /** Every word of the kind. */
    Collection<String> all() { return all; }
}
