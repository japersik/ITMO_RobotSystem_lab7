package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.SQLconnect.SQLManager;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
        Product addProduct = command.getProduct();
        addProduct.setCreationDate(java.time.LocalDateTime.now());
        addProduct.setId(uniqueoIdGeneration(products));
        try (Statement statement = collection.getSqlManager().getConnection().createStatement()) {
            statement.execute("INSERT INTO products (name, x, y, price, partnumber, manafucturecost)" +
                    "VALUES (Сталин, 15, 10, 2, 321, 555);");
        } catch (SQLException e){
            return new ServerMessage("Потеряно соединение с базой данных");
        }

        if (addProduct.checkNull()) {
            return new ServerMessage(Product.printRequest());
        } else if (products.add(addProduct)) {
            collection.getDateChange();
            return new ServerMessage("Элемент успешно добавлен.");
        } else return new ServerMessage("Ошибка добавления элеемнта в коллекцию");

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
