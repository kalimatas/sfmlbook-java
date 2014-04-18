package com.github.kalimatas.c03_World;

import org.jsfml.system.Vector2f;

public class Entity {
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
}
