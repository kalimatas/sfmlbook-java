package com.github.kalimatas.c10_Network.Network;

public class Client {
    // Packets originated in the client
    public enum PacketType {
        PLAYER_EVENT,
        PLAYER_REALTIME_CHANGE,
        REQUEST_COOP_PARTNER,
        POSITION_UPDATE,
        GAME_EVENT,
        QUIT,
    }
}
