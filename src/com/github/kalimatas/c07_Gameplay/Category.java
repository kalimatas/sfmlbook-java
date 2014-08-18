package com.github.kalimatas.c07_Gameplay;

/**
 * Entity/scene node category, used to dispatch commands
 */
public abstract class Category {
    public static final int NONE = 0;
    public static final int SCENE_AIR_LAYER	= 1 << 0;
    public static final int PLAYER_AIRCRAFT = 1 << 1;
    public static final int ALLIED_AIRCRAFT = 1 << 2;
    public static final int ENEMY_AIRCRAFT = 1 << 3;
    public static final int PICKUP = 1 << 4;
    public static final int ALLIED_PROJECTILE = 1 << 5;
    public static final int ENEMY_PROJECTILE = 1 << 6;

    public static final int AIRCRAFT = PLAYER_AIRCRAFT | ALLIED_AIRCRAFT | ENEMY_AIRCRAFT;
    public static final int PROJECTILE = ALLIED_PROJECTILE | ENEMY_PROJECTILE;
}
