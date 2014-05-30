package com.github.kalimatas.c04_Input;

import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.event.Event;

public class Player {

    public void handleEvent(final Event event, CommandQueue commands) {

    }

    public void handleRealtimeInput(CommandQueue commands) {

    }
}

class AircraftMover implements Action {
    private Vector2f velocity;

    AircraftMover(float vx, float vy) {
        velocity = new Vector2f(vx, vy);
    }

    @Override
    public void invoke(SceneNode node, Time dt) {
        Aircraft aircraft = (Aircraft)node;
        aircraft.accelerate(velocity);
    }
}
