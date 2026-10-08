package hue.captains.singapura.js.homing.component.taxonomy;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * An implementation names its anchor as a type argument, and the anchor is read off the
 * declaration: a leaf for {@link ImplFor}, a branch for {@link ImplForBranch}, however far up its
 * supertypes it is given. One that names no class is refused when it is read.
 */
class ImplTest {

    record DangerButtonModule() implements ImplFor<Sketch.DangerButton> {}
    record ButtonModule() implements ImplForBranch<Sketch.Button> {}

    /** The argument given on an interface the module takes on, not on the module itself. */
    interface CardImpl extends ImplFor<Sketch.ProfileCard> {}
    record ProfileCardModule() implements CardImpl {}

    /** No class to read: the argument is a variable. */
    interface Generic<C extends Component<?>> extends ImplFor<C> {}
    record Unread() implements Generic<Sketch.PlainButton> {}

    /** What cannot be read is said by the declaration itself. */
    record Overridden() implements Generic<Sketch.PlainButton> {
        @Override public Sketch.PlainButton leaf() { return Sketch.PlainButton.INSTANCE; }
    }

    @Test
    void aLeaf_isReadOffTheDeclaration() {
        assertSame(Sketch.DangerButton.INSTANCE, new DangerButtonModule().leaf());
        assertSame(Sketch.DangerButton.INSTANCE, new DangerButtonModule().anchor());
    }

    @Test
    void aBranch_isReadOffTheDeclaration() {
        assertSame(Sketch.Button.INSTANCE, new ButtonModule().branch());
        assertSame(Sketch.Button.INSTANCE, new ButtonModule().anchor());
    }

    @Test
    void anArgumentGivenAbove_isFound() {
        assertSame(Sketch.ProfileCard.INSTANCE, new ProfileCardModule().leaf());
    }

    @Test
    void anArgumentNotAClass_isRefused_unlessTheDeclarationSaysIt() {
        var e = assertThrows(IllegalStateException.class, () -> new Unread().leaf());
        assertTrue(e.getMessage().contains("names no node to ImplFor"), e.getMessage());
        assertEquals(Sketch.PlainButton.INSTANCE, new Overridden().anchor());
    }
}
