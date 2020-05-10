package com.itmo.r3135.Server.Commands;

import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Класс обработки комадны filter_contains_name
 * Выводит элементы, значение поля name которых содержит заданную подстроку.
 */
public class FilterContainsNameCommand extends AbstractCommand {
    public FilterContainsNameCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        dataManager.getLock().readLock().lock();
        HashSet<Product> products = dataManager.getProducts();
        if (products.size() > 0) {
            if (!command.getString().isEmpty() && command.getString() != null) {
                ArrayList<Product> productsList = new ArrayList<>(
                        products.stream().filter(product -> product.getName()
                                .contains(command.getString())).collect(Collectors.toCollection(ArrayList::new)));
                dataManager.getLock().readLock().unlock();
                long findProdukts = products.parallelStream()
                        .filter(product -> product.getName().contains(command.getString())).count();
                return new ServerMessage("Всего найдено " + findProdukts + " элементов.", productsList);
            } else {
                dataManager.getLock().readLock().unlock();
                return new ServerMessage("Ошибка ввода имени.");
            }
        } else {
            dataManager.getLock().readLock().unlock();
            return new ServerMessage("Коллекция пуста.");
        }
    }
}
