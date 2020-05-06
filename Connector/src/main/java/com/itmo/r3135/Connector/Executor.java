package com.itmo.r3135.Connector;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * Слушатель приёмника.
 */
public interface Executor {
    /**
     * Вызывается для обработки данных.
     *
     * @param data         данные
     * @param inputAddress адрес отправителя
     * @return
     */
    void execute(byte[] data, SocketAddress inputAddress);
}
