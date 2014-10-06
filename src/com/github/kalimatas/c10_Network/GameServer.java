package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.Network.Server;
import org.jsfml.graphics.FloatRect;
import org.jsfml.system.Clock;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
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

    private Clock clock;
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

    public void setWaitingThreadEnd(boolean flag) {
        waitingThreadEnd = flag;
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

        while (!waitingThreadEnd) {
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
}
