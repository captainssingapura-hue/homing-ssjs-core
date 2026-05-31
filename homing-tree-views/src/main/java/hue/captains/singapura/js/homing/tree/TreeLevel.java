package hue.captains.singapura.js.homing.tree;

/**
 * Sealed sum of leveled types — borrowed from DomOpsParty's L1..L18
 * pattern. {@link TreeNode} carries a {@code TreeLevel} type parameter so
 * the level a node sits at is part of its type identity. Per-tree-kind
 * adapter records bind to specific levels; the compiler enforces that
 * {@code children()} returns nodes of the next level.
 *
 * <p>Eight levels are provided as a conservative upper bound for the
 * trees the framework has so far needed to express (catalogues seldom
 * exceed five; ComposedDoc embedding rarely exceeds four). The substrate
 * is flexible: a tree may start at any level, so a sub-tree extracted
 * from L3 onward simply re-roots at its starting level. The level
 * vocabulary is shared; the absolute number is not load-bearing.</p>
 *
 * <p>Names Are Types: {@code TreeLevel} is sealed (not a raw String /
 * int); Make It Impossible Not Forbidden: a level type permits exactly
 * one valid {@code children()} shape — the level below it.</p>
 *
 * @since homing-tree-views v1 — rigid tree substrate
 */
public sealed interface TreeLevel
        permits TreeLevel.L0, TreeLevel.L1, TreeLevel.L2, TreeLevel.L3,
                TreeLevel.L4, TreeLevel.L5, TreeLevel.L6, TreeLevel.L7,
                TreeLevel.L8 {

    /** Stable depth-from-root, zero-indexed. */
    int depth();

    /** Stable discriminator string for JSON serialisation. */
    String tag();

    /** Singleton — the level immediately below this one, or empty at L8. */
    java.util.Optional<TreeLevel> below();

    record L0() implements TreeLevel {
        public static final L0 INSTANCE = new L0();
        @Override public int depth() { return 0; }
        @Override public String tag() { return "L0"; }
        @Override public java.util.Optional<TreeLevel> below() { return java.util.Optional.of(L1.INSTANCE); }
    }
    record L1() implements TreeLevel {
        public static final L1 INSTANCE = new L1();
        @Override public int depth() { return 1; }
        @Override public String tag() { return "L1"; }
        @Override public java.util.Optional<TreeLevel> below() { return java.util.Optional.of(L2.INSTANCE); }
    }
    record L2() implements TreeLevel {
        public static final L2 INSTANCE = new L2();
        @Override public int depth() { return 2; }
        @Override public String tag() { return "L2"; }
        @Override public java.util.Optional<TreeLevel> below() { return java.util.Optional.of(L3.INSTANCE); }
    }
    record L3() implements TreeLevel {
        public static final L3 INSTANCE = new L3();
        @Override public int depth() { return 3; }
        @Override public String tag() { return "L3"; }
        @Override public java.util.Optional<TreeLevel> below() { return java.util.Optional.of(L4.INSTANCE); }
    }
    record L4() implements TreeLevel {
        public static final L4 INSTANCE = new L4();
        @Override public int depth() { return 4; }
        @Override public String tag() { return "L4"; }
        @Override public java.util.Optional<TreeLevel> below() { return java.util.Optional.of(L5.INSTANCE); }
    }
    record L5() implements TreeLevel {
        public static final L5 INSTANCE = new L5();
        @Override public int depth() { return 5; }
        @Override public String tag() { return "L5"; }
        @Override public java.util.Optional<TreeLevel> below() { return java.util.Optional.of(L6.INSTANCE); }
    }
    record L6() implements TreeLevel {
        public static final L6 INSTANCE = new L6();
        @Override public int depth() { return 6; }
        @Override public String tag() { return "L6"; }
        @Override public java.util.Optional<TreeLevel> below() { return java.util.Optional.of(L7.INSTANCE); }
    }
    record L7() implements TreeLevel {
        public static final L7 INSTANCE = new L7();
        @Override public int depth() { return 7; }
        @Override public String tag() { return "L7"; }
        @Override public java.util.Optional<TreeLevel> below() { return java.util.Optional.of(L8.INSTANCE); }
    }
    record L8() implements TreeLevel {
        public static final L8 INSTANCE = new L8();
        @Override public int depth() { return 8; }
        @Override public String tag() { return "L8"; }
        @Override public java.util.Optional<TreeLevel> below() { return java.util.Optional.empty(); }
    }
}
