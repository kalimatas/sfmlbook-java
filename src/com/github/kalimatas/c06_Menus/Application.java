package com.github.kalimatas.c06_Menus;

import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Text;
import org.jsfml.system.Clock;
import org.jsfml.system.Time;
import org.jsfml.window.VideoMode;
import org.jsfml.window.WindowStyle;
import org.jsfml.window.event.Event;

public class Application {
    private static final Time timePerFrame = Time.getSeconds(1.0f / 60.0f);

    private RenderWindow window;
    private ResourceHolder textures = new ResourceHolder();
    private ResourceHolder fonts = new ResourceHolder();
    private Player player = new Player();

    private StateStack stateStack;

    private Text statisticsText = new Text();
    private Time statisticsUpdateTime = Time.ZERO;
    private int statisticsNumFrames = 0;

    public Application() {
        window = new RenderWindow(new VideoMode(640, 480), "Menus", WindowStyle.CLOSE);
        stateStack = new StateStack(new State.Context(window, textures, fonts, player));

        window.setKeyRepeatEnabled(false);

        fonts.loadFont(Fonts.MAIN, "Media/Sansation.ttf");

        textures.loadTexture(Textures.TITLE_SCREEN, "Media/Textures/TitleScreen.png");
        textures.loadTexture(Textures.BUTTON_NORMAL, "Media/Textures/ButtonNormal.png");
        textures.loadTexture(Textures.BUTTON_SELECTED, "Media/Textures/ButtonSelected.png");
        textures.loadTexture(Textures.BUTTON_PRESSED, "Media/Textures/ButtonPressed.png");

        statisticsText.setFont(fonts.getFont(Fonts.MAIN));
        statisticsText.setPosition(5.f, 5.f);
        statisticsText.setCharacterSize(10);

        registerStates();
        stateStack.pushState(States.TITLE);
    }

    public void run() {
        Clock clock = new Clock();
        Time timeSinceLastUpdate = Time.ZERO;

        while (window.isOpen()) {
            Time dt = clock.restart();
            timeSinceLastUpdate = Time.add(timeSinceLastUpdate, dt);
            while (timeSinceLastUpdate.asMicroseconds() > timePerFrame.asMicroseconds()) {
                timeSinceLastUpdate = Time.sub(timeSinceLastUpdate, timePerFrame);

                processInput();
                update(timePerFrame);

                // Check inside this loop, because stack might be empty before update() call
                if (stateStack.isEmpty()) {
                    window.close();
                }
            }

            updateStatistics(dt);
            render();
        }
    }

    private void processInput() {
        for (Event event : window.pollEvents()) {
            stateStack.handleEvent(event);

            if (event.type == Event.Type.CLOSED) {
                window.close();
            }
        }
    }

    private void update(Time dt) {
        stateStack.update(dt);
    }

    private void render() {
        window.clear();

        stateStack.draw();

        window.setView(window.getDefaultView());
        window.draw(statisticsText);

        window.display();
    }

    private void updateStatistics(Time dt) {
        statisticsUpdateTime = Time.add(statisticsUpdateTime, dt);
        statisticsNumFrames += 1;

        if (statisticsUpdateTime.asSeconds() >= Time.getSeconds(1.0f).asSeconds()) {
            statisticsText.setString("FPS: " + statisticsNumFrames);

            statisticsUpdateTime = Time.sub(statisticsUpdateTime, Time.getSeconds(1.0f));
            statisticsNumFrames = 0;
        }
    }

    private void registerStates() {

    }
}
