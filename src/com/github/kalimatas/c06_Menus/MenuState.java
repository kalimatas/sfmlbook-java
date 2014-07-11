package com.github.kalimatas.c06_Menus;

import com.github.kalimatas.c06_Menus.GUI.Button;
import com.github.kalimatas.c06_Menus.GUI.Callback;
import com.github.kalimatas.c06_Menus.GUI.Container;
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

        Button playButton = new Button(context.fonts, context.textures);
        playButton.setPosition(100, 250);
        playButton.setText("Play");
        playButton.setCallback(new Callback() {
            @Override
            public void invoke() {
                requestStackPop();
                requestStackPush(States.GAME);
            }
        });

        Button settingsButton = new Button(context.fonts, context.textures);
        settingsButton.setPosition(100, 300);
        settingsButton.setText("Settings");
        settingsButton.setCallback(new Callback() {
            @Override
            public void invoke() {
                requestStackPush(States.SETTINGS);
            }
        });

        Button exitButton = new Button(context.fonts, context.textures);
        exitButton.setPosition(100, 350);
        exitButton.setText("Exit");
        exitButton.setCallback(new Callback() {
            @Override
            public void invoke() {
                requestStackPop();
            }
        });

        GUIContainer.pack(playButton);
        GUIContainer.pack(settingsButton);
        GUIContainer.pack(exitButton);
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
