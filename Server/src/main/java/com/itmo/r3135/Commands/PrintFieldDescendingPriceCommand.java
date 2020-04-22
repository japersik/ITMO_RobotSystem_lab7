
package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Класс обработки комадны print_field_descending_price
 */
public class PrintFieldDescendingPriceCommand extends AbstractCommand {

    public PrintFieldDescendingPriceCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Выводит коллекцию, отсортированную по цене в порядке убывания.
     */
    @Override
    public ServerMessage activate(Command command) {
        HashSet<Product> products = collection.getProducts();
        if (!products.isEmpty()) {
            ArrayList<Product> list = products.stream().sorted((o1, o2) -> (int) ((o2.getPrice() - o1.getPrice()) * 100000)).collect(Collectors.toCollection(ArrayList::new));
            return new ServerMessage("Сортировка в порядке убывания цены:", list);

        } else return new ServerMessage("Коллекция пуста.");
    }
}
