package com.github.kalimatas.c07_Gameplay;

import com.github.kalimatas.c07_Gameplay.DataTables.AircraftData;
import com.github.kalimatas.c07_Gameplay.DataTables.DataTables;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;

import java.util.ArrayList;

public class Aircraft extends Entity {
    public enum Type {
        EAGLE,
        RAPTOR,
        AVENGER,
        TYPE_COUNT,
    }

    private Type type;
    private Sprite sprite;

    private static ArrayList<AircraftData> Table = DataTables.initializeAircraftData();

    public Aircraft(Type type, ResourceHolder textures) {
        super(Table.get(type.ordinal()).hitpoints);

        this.type = type;
        this.sprite = new Sprite(textures.getTexture(Table.get(type.ordinal()).texture));

        FloatRect bounds = this.sprite.getLocalBounds();
        this.sprite.setOrigin(bounds.width / 2.f, bounds.height / 2.f);
    }

    @Override
    public void drawCurrent(RenderTarget target, RenderStates states) {
        target.draw(sprite, states);
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
