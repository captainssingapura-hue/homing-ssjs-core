package hue.captains.singapura.js.homing.conformance.rules;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RFC 0044 Phase 7 — ported from {@code ManagerInjectionConformanceTest}: a
 * module must not redeclare an identifier it imports under an alias — chiefly
 * the framework's auto-injected managers ({@code css}, {@code href}, and each
 * {@code ManagerInjector} bind). If the body also declares {@code var/let/const
 * <bind> = …}, the concatenated module fails to parse with "Identifier
 * '&lt;bind&gt;' has already been declared" at first navigation — a browser-only
 * failure that ships green from CI.
 *
 * <p>Working on the <b>complete served text</b> makes this self-contained: the
 * aliased bindings are read directly from the served {@code import { X as bind }}
 * lines (injected prologue and authored imports alike), rather than re-deriving
 * the framework's injection logic. Any {@code const/let/var} redeclaration of one
 * of those binds is flagged.</p>
 */
public record NoManagerRedeclarationRule() implements JsRule {

    public static final NoManagerRedeclarationRule INSTANCE = new NoManagerRedeclarationRule();

    /** Captures the local bind of an aliased import/re-export: {@code … as <bind>}. */
    private static final Pattern ALIAS = Pattern.compile("\\bas\\s+([A-Za-z_$][\\w$]*)");

    @Override public RuleId      id()     { return new RuleId("no-manager-redeclaration"); }
    @Override public String      intent() { return "A module must not redeclare an aliased import (e.g. an auto-injected css/href manager) — it is a load-time SyntaxError."; }
    @Override public DoctrineRef basis()  { return new DoctrineRef("manager-injection"); }

    @Override
    public List<Finding> check(ServedModule module) {
        List<String> raw = module.lines();
        List<String> stripped = JsText.stripComments(raw);

        Set<String> binds = new LinkedHashSet<>();
        Matcher alias = ALIAS.matcher(String.join("\n", stripped));
        while (alias.find()) binds.add(alias.group(1));
        if (binds.isEmpty()) return List.of();

        List<Pattern> redeclarations = new ArrayList<>(binds.size());
        for (String bind : binds) {
            redeclarations.add(Pattern.compile(
                    "\\b(?:var|let|const)\\s+" + Pattern.quote(bind) + "(?![\\w$])\\s*="));
        }

        var findings = new ArrayList<Finding>();
        for (int i = 0; i < stripped.size(); i++) {
            for (Pattern p : redeclarations) {
                if (p.matcher(stripped.get(i)).find()) {
                    findings.add(new Finding(module.moduleClass(), id(),
                            "redeclares an imported binding (load-time SyntaxError): " + raw.get(i).trim(), i));
                    break;
                }
            }
        }
        return List.copyOf(findings);
    }
}
