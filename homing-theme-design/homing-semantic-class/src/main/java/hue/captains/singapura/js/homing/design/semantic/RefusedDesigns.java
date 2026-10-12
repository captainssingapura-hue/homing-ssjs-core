package hue.captains.singapura.js.homing.design.semantic;

import java.util.List;

/** Designs the reader refused, with every problem it found at once. A refused design is never flattened. */
public final class RefusedDesigns extends RuntimeException {

    private final List<DesignProblem> problems;

    public RefusedDesigns(List<DesignProblem> problems) {
        super(problems.size() + (problems.size() == 1 ? " problem" : " problems") + " in the designs:\n  "
              + String.join("\n  ", problems.stream().map(DesignProblem::toString).toList()));
        this.problems = List.copyOf(problems);
    }

    public List<DesignProblem> problems() { return problems; }
}
