package com.github.kalimatas.c05_States;

import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;
import org.jsfml.window.event.Event;

import java.util.LinkedList;

public class MenuState extends State {
    private Sprite backgroundSprite = new Sprite();
    private LinkedList<Text> options = new LinkedList<>();
    private int optionIndex = 0;

    private enum OptionNames {
        PLAY,
        EXIT,
    }

    public MenuState(StateStack stack, Context context) {
        super(stack, context);

        Texture texture = context.textures.getTexture(Textures.TITLE_SCREEN);
        Font font = context.fonts.getFont(Fonts.MAIN);

        backgroundSprite.setTexture(texture);

        // A simple menu demonstration
        Text playOption = new Text();
        playOption.setFont(font);
        playOption.setString("Play");
        Utility.centerOrigin(playOption);
        playOption.setPosition(Vector2f.div(context.window.getView().getSize(), 2.f));
        options.addLast(playOption);

        Text exitOption = new Text();
        exitOption.setFont(font);
        exitOption.setString("Exit");
        Utility.centerOrigin(exitOption);
        exitOption.setPosition(Vector2f.add(playOption.getPosition(), new Vector2f(0.f, 30.f)));
        options.addLast(exitOption);

        updateOptionText();
    }

    @Override
    public void draw() {
        RenderWindow window = getContext().window;

        window.setView(window.getDefaultView());
        window.draw(backgroundSprite);

        for (Text text : options) {
            window.draw(text);
        }
    }

    @Override
    public boolean update(Time dt) {
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        // The demonstration menu logic
        if (event.type != Event.Type.KEY_PRESSED) {
            return false;
        }

        if (event.asKeyEvent().key == Keyboard.Key.RETURN) {
            if (optionIndex == OptionNames.PLAY.ordinal()) {
                requestStackPop();
                requestStackPush(States.GAME);
                // replace the above line with the following to enable loading task
                //requestStackPush(States.LOADING);
            } else if (optionIndex == OptionNames.EXIT.ordinal()) {
                // The exit option was chosen, by removing itself, the stack will be empty, and the game will know it is time to close.
                requestStackPop();
            }
        }

        else if (event.asKeyEvent().key == Keyboard.Key.UP) {
            // Decrement and wrap-around
            if (optionIndex > 0) {
                optionIndex--;
            } else {
                optionIndex = options.size() - 1;
            }

            updateOptionText();
        }

        else if (event.asKeyEvent().key == Keyboard.Key.DOWN) {
            // Increment and wrap-around
            if (optionIndex < options.size() - 1) {
                optionIndex++;
            } else {
                optionIndex = 0;
            }

            updateOptionText();
        }

        return true;
    }

    public void updateOptionText() {
        if (options.isEmpty()) {
            return;
        }

        // White all texts
        for (Text text : options) {
            text.setColor(Color.WHITE);
        }

        // Red the selected text
        options.get(optionIndex).setColor(Color.RED);
    }
}
