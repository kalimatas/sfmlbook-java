package com.github.kalimatas.c07_Gameplay.DataTables;

import com.github.kalimatas.c07_Gameplay.Textures;

import java.util.LinkedList;

public class AircraftData {
    public int hitpoints;
    public float speed;
    public Textures texture;
    public LinkedList<Direction> directions = new LinkedList<>();
}
