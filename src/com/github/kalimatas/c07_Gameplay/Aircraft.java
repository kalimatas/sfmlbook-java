package com.github.kalimatas.c07_Gameplay;

import com.github.kalimatas.c07_Gameplay.DataTables.AircraftData;
import com.github.kalimatas.c07_Gameplay.DataTables.DataTables;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;
import org.jsfml.system.Time;

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
        super.updateCurrent(dt, commands);

        // Update texts
        updateTexts();
    }

    public int getCategory() {
        switch (type) {
            case EAGLE:
                return Category.PLAYER_AIRCRAFT;
            default:
                return Category.ENEMY_AIRCRAFT;
        }
    }

    private void updateTexts() {
        healthDisplay.setString(getHitpoints() + "HP");
        healthDisplay.setPosition(0.f, 50.f);
        healthDisplay.setRotation(-getRotation());
    }
}
