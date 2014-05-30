package com.github.kalimatas.c04_Input;

import org.jsfml.graphics.Font;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Text;
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

    RenderWindow window;
    World world;
    private Player player = new Player();

    Font font = new Font();
    Text statisticsText = new Text();
    Time statisticsUpdateTime = Time.ZERO;
    int statisticsNumFrames = 0;

    Game() throws IOException {
        window = new RenderWindow(new VideoMode(640, 480), "World", WindowStyle.CLOSE);
        world = new World(window);

        // Need to load from stream in order to load from JAR
        font.loadFromStream(getClass().getResourceAsStream("Media/Sansation.ttf"));

        statisticsText.setFont(font);
        statisticsText.setPosition(5.f, 5.f);
        statisticsText.setCharacterSize(10);
    }

    void run() {
        Clock clock = new Clock();
        Time timeSinceLastUpdate = Time.ZERO;

        while (window.isOpen()) {
            Time elapsedTime = clock.restart();
            timeSinceLastUpdate = Time.add(timeSinceLastUpdate, elapsedTime);
            while (timeSinceLastUpdate.asMicroseconds() > timePerFrame.asMicroseconds()) {
                timeSinceLastUpdate = Time.sub(timeSinceLastUpdate, timePerFrame);

                processInput();
                update(timePerFrame);
            }

            updateStatistics(elapsedTime);
            render();
        }
    }

    private void processInput() {
        CommandQueue commands = world.getCommandQueue();

        for (Event event : window.pollEvents()) {
            player.handleEvent(event, commands);

            if (event.type == Event.Type.CLOSED) {
                window.close();
            }

            /*
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
            */
        }

        player.handleRealtimeInput(commands);
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
