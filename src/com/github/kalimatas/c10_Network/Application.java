package com.github.kalimatas.c10_Network;

import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Text;
import org.jsfml.graphics.TextureCreationException;
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

    private MusicPlayer music = new MusicPlayer();
    private SoundPlayer sounds = new SoundPlayer();
    private StateStack stateStack;

    private Text statisticsText = new Text();
    private Time statisticsUpdateTime = Time.ZERO;
    private int statisticsNumFrames = 0;

    public Application() {
        window = new RenderWindow(new VideoMode(1024, 768), "Network", WindowStyle.CLOSE);
        stateStack = new StateStack(new State.Context(window, textures, fonts, player, music, sounds));

        window.setKeyRepeatEnabled(false);
        window.setVerticalSyncEnabled(true);

        fonts.loadFont(Fonts.MAIN, "Media/Sansation.ttf");

        textures.loadTexture(Textures.TITLE_SCREEN, "Media/Textures/TitleScreen.png");
        textures.loadTexture(Textures.BUTTONS, "Media/Textures/Buttons.png");

        statisticsText.setFont(fonts.getFont(Fonts.MAIN));
        statisticsText.setPosition(5.f, 5.f);
        statisticsText.setCharacterSize(10);

        registerStates();
        stateStack.pushState(States.TITLE);

        music.setVolume(25.f);
    }

    public void run() throws TextureCreationException {
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

    private void processInput() throws TextureCreationException {
        for (Event event : window.pollEvents()) {
            stateStack.handleEvent(event);

            if (event.type == Event.Type.CLOSED) {
                window.close();
            }
        }
    }

    private void update(Time dt) throws TextureCreationException {
        stateStack.update(dt);
    }

    private void render() throws TextureCreationException {
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
