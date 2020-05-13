package com.itmo.r3135.Connector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Приёмник данных.
 */
public class Reader {
    /**
     * Размер данных о пакете
     */
    private static final int HEAD_SIZE = 16;
    /**
     * Полный размер пакета
     */
    private static final int FULL_SIZE = 1016;
    /**
     * Полезных данных в пакете
     */
    private static final int DATA_SIZE = FULL_SIZE - HEAD_SIZE;


    private final boolean datagramMode;
    private final HashMap<Integer, Collector> collectors = new HashMap<>();
    private Executor executor;
    private DatagramSocket datagramSocket;
    private DatagramChannel datagramChannel;

    /**
     * Создаёт приёмник данных, использующий DatagramChannel
     *
     * @param datagramChannel Рабочий канал
     */
    public Reader(DatagramChannel datagramChannel) {
        this.datagramMode = false;

        this.datagramChannel = datagramChannel;

    }

    /**
     * Создаёт приёмник данных, использующий DatagramSocket
     *
     */
    public Reader(DatagramSocket datagramSocket) {
        this.datagramMode = true;
        this.datagramSocket = datagramSocket;

    }

    /**
     * Приём через DatagramChannel
     */
    public void chanelRead() {
        byte[] b = new byte[FULL_SIZE];
        ByteBuffer buffer = ByteBuffer.wrap(b);
        while (true)
            try {
                SocketAddress address = datagramChannel.receive(buffer);
                buffer.rewind();
                DatagramPacket datagramPacket = new DatagramPacket(buffer.array(), FULL_SIZE, address);
                workWithPacket(datagramPacket);
            } catch (IOException e) {
                // логирование
            }
    }

    /**
     * Приёмдпри помощи DatagramSocket
     */
    public void datagramRead() {
        while (true)
            try {
                DatagramPacket datagramPacket = new DatagramPacket(new byte[FULL_SIZE], FULL_SIZE);
                datagramSocket.receive(datagramPacket);
//               System.out.println("Wow, new packet");
                workWithPacket(datagramPacket);
            } catch (IOException e) {
//                 логирование
            }
    }

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    /**
     * Запускает поток приёма пакетов
     */
    public void startListening() {
        Thread listening;
        if (datagramMode) listening = new Thread(this::datagramRead);
        else listening = new Thread(this::chanelRead);
        listening.setDaemon(true);
        listening.start();
    }

    /**
     * Обработка  пакет
     *
     * @param packet входящий пакет
     */
    private void workWithPacket(DatagramPacket packet) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(packet.getData());
        byteBuffer.position(12);
        int packetID = byteBuffer.getInt();
        if (!collectors.containsKey(packetID))
            collectors.put(packetID, new Collector(packetID, packet.getSocketAddress()));
        collectors.get(packetID).collect(packet);
    }

    /**
     * Метод для визаульного отображения процесса приёма.
     *
     * @param progressPercentage Часть отображения
     */
    void updateProgress(double progressPercentage) {
        final int width = 20; // progress bar width in chars

        System.out.print("\r[");
        int i = 0;
        for (; i <= (int) (progressPercentage * width); i++) {
            System.out.print(".");
        }
        for (; i < width; i++) {
            System.out.print(" ");
        }
        System.out.print("]");
    }

    /**
     * Сбокщик сообщений пакетов одного запроса в сообщение
     */
    private class Collector {
        private final int packetId;
        private final SocketAddress address;
        private final SortedSet<InfoPacket> packets = new TreeSet<>();

        Collector(int packetId, SocketAddress address) {
            this.packetId = packetId;
            this.address = address;
        }

        /**
         * Добавление пакета в в список для последующей сборки
         *
         * @param packet датаграмма, которую нужно собрать
         */
        void collect(DatagramPacket packet) {
            ByteBuffer buffer = ByteBuffer.wrap(packet.getData());
            InfoPacket infoPacket = new InfoPacket(
                    buffer.getInt(),
                    buffer.getInt(),
                    buffer.getInt(),
                    buffer.getInt()
            );

            buffer.get(infoPacket.data, 0, DATA_SIZE);
            packets.add(infoPacket);
            if (infoPacket.size > 5)
                updateProgress(packets.size() / (1.0 * infoPacket.size));

            if (packets.size() > 0)
                if (packets.size() >= infoPacket.size) {
                    collected();
                }
        }

        /**
         * Сборка покетов в один
         */
        private void collected() {
            int length = 0;
            int marker = 0;
            byte[] data = new byte[packets.size() * DATA_SIZE];
            collectors.remove(packetId);
            for (InfoPacket packet : packets) {
                System.arraycopy(packet.data, 0, data, marker, DATA_SIZE);
                marker += DATA_SIZE;
                length += packet.length;
            }
            byte[] result = new byte[length];
            System.arraycopy(data, 0, result, 0, length);
            executor.execute(data, address);
        }
    }

    /**
     * Класс хранения всей информации о пришдшем пакете
     */
    private class InfoPacket implements Comparable<InfoPacket> {
        int number, size, length, packetId;
        byte[] data = new byte[DATA_SIZE];

        /**
         * @param number   Порядковый номер пакета.
         * @param size     Количество пакетов в сообщении.
         * @param length   Размер информационной части пакета.
         * @param packetId Уникальный номер сообщения.(Одинаковый у всех пакетов одного сообещния).
         */
        InfoPacket(int number, int size, int length, int packetId) {
            this.number = number;
            this.size = size;
            this.length = length;
            this.packetId = packetId;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null || obj.getClass() != this.getClass())
                return false;
            InfoPacket packet = (InfoPacket) obj;
            return packet.number == this.number &&
                    packet.size == this.size &&
                    packet.length == this.length &&
                    packet.packetId == this.packetId &&
                    Arrays.equals(packet.data, this.data);
        }

        @Override
        public int compareTo(InfoPacket o) {
            return number - o.number;
        }


    }
}
