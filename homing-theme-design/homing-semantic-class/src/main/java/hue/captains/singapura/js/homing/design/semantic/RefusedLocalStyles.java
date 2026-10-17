package hue.captains.singapura.js.homing.design.semantic;

import java.util.List;

/** Local styles the reader refused, with every problem it found at once. A refused local style is never flattened. */
public final class RefusedLocalStyles extends RuntimeException {

    private final List<LocalStyleProblem> problems;

    public RefusedLocalStyles(List<LocalStyleProblem> problems) {
        super(problems.size() + (problems.size() == 1 ? " problem" : " problems") + " in the local styles:\n  "
              + String.join("\n  ", problems.stream().map(LocalStyleProblem::toString).toList()));
        this.problems = List.copyOf(problems);
    }

    public List<LocalStyleProblem> problems() { return problems; }
}
