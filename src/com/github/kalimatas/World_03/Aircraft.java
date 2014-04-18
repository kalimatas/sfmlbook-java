package com.github.kalimatas.World_03;

public class Aircraft extends Entity {
    public enum Type {
        EAGLE,
        RAPTOR
    }

    Type type;

    Aircraft(Type type) {
        this.type = type;
    }
}
