package com.github.kalimatas.c10_Network;

import org.jsfml.system.Time;

public interface CommandAction<T extends SceneNode> {
    public void invoke(T node, Time dt);
}
