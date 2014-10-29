package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.Network.*;
import org.jsfml.graphics.FloatRect;
import org.jsfml.system.Clock;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.*;

public class GameServer {
    // A GameServerRemotePeer refers to one instance of the game, may it be local or from another computer
    private class RemotePeer {
        private SocketChannel socketChannel;
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
        private Map<Player.Action, Boolean> realtimeActions = new HashMap<>();
    }

    private Clock clock = new Clock();
    private ServerSocketChannel serverSocketChannel;
    private Selector readSelector;
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

        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), Server.SERVER_PORT));

        readSelector = Selector.open();

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

    public void notifyPlayerRealtimeChange(Integer aircraftIdentifier, Player.Action action, boolean actionEnabled) throws IOException {
        for (int i = 0; i < connectedPlayers; ++i) {
            if (peers.get(i).ready) {
                Packet packet = new Packet();
                packet.append(Server.PacketType.PLAYER_REALTIME_CHANGE);
                packet.append(aircraftIdentifier);
                packet.append(action);
                packet.append(actionEnabled);

                PacketReaderWriter.send(peers.get(i).socketChannel, packet);
            }
        }
    }

    public void notifyPlayerEvent(Integer aircraftIdentifier, Player.Action action) throws IOException {
        for (int i = 0; i < connectedPlayers; ++i) {
            if (peers.get(i).ready) {
                Packet packet = new Packet();
                packet.append(Server.PacketType.PLAYER_EVENT);
                packet.append(aircraftIdentifier);
                packet.append(action);

                PacketReaderWriter.send(peers.get(i).socketChannel, packet);
            }
        }
    }

    public void notifyPlayerSpawn(Integer aircraftIdentifier) throws IOException {
        for (int i = 0; i < connectedPlayers; ++i) {
            if (peers.get(i).ready) {
                Packet packet = new Packet();
                packet.append(Server.PacketType.PLAYER_CONNECT);
                packet.append(aircraftIdentifier);
                packet.append(aircraftInfo.get(aircraftIdentifier).position.x);
                packet.append(aircraftInfo.get(aircraftIdentifier).position.y);

                PacketReaderWriter.send(peers.get(i).socketChannel, packet);
            }
        }
    }

    public void setWaitingThreadEnd(boolean flag) {
        waitingThreadEnd = flag;
    }

    private void setListening(boolean enable) {
        listeningState = enable;
    }

    private void executionThread() throws IOException, InterruptedException {
        setListening(true);

        Time stepInterval = Time.getSeconds(1.f / 60.f);
        Time stepTime = Time.ZERO;
        Time tickInterval = Time.getSeconds(1.f / 20.f);
        Time tickTime = Time.ZERO;
        Clock stepClock = new Clock();
        Clock tickClock = new Clock();

        while (!waitingThreadEnd) {
            handleIncomingConnections();
            handleIncomingPackets();

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
            Thread.sleep(100);
        }
    }

    private void tick() throws IOException {
        updateClientState();

        // Check for mission success = all planes with position.y < offset
        boolean allAircraftsDone = true;
        for (Map.Entry<Integer, AircraftInfo> pair : aircraftInfo.entrySet()) {
            // As long as one player has not crossed the finish line yet, set variable to false
            if (pair.getValue().position.y > 0.f) {
                allAircraftsDone = false;
            }
        }

        if (allAircraftsDone) {
            Packet missionSuccessPacket = new Packet();
            missionSuccessPacket.append(Server.PacketType.MISSION_SUCCESS);
            sendToAll(missionSuccessPacket);
        }

        // Remove IDs of aircraft that have been destroyed (relevant if a client has two, and loses one)
        for (Iterator<Map.Entry<Integer, AircraftInfo>> itr = aircraftInfo.entrySet().iterator(); itr.hasNext(); ) {
            Map.Entry<Integer, AircraftInfo> entry = itr.next();
            if (entry.getValue().hitpoints < 0) {
                itr.remove();
            }
        }

        // Check if its time to attempt to spawn enemies
        if (now().compareTo(Time.add(timeForNextSpawn, lastSpawnTime)) >= 0) {
            // No more enemies are spawned near the end
            if (battleFieldRect.top > 600.f) {
                Random random = new Random();

                int enemyCount = 1 + random.nextInt(2);
                float spawnCenter = random.nextInt(500) - 250;

                // In case only one enemy is being spawned, it appears directly at the spawnCenter
                float planeDistance = 0.f;
                float nextSpawnPosition = spawnCenter;

                // In case there are two enemies being spawned together, each is spawned at each side of the spawnCenter, with a minimum distance
                if (enemyCount == 2) {
                    planeDistance = 150 + random.nextInt(250);
                    nextSpawnPosition = spawnCenter - planeDistance / 2.f;
                }

                // Send the spawn orders to all clients
                for (int i = 0; i < enemyCount; i++) {
                    Packet packet = new Packet();
                    packet.append(Server.PacketType.SPAWN_ENEMY);
                    packet.append(Aircraft.Type.getRandomEnemy());
                    packet.append(worldHeight - battleFieldRect.top + 500);
                    packet.append(nextSpawnPosition);

                    nextSpawnPosition += planeDistance / 2.f;

                    sendToAll(packet);
                }

                lastSpawnTime = now();
                timeForNextSpawn = Time.getMilliseconds(2000 + new Random().nextInt(6000));
            }
        }
    }

    private Time now() {
        return clock.getElapsedTime();
    }

    private RemotePeer getClientByChannel(SocketChannel channel) {
        for (RemotePeer peer : peers) {
            if (peer.socketChannel.equals(channel)) {
                return peer;
            }
        }
        return null;
    }

    private void handleIncomingPackets() throws IOException {
        boolean detectedTimeout = false;

        readSelector.selectNow();

        Set<SelectionKey> readKeys = readSelector.selectedKeys();
        Iterator<SelectionKey> it = readKeys.iterator();

        while (it.hasNext()) {
            SelectionKey key = it.next();
            it.remove();

            SocketChannel channel = (SocketChannel) key.channel();
            // We don't know what peer's channel this is, so let's find out
            RemotePeer peer = getClientByChannel(channel);

            if (!peer.ready) {
                continue;
            }

            Packet packet = null;
            try {
                packet = PacketReaderWriter.receive(channel);
            } catch (NothingToReadException e) {
                e.printStackTrace();
            }

            if (packet != null) {
                // Interpret packet and react to it
                detectedTimeout = handleIncomingPacket(packet, peer);

                // Packet was indeed received, update the ping timer
                peer.lastPacketTime = now();
            }

            if (now().compareTo(Time.add(peer.lastPacketTime, clientTimeoutTime)) >= 0) {
                peer.timedOut = true;
                detectedTimeout = true;
            }
        }

        if (detectedTimeout) {
            handleDisconnections();
        }
    }

    private boolean handleIncomingPacket(Packet packet, RemotePeer receivingPeer) throws IOException {
        Client.PacketType packetType = (Client.PacketType) packet.get();

        Integer aircraftIdentifier;
        int aircraftHitpoints;
        int missileAmmo;
        Vector2f aircraftPosition;
        Player.Action action;
        AircraftInfo ai;
        boolean detectedTimeout = false;

        switch (packetType) {
            case QUIT:
                receivingPeer.timedOut = true;
                detectedTimeout = true;
                break;

            case PLAYER_EVENT:
                aircraftIdentifier = (Integer) packet.get();
                action = (Player.Action) packet.get();

                notifyPlayerEvent(aircraftIdentifier, action);
                break;

            case PLAYER_REALTIME_CHANGE:
                aircraftIdentifier = (Integer) packet.get();
                action = (Player.Action) packet.get();
                boolean actionEnabled = (boolean) packet.get();
                aircraftInfo.get(aircraftIdentifier).realtimeActions.put(action, actionEnabled);
                notifyPlayerRealtimeChange(aircraftIdentifier, action, actionEnabled);
                break;

            case REQUEST_COOP_PARTNER:
                receivingPeer.aircraftIdentifiers.addLast(aircraftIdentifierCounter);

                ai = new AircraftInfo();
                ai.position = new Vector2f(battleFieldRect.width / 2, battleFieldRect.top + battleFieldRect.height / 2);
                ai.hitpoints = 100;
                ai.missileAmmo = 2;
                aircraftInfo.put(aircraftIdentifierCounter, ai);

                Packet requestPacket = new Packet();
                requestPacket.append(Server.PacketType.ACCEPT_COOP_PARTNER);
                requestPacket.append(aircraftIdentifierCounter);
                requestPacket.append(aircraftInfo.get(aircraftIdentifierCounter).position.x);
                requestPacket.append(aircraftInfo.get(aircraftIdentifierCounter).position.y);

                PacketReaderWriter.send(receivingPeer.socketChannel, requestPacket);
                aircraftCount++;

                // Inform every other peer about this new plane
                for (RemotePeer peer : peers) {
                    if (!peer.equals(receivingPeer) && peer.ready) {
                        Packet notifyPacket = new Packet();
                        notifyPacket.append(Server.PacketType.PLAYER_CONNECT);
                        notifyPacket.append(aircraftIdentifierCounter);
                        notifyPacket.append(aircraftInfo.get(aircraftIdentifierCounter).position.x);
                        notifyPacket.append(aircraftInfo.get(aircraftIdentifierCounter).position.y);
                        PacketReaderWriter.send(peer.socketChannel, notifyPacket);
                    }
                }
                aircraftIdentifierCounter++;
                break;

            case POSITION_UPDATE:
                int numAircrafts = (int) packet.get();
                for (int i = 0; i < numAircrafts; i++) {
                    aircraftIdentifier = (Integer) packet.get();
                    aircraftPosition = new Vector2f((float) packet.get(), (float) packet.get());
                    aircraftHitpoints = (int) packet.get();
                    missileAmmo = (int) packet.get();
                    aircraftInfo.get(aircraftIdentifier).position = aircraftPosition;
                    aircraftInfo.get(aircraftIdentifier).hitpoints = aircraftHitpoints;
                    aircraftInfo.get(aircraftIdentifier).missileAmmo = missileAmmo;
                }
                break;

            case GAME_EVENT:
                GameActions.Type actionType = (GameActions.Type) packet.get();
                float x = (float) packet.get();
                float y = (float) packet.get();

                // Enemy explodes: With certain probability, drop pickup
                // To avoid multiple messages spawning multiple pickups, only listen to first peer (host)
                if (actionType == GameActions.Type.ENEMY_EXPLODE && new Random().nextInt(3) == 0 && receivingPeer.equals(peers.get(0))) {
                    Packet actionPacket = new Packet();
                    actionPacket.append(Server.PacketType.SPAWN_PICKUP);
                    actionPacket.append(Pickup.Type.getRandom());
                    actionPacket.append(x);
                    actionPacket.append(y);

                    sendToAll(actionPacket);
                }

                break;
        }

        return detectedTimeout;
    }

    private void updateClientState() throws IOException {
        Packet updateClientStatePacket = new Packet();
        updateClientStatePacket.append(Server.PacketType.UPDATE_CLIENT_STATE);
        updateClientStatePacket.append(battleFieldRect.top + battleFieldRect.height);
        updateClientStatePacket.append(aircraftInfo.size());

        for (Map.Entry<Integer, AircraftInfo> aircraft : aircraftInfo.entrySet()) {
            updateClientStatePacket.append(aircraft.getKey());
            updateClientStatePacket.append(aircraft.getValue().position.x);
            updateClientStatePacket.append(aircraft.getValue().position.y);
        }

        sendToAll(updateClientStatePacket);
    }

    private void handleIncomingConnections() throws IOException {
        if (!listeningState) {
            return;
        }

        SocketChannel clientChannel;
        while ((clientChannel = serverSocketChannel.accept()) != null) {
            clientChannel.configureBlocking(false);
            clientChannel.register(readSelector, SelectionKey.OP_READ);

            RemotePeer peer = peers.get(connectedPlayers);
            peer.socketChannel = clientChannel;

            // order the new client to spawn its own plane ( player 1 )
            AircraftInfo ai = new AircraftInfo();
            ai.position = new Vector2f(battleFieldRect.width / 2, battleFieldRect.top + battleFieldRect.height / 2);
            ai.hitpoints = 100;
            ai.missileAmmo = 2;
            aircraftInfo.put(aircraftIdentifierCounter, ai);

            Packet packet = new Packet();
            packet.append(Server.PacketType.SPAWN_SELF);
            packet.append(aircraftIdentifierCounter);
            packet.append(ai.position.x);
            packet.append(ai.position.y);

            peer.aircraftIdentifiers.addLast(aircraftIdentifierCounter);

            broadcastMessage("New player!");
            informWorldState(peer.socketChannel);
            notifyPlayerSpawn(aircraftIdentifierCounter++);
            aircraftIdentifierCounter++;

            PacketReaderWriter.send(peer.socketChannel, packet);
            peer.ready = true;
            peer.lastPacketTime = now(); // prevent initial timeouts
            connectedPlayers++;
            aircraftCount++;

            if (connectedPlayers >= maxConnectedPlayers) {
                setListening(false);
            } else {
                // Add a new waiting peer
                peers.addLast(new RemotePeer());
            }
        }
    }

    private void handleDisconnections() throws IOException {
        for (Iterator<RemotePeer> itr = peers.iterator(); itr.hasNext(); ) {
            RemotePeer peer = itr.next();
            if (peer.timedOut) {
                // Inform everyone of the disconnection, erase
                for (Integer identifier : peer.aircraftIdentifiers) {
                    Packet packet = new Packet();
                    packet.append(Server.PacketType.PLAYER_DISCONNECT);
                    packet.append(identifier);
                    sendToAll(packet);

                    aircraftInfo.remove(identifier);
                }

                connectedPlayers--;
                aircraftCount -= peer.aircraftIdentifiers.size();

                itr.remove();

                // Go back to a listening state if needed
                if (connectedPlayers < maxConnectedPlayers) {
                    peers.addLast(new RemotePeer());
                    setListening(true);
                }

                broadcastMessage("An ally has disconnected.");
            }
        }
    }

    // Tell the newly connected peer about how the world is currently
    private void informWorldState(SocketChannel channel) throws IOException {
        Packet packet = new Packet();
        packet.append(Server.PacketType.INITIAL_STATE);
        packet.append(worldHeight);
        packet.append(battleFieldRect.top + battleFieldRect.height);
        packet.append(aircraftCount);

        for (int i = 0; i < connectedPlayers; ++i) {
            if (peers.get(i).ready) {
                for (Integer identifier : peers.get(i).aircraftIdentifiers) {
                    packet.append(identifier);
                    packet.append(aircraftInfo.get(identifier).position.x);
                    packet.append(aircraftInfo.get(identifier).position.y);
                    packet.append(aircraftInfo.get(identifier).hitpoints);
                    packet.append(aircraftInfo.get(identifier).missileAmmo);
                }
            }
        }

        PacketReaderWriter.send(channel, packet);
    }

    private void broadcastMessage(final String message) throws IOException {
        for (int i = 0; i < connectedPlayers; ++i) {
            if (peers.get(i).ready) {
                Packet packet = new Packet();
                packet.append(Server.PacketType.BROADCAST_MESSAGE);
                packet.append(message);

                PacketReaderWriter.send(peers.get(i).socketChannel, packet);
            }
        }
    }

    private void sendToAll(Packet packet) throws IOException {
        for (RemotePeer peer : peers) {
            if (peer.ready) {
                PacketReaderWriter.send(peer.socketChannel, packet);
            }
        }
    }
}
