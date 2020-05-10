package com.itmo.r3135.Server.Commands;

import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.ArrayList;

/**
 * Класс обработки комадны show
 * Передаёт все элементы коллекции
 */

public class ShowCommand extends AbstractCommand {

    public ShowCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    /**
     */
    @Override
    public ServerMessage activate(Command command) {
        dataManager.getLock().readLock().lock();
        ArrayList<Product> products = new ArrayList<>(dataManager.getProducts());
        dataManager.getLock().readLock().unlock();
        if (products.size() != 0) {
            return new ServerMessage("Список коллекции: ", products);
        } else return new ServerMessage("Коллекция пуста.");

    }
}
