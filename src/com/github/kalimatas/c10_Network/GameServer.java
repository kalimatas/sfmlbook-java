package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.Network.Packet;
import com.github.kalimatas.c10_Network.Network.Server;
import org.jsfml.graphics.FloatRect;
import org.jsfml.system.Clock;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class GameServer {
    // A GameServerRemotePeer refers to one instance of the game, may it be local or from another computer
    private class RemotePeer {
        private Socket socket;
        private Time lastPacketTime;
        private LinkedList<Integer> aircraftIdentifiers = new LinkedList<>();
        private boolean ready;
        private boolean timedOut;
    }

    // Structure to store information about current aircraft state
    private class AircraftInfo {
        private Vector2f position;
        private Integer hitpoints;
        private Integer missileAmmo;
        private Map<Integer, Boolean> realtimeActions = new HashMap<>();
    }

    private Clock clock = new Clock();
    private ServerSocketChannel channel;
    private ServerSocket listenerSocket;
    private boolean listeningState = false;
    private Time clientTimeoutTime = Time.getSeconds(3.f);

    private int maxConnectedPlayers = 10;
    private int connectedPlayers = 0;

    private float worldHeight = 5000.f;
    private FloatRect battleFieldRect;
    private float battleFieldScrollSpeed = -50.f;

    private int aircraftCount = 0;
    private Map<Integer, AircraftInfo> aircraftInfo = new HashMap<>();

    private LinkedList<RemotePeer> peers = new LinkedList<>(Arrays.asList(new RemotePeer()));
    private Integer aircraftIdentifierCounter = 1;
    private volatile boolean waitingThreadEnd = false;

    private Time lastSpawnTime = Time.ZERO;
    private Time timeForNextSpawn = Time.getSeconds(5.f);

    public GameServer(Vector2f battlefieldSize) throws IOException {
        battleFieldRect = new FloatRect(0.f, worldHeight - battlefieldSize.y, battlefieldSize.x, battlefieldSize.y);

        openChannel();

        (new Thread() {
            public void run() {
                try {
                    executionThread();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void notifyPlayerRealtimeChange(Integer aircraftIdentifier, Player.Action action, boolean actionEnabled) {
        for (int i = 0; i < connectedPlayers; ++i) {
            if (peers.get(i).ready) {
                Packet packet = new Packet();
                packet.append(Server.PacketType.PLAYER_REALTIME_CHANGE);
                packet.append(aircraftIdentifier);
                packet.append(action);
                packet.append(actionEnabled);

                sendPacket(peers.get(i).socket, packet);
            }
        }
    }

    public void notifyPlayerEvent(Integer aircraftIdentifier, Player.Action action) {
        for (int i = 0; i < connectedPlayers; ++i) {
            if (peers.get(i).ready) {
                Packet packet = new Packet();
                packet.append(Server.PacketType.PLAYER_EVENT);
                packet.append(aircraftIdentifier);
                packet.append(action);

                sendPacket(peers.get(i).socket, packet);
            }
        }
    }

    public void notifyPlayerSpawn(Integer aircraftIdentifier) {
        for (int i = 0; i < connectedPlayers; ++i) {
            if (peers.get(i).ready) {
                Packet packet = new Packet();
                packet.append(Server.PacketType.PLAYER_CONNECT);
                packet.append(aircraftIdentifier);
                packet.append(aircraftInfo.get(aircraftIdentifier).position.x);
                packet.append(aircraftInfo.get(aircraftIdentifier).position.y);

                sendPacket(peers.get(i).socket, packet);
            }
        }
    }

    public void setWaitingThreadEnd(boolean flag) {
        waitingThreadEnd = flag;
    }

    private void sendPacket(Socket socket, Packet packet) {
        System.out.println("sending packet...");

        try {
            new ObjectOutputStream(socket.getOutputStream()).writeObject(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openChannel() throws IOException {
        channel = ServerSocketChannel.open();
        channel.configureBlocking(false);
        listenerSocket = channel.socket();
    }

    private void setListening(boolean enable) throws IOException {
        // Check if it isn't already listening
        if (enable) {
            if (!listeningState) {
                openChannel();
                channel.bind(new InetSocketAddress(Server.SERVER_PORT));
                listeningState = true;
            }
        } else {
            listenerSocket.close();
            listeningState = false;
        }
    }

    private void executionThread() throws IOException {
        setListening(true);

        Time stepInterval = Time.getSeconds(1.f / 60.f);
        Time stepTime = Time.ZERO;
        Time tickInterval = Time.getSeconds(1.f / 20.f);
        Time tickTime = Time.ZERO;
        Clock stepClock = new Clock();
        Clock tickClock = new Clock();

        while (!waitingThreadEnd) {
            handleIncomingPackets();
            handleIncomingConnections();

            stepTime = Time.add(stepTime, stepClock.getElapsedTime());
            stepClock.restart();

            tickTime = Time.add(tickTime, tickClock.getElapsedTime());
            tickClock.restart();

            // Fixed update step
            while (stepTime.compareTo(stepInterval) >= 0) {
                battleFieldRect = new FloatRect(battleFieldRect.left, battleFieldRect.top + battleFieldScrollSpeed * stepInterval.asSeconds(), battleFieldRect.width, battleFieldRect.height);
                stepTime = Time.sub(stepTime, stepInterval);
            }

            // Fixed tick step
            while (tickTime.compareTo(tickInterval) >= 0) {
                tick();
                tickTime = Time.sub(tickTime, tickInterval);
            }

            // Sleep to prevent server from consuming 100% CPU
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void tick() {
        // todo
    }

    private Time now() {
        return clock.getElapsedTime();
    }

    private void handleIncomingPackets() {
        // todo
    }

    private void handleIncomingPacket(Packet packet, RemotePeer receivingPeer, boolean detectedTimeout) {
        // todo
    }

    private void updateClientState() {
        // todo
    }

    private void handleIncomingConnections() {
        // todo
    }

    private void handleDisconnections() {
        // todo
    }

    // Tell the newly connected peer about how the world is currently
    private void informWorldState(Socket socket) {
        // todo
    }

    private void broadcastMessage(final String message) {
        // todo
    }

    private void sendToAll(Packet packet) {
        // todo
    }
}
