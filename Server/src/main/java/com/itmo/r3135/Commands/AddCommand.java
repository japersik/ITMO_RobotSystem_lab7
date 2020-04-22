package com.itmo.r3135.Commands;

import com.google.gson.JsonSyntaxException;
import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.HashSet;
import java.util.Random;

/**
 * Класс обработки комадны add
 */
public class AddCommand extends AbstractCommand {
    public AddCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        HashSet<Product> products = this.collection.getProducts();

        try {
            Product addProduct = command.getProduct();

            addProduct.setCreationDate(java.time.LocalDateTime.now());
            addProduct.setId(uniqueoIdGeneration(products));
            if (addProduct.checkNull()) {
                System.out.println("Элемент не удовлетворяет требованиям коллекции");
                return new ServerMessage(Product.printRequest());
            } else if (products.add(addProduct)) {
                collection.getDateChange();
                return new ServerMessage("Элемент успешно добавлен.");
            } else return new ServerMessage("Ошибка добавления элеемнта в коллекцию");
        } catch (JsonSyntaxException ex) {
            return new ServerMessage("Возникла ошибка синтаксиса Json. Элемент не был добавлен");
        }
    }

    private int uniqueoIdGeneration(HashSet<Product> products) {
        Random r = new Random();
        int newId;
        int counter;
        while (true) {
            counter = 0;
            newId = Math.abs(r.nextInt());
            for (Product product : products) {
                if (product.getId() == newId) {
                    break;
                } else counter++;
            }
            if (counter == products.size()) {
                return newId;
            }
        }
    }

}
