package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.GUI.Button;
import com.github.kalimatas.c10_Network.GUI.Callback;
import com.github.kalimatas.c10_Network.GUI.Container;
import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.window.event.Event;

public class MenuState extends State {
    private Sprite backgroundSprite = new Sprite();
    private Container GUIContainer = new Container();

    public MenuState(StateStack stack, Context context) {
        super(stack, context);

        Texture texture = context.textures.getTexture(Textures.TITLE_SCREEN);
        backgroundSprite.setTexture(texture);

        Button playButton = new Button(context);
        playButton.setPosition(100, 300);
        playButton.setText("Play");
        playButton.setCallback(new Callback() {
            @Override
            public void invoke() {
                requestStackPop();
                requestStackPush(States.GAME);
            }
        });

        Button hostPlayButton = new Button(context);
        hostPlayButton.setPosition(100, 350);
        hostPlayButton.setText("Host");
        hostPlayButton.setCallback(new Callback() {
            @Override
            public void invoke() {
                requestStackPop();
                requestStackPush(States.HOST_GAME);
            }
        });

        Button joinPlayButton = new Button(context);
        joinPlayButton.setPosition(100, 400);
        joinPlayButton.setText("Join");
        joinPlayButton.setCallback(new Callback() {
            @Override
            public void invoke() {
                requestStackPop();
                requestStackPush(States.JOIN_GAME);
            }
        });

        Button settingsButton = new Button(context);
        settingsButton.setPosition(100, 450);
        settingsButton.setText("Settings");
        settingsButton.setCallback(new Callback() {
            @Override
            public void invoke() {
                requestStackPush(States.SETTINGS);
            }
        });

        Button exitButton = new Button(context);
        exitButton.setPosition(100, 500);
        exitButton.setText("Exit");
        exitButton.setCallback(new Callback() {
            @Override
            public void invoke() {
                requestStackPop();
            }
        });

        GUIContainer.pack(playButton);
        GUIContainer.pack(hostPlayButton);
        GUIContainer.pack(joinPlayButton);
        GUIContainer.pack(settingsButton);
        GUIContainer.pack(exitButton);

        // Play menu theme
        context.music.play(Musics.MENU_THEME);
    }

    @Override
    public void draw() {
        RenderWindow window = getContext().window;

        window.setView(window.getDefaultView());

        window.draw(backgroundSprite);
        window.draw(GUIContainer);
    }

    @Override
    public boolean update(Time dt) {
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        GUIContainer.handleEvent(event);
        return false;
    }
}
