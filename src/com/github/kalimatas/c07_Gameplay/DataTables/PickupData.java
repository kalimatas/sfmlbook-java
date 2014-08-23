package com.github.kalimatas.c07_Gameplay.DataTables;

import com.github.kalimatas.c07_Gameplay.Aircraft;
import com.github.kalimatas.c07_Gameplay.Textures;

public class PickupData {
    public Action action;
    public Textures texture;

    public interface Action {
        public void invoke(Aircraft aircraft);
    }
}
