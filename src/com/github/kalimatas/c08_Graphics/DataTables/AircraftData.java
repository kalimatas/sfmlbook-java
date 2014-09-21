package com.github.kalimatas.c08_Graphics.DataTables;

import com.github.kalimatas.c08_Graphics.Textures;
import org.jsfml.graphics.IntRect;
import org.jsfml.system.Time;

import java.util.LinkedList;

public class AircraftData {
    public int hitpoints;
    public float speed;
    public Textures texture;
    public IntRect textureRect;
    public Time fireInterval;
    public LinkedList<Direction> directions = new LinkedList<>();
    public boolean hasRollAnimation;
}
