package com.itmo.r3135.Commands;


import com.google.gson.JsonSyntaxException;
import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Класс обработки комадны remove_greater
 */
public class RemoveGreaterCommand extends AbstractCommand {
    public RemoveGreaterCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Удаляет из коллекции все элементы, превышающие заданный.
     */
    @Override
    public ServerMessage activate(Command command) {
        collection.getLock().writeLock().lock();
        HashSet<Product> products = collection.getProducts();
        try {
            int startSize = products.size();
            if (startSize != 0) {
                products.removeAll((products.parallelStream().filter(product -> 0 > product.compareTo(command.getProduct()))).collect(Collectors.toCollection(HashSet::new)));
                collection.uptadeDateChange();
                collection.getLock().writeLock().unlock();
                return new ServerMessage("Удалено " + (startSize - products.size()) + " элементов");
            } else {
                collection.getLock().writeLock().unlock();
                return new ServerMessage("Коллекция пуста.");
            }
        } catch (JsonSyntaxException ex) {
            collection.getLock().writeLock().unlock();
            return new ServerMessage("Возникла ошибка синтаксиса Json.");
        }
    }
}
