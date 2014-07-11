package com.github.kalimatas.c06_Menus.GUI;

import org.jsfml.graphics.*;
import org.jsfml.window.event.Event;

import com.github.kalimatas.c06_Menus.ResourceHolder;
import com.github.kalimatas.c06_Menus.Utility;
import com.github.kalimatas.c06_Menus.Textures;
import com.github.kalimatas.c06_Menus.Fonts;

public class Button extends Component {
    private Callback callback;
    private Texture normalTexture;
    private Texture selectedTexture;
    private Texture pressedTexture;
    private Sprite sprite = new Sprite();
    private Text text;
    private boolean isToggle = false;

    public Button(final ResourceHolder fonts, final ResourceHolder textures) {
        normalTexture = textures.getTexture(Textures.BUTTON_NORMAL);
        selectedTexture = textures.getTexture(Textures.BUTTON_SELECTED);
        pressedTexture = textures.getTexture(Textures.BUTTON_PRESSED);

        sprite.setTexture(normalTexture);
        FloatRect bounds = sprite.getLocalBounds();

        text = new Text("", fonts.getFont(Fonts.MAIN), 16);
        text.setPosition(bounds.width / 2.f, bounds.height / 2.f);
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

        sprite.setTexture(selectedTexture);
    }

    @Override
    public void deselect() {
        super.deselect();

        sprite.setTexture(normalTexture);
    }

    @Override
    public void activate() {
        super.activate();

        // If we are toggle then we should show that the button is pressed and thus "toggled".
        if (isToggle) {
            sprite.setTexture(pressedTexture);
        }

        if (callback != null) {
            callback.invoke();
        }

        // If we are not a toggle then deactivate the button since we are just momentarily activated.
        if (!isToggle) {
            deactivate();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();

        if (isToggle) {
            // Reset texture to right one depending on if we are selected or not.
            if (isSelected()) {
                sprite.setTexture(selectedTexture);
            } else {
                sprite.setTexture(normalTexture);
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
}
