package com.itmo.r3135.Connector;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;

/**
 * Класс проверки соединения
 */
public class PingChecker {

    /**
     * Метод, ожидающий какой-либо откклик от другой стороны соединения
     *
     * @param datagramChannel Адрес приёмной стороны
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private static boolean receive(DatagramChannel datagramChannel) throws InterruptedException, IOException {
        byte[] b;
        System.out.print("Ping:");
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
        return from != null;

    }

    /**
     * Метод проверки соединения
     *
     * @param pingObject    Объект, который нужно передать другой стороне соединения
     * @param socketAddress Адрес приёмной стороны
     * @return Время отклика
     */
    public static <T> long ping(T pingObject, SocketAddress socketAddress) {
        try {
            DatagramChannel datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            System.out.println("Проверка соединения:");
            datagramChannel.connect(socketAddress);
            datagramChannel.disconnect();
            datagramChannel.socket().setSoTimeout(1000);
            Sender sender = new Sender(datagramChannel);
            sender.send(pingObject, socketAddress);
            Date sendDate = new Date();
            boolean receivedMessage = receive(datagramChannel);
            if (receivedMessage) {
                Date receiveDate = new Date();
                long ping = (receiveDate.getTime() - sendDate.getTime());
                System.out.println("Время отклика: " + ping + " ms.");
                return ping;
            }
        } catch (IOException | InterruptedException ignore) {
        }
        System.out.println("Соединение не установлено.");
        return -1;
    }
}
