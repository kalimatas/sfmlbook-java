package com.github.kalimatas.c05_States;

import org.jsfml.system.Time;
import org.jsfml.window.Keyboard;
import org.jsfml.window.event.Event;

public class GameState extends State {
    private World world;
    private Player player;

    public GameState(StateStack stack, Context context) {
        super(stack, context);

        world = new World(context.window);
        player = context.player;
    }

    @Override
    public void draw() {
        world.draw();
    }

    @Override
    public boolean update(Time dt) {
        world.update(dt);

        CommandQueue commands = world.getCommandQueue();
        player.handleRealtimeInput(commands);

        return true;
    }

    @Override
    public boolean handleEvent(Event event) {
        // Game input handling
        CommandQueue commands = world.getCommandQueue();
        player.handleEvent(event, commands);

        // Escape pressed, trigger the pause screen
        if (event.type == Event.Type.KEY_PRESSED && event.asKeyEvent().key == Keyboard.Key.ESCAPE) {
            requestStackPush(States.PAUSE);
        }

        return true;
    }
}
