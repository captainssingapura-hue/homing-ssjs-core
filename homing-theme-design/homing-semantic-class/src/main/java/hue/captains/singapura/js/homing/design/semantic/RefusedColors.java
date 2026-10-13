package hue.captains.singapura.js.homing.design.semantic;

import java.util.List;

/** A colour tree the reader refused, with every problem it found at once. A refused tree resolves nothing. */
public final class RefusedColors extends RuntimeException {

    private final List<ColorProblem> problems;

    public RefusedColors(List<ColorProblem> problems) {
        super(problems.size() + (problems.size() == 1 ? " problem" : " problems") + " in the colour tree:\n  "
              + String.join("\n  ", problems.stream().map(ColorProblem::toString).toList()));
        this.problems = List.copyOf(problems);
    }

    public List<ColorProblem> problems() { return problems; }
}
