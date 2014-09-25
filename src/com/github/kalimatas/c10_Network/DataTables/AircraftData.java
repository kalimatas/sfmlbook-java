package com.github.kalimatas.c10_Network.DataTables;

import com.github.kalimatas.c10_Network.Textures;
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
