package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.Network.*;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Text;
import org.jsfml.graphics.TextureCreationException;
import org.jsfml.system.Clock;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.Keyboard;
import org.jsfml.window.event.Event;

import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MultiplayerGameState extends State {
    private class Helper {
        private InetAddress getAddressFromFile() throws IOException {
            // Try to open existing file (RAII block)
            File file = new File("ip.txt");
            if (file.isFile() && file.exists()) {
                try (BufferedReader reader = Files.newBufferedReader(Paths.get(file.getPath()), Charset.defaultCharset())) {
                    return InetAddress.getByName(reader.readLine());
                }
            }

            // If open/read failed, create new file
            try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.getPath()), Charset.defaultCharset())) {
                InetAddress ip = InetAddress.getLoopbackAddress();
                writer.write(ip.getHostAddress());
                return ip;
            }
        }
    }

    private World world;
    private RenderWindow window;
    private ResourceHolder textureHolder;

    private Map<Integer, Player> players = new HashMap<>();
    private LinkedList<Integer> localPlayerIdentifiers = new LinkedList<>();
    private SocketChannel socketChannel;
    private Selector readSelector;
    private boolean connected = false;
    private GameServer gameServer;
    private Clock tickClock = new Clock();

    private LinkedList<String> broadcasts = new LinkedList<>();
    private Text broadcastText = new Text();
    private Time broadcastElapsedTime = Time.ZERO;

    private Text playerInvitationText = new Text();
    private Time playerInvitationTime = Time.ZERO;

    private Text failedConnectionText = new Text();
    private Clock failedConnectionClock = new Clock();

    private boolean activeState = true;
    private boolean hasFocus = true;
    private boolean host;
    private boolean gameStarted = false;
    private Time clientTimeout = Time.getSeconds(2.f);
    private Time timeSinceLastPacket = Time.getSeconds(0.f);

    public MultiplayerGameState(StateStack stack, Context context, boolean isHost) throws TextureCreationException, IOException {
        super(stack, context);

        this.world = new World(context.window, context.fonts, context.sounds, true);
        this.window = context.window;
        this.textureHolder = context.textures;
        this.host = isHost;

        broadcastText.setFont(context.fonts.getFont(Fonts.MAIN));
        broadcastText.setPosition(1024.f / 2, 100.f);

        playerInvitationText.setFont(context.fonts.getFont(Fonts.MAIN));
        playerInvitationText.setCharacterSize(20);
        playerInvitationText.setColor(Color.WHITE);
        playerInvitationText.setString("Press Enter to spawn player 2");
        playerInvitationText.setPosition(1000 - playerInvitationText.getLocalBounds().width, 760 - playerInvitationText.getLocalBounds().height);

        // We reuse this text for "Attempt to connect" and "Failed to connect" messages
        failedConnectionText.setFont(context.fonts.getFont(Fonts.MAIN));
        failedConnectionText.setString("Attempting to connect...");
        failedConnectionText.setCharacterSize(35);
        failedConnectionText.setColor(Color.WHITE);
        Utility.centerOrigin(failedConnectionText);
        failedConnectionText.setPosition(window.getSize().x / 2.f, window.getSize().y / 2.f);

        // Render a "establishing connection" frame for user feedback
        window.clear(Color.BLACK);
        window.draw(failedConnectionText);
        window.display();
        failedConnectionText.setString("Could not connect to the remote server!");
        Utility.centerOrigin(failedConnectionText);

        InetAddress ip;
        if (isHost) {
            gameServer = new GameServer(new Vector2f(window.getSize()));
            ip = InetAddress.getLoopbackAddress();
        } else {
            ip = new Helper().getAddressFromFile();
        }

        try {
            socketChannel = SocketChannel.open(new InetSocketAddress(ip, Server.SERVER_PORT));
            socketChannel.configureBlocking(false);

            readSelector = Selector.open();
            socketChannel.register(readSelector, SelectionKey.OP_READ);
            connected = true;
        } catch (IOException e) {
            failedConnectionClock.restart();
        }

        // Play game theme
        context.music.play(Musics.MISSION_THEME);
    }

    @Override
    public void draw() throws TextureCreationException {
        if (connected) {
            world.draw();

            // Broadcast messages in default view
            window.setView(window.getDefaultView());

            if (!broadcasts.isEmpty()) {
                window.draw(broadcastText);
            }

            if (localPlayerIdentifiers.size() < 2 && playerInvitationTime.compareTo(Time.getSeconds(0.5f)) < 0) {
                window.draw(playerInvitationText);
            }
        } else {
            window.draw(failedConnectionText);
        }
    }

    public void onActivate() {
        activeState = true;
    }

    public void onDestroy() {
        if (host && connected) {
            // Inform server this client is dying
            Packet packet = new Packet();
            packet.append(Client.PacketType.QUIT);
            try {
                PacketReaderWriter.send(socketChannel, packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean update(Time dt) {
        // Connected to server: Handle all the network logic
        if (connected) {
            world.update(dt);

            // Remove players whose aircrafts were destroyed
            boolean foundLocalPlane = false;
            for (Iterator<Map.Entry<Integer, Player>> itr = players.entrySet().iterator(); itr.hasNext(); ) {
                Map.Entry<Integer, Player> entry = itr.next();
                // Check if there are no more local planes for remote clients
                if (localPlayerIdentifiers.contains(entry.getKey())) {
                    foundLocalPlane = true;
                }

                if (world.getAircraft(entry.getKey()) == null) {
                    itr.remove();

                    // No more players left: Mission failed
                    if (players.isEmpty()) {
                        requestStackPush(States.GAME_OVER);
                    }
                }
            }

            if (!foundLocalPlane && gameStarted) {
                requestStackPush(States.GAME_OVER);
            }

            // Only handle the realtime input if the window has focus and the game is unpaused
            if (activeState && hasFocus) {
                CommandQueue commands = world.getCommandQueue();
                for (Map.Entry<Integer, Player> pair : players.entrySet()) {
                    pair.getValue().handleRealtimeInput(commands);
                }
            }

            // Always handle the network input
            CommandQueue commands = world.getCommandQueue();
            for (Map.Entry<Integer, Player> pair : players.entrySet()) {
                pair.getValue().handleRealtimeNetworkInput(commands);
            }

            // Handle messages from server that may have arrived
            try {
                readSelector.selectNow();

                Set<SelectionKey> readKeys = readSelector.selectedKeys();
                Iterator<SelectionKey> it = readKeys.iterator();

                while (it.hasNext()) {
                    SelectionKey key = it.next();
                    it.remove();

                    SocketChannel channel = (SocketChannel) key.channel();

                    Packet packet = null;
                    try {
                        packet = PacketReaderWriter.receive(channel);
                    } catch (NothingToReadException e) {
                        connected = false;
                        channel.close();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (packet != null) {
                        timeSinceLastPacket = Time.ZERO;
                        Server.PacketType packetType = (Server.PacketType) packet.get();
                        handlePacket(packetType, packet);
                    } else {
                        // Check for timeout with the server
                        if (timeSinceLastPacket.compareTo(clientTimeout) > 0) {
                            connected = false;

                            failedConnectionText.setString("Lost connection to server");
                            Utility.centerOrigin(failedConnectionText);

                            failedConnectionClock.restart();
                        }
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            updateBroadcastMessage(dt);

            // Time counter for blinking 2nd player text
            playerInvitationTime = Time.add(playerInvitationTime, dt);
            if (playerInvitationTime.compareTo(Time.getSeconds(1.f)) > 0) {
                playerInvitationTime = Time.ZERO;
            }

            // Events occurring in the game
            GameActions.Action gameAction;
            while ((gameAction = world.pollGameAction()) != null) {
                Packet packet = new Packet();
                packet.append(Client.PacketType.GAME_EVENT);
                packet.append(gameAction.type);
                packet.append(gameAction.position.x);
                packet.append(gameAction.position.y);

                try {
                    PacketReaderWriter.send(socketChannel, packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Regular position updates
            if (tickClock.getElapsedTime().compareTo(Time.getSeconds(1.f / 20.f)) > 0) {
                Packet positionUpdatePacket = new Packet();
                positionUpdatePacket.append(Client.PacketType.POSITION_UPDATE);
                positionUpdatePacket.append(localPlayerIdentifiers.size());

                Aircraft aircraft;
                for (Integer identifier : localPlayerIdentifiers) {
                    if ((aircraft = world.getAircraft(identifier)) != null) {
                        positionUpdatePacket.append(identifier);
                        positionUpdatePacket.append(aircraft.getPosition().x);
                        positionUpdatePacket.append(aircraft.getPosition().y);
                        positionUpdatePacket.append(aircraft.getHitpoints());
                        positionUpdatePacket.append(aircraft.getMissileAmmo());
                    }
                }

                try {
                    PacketReaderWriter.send(socketChannel, positionUpdatePacket);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                tickClock.restart();
            }

            timeSinceLastPacket = Time.add(timeSinceLastPacket, dt);
        }

        // Failed to connect and waited for more than 5 seconds: Back to menu
        else if (failedConnectionClock.getElapsedTime().compareTo(Time.getSeconds(5.f)) >= 0) {
            requestStateClear();
            requestStackPush(States.MENU);
        }

        return true;
    }

    public void disableAllRealtimeActions() {
        activeState = false;

        for (Integer identifier : localPlayerIdentifiers) {
            players.get(identifier).disableAllRealtimeActions();
        }
    }

    @Override
    public boolean handleEvent(Event event) {
        // Stop game server on window close
        if (event.type == Event.Type.CLOSED && host) {
            gameServer.setWaitingThreadEnd(true);
            return true;
        }

        // Game input handling
        CommandQueue commands = world.getCommandQueue();

        // Forward event to all players
        for (Map.Entry<Integer, Player> pair : players.entrySet()) {
            pair.getValue().handleEvent(event, commands);
        }

        if (event.type == Event.Type.KEY_PRESSED) {
            // Enter pressed, add second player co-op (only if we are one player)
            if (event.asKeyEvent().key == Keyboard.Key.RETURN && localPlayerIdentifiers.size() == 1) {
                Packet packet = new Packet();
                packet.append(Client.PacketType.REQUEST_COOP_PARTNER);

                try {
                    PacketReaderWriter.send(socketChannel, packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            // Escape pressed, trigger the pause screen
            else if (event.asKeyEvent().key == Keyboard.Key.ESCAPE) {
                disableAllRealtimeActions();
                requestStackPush(States.NETWORK_PAUSE);
            }
        } else if (event.type == Event.Type.GAINED_FOCUS) {
            hasFocus = true;
        } else if (event.type == Event.Type.LOST_FOCUS) {
            hasFocus = false;
        }

        return true;
    }

    private void updateBroadcastMessage(Time elapsedTime) {
        if (broadcasts.isEmpty()) {
            return;
        }

        // Update broadcast timer
        broadcastElapsedTime = Time.add(broadcastElapsedTime, elapsedTime);
        if (broadcastElapsedTime.compareTo(Time.getSeconds(2.5f)) > 0) {
            // If message has expired, remove it
            broadcasts.removeFirst();

            // Continue to display next broadcast message
            if (!broadcasts.isEmpty()) {
                broadcastText.setString(broadcasts.peekFirst());
                Utility.centerOrigin(broadcastText);
                broadcastElapsedTime = Time.ZERO;
            }
        }
    }

    private void handlePacket(Server.PacketType packetType, Packet packet) {
        Integer aircraftIdentifier;
        Vector2f aircraftPosition;
        Aircraft aircraft;
        int aircraftCount;
        Player.Action action;

        switch (packetType) {
            // Send message to all clients
            case BROADCAST_MESSAGE:
                String message = (String) packet.get();
                broadcasts.addLast(message);

                // Just added first message, display immediately
                if (broadcasts.size() == 1) {
                    broadcastText.setString(broadcasts.peekFirst());
                    Utility.centerOrigin(broadcastText);
                    broadcastElapsedTime = Time.ZERO;
                }
                break;

            // Sent by the server to order to spawn player 1 airplane on connect
            case SPAWN_SELF:
                aircraftIdentifier = (Integer) packet.get();
                aircraftPosition = new Vector2f((float) packet.get(), (float) packet.get());

                aircraft = world.addAircraft(aircraftIdentifier);
                aircraft.setPosition(aircraftPosition);

                players.put(aircraftIdentifier, new Player(socketChannel, aircraftIdentifier, getContext().keys1));
                localPlayerIdentifiers.addLast(aircraftIdentifier);

                gameStarted = true;
                break;

            //
            case PLAYER_CONNECT:
                aircraftIdentifier = (Integer) packet.get();
                aircraftPosition = new Vector2f((float) packet.get(), (float) packet.get());

                aircraft = world.addAircraft(aircraftIdentifier);
                aircraft.setPosition(aircraftPosition);

                players.put(aircraftIdentifier, new Player(socketChannel, aircraftIdentifier, null));
                break;

            //
            case PLAYER_DISCONNECT:
                aircraftIdentifier = (Integer) packet.get();

                world.removeAircraft(aircraftIdentifier);
                players.remove(aircraftIdentifier);
                break;

            //
            case INITIAL_STATE:
                float worldHeight = (float) packet.get();
                float currentScroll = (float) packet.get();

                world.setWorldHeight(worldHeight);
                world.setCurrentBattleFieldPosition(currentScroll);

                aircraftCount = (int) packet.get();
                for (int i = 0; i < aircraftCount; i++) {
                    aircraftIdentifier = (Integer) packet.get();
                    aircraftPosition = new Vector2f((float) packet.get(), (float) packet.get());
                    Integer hitpoints = (Integer) packet.get();
                    Integer missileAmmo = (Integer) packet.get();

                    aircraft = world.addAircraft(aircraftIdentifier);
                    aircraft.setPosition(aircraftPosition);
                    aircraft.setHitpoints(hitpoints);
                    aircraft.setMissileAmmo(missileAmmo);

                    players.put(aircraftIdentifier, new Player(socketChannel, aircraftIdentifier, null));
                }

                break;

            //
            case ACCEPT_COOP_PARTNER:
                aircraftIdentifier = (Integer) packet.get();

                world.addAircraft(aircraftIdentifier);
                players.put(aircraftIdentifier, new Player(socketChannel, aircraftIdentifier, getContext().keys2));
                localPlayerIdentifiers.addLast(aircraftIdentifier);
                break;

            // Player event (like missile fired) occurs
            case PLAYER_EVENT:
                aircraftIdentifier = (Integer) packet.get();
                action = (Player.Action) packet.get();

                if (players.containsKey(aircraftIdentifier)) {
                    players.get(aircraftIdentifier).handleNetworkEvent(action, world.getCommandQueue());
                }
                break;

            // Player's movement or fire keyboard state changes
            case PLAYER_REALTIME_CHANGE:
                aircraftIdentifier = (Integer) packet.get();
                action = (Player.Action) packet.get();
                boolean actionEnabled = (boolean) packet.get();

                if (players.containsKey(aircraftIdentifier)) {
                    players.get(aircraftIdentifier).handleNetworkRealtimeChange(action, actionEnabled);
                }
                break;

            // New enemy to be created
            case SPAWN_ENEMY:
                Aircraft.Type type = (Aircraft.Type) packet.get();
                float height = (float) packet.get();
                float relativeX = (float) packet.get();

                world.addEnemy(type, relativeX, height);
                world.sortEnemies();
                break;

            // Mission successfully completed
            case MISSION_SUCCESS:
                requestStackPush(States.MISSION_SUCCESS);
                break;

            // Pickup created
            case SPAWN_PICKUP:
                Pickup.Type pickupType = (Pickup.Type) packet.get();
                Vector2f pickupPosition = new Vector2f((float) packet.get(), (float) packet.get());

                world.createPickup(pickupPosition, pickupType);
                break;

            case UPDATE_CLIENT_STATE:
                float currentWorldPosition = (float) packet.get();
                aircraftCount = (int) packet.get();

                float currentViewPosition = world.getViewBounds().top + world.getViewBounds().height;

                // Set the world's scroll compensation according to whether the view is behind or too advanced
                world.setWorldScrollCompensation(currentViewPosition / currentWorldPosition);

                for (int i = 0; i < aircraftCount; i++) {
                    aircraftIdentifier = (Integer) packet.get();
                    aircraftPosition = new Vector2f((float) packet.get(), (float) packet.get());

                    aircraft = world.getAircraft(aircraftIdentifier);
                    boolean isLocalPlane = localPlayerIdentifiers.contains(aircraftIdentifier);
                    if (aircraft != null && !isLocalPlane) {
                        Vector2f interpolatedPosition = Vector2f.add(
                            aircraft.getPosition(),
                            Vector2f.mul(Vector2f.sub(aircraftPosition, aircraft.getPosition()), 0.1f)
                        );
                        aircraft.setPosition(interpolatedPosition);
                    }
                }
                break;
        }
    }
}
