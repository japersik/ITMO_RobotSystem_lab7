package com.itmo.r3135.Commands;

import com.google.gson.JsonSyntaxException;
import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.Collections;
import java.util.HashSet;
import java.util.stream.Collectors;

/**
 * Класс обработки комадны add_if_min
 */
public class AddIfMinCommand extends AbstractCommand {
    public AddIfMinCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Добавляет новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции.
     */
    @Override
    public ServerMessage activate(Command command) {
        HashSet<Product> products = collection.getProducts();
        try {
            if (products.size() != 0) {
                Product addProduct = command.getProduct();
                Product minElem = products.stream().min(Product::compareTo).get();
                if (addProduct.compareTo(minElem) < 0) {
                    serverWorker.processing(new Command(CommandList.ADD, addProduct));
                    collection.getDateChange();
                } else return new ServerMessage("Элемент не минимальный!");
            } else return new ServerMessage("Коллекция пуста, минимальный элемент отсутствует.");
        } catch (JsonSyntaxException ex) {
            return new ServerMessage("Возникла ошибка синтаксиса Json. Элемент не был добавлен");
        }
        return null;
    }
}
