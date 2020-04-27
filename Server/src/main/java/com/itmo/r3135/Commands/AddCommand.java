package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.SQLconnect.SQLManager;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Person;
import com.itmo.r3135.World.Product;


import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.sql.Connection;
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
        HashSet<Product> products = collection.getProducts();
        Product addProduct = command.getProduct();
        addProductSQL(addProduct);
        addProduct.setCreationDate(java.time.LocalDateTime.now());
        if (addProduct.checkNull()) {
            return new ServerMessage(Product.printRequest());
        } else if (products.add(addProduct)) {
            collection.getDateChange();
            return new ServerMessage("Элемент успешно добавлен.");
        } else return new ServerMessage("Ошибка добавления элеемнта в коллекцию");

    }


    private int addProductSQL(Product product) {
        int id = -1;
        int idOwner = addOwnerSQL(product.getOwner());
        if (idOwner != -1)
            try {
                PreparedStatement statement = collection.getSqlManager().getConnection().prepareStatement(
                        "insert into products" +
                                "(id,name, x, y, creationdate, price, partnumber, manufacturecost, unitofmeasure_id, user_id) " +
                                "values (?,?,?,?,?,?,?,?,(select id from unitofmeasures where name = ?),?) returning id"
                );
                statement.setInt(1, idOwner);
                statement.setString(2, product.getName());
                statement.setFloat(3, product.getCoordinates().getX());
                statement.setDouble(4, product.getCoordinates().getY());
                statement.setTimestamp(5, new Timestamp(product.getCreationDate().toEpochSecond(ZoneOffset.UTC) * 1000));
                statement.setDouble(6, product.getPrice());
                statement.setString(7, product.getPartNumber());
                statement.setDouble(8, product.getManufactureCost());
                statement.setString(9, product.getUnitOfMeasure().toString());
                statement.setInt(10, 1);

                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    id = resultSet.getInt("id");
                    System.out.println("Added Product id: " + id);
                }
            } catch (SQLException lal) {
                lal.printStackTrace();
            }
        return id;
    }

    private int addOwnerSQL(Person owner) {
        int id = -1;
        try {
            PreparedStatement statement = collection.getSqlManager().getConnection().prepareStatement(
                    "insert into owners" +
                            "(ownername, ownerbirthday, ownereyecolor_id, ownerhaircolor_id) " +
                            "values (?, ?, (select id from colors where name = ?), (select id from colors where name = ?)) returning id"
            );
            statement.setString(1, owner.getName());
            statement.setTimestamp(2, new Timestamp(owner.getBirthday().toEpochSecond(ZoneOffset.UTC) * 1000));
            statement.setString(3, owner.getEyeColor().toString());
            statement.setString(4, owner.getHairColor().toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) id = resultSet.getInt("id");
            System.out.println(id);
        } catch (SQLException lal) {
            lal.printStackTrace();
        }
        return id;
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
