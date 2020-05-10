package com.itmo.r3135.Server;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;

import java.sql.SQLException;

/**
 * Интерфейс обработчика команд
 */
public interface Mediator {
    ServerMessage processing(Command command) throws SQLException;
}
