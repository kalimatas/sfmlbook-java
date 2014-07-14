package com.github.kalimatas.c03_World;

import org.jsfml.graphics.*;
import org.jsfml.system.Clock;
import org.jsfml.system.Time;
import org.jsfml.window.Keyboard;
import org.jsfml.window.VideoMode;
import org.jsfml.window.WindowStyle;
import org.jsfml.window.event.Event;
import org.jsfml.window.event.KeyEvent;

import java.io.IOException;

public class Game {
    private static final Time timePerFrame = Time.getSeconds(1.0f / 60.0f);

    private RenderWindow window;
    private World world;

    private Font font = new Font();
    private Text statisticsText = new Text();
    private Time statisticsUpdateTime = Time.ZERO;
    private int statisticsNumFrames = 0;

    public Game() throws IOException {
        window = new RenderWindow(new VideoMode(640, 480), "World", WindowStyle.CLOSE);
        world = new World(window);

        // Need to load from stream in order to load from JAR
        font.loadFromStream(getClass().getResourceAsStream("Media/Sansation.ttf"));

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

    private void update(Time deltaTime) {
        world.update(deltaTime);
    }

    private void render() {
        window.clear();
        world.draw();

        window.setView(window.getDefaultView());
        window.draw(statisticsText);
        window.display();
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

    }
}
