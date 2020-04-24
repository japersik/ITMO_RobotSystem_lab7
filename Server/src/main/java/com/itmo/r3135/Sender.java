package com.itmo.r3135;

import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.System.Tools.DatagramTrimer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketAddress;

public class Sender {
    static final Logger logger = LogManager.getLogger("Sender");
    private DatagramSocket socket;

    public Sender(DatagramSocket socket) {
        this.socket = socket;
    }

    public void send(ServerMessage serverMessage, SocketAddress inputAddress) throws IOException, InterruptedException {
        byte[] message = toSerial(serverMessage);
//        for (int i = 0; i < message.length; i++) {
//            System.out.println(message[i]);
//        }
        byte[][] outMessages = DatagramTrimer.trimByte(message);
        logger.info("Sending " + outMessages.length + " packages.");
        for (int i = 0; i < outMessages.length; i++) {
            byte[] packet = outMessages[i];
            DatagramPacket output = new DatagramPacket(packet, packet.length, inputAddress);
            socket.send(output);
            if (outMessages.length > 4)
                Thread.sleep(6);
        }
    }

    private byte[] toSerial(ServerMessage serverMessage) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
        objectOutputStream.writeObject(serverMessage);
        byte[] bytearray = byteArrayOutputStream.toByteArray();
        objectOutputStream.close();
        return bytearray;

    }

}
