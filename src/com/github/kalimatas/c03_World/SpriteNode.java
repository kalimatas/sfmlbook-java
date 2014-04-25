package com.github.kalimatas.c03_World;

import org.jsfml.graphics.*;

public class SpriteNode extends SceneNode {
    Sprite sprite = new Sprite();

    SpriteNode(Texture texture) {
        this.sprite.setTexture(texture);
    }

    SpriteNode(Texture texture, IntRect rect) {
        this.sprite.setTexture(texture);
        this.sprite.setTextureRect(rect);
    }

    protected void drawCurrent(RenderTarget target, RenderStates states) {
        target.draw(sprite, states);
    }
}
