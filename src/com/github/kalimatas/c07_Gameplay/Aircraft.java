package com.github.kalimatas.c07_Gameplay;

import com.github.kalimatas.c07_Gameplay.DataTables.AircraftData;
import com.github.kalimatas.c07_Gameplay.DataTables.DataTables;
import com.github.kalimatas.c07_Gameplay.DataTables.Direction;
import org.jsfml.graphics.FloatRect;
import org.jsfml.graphics.RenderStates;
import org.jsfml.graphics.RenderTarget;
import org.jsfml.graphics.Sprite;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

public class Aircraft extends Entity {
    public enum Type {
        EAGLE,
        RAPTOR,
        AVENGER,
        TYPE_COUNT,
    }

    private Type type;
    private Sprite sprite;
    private Command fireCommand = new Command();
    private Command missileCommand = new Command();
    private Time fireCountdown = Time.ZERO;

    private boolean isFiring = false;
    private boolean isLaunchingMissile = false;
    private boolean isMarkedForRemoval = false;

    private int fireRateLevel = 1;
    private int spreadLevel = 1;
    private int missileAmmo = 2;

    private float travelledDistance = 0.f;
    private Command dropPickupCommand = new Command();
    private int directionIndex = 0;
    private TextNode healthDisplay;
    private TextNode missileDisplay;

    private static ArrayList<AircraftData> Table = DataTables.initializeAircraftData();

    public Aircraft(Type type, final ResourceHolder textures, final ResourceHolder fonts) {
        super(Table.get(type.ordinal()).hitpoints);

        this.type = type;
        this.sprite = new Sprite(textures.getTexture(Table.get(type.ordinal()).texture));

        Utility.centerOrigin(this.sprite);

        fireCommand.category = Category.SCENE_AIR_LAYER;
        fireCommand.commandAction = new CommandAction() {
            @Override
            public void invoke(SceneNode node, Time dt) {
                createBullets(node, textures);
            }
        };

        missileCommand.category = Category.SCENE_AIR_LAYER;
        missileCommand.commandAction = new CommandAction() {
            @Override
            public void invoke(SceneNode node, Time dt) {
                createProjectile(node, Projectile.Type.MISSILE, 0.f, 0.5f, textures);
            }
        };

        dropPickupCommand.category = Category.SCENE_AIR_LAYER;
        dropPickupCommand.commandAction = new CommandAction() {
            @Override
            public void invoke(SceneNode node, Time dt) {
                createPickup(node, textures);
            }
        };

        healthDisplay = new TextNode(fonts, "");
        attachChild(healthDisplay);

        if (getCategory() == Category.PLAYER_AIRCRAFT) {
            missileDisplay = new TextNode(fonts, "");
            missileDisplay.setPosition(0, 70);
            attachChild(missileDisplay);
        }

        updateTexts();
    }

    @Override
    public void drawCurrent(RenderTarget target, RenderStates states) {
        target.draw(sprite, states);
    }

    @Override
    protected void updateCurrent(Time dt, CommandQueue commands) {
        // Entity has been destroyed: Possibly drop pickup, mark for removal
        if (isDestroyed()) {
            checkPickupDrop(commands);

            isMarkedForRemoval = true;
            return;
        }

        // Check if bullets or missiles are fired
        checkProjectileLaunch(dt, commands);

        // Update enemy movement pattern; apply velocity
        updateMovementPattern(dt);
        super.updateCurrent(dt, commands);

        // Update texts
        updateTexts();
    }

    public int getCategory() {
        if (isAllied()) {
            return Category.PLAYER_AIRCRAFT;
        } else {
            return Category.ENEMY_AIRCRAFT;
        }
    }

    public boolean isMarkedForRemoval () {
        return isMarkedForRemoval;
    }

    public boolean isAllied() {
        return type == Type.EAGLE;
    }

    public FloatRect getBoundingRect() {
        return getWorldTransform().transformRect(this.sprite.getGlobalBounds());
    }

    public float getMaxSpeed() {
        return Table.get(this.type.ordinal()).speed;
    }

    public void increaseFireRate() {
        if (fireRateLevel < 10) {
            ++fireRateLevel;
        }
    }

    public void increaseSpread() {
        if (spreadLevel < 3) {
            ++spreadLevel;
        }
    }

    public void collectMissiles(int count) {
        missileAmmo += count;
    }

    public void fire() {
        // Only ships with fire interval != 0 are able to fire
        if (Table.get(this.type.ordinal()).fireInterval != Time.ZERO) {
            isFiring = true;
        }
    }

    public void launchingMissile() {
        if (missileAmmo > 0) {
            isLaunchingMissile = true;
            --missileAmmo;
        }
    }

    private void updateMovementPattern(Time dt) {
        // Enemy airplane: Movement pattern
        final LinkedList<Direction> directions = Table.get(this.type.ordinal()).directions;
        if (!directions.isEmpty()) {
            // Moved long enough in current direction: Change direction
            if (travelledDistance > directions.get(directionIndex).distance) {
                directionIndex = (directionIndex + 1) % directions.size();
                travelledDistance = 0.f;
            }

            // Compute velocity from direction
            float radians = Utility.toRadian(directions.get(directionIndex).angle + 90.f);
            float vx = getMaxSpeed() * (float) Math.cos((double)radians);
            float vy = getMaxSpeed() * (float) Math.sin((double)radians);

            setVelocity(vx, vy);

            travelledDistance += getMaxSpeed() * dt.asSeconds();
        }
    }

    private void checkPickupDrop(CommandQueue commands) {
        if (!isAllied() && new Random().nextInt(3) == 0) {
            commands.push(dropPickupCommand);
        }
    }

    private void checkProjectileLaunch(Time dt, CommandQueue commands) {
        // Enemies try to fire all the time
        if (!isAllied()) {
            fire();
        }

        // Check for automatic gunfire, allow only in intervals
        if (isFiring && fireCountdown.asMicroseconds() <= Time.ZERO.asMicroseconds()) {
            // Interval expired: We can fire a new bullet
            commands.push(fireCommand);
            fireCountdown = Time.add(
                    fireCountdown,
                    Time.div(Table.get(type.ordinal()).fireInterval, fireRateLevel + 1.f)
            );
            isFiring = false;
        } else if (fireCountdown.asMicroseconds() > Time.ZERO.asMicroseconds()) {
            // Interval not expired: Decrease it further
            fireCountdown = Time.sub(fireCountdown, dt);
            isFiring = false;
        }

        // Check for missile launch
        if (isLaunchingMissile) {
            commands.push(missileCommand);
            isLaunchingMissile = false;
        }
    }

    public void createBullets(SceneNode node, final ResourceHolder textures) {
        Projectile.Type type = isAllied() ? Projectile.Type.ALLIED_BULLET : Projectile.Type.ENEMY_BULLET;

        switch (spreadLevel) {
            case 1:
                createProjectile(node, type, 0.f, 0.5f, textures);
                break;
            case 2:
                createProjectile(node, type, -0.33f, 0.33f, textures);
                createProjectile(node, type, +0.33f, 0.33f, textures);
                break;
            case 3:
                createProjectile(node, type, -0.5f, 0.33f, textures);
                createProjectile(node, type, +0.f, 0.5f, textures);
                createProjectile(node, type, +0.5f, 0.33f, textures);
                break;
        }
    }

    public void createProjectile(SceneNode node, Projectile.Type type, float xOffset, float yOffset, final ResourceHolder textures) {
        Projectile projectile = new Projectile(type, textures);

        Vector2f offset = new Vector2f(xOffset * sprite.getGlobalBounds().width, yOffset * sprite.getGlobalBounds().height);
        Vector2f velocity = new Vector2f(0, projectile.getMaxSpeed());

        float sign = isAllied() ? -1.f : +1.f;

        projectile.setPosition(Vector2f.add(getWorldPosition(), Vector2f.mul(offset, sign)));
        projectile.setVelocity(Vector2f.mul(velocity, sign));

        node.attachChild(projectile);
    }

    public void createPickup(SceneNode node, final ResourceHolder textures) {
        Pickup pickup = new Pickup(Pickup.Type.getRandom(), textures);
        pickup.setPosition(getWorldPosition());
        pickup.setVelocity(0.f, 1.f);
        node.attachChild(pickup);
    }

    private void updateTexts() {
        healthDisplay.setString(getHitpoints() + "HP");
        healthDisplay.setPosition(0.f, 50.f);
        healthDisplay.setRotation(-getRotation());

        if (missileDisplay != null) {
            if (missileAmmo == 0) {
                missileDisplay.setString("");
            } else {
                missileDisplay.setString("M: " + missileAmmo);
            }
        }
    }
}
