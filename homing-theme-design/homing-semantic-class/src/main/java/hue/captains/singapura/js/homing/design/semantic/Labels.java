package hue.captains.singapura.js.homing.design.semantic;

/** A token's words, as a picker shows them: each capitalised, the hyphens kept - {@code neo-brutalism} gives {@code Neo-Brutalism}. */
final class Labels {

    private Labels() {}

    static String of(String token) {
        var sb = new StringBuilder(token.length());
        boolean start = true;
        for (char c : token.toCharArray()) {
            sb.append(start ? Character.toUpperCase(c) : c);
            start = c == '-';
        }
        return sb.toString();
    }
}
