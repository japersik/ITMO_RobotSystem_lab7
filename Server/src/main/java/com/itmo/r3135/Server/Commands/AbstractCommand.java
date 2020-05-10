package com.itmo.r3135.Server.Commands;

import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Абстрактный класс команды
 */
public abstract class AbstractCommand {
    protected DataManager dataManager;
    protected Mediator serverWorker;
    static final Logger logger = LogManager.getLogger("CommandWorker");

    /**
     * @param serverWorker обработчик команд.
     */
    public AbstractCommand(DataManager dataManager, Mediator serverWorker) {
        this.dataManager = dataManager;
        this.serverWorker = serverWorker;
    }

    /** Метод, обрабатывающий команду
     *
     * @param command Обрабатываемая команда от клиента
     * @return Сообщение от сервера
     */
    public abstract ServerMessage activate(Command command);
}
