package com.github.kalimatas.c01_Intro;

import org.jsfml.graphics.*;
import org.jsfml.system.Clock;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;
import org.jsfml.window.VideoMode;
import org.jsfml.window.event.Event;
import org.jsfml.window.event.KeyEvent;

import java.io.IOException;

public class Game {
    private static final float playerSpeed = 100.f;
    private static final Time timePerFrame = Time.getSeconds(1.0f / 60.0f);

    private RenderWindow window;
    private Texture texture = new Texture();
    private Sprite player = new Sprite();

    private Font font = new Font();
    private Text statisticsText = new Text();
    private Time statisticsUpdateTime = Time.ZERO;
    private int statisticsNumFrames = 0;

    private boolean isMovingUp = false;
    private boolean isMovingDown = false;
    private boolean isMovingLeft = false;
    private boolean isMovingRight = false;

    public Game() {
        window = new RenderWindow(new VideoMode(640, 480), "SFML Application");

        try {
            // Need to load from stream in order to load from JAR
            texture.loadFromStream(getClass().getResourceAsStream("Media/Textures/Eagle.png"));
        } catch (IOException e) {
            // Handle loading error
            System.out.println(e.getMessage());
        }

        player.setTexture(texture);
        player.setPosition(100.f, 100.f);

        try {
            // Need to load from stream in order to load from JAR
            font.loadFromStream(getClass().getResourceAsStream("Media/Sansation.ttf"));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        statisticsText.setFont(font);
        statisticsText.setPosition(5.f, 5.f);
        statisticsText.setCharacterSize(10);
    }

    public void run() {
        Clock clock = new Clock();
        Time timeSinceLastUpdate = Time.ZERO;

        while (window.isOpen()) {
            Time elapsedTime = clock.restart();
            timeSinceLastUpdate = Time.add(timeSinceLastUpdate, elapsedTime);
            while (timeSinceLastUpdate.asMicroseconds() > timePerFrame.asMicroseconds()) {
                timeSinceLastUpdate = Time.sub(timeSinceLastUpdate, timePerFrame);
                processEvents();
                update(timePerFrame);
            }

            updateStatistics(elapsedTime);
            render();
        }
    }

    private void processEvents() {
        for (Event event : window.pollEvents()) {
            KeyEvent keyEvent;
            switch (event.type) {
                case KEY_PRESSED:
                    keyEvent = event.asKeyEvent();
                    handlePlayerInput(keyEvent.key, true);
                    break;
                case KEY_RELEASED:
                    keyEvent = event.asKeyEvent();
                    handlePlayerInput(keyEvent.key, false);
                    break;
                case CLOSED:
                    window.close();
                    break;
            }
        }
    }

    private void updateStatistics(Time elapsedTime)
    {
        statisticsUpdateTime = Time.add(statisticsUpdateTime, elapsedTime);
        statisticsNumFrames += 1;

        if (statisticsUpdateTime.asSeconds() >= Time.getSeconds(1.0f).asSeconds()) {
            statisticsText.setString(
                    "Frames / Second = " + statisticsNumFrames + "\n" +
                            "Time / Update = " + (statisticsUpdateTime.asMicroseconds() / statisticsNumFrames) + "us");

            statisticsUpdateTime = Time.sub(statisticsUpdateTime, Time.getSeconds(1.0f));
            statisticsNumFrames = 0;
        }
    }

    private void handlePlayerInput(Keyboard.Key key, boolean isPressed) {
        if (key == Keyboard.Key.W) {
            isMovingUp = isPressed;
        } else if (key == Keyboard.Key.S) {
            isMovingDown = isPressed;
        } else if (key == Keyboard.Key.A) {
            isMovingLeft = isPressed;
        } else if (key == Keyboard.Key.D) {
            isMovingRight = isPressed;
        }
    }

    private void update(Time deltaTime) {
        Vector2f movement = new Vector2f(0.f, 0.f);
        if (isMovingUp) {
            movement = Vector2f.add(movement, new Vector2f(0.f, -playerSpeed));
        }
        if (isMovingDown) {
            movement = Vector2f.add(movement, new Vector2f(0.f, playerSpeed));
        }
        if (isMovingLeft) {
            movement = Vector2f.add(movement, new Vector2f(-playerSpeed, 0.f));
        }
        if (isMovingRight) {
            movement = Vector2f.add(movement, new Vector2f(playerSpeed, 0.f));
        }

        player.move(Vector2f.mul(movement, deltaTime.asSeconds()));
    }

    private void render() {
        window.clear();
        window.draw(player);
        window.draw(statisticsText);
        window.display();
    }
}
