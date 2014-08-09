package com.github.kalimatas.c06_Menus;

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

        Utility.centerOrigin(this.sprite);
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

    public int getCategory() {
        switch (type) {
            case EAGLE:
                return Category.PLAYER_AIRCRAFT;
            default:
                return Category.ENEMY_AIRCRAFT;
        }
    }
}
