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
 * Класс обработки комадны filter_contains_name
 */
public class FilterContainsNameCommand extends AbstractCommand {
    public FilterContainsNameCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Выводит элементы, значение поля name которых содержит заданную подстроку.
     */
    @Override
    public ServerMessage activate(Command command) {
        HashSet<Product> products = collection.getProducts();
        if (products.size() > 0) {
            if (!command.getString().isEmpty() && command.getString() != null) {
                ArrayList<Product> productsList = new ArrayList<>(products.stream().filter(product -> product.getName().contains(command.getString())).collect(Collectors.toCollection(ArrayList::new)));
                long findProdukts = products.parallelStream().filter(product -> product.getName().contains(command.getString())).count();
                return new ServerMessage("Всего найдено " + findProdukts + " элементов.", productsList);
            } else return new ServerMessage("Ошибка ввода имени.");
        } else return new ServerMessage("Коллекция пуста.");
    }
}
