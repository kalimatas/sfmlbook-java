package com.github.kalimatas.c08_Graphics;

import com.github.kalimatas.c08_Graphics.DataTables.AircraftData;
import com.github.kalimatas.c08_Graphics.DataTables.DataTables;
import com.github.kalimatas.c08_Graphics.DataTables.Direction;
import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.system.Vector2i;

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
    private Animation explosion;
    private Command fireCommand = new Command();
    private Command missileCommand = new Command();
    private Time fireCountdown = Time.ZERO;

    private boolean isFiring = false;
    private boolean isLaunchingMissile = false;
    private boolean showExplostion = true;
    private boolean spawnedPickup = false;

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
        sprite = new Sprite(textures.getTexture(Table.get(type.ordinal()).texture), Table.get(type.ordinal()).textureRect);

        explosion = new Animation(textures.getTexture(Textures.EXPLOSION));
        explosion.setFrameSize(new Vector2i(256, 256));
        explosion.setNumFrames(16);
        explosion.setDuration(Time.getSeconds(1));

        Utility.centerOrigin(sprite);
        Utility.centerOrigin(explosion);

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
        if (isDestroyed() && showExplostion) {
            target.draw(explosion, states);
        } else {
            target.draw(sprite, states);
        }
    }

    @Override
    protected void updateCurrent(Time dt, CommandQueue commands) {
        // Update texts and roll animation
        updateTexts();
        updateRollAnimation();

        // Entity has been destroyed: Possibly drop pickup, mark for removal
        if (isDestroyed()) {
            checkPickupDrop(commands);
            explosion.update(dt);
            return;
        }

        // Check if bullets or missiles are fired
        checkProjectileLaunch(dt, commands);

        // Update enemy movement pattern; apply velocity
        updateMovementPattern(dt);
        super.updateCurrent(dt, commands);
    }

    public int getCategory() {
        if (isAllied()) {
            return Category.PLAYER_AIRCRAFT;
        } else {
            return Category.ENEMY_AIRCRAFT;
        }
    }

    public boolean isMarkedForRemoval () {
        return isDestroyed() && (explosion.isFinished() || !showExplostion);
    }

    public void remove() {
        super.remove();
        showExplostion = false;
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
        if (!isAllied() && new Random().nextInt(3) == 0 && !spawnedPickup) {
            commands.push(dropPickupCommand);
        }

        spawnedPickup = true;
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

    private void updateRollAnimation() {
        if (Table.get(type.ordinal()).hasRollAnimation) {
            IntRect textureRect = Table.get(type.ordinal()).textureRect;
            int left = textureRect.left;

            // Roll left: Texture rect offset once
            if (getVelocity().x < 0.f) {
                left += textureRect.width;
            }
            // Roll right: Texture rect offset twice
            else if (getVelocity().x > 0.f) {
                left += 2 * textureRect.width;
            }

            sprite.setTextureRect(new IntRect(left, textureRect.top, textureRect.width, textureRect.height));
        }
    }
}
