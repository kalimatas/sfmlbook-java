package com.github.kalimatas.c06_Menus;

import org.jsfml.system.Time;

public interface CommandAction<T extends SceneNode> {
    public void invoke(T node, Time dt);
}
