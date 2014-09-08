package com.github.kalimatas.c08_Graphics.DataTables;

import com.github.kalimatas.c08_Graphics.Aircraft;
import com.github.kalimatas.c08_Graphics.Textures;
import org.jsfml.graphics.IntRect;

public class PickupData {
    public Action action;
    public Textures texture;
    public IntRect textureRect;

    public interface Action {
        public void invoke(Aircraft aircraft);
    }
}
