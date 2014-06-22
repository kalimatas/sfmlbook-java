package com.github.kalimatas.c05_States;

import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;
import org.jsfml.window.event.Event;

public class PauseState extends State {
    private Sprite backgroundSprite = new Sprite();
    private Text pausedText = new Text();
    private Text instructionText = new Text();

    public PauseState(StateStack stack, Context context) {
        super(stack, context);

        Font font = context.fonts.getFont(Fonts.MAIN);
        Vector2f viewSize = context.window.getView().getSize();

        pausedText.setFont(font);
        pausedText.setString("Game Paused");
        pausedText.setCharacterSize(70);
        Utility.centerOrigin(pausedText);
        pausedText.setPosition(0.5f * viewSize.x, 0.4f * viewSize.y);

        instructionText.setFont(font);
        instructionText.setString("Press Backspace to return to the main menu");
        Utility.centerOrigin(instructionText);
        instructionText.setPosition(0.5f * viewSize.x, 0.6f * viewSize.y);
    }

    @Override
    public void draw() {
        RenderWindow window = getContext().window;
        window.setView(window.getDefaultView());

        RectangleShape backgroundShape = new RectangleShape();
        backgroundShape.setFillColor(new Color(0, 0, 0, 150));
        backgroundShape.setSize(window.getView().getSize());

        window.draw(backgroundShape);
        window.draw(pausedText);
        window.draw(instructionText);
    }

    @Override
    public boolean update(Time dt) {
        return false;
    }

    @Override
    public boolean handleEvent(Event event) {
        if (event.type != Event.Type.KEY_PRESSED) {
            return false;
        }

        if (event.asKeyEvent().key == Keyboard.Key.ESCAPE) {
            // Escape pressed, remove to the game
            requestStackPop();
        }

        if (event.asKeyEvent().key == Keyboard.Key.BACKSPACE) {
            // Backspace pressed, remove itself to return to the game
            requestStateClear();
            requestStackPush(States.MENU);
        }

        return false;
    }
}
