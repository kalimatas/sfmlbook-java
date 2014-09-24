package com.github.kalimatas.c09_Audio.DataTables;

import com.github.kalimatas.c09_Audio.*;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.IntRect;
import org.jsfml.system.Time;

import java.util.ArrayList;

public class DataTables {
    public static ArrayList<AircraftData> initializeAircraftData() {
        ArrayList<AircraftData> data = new ArrayList<>(Aircraft.Type.TYPE_COUNT.ordinal());

        AircraftData eagle = new AircraftData();
        eagle.hitpoints = 100;
        eagle.speed = 200.f;
        eagle.fireInterval = Time.getSeconds(1);
        eagle.texture = Textures.ENTITIES;
        eagle.textureRect = new IntRect(0, 0, 48, 64);
        eagle.hasRollAnimation = true;
        data.add(Aircraft.Type.EAGLE.ordinal(), eagle);

        AircraftData raptor = new AircraftData();
        raptor.hitpoints = 20;
        raptor.speed = 80.f;
        raptor.fireInterval = Time.ZERO;
        raptor.texture = Textures.ENTITIES;
        raptor.textureRect = new IntRect(144, 0, 84, 64);
        raptor.directions.addLast(new Direction(+45.f, 80.f));
        raptor.directions.addLast(new Direction(-45.f, 160.f));
        raptor.directions.addLast(new Direction(+45.f, 80.f));
        raptor.hasRollAnimation = false;
        data.add(Aircraft.Type.RAPTOR.ordinal(), raptor);

        AircraftData avenger = new AircraftData();
        avenger.hitpoints = 40;
        avenger.speed = 50.f;
        avenger.fireInterval = Time.getSeconds(2);
        avenger.texture = Textures.ENTITIES;
        avenger.textureRect = new IntRect(228, 0, 60, 59);
        avenger.directions.addLast(new Direction(+45.f, 50.f));
        avenger.directions.addLast(new Direction(0.f, 50.f));
        avenger.directions.addLast(new Direction(-45.f, 100.f));
        avenger.directions.addLast(new Direction(0.f, 50.f));
        avenger.directions.addLast(new Direction(+45.f, 50.f));
        avenger.hasRollAnimation = false;
        data.add(Aircraft.Type.AVENGER.ordinal(), avenger);

        return data;
    }

    public static ArrayList<ProjectileData> initializeProjectileData() {
        ArrayList<ProjectileData> data = new ArrayList<>(Projectile.Type.TYPE_COUNT.ordinal());

        ProjectileData alliedBullet = new ProjectileData();
        alliedBullet.damage = 10;
        alliedBullet.speed = 300.f;
        alliedBullet.texture = Textures.ENTITIES;
        alliedBullet.textureRect = new IntRect(175, 64, 3, 14);
        data.add(Projectile.Type.ALLIED_BULLET.ordinal(), alliedBullet);

        ProjectileData enemyBullet = new ProjectileData();
        enemyBullet.damage = 10;
        enemyBullet.speed = 300.f;
        enemyBullet.texture = Textures.ENTITIES;
        enemyBullet.textureRect = new IntRect(178, 64, 3, 14);
        data.add(Projectile.Type.ENEMY_BULLET.ordinal(), enemyBullet);

        ProjectileData missile = new ProjectileData();
        missile.damage = 200;
        missile.speed = 150.f;
        missile.texture = Textures.ENTITIES;
        missile.textureRect = new IntRect(160, 64, 15, 32);
        data.add(Projectile.Type.MISSILE.ordinal(), missile);

        return data;
    }

    public static ArrayList<PickupData> initializePickupData() {
        ArrayList<PickupData> data = new ArrayList<>(Pickup.Type.TYPE_COUNT.ordinal());

        PickupData healthRefill = new PickupData();
        healthRefill.texture = Textures.ENTITIES;
        healthRefill.textureRect = new IntRect(0, 64, 40, 40);
        healthRefill.action = new PickupData.Action() {
            @Override
            public void invoke(Aircraft aircraft) {
                aircraft.repair(25);
            }
        };
        data.add(Pickup.Type.HEALTH_REFILL.ordinal(), healthRefill);

        PickupData missileRefill = new PickupData();
        missileRefill.texture = Textures.ENTITIES;
        missileRefill.textureRect = new IntRect(40, 64, 40, 40);
        missileRefill.action = new PickupData.Action() {
            @Override
            public void invoke(Aircraft aircraft) {
                aircraft.collectMissiles(3);
            }
        };
        data.add(Pickup.Type.MISSILE_REFILL.ordinal(), missileRefill);

        PickupData fireSpread = new PickupData();
        fireSpread.texture = Textures.ENTITIES;
        fireSpread.textureRect = new IntRect(80, 64, 40, 40);
        fireSpread.action = new PickupData.Action() {
            @Override
            public void invoke(Aircraft aircraft) {
                aircraft.increaseSpread();
            }
        };
        data.add(Pickup.Type.FIRE_SPREAD.ordinal(), fireSpread);

        PickupData fireRate = new PickupData();
        fireRate.texture = Textures.ENTITIES;
        fireRate.textureRect = new IntRect(120, 64, 40, 40);
        fireRate.action = new PickupData.Action() {
            @Override
            public void invoke(Aircraft aircraft) {
                aircraft.increaseFireRate();
            }
        };
        data.add(Pickup.Type.FIRE_RATE.ordinal(), fireRate);

        return data;
    }

    public static ArrayList<ParticleData> initializeParticleData() {
        ArrayList<ParticleData> data = new ArrayList<>(Particle.Type.PARTICLE_COUNT.ordinal());

        ParticleData propellant = new ParticleData();
        propellant.color = new Color(255, 255, 50);
        propellant.lifetime = Time.getSeconds(0.6f);
        data.add(Particle.Type.PROPELLANT.ordinal(), propellant);

        ParticleData smoke = new ParticleData();
        smoke.color = new Color(50, 50, 50);
        smoke.lifetime = Time.getSeconds(4.0f);
        data.add(Particle.Type.SMOKE.ordinal(), smoke);

        return data;
    }
}
