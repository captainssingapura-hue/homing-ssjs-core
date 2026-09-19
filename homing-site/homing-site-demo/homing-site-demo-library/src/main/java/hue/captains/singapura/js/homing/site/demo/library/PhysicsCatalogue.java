package hue.captains.singapura.js.homing.site.demo.library;

import hue.captains.singapura.js.homing.site.catalogue.L2_Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.Leaf;

import java.util.List;

/** The third level: a section of the science shelf with one book. */
public record PhysicsCatalogue() implements L2_Catalogue<ScienceCatalogue, PhysicsCatalogue> {

    public static final PhysicsCatalogue INSTANCE = new PhysicsCatalogue();

    static final BookPage FEYNMAN = new BookPage("QED: The Strange Theory of Light and Matter", "Richard Feynman", 1985,
            "Four lectures that explain what light does with nothing but arrows and honesty.");

    @Override public ScienceCatalogue parent() { return ScienceCatalogue.INSTANCE; }
    @Override public String name()    { return "Physics"; }
    @Override public String summary() { return "The deepest vertex in this tree."; }
    @Override public String badge()   { return "SECTION"; }
    @Override public String icon()    { return "⚛️"; }

    @Override
    public List<Leaf<PhysicsCatalogue>> leaves() {
        return List.of(Leaf.of(this, "QED", FEYNMAN.author() + ", " + FEYNMAN.year(), FEYNMAN).badge("BOOK"));
    }
}
