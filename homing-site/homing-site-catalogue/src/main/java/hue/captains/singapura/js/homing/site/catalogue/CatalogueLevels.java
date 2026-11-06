package hue.captains.singapura.js.homing.site.catalogue;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

import java.util.Optional;

/** Where a catalogue sits: its level, and the parent it declares. */
public record CatalogueLevels() implements StatelessFunctionalObject {

    public static final CatalogueLevels INSTANCE = new CatalogueLevels();

    /** The level its type declares, within its own tree: 0 for a root - a grafted one too - up to 8. */
    public int levelOf(Catalogue<?> c) {
        return switch (c) {
            case L0_Catalogue<?> ignored    -> 0;
            case L1_Catalogue<?, ?> ignored -> 1;
            case L2_Catalogue<?, ?> ignored -> 2;
            case L3_Catalogue<?, ?> ignored -> 3;
            case L4_Catalogue<?, ?> ignored -> 4;
            case L5_Catalogue<?, ?> ignored -> 5;
            case L6_Catalogue<?, ?> ignored -> 6;
            case L7_Catalogue<?, ?> ignored -> 7;
            case L8_Catalogue<?, ?> ignored -> 8;
        };
    }

    /** The parent a catalogue declares for itself; empty for a root. */
    public Optional<Catalogue<?>> declaredParentOf(Catalogue<?> c) {
        return switch (c) {
            case L0_Catalogue<?> ignored -> Optional.empty();
            case L1_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L2_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L3_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L4_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L5_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L6_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L7_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
            case L8_Catalogue<?, ?> l    -> Optional.ofNullable(l.parent());
        };
    }
}
