package com.github.kalimatas.c09_Audio.DataTables;

import com.github.kalimatas.c09_Audio.Aircraft;
import com.github.kalimatas.c09_Audio.Textures;
import org.jsfml.graphics.IntRect;

public class PickupData {
    public Action action;
    public Textures texture;
    public IntRect textureRect;

    public interface Action {
        public void invoke(Aircraft aircraft);
    }
}
