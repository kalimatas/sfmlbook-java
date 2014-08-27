package com.github.kalimatas.c08_Graphics.DataTables;

import com.github.kalimatas.c08_Graphics.Aircraft;
import com.github.kalimatas.c08_Graphics.Textures;

public class PickupData {
    public Action action;
    public Textures texture;

    public interface Action {
        public void invoke(Aircraft aircraft);
    }
}
