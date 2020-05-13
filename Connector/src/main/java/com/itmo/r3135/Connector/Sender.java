package com.itmo.r3135.Connector;

//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.ArrayList;
import java.util.List;

/**
 * Передатчик данных
 */
public class Sender {

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
    private DatagramSocket datagramSocket;
    private DatagramChannel datagramChannel;

    /**
     * Создаёт передатчик данных, использующий DatagramChannel
     *
     * @param datagramChannel Рабочий канал
     */
    public Sender(DatagramChannel datagramChannel) {
        this.datagramMode = false;
        this.datagramChannel = datagramChannel;
    }

    /**
     * Создаёт передатчик данных, использующий DatagramSocket
     *
     * @param datagramSocket Рабочий сокет
     */
    public Sender(DatagramSocket datagramSocket) {
        this.datagramMode = true;
        this.datagramSocket = datagramSocket;
    }

    /**
     * Отправка объекта на указаный адрес
     *
     * @param sendObject    отправляемый объект
     * @param socketAddress адрес получателя
     */
    public <T> void send(T sendObject, SocketAddress socketAddress) {
        if (datagramMode) sendDatagram(sendObject, socketAddress);
        else sendChanel(sendObject, socketAddress);
    }

    /**
     * Отправка объекта на указаный адрес через DatagramChannel
     *
     * @param sendObject    отправляемый объект
     * @param socketAddress адрес получателя
     */
    private <T> void sendChanel(T sendObject, SocketAddress socketAddress) {
        byte[] data = toSerial(sendObject);
        List<byte[]> packets = splitPackage(data);
        try {
            for (byte[] packet : packets) {
                datagramChannel.send(ByteBuffer.wrap(packet), socketAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Отправка объекта на указаный адрес через DatagramSocket
     *
     * @param sendObject    отправляемый объект
     * @param socketAddress адрес получателя
     */
    private <T> void sendDatagram(T sendObject, SocketAddress socketAddress) {
        byte[] data = toSerial(sendObject);
        List<byte[]> outMessages = splitPackage(data);
        try {
            for (byte[] outMessage : outMessages) {
                DatagramPacket output = new DatagramPacket(outMessage, FULL_SIZE, socketAddress);
                datagramSocket.send(output);
                Thread.sleep(4);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Сериализация объекта
     *
     * @param serObject Сериализуемый объект
     * @return Объект в сериализованном виде
     */
    private <T> byte[] toSerial(T serObject) {
        byte[] bytearray = new byte[0];
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream)) {
            objectOutputStream.writeObject(serObject);
            bytearray = byteArrayOutputStream.toByteArray();
        } catch (IOException ignore) {
        }
        return bytearray;

    }

    /**
     * Делит сообщение на пакеты и добавляет информацию о сообщении в пакеты.
     *
     * @param data Массив байт для разделения на пакеты
     * @return Содержимое пакетов с информацией о сообщении в начале
     */
    private List<byte[]> splitPackage(byte[] data) {
        List<byte[]> result = new ArrayList<>();
        int requestID = (int) (Math.random() * Integer.MAX_VALUE);
        for (int marker = 0; marker < data.length; marker += DATA_SIZE) {
            ByteBuffer byteBuffer = ByteBuffer.allocate(FULL_SIZE);
            int bytesLeft = data.length - marker;

            byteBuffer.putInt(marker / DATA_SIZE);
            byteBuffer.putInt((int) Math.ceil(1.0 * data.length / DATA_SIZE));
            byteBuffer.putInt(Math.min(bytesLeft, DATA_SIZE));
            byteBuffer.putInt(requestID);

            byteBuffer.put(data, marker, Math.min(bytesLeft, DATA_SIZE));
            result.add(byteBuffer.array());
        }
        return result;
    }
}
