package hue.captains.singapura.js.homing.site.demo.library;

import hue.captains.singapura.js.homing.site.catalogue.L1_Catalogue;
import hue.captains.singapura.js.homing.site.catalogue.Leaf;

import java.util.List;

/** A shelf with two books and nothing under it. */
public record FictionCatalogue() implements L1_Catalogue<LibraryCatalogue, FictionCatalogue> {

    public static final FictionCatalogue INSTANCE = new FictionCatalogue();

    static final BookPage MIDDLEMARCH = new BookPage("Middlemarch", "George Eliot", 1871,
            "A study of provincial life: a town, a marriage or two, and the slow work of finding out what one is for.");
    static final BookPage SOLARIS = new BookPage("Solaris", "Stanislaw Lem", 1961,
            "A station over an ocean that thinks, and the visitors it sends up from memory.");

    @Override public LibraryCatalogue parent() { return LibraryCatalogue.INSTANCE; }
    @Override public String name()    { return "Fiction"; }
    @Override public String summary() { return "Novels, on one shelf."; }
    @Override public String badge()   { return "SHELF"; }
    @Override public String icon()    { return "📖"; }

    @Override
    public List<Leaf<FictionCatalogue>> leaves() {
        return List.of(
                Leaf.of(this, MIDDLEMARCH.title(), MIDDLEMARCH.author() + ", " + MIDDLEMARCH.year(), MIDDLEMARCH).badge("BOOK"),
                Leaf.of(this, SOLARIS.title(),     SOLARIS.author()     + ", " + SOLARIS.year(),     SOLARIS).badge("BOOK"));
    }
}
