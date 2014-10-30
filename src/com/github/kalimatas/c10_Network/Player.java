package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.Network.Client;
import com.github.kalimatas.c10_Network.Network.Packet;
import com.github.kalimatas.c10_Network.Network.PacketReaderWriter;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.event.Event;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class Player {
    private final KeyBinding keyBinding;
    private Map<Action, Command> actionBinding = new HashMap<>();
    private Map<Action, Boolean> actionProxies = new HashMap<>();
    private MissionStatus currentMissionStatus = MissionStatus.MISSION_RUNNING;
    private Integer identifier;
    private SocketChannel socketChannel;

    public enum Action {
        MOVE_LEFT(0),
        MOVE_RIGHT(1),
        MOVE_UP(2),
        MOVE_DOWN(3),
        FIRE(4),
        LAUNCH_MISSILE(5),
        ACTION_COUNT(6);

        private int actionIndex;

        private Action(final int actionIndex) {
            this.actionIndex = actionIndex;
        }

        public static Action getAction(int actionIndex) {
            for (Action action : values()) {
                if (action.actionIndex == actionIndex) {
                    return action;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    public enum MissionStatus {
        MISSION_RUNNING,
        MISSION_SUCCESS,
        MISSION_FAILURE,
    }

    public Player(SocketChannel socketChannel, Integer identifier, final KeyBinding binding) {
        this.keyBinding = binding;
        this.identifier = identifier;
        this.socketChannel = socketChannel;

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
        // Event
        if (event.type == Event.Type.KEY_PRESSED) {
            Action action;
            if (keyBinding != null && (action = keyBinding.checkAction(event.asKeyEvent().key)) != null && !keyBinding.isRealtimeAction(action)) {
                // Network connected -> send event over network
                if (socketChannel != null) {
                    Packet packet = new Packet();
                    packet.append(Client.PacketType.PLAYER_EVENT);
                    packet.append(identifier);
                    packet.append(action);
                    try {
                        PacketReaderWriter.send(socketChannel, packet);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // Network disconnected -> local event
                else {
                    commands.push(actionBinding.get(action));
                }
            }
        }

        // Realtime change (network connected)
        if ((event.type == Event.Type.KEY_PRESSED || event.type == Event.Type.KEY_RELEASED) && socketChannel != null) {
            Action action;
            if (keyBinding != null && (action = keyBinding.checkAction(event.asKeyEvent().key)) != null && keyBinding.isRealtimeAction(action)) {
                // Send realtime change over network
                Packet packet = new Packet();
                packet.append(Client.PacketType.PLAYER_REALTIME_CHANGE);
                packet.append(identifier);
                packet.append(action);
                packet.append(event.type == Event.Type.KEY_PRESSED);
                try {
                    PacketReaderWriter.send(socketChannel, packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isLocal() {
        // No key binding means this player is remote
        return keyBinding != null;
    }

    public void disableAllRealtimeActions() {
        for (Map.Entry<Action, Boolean> action : actionProxies.entrySet()) {
            Packet packet = new Packet();
            packet.append(Client.PacketType.PLAYER_REALTIME_CHANGE);
            packet.append(identifier);
            packet.append(action.getKey());
            packet.append(false);
            try {
                PacketReaderWriter.send(socketChannel, packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void handleRealtimeInput(CommandQueue commands) {
        // Check if this is a networked game and local player or just a single player game
        if ((socketChannel != null && isLocal()) || socketChannel == null) {
            // Lookup all actions and push corresponding commands to queue
            LinkedList<Action> activeActions = keyBinding.getRealtimeActions();
            for (Action action : activeActions) {
                commands.push(actionBinding.get(action));
            }
        }
    }

    public void handleRealtimeNetworkInput(CommandQueue commands) {
        if (socketChannel != null && !isLocal()) {
            // Traverse all realtime input proxies. Because this is a networked game, the input isn't handled directly
            for (Map.Entry<Action, Boolean> pair : actionProxies.entrySet()) {
                if (pair.getValue() && keyBinding.isRealtimeAction(pair.getKey())) {
                    commands.push(actionBinding.get(pair.getKey()));
                }
            }
        }
    }

    public void handleNetworkEvent(Action action, CommandQueue commands) {
        commands.push(actionBinding.get(action));
    }

    public void handleNetworkRealtimeChange(Action action, boolean actionEnabled) {
        actionProxies.put(action, actionEnabled);
    }

    public void setMissionStatus(MissionStatus status) {
        currentMissionStatus = status;
    }

    public MissionStatus getMissionStatus() {
        return currentMissionStatus;
    }

    private void initializeActions() {
        {
            Command command = new Command();
            command.commandAction = new AircraftMover(-1, 0, identifier);
            actionBinding.put(Action.MOVE_LEFT, command);
        }

        {
            Command command = new Command();
            command.commandAction = new AircraftMover(+1, 0, identifier);
            actionBinding.put(Action.MOVE_RIGHT, command);
        }

        {
            Command command = new Command();
            command.commandAction = new AircraftMover(0, -1, identifier);
            actionBinding.put(Action.MOVE_UP, command);
        }

        {
            Command command = new Command();
            command.commandAction = new AircraftMover(0, +1, identifier);
            actionBinding.put(Action.MOVE_DOWN, command);
        }

        {
            Command command = new Command();
            command.commandAction = new AircraftFireTrigger(identifier);
            actionBinding.put(Action.FIRE, command);
        }

        {
            Command command = new Command();
            command.commandAction = new AircraftMissileTrigger(identifier);
            actionBinding.put(Action.LAUNCH_MISSILE, command);
        }
    }
}

class AircraftMover implements CommandAction<Aircraft> {
    private Vector2f velocity;
    private Integer aircraftId;

    AircraftMover(float vx, float vy, Integer identifier) {
        velocity = new Vector2f(vx, vy);
        aircraftId = identifier;
    }

    @Override
    public void invoke(Aircraft aircraft, Time dt) {
        if (aircraft.getIdentifier().equals(aircraftId)) {
            aircraft.accelerate(Vector2f.mul(velocity, aircraft.getMaxSpeed()));
        }
    }
}

class AircraftFireTrigger implements CommandAction<Aircraft> {
    private Integer aircraftId;

    AircraftFireTrigger(Integer identifier) {
        aircraftId = identifier;
    }

    @Override
    public void invoke(Aircraft aircraft, Time dt) {
        if (aircraft.getIdentifier().equals(aircraftId)) {
            aircraft.fire();
        }
    }
}

class AircraftMissileTrigger implements CommandAction<Aircraft> {
    private Integer aircraftId;

    AircraftMissileTrigger(Integer identifier) {
        aircraftId = identifier;
    }

    @Override
    public void invoke(Aircraft aircraft, Time dt) {
        if (aircraft.getIdentifier().equals(aircraftId)) {
            aircraft.launchingMissile();
        }
    }
}
