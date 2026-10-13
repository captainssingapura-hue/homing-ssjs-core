package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.Carrier;
import hue.captains.singapura.js.homing.design.Extent;
import hue.captains.singapura.js.homing.design.Growth;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * What an answer may say: a body that means the same under any class it is copied to, and keeps
 * colour the palette's. It holds declarations of its target's own properties, blocks nested on the
 * element itself ({@code &:hover}, {@code &::before}, {@code &[aria-pressed="true"]}) and blocks for
 * a mode ({@code @media}, {@code @supports}). It names no class, reaches into no child, and sets no
 * property its target does not own - a registered one included.
 *
 * <ul>
 *   <li><b>A semantic colour is said whole.</b> A colour target's body gives every physical colour
 *       of its set in every block it declares anything in, the top included: the set is never
 *       mixed from two answers, and nothing is left to a silent default.</li>
 *   <li><b>Colour is the palette's.</b> A body on any other target names no colour - no hex, no
 *       colour function, no colour's name; where its geometry is drawn in colour, it reads the
 *       registered colours of the leaf that colours it ({@link Target#colouredBy()}), and those
 *       alone.</li>
 *   <li><b>What a body reads</b> is the element's own: its degrees ({@code --extent},
 *       {@code --size}, {@code --aspect}) and those colours. Any other variable is refused.</li>
 * </ul>
 *
 * A target that is not CSS - a sound, an asset - is said nothing of here. {@link Target.Animation} is answered
 * with an effect, never a body: the effect's declarations are judged here as a body's are ({@link Effects}).
 */
public record AnswerBody() implements StatelessFunctionalObject {

    public static final AnswerBody INSTANCE = new AnswerBody();

    /** One selector of a nested block: the element itself, with pseudo-classes, pseudo-elements and attributes only. */
    private static final Pattern SELF = Pattern.compile("&((::?[a-zA-Z-]+(\\([^()]*\\))?)|(\\[[^\\]]*\\]))*");

    private static final Pattern VAR = Pattern.compile("var\\(\\s*(--[a-zA-Z0-9-]+)");
    private static final Pattern HEX = Pattern.compile("#[0-9a-fA-F]{3,8}\\b");
    private static final Pattern COLOUR_FUNCTION = Pattern.compile("\\b(rgba?|hsla?|hwb|lab|lch|oklab|oklch|color)\\(", Pattern.CASE_INSENSITIVE);
    private static final Pattern WORD = Pattern.compile("[a-zA-Z][a-zA-Z0-9-]*");
    private static final Pattern FRAME = Pattern.compile("from|to|\\d+(\\.\\d+)?%");

    /** The numbers an element sets on itself, which any body may read. */
    private static final Set<String> DEGREES = Set.of(Extent.VAR, Growth.SIZE.var(), Growth.ASPECT.var());

    /** CSS's named colours: a colour said by name is said all the same. */
    private static final Set<String> NAMED = Set.of(("aliceblue antiquewhite aqua aquamarine azure beige bisque black blanchedalmond blue "
            + "blueviolet brown burlywood cadetblue chartreuse chocolate coral cornflowerblue cornsilk crimson cyan darkblue darkcyan "
            + "darkgoldenrod darkgray darkgreen darkgrey darkkhaki darkmagenta darkolivegreen darkorange darkorchid darkred darksalmon "
            + "darkseagreen darkslateblue darkslategray darkslategrey darkturquoise darkviolet deeppink deepskyblue dimgray dimgrey "
            + "dodgerblue firebrick floralwhite forestgreen fuchsia gainsboro ghostwhite gold goldenrod gray green greenyellow grey "
            + "honeydew hotpink indianred indigo ivory khaki lavender lavenderblush lawngreen lemonchiffon lightblue lightcoral "
            + "lightcyan lightgoldenrodyellow lightgray lightgreen lightgrey lightpink lightsalmon lightseagreen lightskyblue "
            + "lightslategray lightslategrey lightsteelblue lightyellow lime limegreen linen magenta maroon mediumaquamarine "
            + "mediumblue mediumorchid mediumpurple mediumseagreen mediumslateblue mediumspringgreen mediumturquoise "
            + "mediumvioletred midnightblue mintcream mistyrose moccasin navajowhite navy oldlace olive olivedrab orange orangered "
            + "orchid palegoldenrod palegreen paleturquoise palevioletred papayawhip peachpuff peru pink plum powderblue purple "
            + "rebeccapurple red rosybrown royalblue saddlebrown salmon sandybrown seagreen seashell sienna silver skyblue "
            + "slateblue slategray slategrey snow springgreen steelblue tan teal thistle tomato turquoise violet wheat white "
            + "whitesmoke yellow yellowgreen").split(" "));

    /**
     * Who may say what: a name for the messages, the properties it may declare - null for any but an animation's
     * own - the colour targets whose registered colours it may read, and whether it is a semantic colour itself.
     */
    record Rights(String who, Set<String> owns, List<Target.Color> colouredBy, boolean colour) {
        static Rights of(Target t) { return new Rights(t.token(), t.properties(), t.colouredBy(), t instanceof Target.Color); }
    }

    /** Refuses a body its target cannot say; says nothing of an empty one. */
    public void check(Target target, String body) {
        if (body.isBlank()) return;
        if (target.carrier() != Carrier.CSS) throw new IllegalArgumentException(target.token() + " is no CSS target: an answer for it says nothing here");
        if (target instanceof Target.Animation)
            throw new IllegalArgumentException(target.token() + " is answered with an animation effect, never with a body: name the effect");
        Rights rights = Rights.of(target);
        Set<String> top = block(rights, stripComments(body), 0);
        if (rights.colour() && top.isEmpty())
            throw new IllegalArgumentException(target.token() + " is said in a block alone: a semantic colour is said at the top, a mode or a state beside it");
    }

    /** The properties flat declarations declare - no block in them - refused where the rights do not reach. */
    Set<String> declarations(Rights rights, String text) {
        String bare = stripComments(text);
        if (bare.indexOf('{') >= 0 || bare.indexOf('}') >= 0)
            throw new IllegalArgumentException(rights.who() + " declares in a block: its declarations are flat, the element's own");
        return block(rights, bare, 0);
    }

    /** The frames of keyframes - {@code from}, {@code to}, a percentage, or several - and every property they move. */
    Set<String> frames(Rights rights, String text) {
        String bare = stripComments(text).strip();
        var moved = new TreeSet<String>();
        int i = 0;
        while (i < bare.length()) {
            int open = bare.indexOf('{', i);
            if (open < 0) {
                if (!bare.substring(i).isBlank()) throw new IllegalArgumentException(rights.who() + "'s frames end in '" + bare.substring(i).strip() + "', which is no frame");
                break;
            }
            String selector = bare.substring(i, open).strip();
            for (String s : selector.split(","))
                if (!FRAME.matcher(s.strip()).matches())
                    throw new IllegalArgumentException(rights.who() + "'s frame '" + selector + "' is no frame: from, to or a percentage");
            int close = matching(bare, open, '{', '}');
            moved.addAll(declarations(rights, bare.substring(open + 1, close)));
            i = close + 1;
        }
        return moved;
    }

    /** The properties a block declares; its nested blocks checked as it goes. */
    private static Set<String> block(Rights target, String text, int depth) {
        var declared = new HashSet<String>();
        var buf = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            char ch = text.charAt(i);
            if (ch == '"' || ch == '\'') { int end = closing(text, i); buf.append(text, i, end + 1); i = end + 1; continue; }
            if (ch == '(') { int end = matching(text, i, '(', ')'); buf.append(text, i, end + 1); i = end + 1; continue; }
            if (ch == ';') { declare(target, buf.toString(), declared); buf.setLength(0); i++; continue; }
            if (ch == '{') {
                int end = matching(text, i, '{', '}');
                nested(target, buf.toString().strip(), text.substring(i + 1, end), depth);
                buf.setLength(0);
                i = end + 1;
                continue;
            }
            if (ch == '}') throw new IllegalArgumentException("a '}' closes nothing");
            buf.append(ch);
            i++;
        }
        if (!buf.toString().isBlank()) declare(target, buf.toString(), declared);
        if (target.colour() && !declared.isEmpty() && !declared.containsAll(target.owns())) {
            var missing = new TreeSet<>(target.owns());
            missing.removeAll(declared);
            throw new IllegalArgumentException(target.who() + " is a semantic colour, said whole or not at all: "
                    + String.join(", ", missing) + (missing.size() == 1 ? " is" : " are") + " not said");
        }
        return declared;
    }

    private static void nested(Rights target, String selector, String inner, int depth) {
        if (depth > 3) throw new IllegalArgumentException("nested too deep");
        if (selector.startsWith("@media") || selector.startsWith("@supports")) { block(target, inner, depth + 1); return; }
        for (String part : selector.split(",")) {
            String p = part.strip();
            if (!SELF.matcher(p).matches())
                throw new IllegalArgumentException("'" + selector + "' is not the element itself: a body nests only on & with its own states, pseudo-elements and attributes");
        }
        block(target, inner, depth + 1);
    }

    private static void declare(Rights target, String text, Set<String> declared) {
        String d = text.strip();
        if (d.isEmpty()) return;
        int colon = d.indexOf(':');
        if (colon <= 0) throw new IllegalArgumentException("'" + d + "' is no declaration");
        String property = d.substring(0, colon).strip().toLowerCase(Locale.ROOT);
        String value = d.substring(colon + 1);
        if (target.owns() == null) {
            if (property.equals("animation") || property.startsWith("animation-"))
                throw new IllegalArgumentException(target.who() + " declares " + property + ": its motion is the framework's to write, from its frames and its timing");
        } else if (!target.owns().contains(property))
            throw new IllegalArgumentException(target.who() + " does not own " + property + "; it owns " + String.join(", ", new TreeSet<>(target.owns())));
        reads(target, value);
        if (!target.colour()) namesNoColour(target, value);
        declared.add(property);
    }

    /** The variables a value reads: the element's degrees, and the colours of the leaf that colours this one. */
    private static void reads(Rights target, String value) {
        Matcher m = VAR.matcher(value);
        while (m.find()) {
            String name = m.group(1);
            if (DEGREES.contains(name) || (target.owns() != null && target.owns().contains(name))) continue;
            if (target.colouredBy().stream().anyMatch(c -> c.properties().contains(name))) continue;
            throw new IllegalArgumentException(target.who() + " reads " + name + ": it reads its degrees"
                    + (target.colouredBy().isEmpty() ? "" : " and " + String.join(", ", target.colouredBy().stream().map(Target::token).toList()) + "'s colours")
                    + ", and nothing else");
        }
    }

    /** A value off the colour targets names no colour: the palette's are read, never said. */
    private static void namesNoColour(Rights target, String value) {
        String bare = value.replaceAll("\"[^\"]*\"|'[^']*'", " ");
        String found = null;
        Matcher hex = HEX.matcher(bare);
        Matcher fn = COLOUR_FUNCTION.matcher(bare);
        if (hex.find()) found = hex.group();
        else if (fn.find()) found = fn.group() + "...)";
        else {
            Matcher w = WORD.matcher(bare);
            while (w.find()) if (NAMED.contains(w.group().toLowerCase(Locale.ROOT))) { found = w.group(); break; }
        }
        if (found != null)
            throw new IllegalArgumentException(target.who() + " names a colour, " + found + ": colour is the palette's"
                    + (target.colouredBy().isEmpty() ? ", and " + target.who() + " is drawn in none"
                       : "; it is drawn in " + String.join(", ", target.colouredBy().stream().map(Target::token).toList()) + "'s"));
    }

    private static int closing(String text, int open) {
        char q = text.charAt(open);
        for (int i = open + 1; i < text.length(); i++) {
            if (text.charAt(i) == '\\') { i++; continue; }
            if (text.charAt(i) == q) return i;
        }
        throw new IllegalArgumentException("a string is never closed");
    }

    private static int matching(String text, int open, char o, char c) {
        int depth = 0;
        for (int i = open; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == '"' || ch == '\'') { i = closing(text, i); continue; }
            if (ch == o) depth++;
            else if (ch == c && --depth == 0) return i;
        }
        throw new IllegalArgumentException("a '" + o + "' is never closed");
    }

    private static String stripComments(String text) { return text.replaceAll("(?s)/\\*.*?\\*/", " "); }
}
