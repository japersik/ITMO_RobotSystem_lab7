package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.HashSet;
import java.util.stream.Collectors;

public class RemoveByIdCommand extends AbstractCommand {

    /**
     * Класс обработки комадны remove_by_id
     */
    public RemoveByIdCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Удаляет элемент по его id.
     */
    @Override
    public ServerMessage activate(Command command) {
        HashSet<Product> products = collection.getProducts();
        int startSize = products.size();
        if (products.size() > 0) {
            int id = command.getIntValue();
            products.removeAll((products.parallelStream().filter(product -> product.getId() == id)
                    .collect(Collectors.toCollection(HashSet::new))));
            if (startSize == products.size()) {
                return new ServerMessage("Элемент с id " + id + " не существует.");
            }
            collection.uptadeDateChange();
            return new ServerMessage("Элемент коллекции успешно удалён.");
        } else return new ServerMessage("Коллекция пуста.");
    }
}
