package com.github.kalimatas.c05_States;

import org.jsfml.graphics.*;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.event.Event;

public class LoadingState extends State {
    private Text loadingText = new Text();
    private RectangleShape progressBarBackground = new RectangleShape();
    private RectangleShape progressBar = new RectangleShape();
    private ParallelTask loadingTask = new ParallelTask();

    public LoadingState(StateStack stack, Context context) {
        super(stack, context);

        RenderWindow window = getContext().window;
        Font font = context.fonts.getFont(Fonts.MAIN);
        Vector2f viewSize = window.getView().getSize();

        loadingText.setFont(font);
        loadingText.setString("Loading Resources");
        Utility.centerOrigin(loadingText);
        loadingText.setPosition(viewSize.x / 2.f, viewSize.y / 2.f + 50.f);

        progressBarBackground.setFillColor(Color.WHITE);
        progressBarBackground.setSize(new Vector2f(viewSize.x - 20, 10));
        progressBarBackground.setPosition(10, loadingText.getPosition().y + 40);

        progressBar.setFillColor(new Color(100, 100, 100));
        progressBar.setSize(new Vector2f(200, 10));
        progressBar.setPosition(10, loadingText.getPosition().y + 40);

        setCompletion(0.f);

        loadingTask.execute();
    }

    @Override
    public void draw() {
        RenderWindow window = getContext().window;

        window.setView(window.getDefaultView());

        window.draw(loadingText);
        window.draw(progressBarBackground);
        window.draw(progressBar);
    }

    @Override
    public boolean update(Time dt) {
        // Update the progress bar from the remote task or finish it
        if (loadingTask.isFinished()) {
            requestStackPop();
            requestStackPush(States.GAME);
        } else {
            setCompletion(loadingTask.getCompletion());
        }
        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        return true;
    }

    public void setCompletion(float percent) {
        if (percent > 1.f) { // clamp
            percent = 1.f;
        }

        progressBar.setSize(new Vector2f(progressBarBackground.getSize().x * percent, progressBar.getSize().y));
    }
}
