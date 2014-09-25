package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.DataTables.DataTables;
import com.github.kalimatas.c10_Network.DataTables.PickupData;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;

import java.util.ArrayList;
import java.util.Random;

public class Pickup extends Entity {
    public enum Type {
        HEALTH_REFILL(0),
        MISSILE_REFILL(1),
        FIRE_SPREAD(2),
        FIRE_RATE(3),
        TYPE_COUNT(4);

        private int typeIndex;

        private Type(final int typeIndex) {
            this.typeIndex = typeIndex;
        }

        public static Type getRandom() {
            int randomTypeIndex = new Random().nextInt(TYPE_COUNT.ordinal());
            for (Type type : values()) {
                if (type.typeIndex == randomTypeIndex) {
                    return type;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    private Type type;
    private Sprite sprite;

    private static ArrayList<PickupData> Table = DataTables.initializePickupData();

    public Pickup(Type type, final ResourceHolder textures) {
        super(1);

        this.type = type;
        this.sprite = new Sprite(textures.getTexture(Table.get(type.ordinal()).texture), Table.get(type.ordinal()).textureRect);

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
