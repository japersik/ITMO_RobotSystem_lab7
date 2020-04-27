package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.HashSet;

/**
 * Класс обработки комадны execute_script
 */
public class ExecuteScriptCommand extends AbstractCommand {
    public ExecuteScriptCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Выполняет скрипт записанный в файле.
     * В программе стоит ограничение на выполнение рекурсивных итераций в цикле - 20 вложенных циклов. Мы не рекомендуем вызывать скрипты в самом скрипте.
     */
    @Override
    public ServerMessage activate(Command command) {
        collection.getLock().writeLock().lock();
        HashSet<Product> oldProducts = new HashSet<>(collection.getProducts());
        try {
            for (Command executeCommand : command.getEcexuteCommands()) {
                executeCommand.setLoginPassword(command.getLogin(), command.getPassword());
                serverWorker.processing(executeCommand);
            }
            collection.updateDateChange();
            collection.getLock().readLock().unlock();
            return new ServerMessage("Скрикт был выполнен.");
        } catch (Exception e) {
            collection.setProducts(oldProducts);
            collection.getLock().readLock().unlock();
            return new ServerMessage("Скрикт не был выполнен. Коллекция не изменилась. Если проблема посторится, обратитесь к в тех. поддержку.");
        }
    }
}
