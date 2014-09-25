package com.github.kalimatas.c10_Network.DataTables;

import com.github.kalimatas.c10_Network.Aircraft;
import com.github.kalimatas.c10_Network.Textures;
import org.jsfml.graphics.IntRect;

public class PickupData {
    public Action action;
    public Textures texture;
    public IntRect textureRect;

    public interface Action {
        public void invoke(Aircraft aircraft);
    }
}
