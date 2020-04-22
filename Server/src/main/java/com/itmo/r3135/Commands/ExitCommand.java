package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Класс обработки комадны help
 */
public class ExitCommand extends AbstractCommand {
    static final Logger logger = LogManager.getLogger("ServerWorker");
    public ExitCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Закрывает программу без сохранения.
     */
    @Override
    public ServerMessage activate(Command command) {
        logger.info("The program is completed by the command 'exit'.");
//        System.out.println("Работа программы завершена командой 'exit'");
        System.exit(0);
    return null;
    }
}
