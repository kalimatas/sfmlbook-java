package com.github.kalimatas.c07_Gameplay;

import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

public class World {
    private enum Layer {
        BACKGROUND,
        AIR,
        LAYERCOUNT,
    }

    private RenderWindow window;
    private View worldView;
    private ResourceHolder textures = new ResourceHolder();
    private ResourceHolder fonts;
    private SceneNode sceneGraph = new SceneNode();
    private SceneNode[] sceneLayers = new SceneNode[Layer.LAYERCOUNT.ordinal()];
    private CommandQueue commandQueue = new CommandQueue();

    private FloatRect worldBounds;
    private Vector2f spawnPosition;
    private float scrollSpeed = -50.f;
    private Aircraft playerAircraft;

    private LinkedList<SpawnPoint> enemySpawnPoints = new LinkedList<>();
    private LinkedList<Aircraft> activeEnemies = new LinkedList<>();

    private class SpawnPoint {
        Aircraft.Type type;
        float x;
        float y;

        SpawnPoint(Aircraft.Type type, float x, float y) {
            this.type = type;
            this.x = x;
            this.y = y;
        }
    }

    public World (RenderWindow window, ResourceHolder fonts) {
        this.window = window;
        this.fonts = fonts;
        this.worldView = new View(window.getDefaultView().getCenter(), window.getDefaultView().getSize());
        this.worldBounds = new FloatRect(0.f, 0.f, worldView.getSize().x, 2000.f);
        this.spawnPosition = new Vector2f(worldView.getSize().x / 2.f, worldBounds.height - worldView.getSize().y / 2.f);

        loadTextures();
        buildScene();

        // Prepare the view
        this.worldView.setCenter(this.spawnPosition);
    }

    public void update(Time dt) {
        // Scroll the world, reset player velocity
        worldView.move(0.f, scrollSpeed * dt.asSeconds());
        playerAircraft.setVelocity(0.f, 0.f);

        // Setup commands to destroy entities, and guide missiles
        guideMissiles();

        // Forward commands to scene graph, adapt velocity (scrolling, diagonal correction)
        while (!commandQueue.isEmpty()) {
            sceneGraph.onCommand(commandQueue.pop(), dt);
        }
        adaptPlayerVelocity();

        // Remove all destroyed entities, create new ones
        // todo
        spawnEnemies();

        // Regular update step, adapt position (correct if outside view)
        sceneGraph.update(dt, commandQueue);
        adaptPlayerPosition();
    }

    public void draw() {
        window.setView(worldView);
        window.draw(sceneGraph);
    }

    public CommandQueue getCommandQueue() {
        return commandQueue;
    }

    private void loadTextures() {
        textures.loadTexture(Textures.EAGLE, "Media/Textures/Eagle.png");
        textures.loadTexture(Textures.RAPTOR, "Media/Textures/Raptor.png");
        textures.loadTexture(Textures.AVENGER, "Media/Textures/Avenger.png");
        textures.loadTexture(Textures.DESERT, "Media/Textures/Desert.png");

        textures.loadTexture(Textures.BULLET, "Media/Textures/Bullet.png");
        textures.loadTexture(Textures.MISSILE, "Media/Textures/Missile.png");
    }

    private void buildScene() {
        // Initialize the different layers
        for (int i = 0; i < Layer.LAYERCOUNT.ordinal(); i++) {
            int category = i == Layer.AIR.ordinal() ? Category.SCENE_AIR_LAYER : Category.NONE;

            SceneNode layer = new SceneNode();
            layer.setCategory(category);
            sceneLayers[i] = layer;

            sceneGraph.attachChild(layer);
        }

        // Prepare the tiled background
        Texture texture = textures.getTexture(Textures.DESERT);
        IntRect textureRect = new IntRect(worldBounds);
        texture.setRepeated(true);

        // Add the background sprite to the scene
        SpriteNode backgroundSprite = new SpriteNode(texture, textureRect);
        backgroundSprite.setPosition(worldBounds.left, worldBounds.top);
        sceneLayers[Layer.BACKGROUND.ordinal()].attachChild(backgroundSprite);

        // Add player's aircraft
        playerAircraft = new Aircraft(Aircraft.Type.EAGLE, textures, fonts);
        playerAircraft.setPosition(spawnPosition);
        sceneLayers[Layer.AIR.ordinal()].attachChild(playerAircraft);

        // Add enemy aircraft
        addEnemies();
    }

    private void adaptPlayerPosition() {
        // Keep player's position inside the screen bounds, at least borderDistance units from the border
        FloatRect viewBounds = getViewBounds();
        final float borderDistance = 40.f;

        Vector2f position = playerAircraft.getPosition();
        float xPosition = Math.max(position.x, viewBounds.left + borderDistance);
        xPosition = Math.min(xPosition, viewBounds.left + viewBounds.width - borderDistance);
        float yPosition = Math.max(position.y, viewBounds.top + borderDistance);
        yPosition = Math.min(yPosition, viewBounds.top + viewBounds.height - borderDistance);

        playerAircraft.setPosition(xPosition, yPosition);
    }

    private void adaptPlayerVelocity() {
        Vector2f velocity = playerAircraft.getVelocity();

        // If moving diagonally, reduce velocity (to have always same velocity)
        if (velocity.x != 0.f && velocity.y != 0.f) {
            playerAircraft.setVelocity(Vector2f.div(velocity, (float)Math.sqrt(2.f)));
        }

        // Add scrolling velocity
        playerAircraft.accelerate(0.f, scrollSpeed);
    }

    private void addEnemies() {
        // Add enemies to the spawn point container
        addEnemy(Aircraft.Type.RAPTOR,    0.f,  500.f);
        addEnemy(Aircraft.Type.RAPTOR,    0.f, 1000.f);
        addEnemy(Aircraft.Type.RAPTOR, +100.f, 1100.f);
        addEnemy(Aircraft.Type.RAPTOR, -100.f, 1100.f);
        addEnemy(Aircraft.Type.AVENGER, -70.f, 1400.f);
        addEnemy(Aircraft.Type.AVENGER, -70.f, 1600.f);
        addEnemy(Aircraft.Type.AVENGER,  70.f, 1400.f);
        addEnemy(Aircraft.Type.AVENGER,  70.f, 1600.f);

        // Sort all enemies according to their y value, such that lower enemies are checked first for spawning
        Collections.sort(enemySpawnPoints, new Comparator<SpawnPoint>() {
            @Override
            public int compare(SpawnPoint o1, SpawnPoint o2) {
                if (o1.y < o2.y) {
                    return -1;
                } else if (o1.y > o2.y) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private void addEnemy(Aircraft.Type type, float relX, float relY) {
        SpawnPoint spawn = new SpawnPoint(type, spawnPosition.x + relX, spawnPosition.y - relY);
        enemySpawnPoints.addLast(spawn);
    }

    private void spawnEnemies() {
        // Spawn all enemies entering the view area (including distance) this frame
        while (!enemySpawnPoints.isEmpty() && enemySpawnPoints.peekLast().y > getBattlefieldBounds().top) {
            SpawnPoint spawn = enemySpawnPoints.removeLast();

            Aircraft enemy = new Aircraft(spawn.type, textures, fonts);
            enemy.setPosition(spawn.x, spawn.y);
            enemy.rotate(180.f);

            sceneLayers[Layer.AIR.ordinal()].attachChild(enemy);
        }
    }

    private void guideMissiles() {
        // Setup command that stores all enemies in activeEnemies
        Command enemyCollector = new Command();
        enemyCollector.category = Category.ENEMY_AIRCRAFT;
        enemyCollector.commandAction = new CommandAction<Aircraft>() {
            @Override
            public void invoke(Aircraft enemy, Time dt) {
                if (!enemy.isDestroyed()) {
                    activeEnemies.addLast(enemy);
                }
            }
        };

        // Setup command that guides all missiles to the enemy which is currently closest to the player
        Command missileGuider = new Command();
        missileGuider.category = Category.ALLIED_PROJECTILE;
        missileGuider.commandAction = new CommandAction<Projectile>() {
            @Override
            public void invoke(Projectile missile, Time dt) {
                // Ignore unguided bullets
                if (!missile.isGuided()) {
                    return;
                }

                float minDistance = Float.MAX_VALUE;
                Aircraft closestEnemy = null;

                // Find closest enemy
                for (Aircraft enemy : activeEnemies) {
                    float enemyDistance = enemy.distance(missile, enemy);

                    if (enemyDistance < minDistance) {
                        closestEnemy = enemy;
                        minDistance = enemyDistance;
                    }
                }

                if (closestEnemy != null) {
                    missile.guideTowards(closestEnemy.getWorldPosition());
                }
            }
        };

        // Push commands, reset active enemies
        commandQueue.push(enemyCollector);
        commandQueue.push(missileGuider);
        activeEnemies.clear();
    }

    private FloatRect getViewBounds() {
        return new FloatRect(
                Vector2f.sub(worldView.getCenter(), Vector2f.div(worldView.getSize(), 2.f)),
                worldView.getSize()
        );
    }

    private FloatRect getBattlefieldBounds() {
        // Return view bounds + some area at top, where enemies spawn
        FloatRect bounds = getViewBounds();
        return new FloatRect(bounds.left, bounds.top - 100.f, bounds.width, bounds.height + 100.f);
    }
}
