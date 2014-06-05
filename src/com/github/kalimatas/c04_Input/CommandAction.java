package com.github.kalimatas.c04_Input;

import org.jsfml.system.Time;

public interface CommandAction {
    public void invoke(SceneNode node, Time dt);
}
