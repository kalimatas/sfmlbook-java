package com.github.kalimatas.c10_Network.Network;

public class Server {
    public static final int SERVER_PORT = 5000;

    // Packets originated in the server
    public enum PacketType {
        BROADCAST_MESSAGE,	// format: [Integer:packetType] [string:message]
        SPAWN_SELF,			// format: [Integer:packetType]
        INITIAL_STATE,
        PLAYER_EVENT,
        PLAYER_REALTIME_CHANGE,
        PLAYER_CONNECT,
        PLAYER_DISCONNECT,
        ACCEPT_COOP_PARTNER,
        SPAWN_ENEMY,
        SPAWN_PICKUP,
        UPDATE_CLIENT_STATE,
        MISSION_SUCCESS,
    }
}
