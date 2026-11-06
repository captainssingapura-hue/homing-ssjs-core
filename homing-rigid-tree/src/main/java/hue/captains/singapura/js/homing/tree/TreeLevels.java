package hue.captains.singapura.js.homing.tree;

import hue.captains.singapura.tao.ontology.StatelessFunctionalObject;

/** The levels of the rigid tree, by depth ({@link TreeLevel}). */
public record TreeLevels() implements StatelessFunctionalObject {

    public static final TreeLevels INSTANCE = new TreeLevels();

    /**
     * The level singleton at the given depth. {@code d} must be in
     * {@code 0..18}; anything else throws (the rigid cap is not silently
     * clamped). This is the single place depth maps to a typed level.
     */
    public TreeLevel atDepth(int d) {
        return switch (d) {
            case 0  -> TreeLevel.L0.INSTANCE;   case 1  -> TreeLevel.L1.INSTANCE;   case 2  -> TreeLevel.L2.INSTANCE;
            case 3  -> TreeLevel.L3.INSTANCE;   case 4  -> TreeLevel.L4.INSTANCE;   case 5  -> TreeLevel.L5.INSTANCE;
            case 6  -> TreeLevel.L6.INSTANCE;   case 7  -> TreeLevel.L7.INSTANCE;   case 8  -> TreeLevel.L8.INSTANCE;
            case 9  -> TreeLevel.L9.INSTANCE;   case 10 -> TreeLevel.L10.INSTANCE;  case 11 -> TreeLevel.L11.INSTANCE;
            case 12 -> TreeLevel.L12.INSTANCE;  case 13 -> TreeLevel.L13.INSTANCE;  case 14 -> TreeLevel.L14.INSTANCE;
            case 15 -> TreeLevel.L15.INSTANCE;  case 16 -> TreeLevel.L16.INSTANCE;  case 17 -> TreeLevel.L17.INSTANCE;
            case 18 -> TreeLevel.L18.INSTANCE;
            default -> throw new IllegalArgumentException(
                    "TreeLevel depth " + d + " is outside the rigid range 0.." + TreeLevel.MAX_DEPTH);
        };
    }
}
