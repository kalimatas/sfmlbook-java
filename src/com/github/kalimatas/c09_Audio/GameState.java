package com.github.kalimatas.c09_Audio;

import org.jsfml.graphics.TextureCreationException;
import org.jsfml.system.Time;
import org.jsfml.window.Keyboard;
import org.jsfml.window.event.Event;

public class GameState extends State {
    private World world;
    private Player player;

    public GameState(StateStack stack, Context context) throws TextureCreationException {
        super(stack, context);

        world = new World(context.window, context.fonts, context.sounds);
        player = context.player;
        player.setMissionStatus(Player.MissionStatus.MISSION_RUNNING);

        // Play game theme
        context.music.play(Musics.MISSION_THEME);
    }

    @Override
    public void draw() throws TextureCreationException {
        world.draw();
    }

    @Override
    public boolean update(Time dt) {
        world.update(dt);

        if (!world.hasAlivePlayer()) {
            player.setMissionStatus(Player.MissionStatus.MISSION_FAILURE);
            requestStackPush(States.GAME_OVER);
        } else if (world.hasPlayerReachedEnd()) {
            player.setMissionStatus(Player.MissionStatus.MISSION_SUCCESS);
            requestStackPush(States.GAME_OVER);
        }

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
