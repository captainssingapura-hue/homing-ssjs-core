package hue.captains.singapura.js.homing.design.local;

import hue.captains.singapura.js.homing.design.color.ColorScheme;

/** The scheme these tests' local styles are in: a scheme from scratch, in the tests' own jar. */
public class BaseScheme implements ColorScheme {
    public static final BaseScheme INSTANCE = new BaseScheme();
    protected BaseScheme() {}
}
