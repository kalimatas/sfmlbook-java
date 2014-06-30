package com.github.kalimatas.c06_Menus;

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
    private CommandQueue commandQueue = new CommandQueue();

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
        // Scroll the world, reset player velocity
        worldView.move(0.f, scrollSpeed * dt.asSeconds());
        playerAircraft.setVelocity(0.f, 0.f);

        // Forward commands to scene graph, adapt velocity (scrolling, diagonal correction)
        while (!commandQueue.isEmpty()) {
            sceneGraph.onCommand(commandQueue.pop(), dt);
        }
        adaptPlayerVelocity();

        // Regular update step, adapt position (correct if outside view)
        sceneGraph.update(dt);
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
        sceneLayers[Layer.AIR.ordinal()].attachChild(playerAircraft);
    }

    private void adaptPlayerPosition() {
        // Keep player's position inside the screen bounds, at least borderDistance units from the border
        FloatRect viewBounds = new FloatRect(
            Vector2f.sub(worldView.getCenter(), Vector2f.div(worldView.getSize(), 2.f)),
                worldView.getSize()
        );
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
}
