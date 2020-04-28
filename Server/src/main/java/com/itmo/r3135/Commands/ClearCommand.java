package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Класс обработки комадны clear
 */
public class ClearCommand extends AbstractCommand {
    public ClearCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Очищает коллекцию.
     */
    @Override
    public ServerMessage activate(Command command) {
        collection.getLock().writeLock().lock();
        HashSet<Product> products = collection.getProducts();
        products.clear();
        collection.getLock().writeLock().unlock();
        return new ServerMessage("Коллекция очищена.");
    }
}
