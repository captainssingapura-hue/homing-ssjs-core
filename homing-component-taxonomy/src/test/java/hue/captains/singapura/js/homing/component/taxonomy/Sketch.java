package hue.captains.singapura.js.homing.component.taxonomy;

import java.util.List;

/**
 * A small taxonomy to read: three kinds under the root, a button kind under one of them, and
 * the components the RFC's examples use - a plain button, a danger button and an alternating
 * one (its label played by a caption), a caption, and a dialog whose OK and Cancel are played by
 * plain buttons and whose title by a caption.
 */
final class Sketch {

    private Sketch() {}

    record Control() implements Kind<Root> {
        static final Control INSTANCE = new Control();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Container() implements Kind<Root> {
        static final Container INSTANCE = new Container();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Text() implements Kind<Root> {
        static final Text INSTANCE = new Text();
        @Override public Root parent() { return Root.INSTANCE; }
    }

    record Button() implements Kind<Control> {
        static final Button INSTANCE = new Button();
        @Override public Control parent() { return Control.INSTANCE; }
    }

    record Caption() implements Component<Text> {
        static final Caption INSTANCE = new Caption();
        @Override public Text parent() { return Text.INSTANCE; }
    }

    record PlainButton() implements Component<Button> {
        static final PlainButton INSTANCE = new PlainButton();
        @Override public Button parent() { return Button.INSTANCE; }
    }

    record DangerButton() implements Component<Button> {
        static final DangerButton INSTANCE = new DangerButton();
        @Override public Button parent() { return Button.INSTANCE; }
    }

    record AlternatingButton() implements Component<Button> {
        static final AlternatingButton INSTANCE = new AlternatingButton();
        @Override public Button parent() { return Button.INSTANCE; }
        @Override public List<Role<?>> roles() { return List.of(Label.INSTANCE); }

        record Label() implements Role<Caption> {
            static final Label INSTANCE = new Label();
            @Override public Caption base() { return Caption.INSTANCE; }
        }
    }

    record Dialog() implements Component<Container> {
        static final Dialog INSTANCE = new Dialog();
        @Override public Container parent() { return Container.INSTANCE; }
        @Override public List<Role<?>> roles() { return List.of(Ok.INSTANCE, Cancel.INSTANCE, Title.INSTANCE); }

        record Ok() implements Role<PlainButton> {
            static final Ok INSTANCE = new Ok();
            @Override public PlainButton base() { return PlainButton.INSTANCE; }
        }

        record Cancel() implements Role<PlainButton> {
            static final Cancel INSTANCE = new Cancel();
            @Override public PlainButton base() { return PlainButton.INSTANCE; }
        }

        record Title() implements Role<Caption> {
            static final Title INSTANCE = new Title();
            @Override public Caption base() { return Caption.INSTANCE; }
        }
    }
}
