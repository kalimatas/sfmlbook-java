package com.github.kalimatas.c05_States;

import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;
import org.jsfml.window.event.Event;

import java.util.HashMap;
import java.util.Map;

public class Player {
    private Map<Keyboard.Key, Action> keyBinding = new HashMap<>();
    private Map<Action, Command> actionBinding = new HashMap<>();

    public enum Action {
        MOVE_LEFT,
        MOVE_RIGHT,
        MOVE_UP,
        MOVE_DOWN,
        ACTION_COUNT,
    }

    public Player() {
        // Set initial key bindings
        keyBinding.put(Keyboard.Key.LEFT, Action.MOVE_LEFT);
        keyBinding.put(Keyboard.Key.RIGHT, Action.MOVE_RIGHT);
        keyBinding.put(Keyboard.Key.DOWN, Action.MOVE_DOWN);
        keyBinding.put(Keyboard.Key.UP, Action.MOVE_UP);

        // Set initial action bindings
        initializeActions();

        // Assign all categories to player's aircraft
        for (Map.Entry<Action, Command> pair : actionBinding.entrySet()) {
            Command value = pair.getValue();
            value.category = Category.PLAYER_AIRCRAFT;
            pair.setValue(value);
        }
    }

    public void handleEvent(final Event event, CommandQueue commands) {
        if (event.type == Event.Type.KEY_PRESSED) {
            // Check if pressed key appears in key binding, trigger command if so
            Action foundAction = keyBinding.get(event.asKeyEvent().key);
            if (foundAction != null && !isRealtimeAction(foundAction)) {
                commands.push(actionBinding.get(foundAction));
            }
        }
    }

    public void handleRealtimeInput(CommandQueue commands) {
        // Traverse all assigned keys and check if they are pressed
        for (Map.Entry<Keyboard.Key, Action> pair : keyBinding.entrySet()) {
            // If key is pressed, lookup action and trigger corresponding command
            if (Keyboard.isKeyPressed(pair.getKey()) && isRealtimeAction(pair.getValue())) {
                commands.push(actionBinding.get(pair.getValue()));
            }
        }
    }

    public void assignKey(Action action, Keyboard.Key key) {
        // Remove all keys that already map to action
        keyBinding.values().remove(action);

        // Insert new binding
        keyBinding.put(key, action);
    }

    public Keyboard.Key getAssignedKey(Action action) {
        for (Map.Entry<Keyboard.Key, Action> pair : keyBinding.entrySet()) {
            if (action.equals(pair.getValue())) {
                return pair.getKey();
            }
        }

        return Keyboard.Key.UNKNOWN;
    }

    private void initializeActions() {
        final float playerSpeed = 200.f;

        {
            Command command = new Command();
            command.commandAction = new AircraftMover(-playerSpeed, 0.f);
            actionBinding.put(Action.MOVE_LEFT, command);
        }

        {
            Command command = new Command();
            command.commandAction = new AircraftMover(playerSpeed, 0.f);
            actionBinding.put(Action.MOVE_RIGHT, command);
        }

        {
            Command command = new Command();
            command.commandAction = new AircraftMover(0.f, -playerSpeed);
            actionBinding.put(Action.MOVE_UP, command);
        }

        {
            Command command = new Command();
            command.commandAction = new AircraftMover(0.f, playerSpeed);
            actionBinding.put(Action.MOVE_DOWN, command);
        }
    }

    private boolean isRealtimeAction(Action action) {
        switch (action) {
            case MOVE_LEFT:
            case MOVE_RIGHT:
            case MOVE_DOWN:
            case MOVE_UP:
                return true;
            default:
                return false;
        }
    }
}

class AircraftMover implements CommandAction<Aircraft> {
    private Vector2f velocity;

    AircraftMover(float vx, float vy) {
        velocity = new Vector2f(vx, vy);
    }

    @Override
    public void invoke(Aircraft aircraft, Time dt) {
        aircraft.accelerate(velocity);
    }
}
