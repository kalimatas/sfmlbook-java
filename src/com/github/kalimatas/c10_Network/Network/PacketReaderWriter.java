package com.github.kalimatas.c10_Network.Network;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class PacketReaderWriter {
    private static final int PACKET_SIZE_LENGTH = 4;
    private static final ByteBuffer packetSizeReadBuffer = ByteBuffer.allocate(PACKET_SIZE_LENGTH);
    private static ByteBuffer clientReadBuffer;

    private static byte[] encode(Packet packet) throws IOException {
        try (
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos)
        ) {
            oos.writeObject(packet);
            return baos.toByteArray();
        }
    }

    private static Packet decode(byte[] encodedPacket) throws IOException, ClassNotFoundException {
        try (ObjectInputStream oi = new ObjectInputStream(new ByteArrayInputStream(encodedPacket))) {
            return (Packet) oi.readObject();
        }
    }

    public static void send(SocketChannel channel, Packet packet) throws IOException {
        byte[] encodedPacket = encode(packet);

        ByteBuffer packetSizeBuffer = ByteBuffer.allocate(PACKET_SIZE_LENGTH).putInt(encodedPacket.length);
        packetSizeBuffer.flip();

        // Send packet size
        channel.write(packetSizeBuffer);

        // Send packet content
        ByteBuffer packetBuffer = ByteBuffer.wrap(encodedPacket);
        channel.write(packetBuffer);
    }

    public static Packet receive(SocketChannel channel) throws IOException, NothingToReadException {
        int bytesRead;

        // Read packet size
        packetSizeReadBuffer.clear();
        bytesRead = channel.read(packetSizeReadBuffer);

        if (bytesRead == -1) {
            channel.close();
            throw new NothingToReadException();
        }

        if (bytesRead == 0) return null;

        packetSizeReadBuffer.flip();
        int packetSize = packetSizeReadBuffer.getInt();

        // Read packet
        clientReadBuffer = ByteBuffer.allocate(packetSize);
        bytesRead = channel.read(clientReadBuffer);

        if (bytesRead == -1) {
            channel.close();
            throw new NothingToReadException();
        }

        if (bytesRead == 0) return null;

        clientReadBuffer.flip();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(clientReadBuffer.array(), 0, bytesRead);
        clientReadBuffer.clear();

        try {
            return decode(baos.toByteArray());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
