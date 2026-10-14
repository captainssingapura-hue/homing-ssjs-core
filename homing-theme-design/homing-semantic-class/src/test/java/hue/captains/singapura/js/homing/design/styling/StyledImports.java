package hue.captains.singapura.js.homing.design.styling;

import hue.captains.singapura.js.homing.design.semantic.ColorImports;
import hue.captains.singapura.js.homing.design.semantic.ColorImportsExtension;
import hue.captains.singapura.js.homing.design.semantic.MineColours.Flag;
import hue.captains.singapura.js.homing.design.semantic.ResolvingColours.Page;
import hue.captains.singapura.js.homing.design.semantic.StyleImports;
import hue.captains.singapura.js.homing.design.styling.StyledStyles.Plain;

import java.util.List;

/** The extension's imports, for the file it gives plain - control.extension.css - its badge's colours (D32). */
public record StyledImports() implements ColorImportsExtension {

    @Override public String module() { return "extension"; }

    @Override public List<StyleImports<?>> imports() {
        return List.of(new StyleImports<>(Plain.class, new ColorImports<>(StyledExtension.Badge.INSTANCE, new Flag(), new Page())));
    }
}
