package hue.captains.singapura.js.homing.design.semantic;

import hue.captains.singapura.js.homing.design.color.ColorScheme;

/** The base these tests' colours are in: a scheme from scratch - a stateless singleton - in the tests' own jar beside its colours. */
public class BaseScheme implements ColorScheme {
    public static final BaseScheme INSTANCE = new BaseScheme();
    protected BaseScheme() {}
}
