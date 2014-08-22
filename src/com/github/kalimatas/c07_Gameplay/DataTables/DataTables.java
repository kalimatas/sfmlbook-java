package com.github.kalimatas.c07_Gameplay.DataTables;

import com.github.kalimatas.c07_Gameplay.Aircraft;
import com.github.kalimatas.c07_Gameplay.Projectile;
import com.github.kalimatas.c07_Gameplay.Textures;
import org.jsfml.system.Time;

import java.util.ArrayList;

public class DataTables {
    public static ArrayList<AircraftData> initializeAircraftData() {
        ArrayList<AircraftData> data = new ArrayList<>(Aircraft.Type.TYPE_COUNT.ordinal());

        AircraftData eagle = new AircraftData();
        eagle.hitpoints = 100;
        eagle.speed = 200.f;
        eagle.fireInterval = Time.getSeconds(1);
        eagle.texture = Textures.EAGLE;
        data.add(Aircraft.Type.EAGLE.ordinal(), eagle);

        AircraftData raptor = new AircraftData();
        raptor.hitpoints = 20;
        raptor.speed = 80.f;
        raptor.fireInterval = Time.ZERO;
        raptor.texture = Textures.RAPTOR;
        raptor.directions.addLast(new Direction(+45.f, 80.f));
        raptor.directions.addLast(new Direction(-45.f, 160.f));
        raptor.directions.addLast(new Direction(+45.f, 80.f));
        data.add(Aircraft.Type.RAPTOR.ordinal(), raptor);

        AircraftData avenger = new AircraftData();
        avenger.hitpoints = 40;
        avenger.speed = 50.f;
        avenger.fireInterval = Time.getSeconds(2);
        avenger.texture = Textures.AVENGER;
        avenger.directions.addLast(new Direction(+45.f, 50.f));
        avenger.directions.addLast(new Direction(0.f, 50.f));
        avenger.directions.addLast(new Direction(-45.f, 100.f));
        avenger.directions.addLast(new Direction(0.f, 50.f));
        avenger.directions.addLast(new Direction(+45.f, 50.f));
        data.add(Aircraft.Type.AVENGER.ordinal(), avenger);

        return data;
    }

    public static ArrayList<ProjectileData> initializeProjectileData() {
        ArrayList<ProjectileData> data = new ArrayList<>(Projectile.Type.TYPE_COUNT.ordinal());

        ProjectileData alliedBullet = new ProjectileData();
        alliedBullet.damage = 10;
        alliedBullet.speed = 300.f;
        alliedBullet.texture = Textures.BULLET;
        data.add(Projectile.Type.ALLIED_BULLET.ordinal(), alliedBullet);

        ProjectileData enemyBullet = new ProjectileData();
        enemyBullet.damage = 10;
        enemyBullet.speed = 300.f;
        enemyBullet.texture = Textures.BULLET;
        data.add(Projectile.Type.ENEMY_BULLET.ordinal(), enemyBullet);

        ProjectileData missile = new ProjectileData();
        missile.damage = 200;
        missile.speed = 150.f;
        missile.texture = Textures.MISSILE;
        data.add(Projectile.Type.MISSILE.ordinal(), missile);

        return data;
    }
}
