package com.github.kalimatas.c10_Network;

import org.jsfml.window.Keyboard;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class KeyBinding {
    private Map<Keyboard.Key, Player.Action> keyMap = new HashMap<>();

    public KeyBinding(int controlPreconfiguration) {
        // Set initial key bindings for player 1
        if (controlPreconfiguration == 1) {
            keyMap.put(Keyboard.Key.LEFT, Player.Action.MOVE_LEFT);
            keyMap.put(Keyboard.Key.RIGHT, Player.Action.MOVE_RIGHT);
            keyMap.put(Keyboard.Key.DOWN, Player.Action.MOVE_DOWN);
            keyMap.put(Keyboard.Key.UP, Player.Action.MOVE_UP);
            keyMap.put(Keyboard.Key.SPACE, Player.Action.FIRE);
            keyMap.put(Keyboard.Key.M, Player.Action.LAUNCH_MISSILE);
        }
        else if (controlPreconfiguration == 2) {
            // Player 2
            keyMap.put(Keyboard.Key.A, Player.Action.MOVE_LEFT);
            keyMap.put(Keyboard.Key.D, Player.Action.MOVE_RIGHT);
            keyMap.put(Keyboard.Key.S, Player.Action.MOVE_DOWN);
            keyMap.put(Keyboard.Key.W, Player.Action.MOVE_UP);
            keyMap.put(Keyboard.Key.F, Player.Action.FIRE);
            keyMap.put(Keyboard.Key.R, Player.Action.LAUNCH_MISSILE);
        }
    }

    public void assignKey(Player.Action action, Keyboard.Key key) {
        // Remove all keys that already map to action
        keyMap.values().remove(action);

        // Insert new binding
        keyMap.put(key, action);
    }

    public Keyboard.Key getAssignedKey(Player.Action action) {
        for (Map.Entry<Keyboard.Key, Player.Action> pair : keyMap.entrySet()) {
            if (action.equals(pair.getValue())) {
                return pair.getKey();
            }
        }

        return Keyboard.Key.UNKNOWN;
    }

    public Player.Action checkAction(Keyboard.Key key) {
        // There are no references in Java, so the method is modified,
        // but the idea is the same.
        return keyMap.containsKey(key)
            ? keyMap.get(key)
            : null;
    }

    public LinkedList<Player.Action> getRealtimeActions() {
        // Return all realtime actions that are currently active.
        LinkedList<Player.Action> actions = new LinkedList<>();

        for (Map.Entry<Keyboard.Key, Player.Action> pair : keyMap.entrySet()) {
            // If key is pressed and an action is a realtime action, store it
            if (Keyboard.isKeyPressed(pair.getKey()) && isRealtimeAction(pair.getValue())) {
                actions.addLast(pair.getValue());
            }
        }

        return actions;
    }

    public boolean isRealtimeAction(Player.Action action) {
        switch (action) {
            case MOVE_LEFT:
            case MOVE_RIGHT:
            case MOVE_DOWN:
            case MOVE_UP:
            case FIRE:
                return true;
            default:
                return false;
        }
    }
}
