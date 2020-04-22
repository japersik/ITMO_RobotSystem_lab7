package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Класс обработки комадны show
 */

public class ShowCommand extends AbstractCommand {

    public ShowCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Функция выводит на экран все элементы коллекции.
     */
    @Override
    public ServerMessage activate(Command command) {
        ArrayList<Product> products = new ArrayList<>(collection.getProducts());
        //Надо добавить сортировку
        products.sort(Comparator.comparingInt(o -> o.getName().length()));
        if (products.size() != 0) {
//            for (Product product : products) System.out.println(product);
            return new ServerMessage("Список коллекции: ", products);
        } else return new ServerMessage("Коллекция пуста.");

    }
}
