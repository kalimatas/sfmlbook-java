package com.github.kalimatas.c04_Input;

/**
 * Entity/scene node category, used to dispatch commands
 */
public abstract class Category {
    public static final int NONE = 0;
    public static final int SCENE = 1 << 0;
    public static final int PLAYER_AIRCRAFT = 1 << 1;
    public static final int ALLIED_AIRCRAFT = 1 << 2;
    public static final int ENEMY_AIRCRAFT = 1 << 3;
}
