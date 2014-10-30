package com.github.kalimatas.c10_Network;

import org.jsfml.graphics.TextureCreationException;
import org.jsfml.system.Time;
import org.jsfml.window.event.Event;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.ListIterator;

public class StateStack {
    private LinkedList<State> stack = new LinkedList<>();
    private LinkedList<PendingChange> pendingList = new LinkedList<>();
    private State.Context context;

    public enum Action {
        PUSH,
        POP,
        CLEAR,
    }

    public StateStack(State.Context context) {
        this.context = context;
    }

    public void update(Time dt) throws TextureCreationException, IOException {
        // Iterate from top to bottom, stop as soon as update() returns false
        ListIterator<State> it = stack.listIterator(stack.size());
        while (it.hasPrevious()) {
            if (!it.previous().update(dt)) {
                break;
            }
        }

        applyPendingChanges();
    }

    public void draw() throws TextureCreationException {
        // Draw all active states from bottom to top
        for (State state : stack) {
            state.draw();
        }
    }

    public void handleEvent(final Event event) throws TextureCreationException, IOException {
        // Iterate from top to bottom, stop as soon as handleEvent() returns false
        ListIterator<State> it = stack.listIterator(stack.size());
        while (it.hasPrevious()) {
            if (!it.previous().handleEvent(event)) {
                break;
            }
        }

        applyPendingChanges();
    }

    public void pushState(States stateId) {
        pendingList.addLast(new PendingChange(Action.PUSH, stateId));
    }

    public void popState() {
        pendingList.addLast(new PendingChange(Action.POP));
    }

    public void clearStates() {
        pendingList.addLast(new PendingChange(Action.CLEAR));
    }

    public boolean isEmpty() {
        return stack.isEmpty();
    }

    private State createState(States stateId) throws TextureCreationException, IOException {
        switch (stateId) {
            case TITLE:
                return new TitleState(this, context);
            case MENU:
                return new MenuState(this, context);
            case GAME:
                return new GameState(this, context);
            case HOST_GAME:
                return new MultiplayerGameState(this, context, true);
            case JOIN_GAME:
                return new MultiplayerGameState(this, context, false);
            case PAUSE:
                return new PauseState(this, context, false);
            case NETWORK_PAUSE:
                return new PauseState(this, context, true);
            case SETTINGS:
                return new SettingsState(this, context);
            case GAME_OVER:
                return new GameOverState(this, context, "Mission Failed!");
            case MISSION_SUCCESS:
                return new GameOverState(this, context, "Mission Successful!");
            default:
                throw new InvalidParameterException();
        }
    }

    private void applyPendingChanges() throws TextureCreationException, IOException {
        for (PendingChange change : pendingList) {
            switch (change.action) {
                case PUSH:
                    stack.addLast(createState(change.stateId));
                    break;
                case POP:
                    stack.peekLast().onDestroy();
                    stack.removeLast();

                    if (!stack.isEmpty()) {
                        stack.peekLast().onActivate();
                    }
                    break;
                case CLEAR:
                    for (State state : stack) {
                        state.onDestroy();
                    }

                    stack.clear();
                    break;
            }
        }

        pendingList.clear();
    }

    private class PendingChange {
        private Action action;
        private States stateId;

        PendingChange(Action action, States stateId) {
            this.action = action;
            this.stateId = stateId;
        }

        PendingChange(Action action) {
            this(action, States.NONE);
        }
    }
}
