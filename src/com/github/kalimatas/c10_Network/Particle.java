package com.github.kalimatas.c10_Network;

import org.jsfml.graphics.Color;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

public class Particle {
    public enum Type {
        PROPELLANT,
        SMOKE,
        PARTICLE_COUNT,
    }

    public Vector2f position;
    public Color color;
    public Time lifetime;
}
