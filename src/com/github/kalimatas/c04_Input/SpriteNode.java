package com.github.kalimatas.c04_Input;

import org.jsfml.graphics.*;

public class SpriteNode extends SceneNode {
    private Sprite sprite = new Sprite();

    public SpriteNode(Texture texture) {
        this.sprite.setTexture(texture);
    }

    public SpriteNode(Texture texture, IntRect rect) {
        this.sprite.setTexture(texture);
        this.sprite.setTextureRect(rect);
    }

    protected void drawCurrent(RenderTarget target, RenderStates states) {
        target.draw(sprite, states);
    }
}
