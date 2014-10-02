package com.github.kalimatas.c10_Network.Network;

import org.jsfml.system.Vector2f;

public class GameActions {
    public enum Type {
        ENEMY_EXPLODE,
    }

    public static class Action {
        public Type type;
        public Vector2f position;

        public Action() {
            // leave uninitialized
        }

        public Action(Type type, Vector2f position) {
            this.type = type;
            this.position = position;
        }
    }
}
