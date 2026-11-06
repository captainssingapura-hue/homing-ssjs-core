package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/** A token's words, as a picker shows them: each capitalised, the hyphens kept - {@code neo-brutalism} gives {@code Neo-Brutalism}. */
record Labels() implements StatelessFunctionalObject {

    static final Labels INSTANCE = new Labels();

    String of(String token) {
        var sb = new StringBuilder(token.length());
        boolean start = true;
        for (char c : token.toCharArray()) {
            sb.append(start ? Character.toUpperCase(c) : c);
            start = c == '-';
        }
        return sb.toString();
    }
}
