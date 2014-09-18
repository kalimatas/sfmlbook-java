package com.github.kalimatas.c08_Graphics;

import com.github.kalimatas.c08_Graphics.DataTables.DataTables;
import com.github.kalimatas.c08_Graphics.DataTables.ProjectileData;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

import java.util.ArrayList;

public class Projectile extends Entity {
    public enum Type {
        ALLIED_BULLET,
        ENEMY_BULLET,
        MISSILE,
        TYPE_COUNT,
    }

    private Type type;
    private Sprite sprite;
    private Vector2f targetDirection = new Vector2f(0.f, 0.f);

    private static ArrayList<ProjectileData> Table = DataTables.initializeProjectileData();

    public Projectile(Type type, ResourceHolder textures) {
        super(1);

        this.type = type;
        this.sprite = new Sprite(textures.getTexture(Table.get(type.ordinal()).texture), Table.get(type.ordinal()).textureRect);

        Utility.centerOrigin(this.sprite);

        // Add particle system for missiles
        if (isGuided()) {
            EmitterNode smoke = new EmitterNode(Particle.Type.SMOKE);
            smoke.setPosition(0.f, getBoundingRect().height / 2.f);
            attachChild(smoke);

            EmitterNode propellant = new EmitterNode(Particle.Type.PROPELLANT);
            propellant.setPosition(0.f, getBoundingRect().height / 2.f);
            attachChild(propellant);
        }
    }

    public void guideTowards(Vector2f position) {
        targetDirection = Utility.unitVector(Vector2f.sub(position, getWorldPosition()));
    }

    public boolean isGuided() {
        return type == Type.MISSILE;
    }

    @Override
    protected void updateCurrent(Time dt, CommandQueue commands) {
        if (isGuided()) {
            final float approachRate = 200.f;

            Vector2f newVelocity = Utility.unitVector(Vector2f.add(Vector2f.mul(targetDirection, approachRate * dt.asSeconds()), getVelocity()));
            newVelocity = Vector2f.mul(newVelocity, getMaxSpeed());
            float angle = (float) Math.atan2(newVelocity.y, newVelocity.x);

            setRotation(Utility.toDegree(angle) + 90.f);
            setVelocity(newVelocity);
        }

        super.updateCurrent(dt, commands);
    }

    @Override
    public void drawCurrent(RenderTarget target, RenderStates states) {
        target.draw(sprite, states);
    }

    public int getCategory() {
        if (type == Type.ENEMY_BULLET) {
            return Category.ENEMY_PROJECTILE;
        } else {
            return Category.ALLIED_PROJECTILE;
        }
    }

    public FloatRect getBoundingRect() {
        return getWorldTransform().transformRect(this.sprite.getGlobalBounds());
    }

    public float getMaxSpeed() {
        return Table.get(this.type.ordinal()).speed;
    }

    public int getDamage() {
        return Table.get(this.type.ordinal()).damage;
    }
}
