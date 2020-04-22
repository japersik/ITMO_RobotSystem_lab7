package com.itmo.r3135;

import com.itmo.r3135.System.Command;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class Reader {
    DatagramSocket socket;
    private byte[] b = new byte[65535];
    private DatagramPacket input = new DatagramPacket(b, b.length);

    public Reader(DatagramSocket socket) {
        this.socket = socket;
    }

    public Command nextCommand() throws IOException {
        socket.receive(input);
        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                new ByteArrayInputStream(b));) {
            Command command = (Command) objectInputStream.readObject();
            objectInputStream.close();
            return command;
        } catch (ClassNotFoundException e) {
            System.out.println("Ошибка десериализации");
            return null;
        }
    }

    public DatagramPacket getInput() {
        return input;
    }
}
