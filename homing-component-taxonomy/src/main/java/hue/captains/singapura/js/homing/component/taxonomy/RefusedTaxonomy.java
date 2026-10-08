package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/** A taxonomy the reader refused, with every problem it found at once. */
public final class RefusedTaxonomy extends RuntimeException {

    private final List<TaxonomyProblem> problems;

    public RefusedTaxonomy(List<TaxonomyProblem> problems) {
        super(problems.size() + (problems.size() == 1 ? " problem" : " problems") + " in the taxonomy:\n  "
              + String.join("\n  ", problems.stream().map(TaxonomyProblem::toString).toList()));
        this.problems = List.copyOf(problems);
    }

    public List<TaxonomyProblem> problems() { return problems; }
}
