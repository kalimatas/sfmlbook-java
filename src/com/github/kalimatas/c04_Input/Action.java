package com.github.kalimatas.c04_Input;

import org.jsfml.system.Time;

public interface Action {
    public void invoke(SceneNode node, Time dt);
}
