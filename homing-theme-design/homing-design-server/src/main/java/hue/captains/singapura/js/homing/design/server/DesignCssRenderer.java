package hue.captains.singapura.js.homing.design.server;

import hue.captains.singapura.js.homing.core.CssGroup;
import hue.captains.singapura.js.homing.design.Deployment;
import hue.captains.singapura.js.homing.design.Design;
import hue.captains.singapura.js.homing.design.DesignClass;
import hue.captains.singapura.js.homing.design.DesignExtension;
import hue.captains.singapura.js.homing.design.Sheets;
import hue.captains.singapura.js.homing.design.Target;
import hue.captains.singapura.js.homing.server.CssRenderer;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Renders a target group's sheet under a design: every pair the deployment's
 * components wear onto the target, resolved through the design and its
 * extensions, as the target's template filled with the design's variables
 * plus the {@code :root} bindings those variables read. A pair the design has
 * no word for is absent from the sheet — completeness is the deployment's
 * build-time check, not the renderer's; the renderer's job is to serve what
 * there is.
 *
 * <p>Owns exactly the {@link Target} groups. Everything else falls through to
 * the server's declared-body rendering.</p>
 */
public final class DesignCssRenderer implements CssRenderer {

    private final List<Design> designs;
    private final List<DesignExtension> extensions;
    private final Set<DesignClass<?>> worn;

    /** @param worn every pair the served components wear — the requirement set the sheets are cut to */
    public DesignCssRenderer(List<Design> designs, List<DesignExtension> extensions, Set<DesignClass<?>> worn) {
        this.designs = List.copyOf(designs);
        this.extensions = List.copyOf(extensions);
        this.worn = Set.copyOf(worn);
    }

    @Override public boolean owns(CssGroup<?> group) { return group instanceof Target; }

    @Override
    public Optional<String> render(CssGroup<?> group, String themeSlug) {
        if (!(group instanceof Target target)) return Optional.empty();
        Design design = designs.stream().filter(d -> d.slug().equals(themeSlug)).findFirst()
                .orElse(designs.isEmpty() ? null : designs.get(0));
        if (design == null) return Optional.of("/* no design registered */\n");

        Set<DesignClass<?>> required = new LinkedHashSet<>();
        for (DesignClass<?> p : worn) if (p.target() == target.getClass()) required.add(p);
        var resolution = new Deployment(required, design, extensions).resolve();

        var sb = new StringBuilder("/* ").append(target.token()).append(" under ").append(design.slug()).append(" */\n");
        sb.append(Sheets.rootSheet(resolution));
        String rules = Sheets.targetSheets(resolution).get(target.token());
        if (rules != null) sb.append(rules);
        // What did not resolve, for the reader of the sheet — never for the browser's behaviour.
        resolution.findings().stream()
                .filter(f -> f.kind() != Deployment.Finding.Kind.MISSING)
                .forEach(f -> sb.append("/* ").append(f).append(" */\n"));
        return Optional.of(sb.toString());
    }
}
