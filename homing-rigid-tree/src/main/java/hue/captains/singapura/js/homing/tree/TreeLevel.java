package hue.captains.singapura.js.homing.tree;

import java.util.Optional;

/**
 * Sealed sum of leveled types — borrowed from DomOpsParty's level pattern.
 * {@link TreeNode} carries a {@code TreeLevel} type parameter so the level a
 * node sits at is part of its type identity. Per-tree-kind adapter records
 * bind to specific levels; the compiler enforces that {@code children()}
 * returns nodes of the next level.
 *
 * <p>RFC 0040: the ladder runs {@code L0..L18} — capped at 18, following
 * DomOpsParty, as a deliberate rigid bound. The cap is load-bearing: a
 * {@link #shifted(int)} or {@link #atDepth(int)} that would fall outside
 * {@code 0..18} is a hard error (Make It Impossible, not a silent clamp).
 * A tree may start at any level; grafting a sub-tree re-levels it by a pure
 * shift over this sealed set.</p>
 *
 * <p>Names Are Types: {@code TreeLevel} is sealed (not a raw String / int);
 * Make It Impossible Not Forbidden: a level type permits exactly one valid
 * {@code children()} shape — the level below it.</p>
 *
 * @since homing-tree-views v1; capped at L18 in homing-rigid-tree (RFC 0040)
 */
public sealed interface TreeLevel
        permits TreeLevel.L0, TreeLevel.L1, TreeLevel.L2,  TreeLevel.L3,
                TreeLevel.L4, TreeLevel.L5, TreeLevel.L6,  TreeLevel.L7,
                TreeLevel.L8, TreeLevel.L9, TreeLevel.L10, TreeLevel.L11,
                TreeLevel.L12, TreeLevel.L13, TreeLevel.L14, TreeLevel.L15,
                TreeLevel.L16, TreeLevel.L17, TreeLevel.L18 {

    /** The deepest level the rigid tree admits — the cap. */
    int MAX_DEPTH = 18;

    /** Stable depth-from-root, zero-indexed (0..18). */
    int depth();

    /** Stable discriminator string for JSON serialisation ({@code "L0".."L18"}). */
    String tag();

    /** The level immediately below this one, or empty at the {@code L18} cap. */
    default Optional<TreeLevel> below() {
        return depth() < MAX_DEPTH ? Optional.of(atDepth(depth() + 1)) : Optional.empty();
    }

    /** The level immediately above this one, or empty at the {@code L0} root. */
    default Optional<TreeLevel> above() {
        return depth() > 0 ? Optional.of(atDepth(depth() - 1)) : Optional.empty();
    }

    /**
     * This level shifted {@code by} steps deeper (negative shifts shallower) —
     * the arithmetic graft relies on. The result must land in {@code 0..18};
     * a shift past the cap is a hard error (the rigid bound).
     */
    default TreeLevel shifted(int by) {
        return atDepth(depth() + by);
    }

    /**
     * The level singleton at the given depth. {@code d} must be in
     * {@code 0..18}; anything else throws (the rigid cap is not silently
     * clamped). This is the single place depth maps to a typed level.
     */
    static TreeLevel atDepth(int d) {
        return switch (d) {
            case 0  -> L0.INSTANCE;   case 1  -> L1.INSTANCE;   case 2  -> L2.INSTANCE;
            case 3  -> L3.INSTANCE;   case 4  -> L4.INSTANCE;   case 5  -> L5.INSTANCE;
            case 6  -> L6.INSTANCE;   case 7  -> L7.INSTANCE;   case 8  -> L8.INSTANCE;
            case 9  -> L9.INSTANCE;   case 10 -> L10.INSTANCE;  case 11 -> L11.INSTANCE;
            case 12 -> L12.INSTANCE;  case 13 -> L13.INSTANCE;  case 14 -> L14.INSTANCE;
            case 15 -> L15.INSTANCE;  case 16 -> L16.INSTANCE;  case 17 -> L17.INSTANCE;
            case 18 -> L18.INSTANCE;
            default -> throw new IllegalArgumentException(
                    "TreeLevel depth " + d + " is outside the rigid range 0.." + MAX_DEPTH);
        };
    }

    record L0()  implements TreeLevel { public static final L0  INSTANCE = new L0();  @Override public int depth() { return 0; }  @Override public String tag() { return "L0"; } }
    record L1()  implements TreeLevel { public static final L1  INSTANCE = new L1();  @Override public int depth() { return 1; }  @Override public String tag() { return "L1"; } }
    record L2()  implements TreeLevel { public static final L2  INSTANCE = new L2();  @Override public int depth() { return 2; }  @Override public String tag() { return "L2"; } }
    record L3()  implements TreeLevel { public static final L3  INSTANCE = new L3();  @Override public int depth() { return 3; }  @Override public String tag() { return "L3"; } }
    record L4()  implements TreeLevel { public static final L4  INSTANCE = new L4();  @Override public int depth() { return 4; }  @Override public String tag() { return "L4"; } }
    record L5()  implements TreeLevel { public static final L5  INSTANCE = new L5();  @Override public int depth() { return 5; }  @Override public String tag() { return "L5"; } }
    record L6()  implements TreeLevel { public static final L6  INSTANCE = new L6();  @Override public int depth() { return 6; }  @Override public String tag() { return "L6"; } }
    record L7()  implements TreeLevel { public static final L7  INSTANCE = new L7();  @Override public int depth() { return 7; }  @Override public String tag() { return "L7"; } }
    record L8()  implements TreeLevel { public static final L8  INSTANCE = new L8();  @Override public int depth() { return 8; }  @Override public String tag() { return "L8"; } }
    record L9()  implements TreeLevel { public static final L9  INSTANCE = new L9();  @Override public int depth() { return 9; }  @Override public String tag() { return "L9"; } }
    record L10() implements TreeLevel { public static final L10 INSTANCE = new L10(); @Override public int depth() { return 10; } @Override public String tag() { return "L10"; } }
    record L11() implements TreeLevel { public static final L11 INSTANCE = new L11(); @Override public int depth() { return 11; } @Override public String tag() { return "L11"; } }
    record L12() implements TreeLevel { public static final L12 INSTANCE = new L12(); @Override public int depth() { return 12; } @Override public String tag() { return "L12"; } }
    record L13() implements TreeLevel { public static final L13 INSTANCE = new L13(); @Override public int depth() { return 13; } @Override public String tag() { return "L13"; } }
    record L14() implements TreeLevel { public static final L14 INSTANCE = new L14(); @Override public int depth() { return 14; } @Override public String tag() { return "L14"; } }
    record L15() implements TreeLevel { public static final L15 INSTANCE = new L15(); @Override public int depth() { return 15; } @Override public String tag() { return "L15"; } }
    record L16() implements TreeLevel { public static final L16 INSTANCE = new L16(); @Override public int depth() { return 16; } @Override public String tag() { return "L16"; } }
    record L17() implements TreeLevel { public static final L17 INSTANCE = new L17(); @Override public int depth() { return 17; } @Override public String tag() { return "L17"; } }
    record L18() implements TreeLevel { public static final L18 INSTANCE = new L18(); @Override public int depth() { return 18; } @Override public String tag() { return "L18"; } }
}
