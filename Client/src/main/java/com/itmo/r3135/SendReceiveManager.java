package com.itmo.r3135;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.System.Tools.DatagramTrimer;

import java.io.*;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.Date;

public class SendReceiveManager {
    SocketAddress socketAddress;
    DatagramChannel datagramChannel;

    public SendReceiveManager(SocketAddress socketAddress, DatagramChannel datagramChannel) {
        this.socketAddress = socketAddress;
        this.datagramChannel = datagramChannel;
    }

    public void send(Command message) {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(message);
            byte[] bytearray = byteArrayOutputStream.toByteArray();
            ByteBuffer buffer = ByteBuffer.wrap(bytearray);
            datagramChannel.send(buffer, socketAddress);
            objectOutputStream.close();
            System.out.println("Сообщение " + message.getCommand() + " отправлено");
        } catch (IOException e) {
            System.out.println("IOException во время отправки");
        }
    }

    public ServerMessage recive() throws IOException, InterruptedException {
        ArrayList<byte[]> messageList = new ArrayList<>();
        int packetCounter = 0;
        byte[] b;
        do {
            System.out.print("Пакет № " + (packetCounter + 1));
            b = new byte[65535];
            ByteBuffer buffer = ByteBuffer.wrap(b);
            SocketAddress from = null;
            Thread.sleep(5);
            for (int i = 0; i < 2000; i++) {
                if (i % 100 == 0) System.out.print(".");
                from = datagramChannel.receive(buffer);
                if (from != null) break;
                Thread.sleep(10);
            }
            System.out.println();
            if (from != null) {
                ++packetCounter;
                messageList.add(b);
            } else {
                if (messageList.size() != 0) System.out.println("Пакеты сообщения потерялить.");
                else {
                    System.out.println("Ответ не был получен!");
                }
                return null;
            }
        } while (!DatagramTrimer.isFinal(b));
        System.out.println("Получено пакетов " + packetCounter);
        byte[] fullMessage = new byte[0];
        for (byte[] message : messageList) {
            fullMessage = DatagramTrimer.connectByte(fullMessage, message);
        }
        return fromSerial(fullMessage);

    }

    private ServerMessage fromSerial(byte[] b) {
        try (
                ObjectInputStream objectInputStream = new ObjectInputStream(
                        new ByteArrayInputStream(b));
        ) {
            ServerMessage serverMessage = (ServerMessage) objectInputStream.readObject();
            objectInputStream.close();
            return serverMessage;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка десериализации.");
            return null;
        }
    }

    public long ping() {
        return ping("", "");
    }

    public long ping(String login, String password) {
        try {
            System.out.println("Проверка соединения:");
            datagramChannel.connect(socketAddress);
            datagramChannel.disconnect();
            datagramChannel.socket().setSoTimeout(1000);
            Command command = new Command(CommandList.PING);
            command.setLoginPassword(login, password);
            send(command);
            Date sendDate = new Date();
            ServerMessage receivedMessage = recive();
            if (receivedMessage != null) {
                Date receiveDate = new Date();
                long ping = (receiveDate.getTime() - sendDate.getTime());
                System.out.println("Время отклика: " + ping + " ms.");
                System.out.println(receivedMessage.getMessage());
                return ping;
            }
        } catch (IOException | InterruptedException ignore) {
        }
        System.out.println("Соединение не установлено.");
        return -1;
    }

}
