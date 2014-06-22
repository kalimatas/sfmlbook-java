package com.github.kalimatas.c05_States;

import org.jsfml.system.Time;
import org.jsfml.window.event.Event;

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

    public void registerState(States stateId) {
        // There is no way to create an object of a T type
        // in a generic metho, so I don't know how to implement this logic.
        // Instead we just create a new State with "createState" method.
    }

    public void update(Time dt) {
        ListIterator<State> it = stack.listIterator(stack.size());
        while (it.hasPrevious()) {
            if (!it.previous().update(dt)) {
                break;
            }
        }

        applyPendingChanges();
    }

    public void draw() {
        for (State state : stack) {
            state.draw();
        }
    }

    public void handleEvent(final Event event) {
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

    private State createState(States stateId) {
        switch (stateId) {
            case TITLE:
                return new TitleState(this, context);
            case MENU:
                return new MenuState(this, context);
            case LOADING:
                return new LoadingState(this, context);
            case GAME:
                return new GameState(this, context);
            case PAUSE:
                return new PauseState(this, context);
            default:
                throw new InvalidParameterException();
        }
    }

    private void applyPendingChanges() {
        for (PendingChange change : pendingList) {
            switch (change.action) {
                case PUSH:
                    stack.addLast(createState(change.stateId));
                    break;
                case POP:
                    stack.removeLast();
                    break;
                case CLEAR:
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
