package com.github.kalimatas.c10_Network;

import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

abstract public class Entity extends SceneNode {
    private Vector2f velocity = new Vector2f(0.f, 0.f);
    private int hitpoints;

    public Entity(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    public void setVelocity(Vector2f velocity) {
        this.velocity = velocity;
    }

    public void setVelocity(float vx, float vy) {
        this.velocity = new Vector2f(vx, vy);
    }

    public Vector2f getVelocity() {
        return velocity;
    }

    public void accelerate(Vector2f velocity) {
        this.velocity = Vector2f.add(this.velocity, velocity);
    }

    public void accelerate(float vx, float vy) {
        this.velocity = Vector2f.add(this.velocity, new Vector2f(vx, vy));
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public void setHitpoints(int points) {
        hitpoints = points;
    }

    public void repair(int points) {
        if (!(points > 0)) {
            throw new IllegalArgumentException();
        }

        hitpoints += points;
    }

    public void damage(int points) {
        if (!(points > 0)) {
            throw new IllegalArgumentException();
        }

        hitpoints -= points;
    }

    public void destroy() {
        hitpoints = 0;
    }

    public void remove() {
        destroy();
    }

    public boolean isDestroyed() {
        return hitpoints <= 0;
    }

    protected void updateCurrent(Time dt, CommandQueue commands) {
        move(Vector2f.mul(velocity, dt.asSeconds()));
    }
}
