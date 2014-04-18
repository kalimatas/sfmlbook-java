package com.github.kalimatas.c03_World;

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
