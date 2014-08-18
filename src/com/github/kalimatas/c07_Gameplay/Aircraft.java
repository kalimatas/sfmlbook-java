package com.github.kalimatas.c07_Gameplay;

import com.github.kalimatas.c07_Gameplay.DataTables.AircraftData;
import com.github.kalimatas.c07_Gameplay.DataTables.DataTables;
import com.github.kalimatas.c07_Gameplay.DataTables.Direction;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;
import org.jsfml.system.Time;

import java.util.ArrayList;
import java.util.LinkedList;

public class Aircraft extends Entity {
    public enum Type {
        EAGLE,
        RAPTOR,
        AVENGER,
        TYPE_COUNT,
    }

    private Type type;
    private Sprite sprite;

    private float travelledDistance = 0.f;
    private int directionIndex = 0;
    private TextNode healthDisplay;

    private static ArrayList<AircraftData> Table = DataTables.initializeAircraftData();

    public Aircraft(Type type, final ResourceHolder textures, final ResourceHolder fonts) {
        super(Table.get(type.ordinal()).hitpoints);

        this.type = type;
        this.sprite = new Sprite(textures.getTexture(Table.get(type.ordinal()).texture));

        Utility.centerOrigin(this.sprite);

        healthDisplay = new TextNode(fonts, "");
        attachChild(healthDisplay);

        updateTexts();
    }

    @Override
    public void drawCurrent(RenderTarget target, RenderStates states) {
        target.draw(sprite, states);
    }

    @Override
    protected void updateCurrent(Time dt, CommandQueue commands) {
        // Update enemy movement pattern; apply velocity
        updateMovementPattern(dt);
        super.updateCurrent(dt, commands);

        // Update texts
        updateTexts();
    }

    public int getCategory() {
        if (isAllied()) {
            return Category.PLAYER_AIRCRAFT;
        } else {
            return Category.ENEMY_AIRCRAFT;
        }
    }

    public boolean isAllied() {
        return type == Type.EAGLE;
    }

    public FloatRect getBoundingRect() {
        return getWorldTransform().transformRect(this.sprite.getGlobalBounds());
    }

    public float getMaxSpeed() {
        return Table.get(this.type.ordinal()).speed;
    }

    private void updateMovementPattern(Time dt) {
        // Enemy airplane: Movement pattern
        final LinkedList<Direction> directions = Table.get(this.type.ordinal()).directions;
        if (!directions.isEmpty()) {
            // Moved long enough in current direction: Change direction
            if (travelledDistance > directions.get(directionIndex).distance) {
                directionIndex = (directionIndex + 1) % directions.size();
                travelledDistance = 0.f;
            }

            // Compute velocity from direction
            float radians = Utility.toRadian(directions.get(directionIndex).angle + 90.f);
            float vx = getMaxSpeed() * (float) Math.cos((double)radians);
            float vy = getMaxSpeed() * (float) Math.sin((double)radians);

            setVelocity(vx, vy);

            travelledDistance += getMaxSpeed() * dt.asSeconds();
        }
    }

    private void updateTexts() {
        healthDisplay.setString(getHitpoints() + "HP");
        healthDisplay.setPosition(0.f, 50.f);
        healthDisplay.setRotation(-getRotation());
    }
}
