package com.github.kalimatas.c07_Gameplay;

import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.event.Event;

public class GameOverState extends State {
    private Text gameOverText = new Text();
    private Time elapsedTime = Time.ZERO;

    public GameOverState(StateStack stack, Context context) {
        super(stack, context);

        Font font = context.fonts.getFont(Fonts.MAIN);
        Vector2f windowSize = new Vector2f(context.window.getSize());

        gameOverText.setFont(font);
        if (context.player.getMissionStatus() == Player.MissionStatus.MISSION_FAILURE) {
            gameOverText.setString("Mission failed!");
        } else {
            gameOverText.setString("Mission successful!");
        }

        gameOverText.setCharacterSize(70);
        Utility.centerOrigin(gameOverText);
        gameOverText.setPosition(0.5f * windowSize.x, 0.4f * windowSize.y);
    }

    @Override
    public void draw() {
        RenderWindow window = getContext().window;
        window.setView(window.getDefaultView());

        // Create dark, semitransparent background
        RectangleShape backgroundShape = new RectangleShape();
        backgroundShape.setFillColor(new Color(0, 0, 0, 150));
        backgroundShape.setSize(window.getView().getSize());

        window.draw(backgroundShape);
        window.draw(gameOverText);
    }

    @Override
    public boolean update(Time dt) {
        // Show state for 3 seconds, after return to menu
        elapsedTime = Time.add(elapsedTime, dt);
        if (elapsedTime.compareTo(Time.getSeconds(3)) > 0) {
            requestStateClear();
            requestStackPush(States.MENU);
        }

        return false;
    }

    @Override
    public boolean handleEvent(Event event) {
        return false;
    }
}
