package com.itmo.r3135.Server.Commands;

import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.HashSet;

/**
 * Класс обработки комадны execute_script
 * Выполняет скрипт полученный в виде листа комманд
 */
public class ExecuteScriptCommand extends AbstractCommand {
    public ExecuteScriptCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }


    @Override
    public ServerMessage activate(Command command) {
        dataManager.getLock().writeLock().lock();
        HashSet<Product> oldProducts = new HashSet<>(dataManager.getProducts());
        dataManager.getLock().writeLock().unlock();

        try {
            for (Command executeCommand : command.getExecuteCommands()) {
                executeCommand.setLoginPassword(command.getLogin(), command.getPassword());
                serverWorker.processing(executeCommand);
            }
            return new ServerMessage("Скрикт был выполнен.");
        } catch (Exception e) {
            dataManager.getLock().writeLock().lock();
            dataManager.setProducts(oldProducts);
            dataManager.getLock().writeLock().unlock();
            return new ServerMessage("Скрикт не был выполнен. Коллекция не изменилась. Если проблема посторится, обратитесь к в тех. поддержку.");
        }
    }
}
