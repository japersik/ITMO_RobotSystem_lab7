package com.itmo.r3135.Connector;

import java.net.SocketAddress;

/**
 * Слушатель приёмника.
 */
public interface Executor {
    /**
     * Вызывается для обработки принятых данных.
     *
     * @param data         Данные
     * @param inputAddress Адрес отправителя
     */
    void execute(byte[] data, SocketAddress inputAddress);
}
