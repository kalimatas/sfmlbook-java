package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.Network.NothingToReadException;
import com.github.kalimatas.c10_Network.Network.Packet;
import com.github.kalimatas.c10_Network.Network.PacketReaderWriter;
import com.github.kalimatas.c10_Network.Network.Server;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Text;
import org.jsfml.graphics.TextureCreationException;
import org.jsfml.system.Clock;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
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
        // todo
    }

    @Override
    public boolean update(Time dt) {
        // Connected to server: Handle all the network logic
        if (connected) {
            world.update(dt);

            // Remove players whose aircrafts were destroyed
            // todo

            // Only handle the realtime input if the window has focus and the game is unpaused
            // todo

            // Always handle the network input
            // todo

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
            // todo

            // Events occurring in the game
            // todo

            // Regular position updates
            // todo

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
        // todo
    }

    @Override
    public boolean handleEvent(Event event) {
        // Stop game server on window close
        if (event.type == Event.Type.CLOSED && host) {
            gameServer.setWaitingThreadEnd(true);
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
                Integer aircraftSelfIdentifier = (Integer) packet.get();
                Vector2f aircraftSelfPosition = new Vector2f((float) packet.get(), (float) packet.get());

                Aircraft aircraftSelf = world.addAircraft(aircraftSelfIdentifier);
                aircraftSelf.setPosition(aircraftSelfPosition);

                players.put(aircraftSelfIdentifier, new Player(socketChannel, aircraftSelfIdentifier, getContext().keys1));
                localPlayerIdentifiers.addLast(aircraftSelfIdentifier);

                gameStarted = true;
                break;

            //
            case PLAYER_CONNECT:
                Integer aircraftConnectIdentifier = (Integer) packet.get();
                Vector2f aircraftConnectPosition = new Vector2f((float) packet.get(), (float) packet.get());

                Aircraft aircraftConnect = world.addAircraft(aircraftConnectIdentifier);
                aircraftConnect.setPosition(aircraftConnectPosition);

                players.put(aircraftConnectIdentifier, new Player(socketChannel, aircraftConnectIdentifier, null));
                break;

            //
            case PLAYER_DISCONNECT:
                Integer aircraftDisconnectIdentifier = (Integer) packet.get();

                world.removeAircraft(aircraftDisconnectIdentifier);
                players.remove(aircraftDisconnectIdentifier);
                break;

            //
            case INITIAL_STATE:
                float worldHeight = (float) packet.get();
                float currentScroll = (float) packet.get();

                world.setWorldHeight(worldHeight);
                world.setCurrentBattleFieldPosition(currentScroll);

                int aircraftCount = (int) packet.get();
                for (int i = 0; i < aircraftCount; i++) {
                    Integer aircraftInitialIdentifier = (Integer) packet.get();
                    Vector2f aircraftPosition = new Vector2f((float) packet.get(), (float) packet.get());
                    Integer hitpoints = (Integer) packet.get();
                    Integer missileAmmo = (Integer) packet.get();

                    Aircraft aircraft = world.addAircraft(aircraftInitialIdentifier);
                    aircraft.setPosition(aircraftPosition);
                    aircraft.setHitpoints(hitpoints);
                    aircraft.setMissileAmmo(missileAmmo);

                    players.put(aircraftInitialIdentifier, new Player(socketChannel, aircraftInitialIdentifier, null));
                }

                break;

            //
            case ACCEPT_COOP_PARTNER:
                break;

            // Player event (like missile fired) occurs
            case PLAYER_EVENT:
                break;

            // Player's movement or fire keyboard state changes
            case PLAYER_REALTIME_CHANGE:
                break;

            // New enemy to be created
            case SPAWN_ENEMY:
                break;

            // Mission successfully completed
            case MISSION_SUCCESS:
                break;

            // Pickup created
            case SPAWN_PICKUP:
                break;

            case UPDATE_CLIENT_STATE:
                break;

        }
    }
}
