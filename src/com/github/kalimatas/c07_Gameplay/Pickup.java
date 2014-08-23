package com.github.kalimatas.c07_Gameplay;

import com.github.kalimatas.c07_Gameplay.DataTables.DataTables;
import com.github.kalimatas.c07_Gameplay.DataTables.PickupData;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;

import java.util.ArrayList;

public class Pickup extends Entity {
    public enum Type {
        HEALTH_REFILL,
        MISSILE_REFILL,
        FIRE_SPREAD,
        FIRE_RATE,
        TYPE_COUNT,
    }

    private Type type;
    private Sprite sprite;

    private static ArrayList<PickupData> Table = DataTables.initializePickupData();

    public Pickup(Type type, final ResourceHolder textures) {
        super(1);

        this.type = type;
        this.sprite = new Sprite(textures.getTexture(Table.get(type.ordinal()).texture));

        Utility.centerOrigin(this.sprite);
    }

    public int getCategory() {
        return Category.PICKUP;
    }

    public FloatRect getBoundingRect() {
        return getWorldTransform().transformRect(this.sprite.getGlobalBounds());
    }

    public void apply(Aircraft player) {
        Table.get(type.ordinal()).action.invoke(player);
    }

    @Override
    public void drawCurrent(RenderTarget target, RenderStates states) {
        target.draw(sprite, states);
    }
}
