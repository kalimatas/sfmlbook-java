package com.github.kalimatas.c03_World;

import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

abstract public class Entity extends SceneNode {
    private Vector2f velocity = new Vector2f(0.f, 0.f);

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public void setVelocity(float vx, float vy) {
        this.velocity = new Vector2f(vx, vy);
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    protected void updateCurrent(Time dt) {
        move(Vector2f.mul(velocity, dt.asSeconds()));
    }
}
