package com.github.kalimatas.c07_Gameplay.DataTables;

import com.github.kalimatas.c07_Gameplay.Textures;
import org.jsfml.system.Time;

import java.util.LinkedList;

public class AircraftData {
    public int hitpoints;
    public float speed;
    public Textures texture;
    public Time fireInterval;
    public LinkedList<Direction> directions = new LinkedList<>();
}
