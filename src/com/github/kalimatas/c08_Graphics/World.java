package com.github.kalimatas.c08_Graphics;

import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

import java.util.*;

public class World {
    private enum Layer {
        BACKGROUND,
        LOWER_AIR,
        UPPER_AIR,
        LAYERCOUNT,
    }

    private RenderTarget target;
    private RenderTexture sceneTexture = new RenderTexture();
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

    private BloomEffect bloomEffect = new BloomEffect();

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

    public World(RenderTarget outputTarget, ResourceHolder fonts) throws TextureCreationException {
        this.target = outputTarget;
        this.sceneTexture.create(this.target.getSize().x, this.target.getSize().y);

        this.fonts = fonts;
        this.worldView = new View(outputTarget.getDefaultView().getCenter(), outputTarget.getDefaultView().getSize());
        this.worldBounds = new FloatRect(0.f, 0.f, worldView.getSize().x, 5000.f);
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
        destroyEntetiesOutsideView();
        guideMissiles();

        // Forward commands to scene graph, adapt velocity (scrolling, diagonal correction)
        while (!commandQueue.isEmpty()) {
            sceneGraph.onCommand(commandQueue.pop(), dt);
        }
        adaptPlayerVelocity();

        // Collision detection and response (may destroy entities)
        handleCollisions();

        // Remove all destroyed entities, create new ones
        sceneGraph.removeWrecks();
        spawnEnemies();

        // Regular update step, adapt position (correct if outside view)
        sceneGraph.update(dt, commandQueue);
        adaptPlayerPosition();
    }

    public void draw() throws TextureCreationException {
        if (PostEffect.isSupported()) {
            sceneTexture.clear();
            sceneTexture.setView(worldView);
            sceneTexture.draw(sceneGraph);
            sceneTexture.display();
            bloomEffect.apply(sceneTexture, target);
        } else {
            target.setView(worldView);
            target.draw(sceneGraph);
        }
    }

    public CommandQueue getCommandQueue() {
        return commandQueue;
    }

    public boolean hasAlivePlayer() {
        return !playerAircraft.isMarkedForRemoval();
    }

    public boolean hasPlayerReachedEnd() {
        return !worldBounds.contains(playerAircraft.getPosition());
    }

    private void loadTextures() {
        textures.loadTexture(Textures.ENTITIES, "Media/Textures/Entities.png");
        textures.loadTexture(Textures.JUNGLE, "Media/Textures/Jungle.png");
        textures.loadTexture(Textures.EXPLOSION, "Media/Textures/Explosion.png");
        textures.loadTexture(Textures.PARTICLE, "Media/Textures/Particle.png");
        textures.loadTexture(Textures.FINISH_LINE, "Media/Textures/FinishLine.png");
    }

    private void buildScene() {
        // Initialize the different layers
        for (int i = 0; i < Layer.LAYERCOUNT.ordinal(); i++) {
            int category = i == Layer.LOWER_AIR.ordinal() ? Category.SCENE_AIR_LAYER : Category.NONE;

            SceneNode layer = new SceneNode();
            layer.setCategory(category);
            sceneLayers[i] = layer;

            sceneGraph.attachChild(layer);
        }

        // Prepare the tiled background
        Texture jungleTexture = textures.getTexture(Textures.JUNGLE);
        jungleTexture.setRepeated(true);

        float viewHeight = worldView.getSize().y;
        IntRect textureRect = new IntRect((int) worldBounds.left, (int) worldBounds.top, (int) worldBounds.width, (int) worldBounds.height + (int) viewHeight);

        // Add the background sprite to the scene
        SpriteNode jungleSprite = new SpriteNode(jungleTexture, textureRect);
        jungleSprite.setPosition(worldBounds.left, worldBounds.top - viewHeight);
        sceneLayers[Layer.BACKGROUND.ordinal()].attachChild(jungleSprite);

        // Add the finish line to the scene
        Texture finishTexture = textures.getTexture(Textures.FINISH_LINE);
        SpriteNode finishSprite = new SpriteNode(finishTexture);
        finishSprite.setPosition(0.f, -76.f);
        sceneLayers[Layer.BACKGROUND.ordinal()].attachChild(finishSprite);

        // Add particle node to the scene
        ParticleNode smokeNode = new ParticleNode(Particle.Type.SMOKE, textures);
        sceneLayers[Layer.LOWER_AIR.ordinal()].attachChild(smokeNode);

        // Add propellant particle node to the scene
        ParticleNode propellantNode = new ParticleNode(Particle.Type.PROPELLANT, textures);
        sceneLayers[Layer.LOWER_AIR.ordinal()].attachChild(propellantNode);

        // Add player's aircraft
        playerAircraft = new Aircraft(Aircraft.Type.EAGLE, textures, fonts);
        playerAircraft.setPosition(spawnPosition);
        sceneLayers[Layer.UPPER_AIR.ordinal()].attachChild(playerAircraft);

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

    private boolean matchesCategories(SceneNode.Pair colliders, int type1, int type2) {
        int category1 = colliders.first.getCategory();
        int category2 = colliders.second.getCategory();

        // Make sure first pair entry has category type1 and second has type2
        if ((type1 & category1) > 0 && (type2 & category2) > 0) {
            return true;
        } else if ((type1 & category2) > 0 && (type2 & category1) > 0) {
            SceneNode tmp = colliders.first;
            colliders.first = colliders.second;
            colliders.second = tmp;
            return true;
        } else {
            return false;
        }
    }

    private void handleCollisions() {
        Set<SceneNode.Pair> collisionPairs = new HashSet<>();
        sceneGraph.checkSceneCollision(sceneGraph, collisionPairs);

        for (SceneNode.Pair pair : collisionPairs) {
            if (matchesCategories(pair, Category.PLAYER_AIRCRAFT, Category.ENEMY_AIRCRAFT)) {
                Aircraft player = (Aircraft) pair.first;
                Aircraft enemy = (Aircraft) pair.second;

                // Collision: Player damage = enemy's remaining HP
                player.damage(enemy.getHitpoints());
                enemy.destroy();
            }
            else if (matchesCategories(pair, Category.PLAYER_AIRCRAFT, Category.PICKUP)) {
                Aircraft player = (Aircraft) pair.first;
                Pickup pickup = (Pickup) pair.second;

                // Apply pickup effect to player, destroy pickup
                pickup.apply(player);
                pickup.destroy();
            }
            else if (matchesCategories(pair, Category.ENEMY_AIRCRAFT, Category.ALLIED_PROJECTILE)
                  || matchesCategories(pair, Category.PLAYER_AIRCRAFT, Category.ENEMY_PROJECTILE))
            {
                Aircraft aircraft = (Aircraft) pair.first;
                Projectile projectile = (Projectile) pair.second;

                // Apply projectile damage to aircraft, destroy projectile
                aircraft.damage(projectile.getDamage());
                projectile.destroy();
            }
        }
    }

    private void addEnemies() {
        // Add enemies to the spawn point container
        addEnemy(Aircraft.Type.RAPTOR, 0.f, 500.f);
        addEnemy(Aircraft.Type.RAPTOR, 0.f, 1000.f);
        addEnemy(Aircraft.Type.RAPTOR, +100.f, 1150.f);
        addEnemy(Aircraft.Type.RAPTOR, -100.f, 1150.f);
        addEnemy(Aircraft.Type.AVENGER, 70.f, 1500.f);
        addEnemy(Aircraft.Type.AVENGER, -70.f, 1500.f);
        addEnemy(Aircraft.Type.AVENGER, -70.f, 1710.f);
        addEnemy(Aircraft.Type.AVENGER, 70.f, 1700.f);
        addEnemy(Aircraft.Type.AVENGER, 30.f, 1850.f);
        addEnemy(Aircraft.Type.RAPTOR, 300.f, 2200.f);
        addEnemy(Aircraft.Type.RAPTOR, -300.f, 2200.f);
        addEnemy(Aircraft.Type.RAPTOR, 0.f, 2200.f);
        addEnemy(Aircraft.Type.RAPTOR, 0.f, 2500.f);
        addEnemy(Aircraft.Type.AVENGER, -300.f, 2700.f);
        addEnemy(Aircraft.Type.AVENGER, -300.f, 2700.f);
        addEnemy(Aircraft.Type.RAPTOR, 0.f, 3000.f);
        addEnemy(Aircraft.Type.RAPTOR, 250.f, 3250.f);
        addEnemy(Aircraft.Type.RAPTOR, -250.f, 3250.f);
        addEnemy(Aircraft.Type.AVENGER, 0.f, 3500.f);
        addEnemy(Aircraft.Type.AVENGER, 0.f, 3700.f);
        addEnemy(Aircraft.Type.RAPTOR, 0.f, 3800.f);
        addEnemy(Aircraft.Type.AVENGER, 0.f, 4000.f);
        addEnemy(Aircraft.Type.AVENGER, -200.f, 4200.f);
        addEnemy(Aircraft.Type.RAPTOR, 200.f, 4200.f);
        addEnemy(Aircraft.Type.RAPTOR, 0.f, 4400.f);

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

            sceneLayers[Layer.UPPER_AIR.ordinal()].attachChild(enemy);
        }
    }

    private void destroyEntetiesOutsideView() {
        Command command = new Command();
        command.category = Category.PROJECTILE | Category.ENEMY_AIRCRAFT;
        command.commandAction = new CommandAction<Entity>() {
            @Override
            public void invoke(Entity e, Time dt) {
                if (getBattlefieldBounds().intersection(e.getBoundingRect()) == null) {
                    e.remove();
                }
            }
        };

        commandQueue.push(command);
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
