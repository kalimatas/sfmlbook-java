package com.github.kalimatas.c09_Audio.GUI;

import com.github.kalimatas.c09_Audio.*;
import org.jsfml.graphics.*;
import org.jsfml.window.event.Event;

public class Button extends Component {
    public enum Type {
        NORMAL,
        SELECTED,
        PRESSED,
        BUTTON_COUNT,
    }

    private Callback callback;
    private Sprite sprite = new Sprite();
    private Text text;
    private boolean isToggle = false;
    private SoundPlayer sounds;

    public Button(State.Context context) {
        sprite.setTexture(context.textures.getTexture(Textures.BUTTONS));

        changeTexture(Type.NORMAL);

        FloatRect bounds = sprite.getLocalBounds();
        text = new Text("", context.fonts.getFont(Fonts.MAIN), 16);
        text.setPosition(bounds.width / 2.f, bounds.height / 2.f);

        sounds = context.sounds;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setText(final String text) {
        this.text.setString(text);
        Utility.centerOrigin(this.text);
    }

    public void setToggle(boolean flag) {
        isToggle = flag;
    }

    @Override
    public boolean isSelectable() {
        return true;
    }

    @Override
    public void select() {
        super.select();

        changeTexture(Type.SELECTED);
    }

    @Override
    public void deselect() {
        super.deselect();

        changeTexture(Type.NORMAL);
    }

    @Override
    public void activate() {
        super.activate();

        // If we are toggle then we should show that the button is pressed and thus "toggled".
        if (isToggle) {
            changeTexture(Type.PRESSED);
        }

        if (callback != null) {
            callback.invoke();
        }

        // If we are not a toggle then deactivate the button since we are just momentarily activated.
        if (!isToggle) {
            deactivate();
        }

        sounds.play(SoundEffects.BUTTON);
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (isToggle) {
            // Reset texture to right one depending on if we are selected or not.
            if (isSelected()) {
                changeTexture(Type.SELECTED);
            } else {
                changeTexture(Type.NORMAL);
            }
        }
    }

    @Override
    public void handleEvent(Event event) {

    }

    @Override
    public void draw(RenderTarget target, RenderStates states) {
        RenderStates rs = new RenderStates(states, Transform.combine(states.transform, getTransform()));
        target.draw(sprite, rs);
        target.draw(text, rs);
    }

    private void changeTexture(Type buttonType) {
        sprite.setTextureRect(new IntRect(0, 50 * buttonType.ordinal(), 200, 50));
    }
}
