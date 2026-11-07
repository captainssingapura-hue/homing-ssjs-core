package hue.captains.singapura.js.homing.core;

import hue.captains.singapura.js.homing.core.UrlTemplate.Segment;
import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.lang.reflect.Method;
import java.lang.reflect.RecordComponent;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Reads a URL template against its params record ({@link UrlTemplate}). */
public record UrlTemplateParser() implements StatelessFunctionalObject {

    public static final UrlTemplateParser INSTANCE = new UrlTemplateParser();

    /** The template's segments; refuses a malformed one, or one its params record does not answer. */
    List<Segment> segments(String template, Class<?> paramsType) {
        if (template == null) {
            throw new IllegalStateException("URL template is null");
        }

        Map<String, RecordComponent> components = describeParams(paramsType);

        List<Segment> segments = new ArrayList<>();
        StringBuilder literal = new StringBuilder();

        int i = 0;
        int n = template.length();
        while (i < n) {
            char c = template.charAt(i);
            if (c == '{') {
                int end = template.indexOf('}', i + 1);
                if (end < 0) {
                    throw new IllegalStateException(
                            "Unclosed '{' in template: " + template + " (at index " + i + ")");
                }
                if (literal.length() > 0) {
                    segments.add(new Segment.Literal(literal.toString()));
                    literal.setLength(0);
                }
                String inside = template.substring(i + 1, end);
                segments.add(parseSlot(inside, template, components));
                i = end + 1;
            } else if (c == '}') {
                throw new IllegalStateException(
                        "Unmatched '}' in template: " + template + " (at index " + i + ")");
            } else {
                literal.append(c);
                i++;
            }
        }
        if (literal.length() > 0) {
            segments.add(new Segment.Literal(literal.toString()));
        }

        return segments;
    }

    private Segment.Param parseSlot(
            String inside, String template, Map<String, RecordComponent> components) {

        if (inside.isEmpty()) {
            throw new IllegalStateException("Empty '{}' slot in template: " + template);
        }

        Slot slot = slotOf(inside, template);
        if (slot.name().isEmpty() || !isValidName(slot.name())) {
            throw new IllegalStateException(
                    "Bad param name '" + slot.name() + "' in template slot '{" + inside + "}': " + template);
        }

        RecordComponent comp = componentOf(slot, inside, template, components);
        return new Segment.Param(slot.name(), slot.optional(), slot.defaultValue(), comp.getAccessor());
    }

    /** What a slot says: name [? [: default]]. */
    private Slot slotOf(String inside, String template) {
        int q = inside.indexOf('?');
        if (q < 0) {
            return new Slot(inside, false, null);
        }
        String tail = inside.substring(q + 1);
        if (!tail.isEmpty() && !tail.startsWith(":")) {
            throw new IllegalStateException(
                    "Bad slot syntax '{" + inside + "}' in template: " + template
                  + " — after '?' expected ':default' or end");
        }
        return new Slot(inside.substring(0, q), true, tail.isEmpty() ? null : tail.substring(1));
    }

    /** The params component a slot names, as required or as optional as the slot says. */
    private RecordComponent componentOf(
            Slot slot, String inside, String template, Map<String, RecordComponent> components) {

        if (components.isEmpty()) {
            throw new IllegalStateException(
                    "Template '" + template + "' uses interpolation slot '{" + inside + "}' "
                  + "but paramsType is Void.class — declare a Params record to use slots");
        }

        String name = slot.name();
        RecordComponent comp = components.get(name);
        if (comp == null) {
            throw new IllegalStateException(
                    "Template slot '{" + inside + "}' references unknown param '" + name
                  + "'. Known params: " + components.keySet());
        }

        boolean componentIsOptional = comp.getType() == Optional.class;
        if (slot.optional() && !componentIsOptional) {
            throw new IllegalStateException(
                    "Template slot '{" + inside + "}' is optional ('?') but Params component '"
                  + name + "' is required (" + comp.getType().getSimpleName()
                  + "). Either drop the '?' or change the component to Optional.");
        }
        if (!slot.optional() && componentIsOptional) {
            throw new IllegalStateException(
                    "Template slot '{" + inside + "}' is required but Params component '"
                  + name + "' is Optional. Use '{" + name + "?}' for optional interpolation.");
        }
        return comp;
    }

    private boolean isValidName(String s) {
        if (s.isEmpty()) return false;
        if (!Character.isJavaIdentifierStart(s.charAt(0))) return false;
        for (int i = 1; i < s.length(); i++) {
            if (!Character.isJavaIdentifierPart(s.charAt(i))) return false;
        }
        return true;
    }

    private Map<String, RecordComponent> describeParams(Class<?> paramsType) {
        if (paramsType == null
                || paramsType == Void.class
                || paramsType == void.class
                || paramsType == AppModule._None.class) {
            return Map.of();
        }
        if (!paramsType.isRecord()) {
            throw new IllegalStateException(
                    "paramsType must be a record class, AppModule._None.class, or Void.class, got: "
                            + paramsType.getName());
        }
        Map<String, RecordComponent> out = new LinkedHashMap<>();
        for (var rc : paramsType.getRecordComponents()) {
            out.put(rc.getName(), rc);
        }
        return out;
    }

    /** A slot as written: its name, whether it is optional, and its default or null. */
    private record Slot(String name, boolean optional, String defaultValue) {}
}
