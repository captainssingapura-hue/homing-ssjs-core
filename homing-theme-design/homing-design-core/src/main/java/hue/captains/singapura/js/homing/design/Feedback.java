package hue.captains.singapura.js.homing.design;

/**
 * Feedback — what the system is telling the user about an outcome. The
 * branch a product most often extends: fin-dash adds {@code Up} and
 * {@code Down} here, and a design that fulfils the branch covers them.
 */
public interface Feedback extends Semantic {

    record Danger() implements Feedback {}

    record Warning() implements Feedback {}

    record Success() implements Feedback {}

    record Info() implements Feedback {}
}
