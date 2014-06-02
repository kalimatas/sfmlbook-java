package com.github.kalimatas.c04_Input;

import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;
import org.jsfml.window.event.Event;

public class Player {

    public void handleEvent(final Event event, CommandQueue commands) {
        if (event.type == Event.Type.KEY_PRESSED && event.asKeyEvent().key == Keyboard.Key.P) {
            Command output = new Command();
            output.category = Category.PLAYER_AIRCRAFT;
            output.action = new Action() {
                @Override
                public void invoke(SceneNode node, Time dt) {
                    System.out.println(node.getPosition().x + "," + node.getPosition().y);
                }
            };
            commands.push(output);
        }
    }

    public void handleRealtimeInput(CommandQueue commands) {
        final float playerSpeed = 30.f;

        if (Keyboard.isKeyPressed(Keyboard.Key.UP)) {
            Command moveUp = new Command();
            moveUp.category = Category.PLAYER_AIRCRAFT;
            moveUp.action = new AircraftMover(0.f, -playerSpeed);
            commands.push(moveUp);
        }
        if (Keyboard.isKeyPressed(Keyboard.Key.DOWN)) {
            Command moveDown = new Command();
            moveDown.category = Category.PLAYER_AIRCRAFT;
            moveDown.action = new AircraftMover(0.f, +playerSpeed);
            commands.push(moveDown);
        }
        if (Keyboard.isKeyPressed(Keyboard.Key.LEFT)) {
            Command moveLeft = new Command();
            moveLeft.category = Category.PLAYER_AIRCRAFT;
            moveLeft.action = new AircraftMover(-playerSpeed, 0.f);
            commands.push(moveLeft);
        }
        if (Keyboard.isKeyPressed(Keyboard.Key.RIGHT)) {
            Command moveRight = new Command();
            moveRight.category = Category.PLAYER_AIRCRAFT;
            moveRight.action = new AircraftMover(+playerSpeed, 0.f);
            commands.push(moveRight);
        }
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
