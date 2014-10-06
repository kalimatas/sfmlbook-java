package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.Network.Packet;
import com.github.kalimatas.c10_Network.Network.Server;
import org.jsfml.graphics.Color;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Text;
import org.jsfml.graphics.TextureCreationException;
import org.jsfml.system.Clock;
import org.jsfml.system.Time;
import org.jsfml.system.Vector2f;
import org.jsfml.window.event.Event;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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

    private Map<Integer, Player> players = new HashMap<>(); // todo: int?
    private LinkedList<Integer> localPlayerIdentifiers = new LinkedList<>();
    private Socket socket;
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

        SocketChannel channel = SocketChannel.open();
        socket = channel.socket();

        try {
            socket.connect(new InetSocketAddress(ip, Server.SERVER_PORT), 5);
            channel.configureBlocking(false);
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

            updateBroadcastMessage(dt);
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
        // todo
    }

    /**
     * todo: packetType Integer?
     */
    private void handlePacket(Integer packetType, Packet packet) {

    }
}
