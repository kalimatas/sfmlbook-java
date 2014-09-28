package com.github.kalimatas.c10_Network;

import com.github.kalimatas.c10_Network.Network.Packet;
import org.jsfml.graphics.RenderWindow;
import org.jsfml.graphics.Text;
import org.jsfml.graphics.TextureCreationException;
import org.jsfml.system.Clock;
import org.jsfml.system.Time;
import org.jsfml.window.event.Event;

import java.net.Socket;
import java.util.LinkedList;
import java.util.Map;

public class MultiplayerGameState extends State
{
    private World world;
    private RenderWindow window;
    private ResourceHolder textureHolder;

    private Map<Integer, Player> players; // todo: int?
    private LinkedList<Integer> localPlayerIdentifiers; // todo: int?
    private Socket socket;
    private boolean connected = false;
    // todo: game server
    private Clock tickClock;

    private LinkedList<String> broadcasts;
    private Text broadcastText;
    private Time broadcastElapsedTime;

    private Text playerInvitationText;
    private Time playerInvitationTime;

    private Text failedConnectionText;
    private Clock failedConnectionClock;

    private boolean activeState = true;
    private boolean hasFocus = true;
    private boolean host;
    private boolean gameStarted = false;
    private Time clientTimeout = Time.getSeconds(2.f);
    private Time timeSinceLastPacket = Time.getSeconds(0.f);

    public MultiplayerGameState(StateStack stack, Context context, boolean isHost) {
        super(stack, context);

        // todo: world networked
        this.window = context.window;
        this.textureHolder = context.textures;
        this.host = isHost;
    }

    @Override
    public void draw() throws TextureCreationException {

    }

    public void onActivate() {
        activeState = true;
    }

    public void onDestroy() {
        // todo
    }

    @Override
    public boolean update(Time dt) {
        return false;
    }

    public void disableAllRealtimeActions() {
        // todo
    }

    @Override
    public boolean handleEvent(Event event) {
        return false;
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
