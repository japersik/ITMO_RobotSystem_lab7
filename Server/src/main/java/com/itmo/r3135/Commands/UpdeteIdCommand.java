package com.itmo.r3135.Commands;

import com.google.gson.JsonSyntaxException;
import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.HashSet;

/**
 * Класс обработки комадны update_id
 */
public class UpdeteIdCommand extends AbstractCommand {
    public UpdeteIdCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Заменяет в колеекции элемент с определенным id.
     */
    @Override
    public ServerMessage activate(Command command) {
        HashSet<Product> products = collection.getProducts();
        try {
            int id = command.getIntValue();
            Product newProduct = command.getProduct();
            int startSize = products.size();
            if (newProduct.checkNull()) {
                return new ServerMessage("Элемент не удовлетворяет требованиям коллекции");
            } else {
                serverWorker.processing(new Command(CommandList.REMOVE_BY_ID, id));
                if (startSize - products.size() == 1)
                    if (products.add(newProduct)) {
                        collection.getDateChange();
                        collection.uptadeDateChange();
                        return new ServerMessage("Элемент успешно обновлён.");
                    } else return new ServerMessage("При замене элементов что-то пошло не так");
            }
        } catch (JsonSyntaxException ex) {
            return new ServerMessage("Возникла ошибка при замене элемента");
        }
        return  null;
    }

}
