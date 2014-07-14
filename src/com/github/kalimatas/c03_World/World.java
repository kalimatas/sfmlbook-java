package com.github.kalimatas.c03_World;

import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

public class World {
    private enum Layer {
        BACKGROUND,
        AIR,
        LAYERCOUNT,
    }

    private RenderWindow window;
    private View worldView;
    private ResourceHolder textures = new ResourceHolder();
    private SceneNode sceneGraph = new SceneNode();
    private SceneNode[] sceneLayers = new SceneNode[Layer.LAYERCOUNT.ordinal()];

    private FloatRect worldBounds;
    private Vector2f spawnPosition;
    private float scrollSpeed = -50.f;
    private Aircraft playerAircraft;

    public World (RenderWindow window) {
        this.window = window;
        this.worldView = new View(window.getDefaultView().getCenter(), window.getDefaultView().getSize());
        this.worldBounds = new FloatRect(0.f, 0.f, worldView.getSize().x, 2000.f);
        this.spawnPosition = new Vector2f(worldView.getSize().x / 2.f, worldBounds.height - worldView.getSize().y / 2.f);

        loadTextures();
        buildScene();

        // Prepare the view
        this.worldView.setCenter(this.spawnPosition);
    }

    public void update(Time dt) {
        worldView.move(0.f, scrollSpeed * dt.asSeconds());

        Vector2f position = playerAircraft.getPosition();
        Vector2f velocity = playerAircraft.getVelocity();

        if (position.x <= worldBounds.left + 150
            || position.x >= worldBounds.left + worldBounds.width - 150)
        {
            Vector2f newVelocity = new Vector2f(-velocity.x, velocity.y);
            playerAircraft.setVelocity(newVelocity);
        }

        sceneGraph.update(dt);
    }

    public void draw() {
        window.setView(worldView);
        window.draw(sceneGraph);
    }

    private void loadTextures() {
        textures.loadTexture(Textures.EAGLE, "Media/Textures/Eagle.png");
        textures.loadTexture(Textures.RAPTOR, "Media/Textures/Raptor.png");
        textures.loadTexture(Textures.DESERT, "Media/Textures/Desert.png");
    }

    private void buildScene() {
        // Initialize the different layers
        for (int i = 0; i < Layer.LAYERCOUNT.ordinal(); i++) {
            SceneNode layer = new SceneNode();
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
        playerAircraft = new Aircraft(Aircraft.Type.EAGLE, textures);
        playerAircraft.setPosition(spawnPosition);
        playerAircraft.setVelocity(40.f, scrollSpeed);
        sceneLayers[Layer.AIR.ordinal()].attachChild(playerAircraft);

        // Add two escorting aircrafts, placed relatively to the main plane
        Aircraft leftEscort = new Aircraft(Aircraft.Type.RAPTOR, textures);
        leftEscort.setPosition(-80.f, 50.f);
        playerAircraft.attachChild(leftEscort);

        Aircraft rightEscort = new Aircraft(Aircraft.Type.RAPTOR, textures);
        rightEscort.setPosition(80.f, 50.f);
        playerAircraft.attachChild(rightEscort);
    }
}
