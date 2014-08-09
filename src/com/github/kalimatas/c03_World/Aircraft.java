package com.github.kalimatas.c03_World;

import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;

public class Aircraft extends Entity {
    public enum Type {
        EAGLE,
        RAPTOR
    }

    private Type type;
    private Sprite sprite;

    public Aircraft(Type type, final ResourceHolder textures) {
        this.type = type;
        this.sprite = new Sprite(textures.getTexture(toTextureID(type)));

        FloatRect bounds = this.sprite.getLocalBounds();
        this.sprite.setOrigin(bounds.width / 2.f, bounds.height / 2.f);
    }

    @Override
    public void drawCurrent(RenderTarget target, RenderStates states) {
        target.draw(sprite, states);
    }

    private static Textures toTextureID(Type type) {
        switch (type) {
            case EAGLE:
                return Textures.EAGLE;
            case RAPTOR:
                return Textures.RAPTOR;
        }
        return Textures.EAGLE;
    }
}
