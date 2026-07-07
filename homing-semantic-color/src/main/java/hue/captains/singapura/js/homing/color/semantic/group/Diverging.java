package hue.captains.singapura.js.homing.color.semantic.group;

import com.hominglabs.rebar.core.desc.Desc;
import com.hominglabs.rebar.core.level.L2;

/** Deviation about a midpoint — a bipolar ramp (deficit &lt;- neutral -&gt; surplus). */
public record Diverging() implements L2<Encoding>, ColorGroup {

    public static final Diverging INSTANCE = new Diverging();

    @Override public Encoding parent() { return Encoding.INSTANCE; }

    @Override public Desc desc() {
        return Descs.of("Deviation about a midpoint — a bipolar ramp.");
    }
}
