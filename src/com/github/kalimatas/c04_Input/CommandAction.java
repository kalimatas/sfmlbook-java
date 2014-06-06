package com.github.kalimatas.c04_Input;

import org.jsfml.system.Time;

public interface CommandAction<T extends SceneNode> {
    public void invoke(T node, Time dt);
}
