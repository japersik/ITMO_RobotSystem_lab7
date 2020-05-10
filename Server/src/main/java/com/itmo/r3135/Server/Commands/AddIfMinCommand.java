package com.itmo.r3135.Server.Commands;

import com.google.gson.JsonSyntaxException;
import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.sql.SQLException;
import java.util.HashSet;

/**
 * Класс обработки комадны add_if_min
 * Добавляет новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции.
 *
 */
public class AddIfMinCommand extends AbstractCommand {
    public AddIfMinCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        Product addProduct = command.getProduct();

        dataManager.getLock().writeLock().lock();
        HashSet<Product> products = dataManager.getProducts();
        try {
            if (products.size() != 0) {
                Product minElem = products.stream().min(Product::compareTo).get();
                dataManager.getLock().writeLock().unlock();
                if (addProduct.compareTo(minElem) < 0) {
                    Command addCommand = new Command(CommandList.ADD, addProduct);
                    addCommand.setLoginPassword(command.getLogin(),command.getPassword());
                    return serverWorker.processing(addCommand);
                } else {
                    return new ServerMessage("Элемент не минимальный!");
                }
            } else {
                dataManager.getLock().writeLock().unlock();
                return new ServerMessage("Коллекция пуста, минимальный элемент отсутствует.");
            }
        } catch (JsonSyntaxException | SQLException ex) {
            dataManager.getLock().writeLock().unlock();
            return new ServerMessage("Возникла ошибка синтаксиса Json. Элемент не был добавлен");
        }
    }
}
