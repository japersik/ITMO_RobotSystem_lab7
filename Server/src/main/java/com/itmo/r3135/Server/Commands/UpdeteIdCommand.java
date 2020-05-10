package com.itmo.r3135.Server.Commands;

import com.google.gson.JsonSyntaxException;
import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Person;
import com.itmo.r3135.World.Product;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.HashSet;

/**
 * Класс обработки комадны update
 * Заменяет в колеекции элемент с заданным id.
 */
public class UpdeteIdCommand extends AbstractCommand {
    public UpdeteIdCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        int userId = dataManager.getSqlManager().getUserId(command.getLogin());
        if (userId == -1) return new ServerMessage("Ошибка авторизации!");
        try {
            int id = command.getIntValue();
            Product newProduct = command.getProduct();
            if (newProduct.checkNull()) {
                return new ServerMessage("Элемент не удовлетворяет требованиям коллекции");
            } else {
                if (updateProductSQL(newProduct, id) != -1) {
                    HashSet<Product> products = dataManager.getProducts();
                    for (Product productFoUpdate : products) {
                        if (productFoUpdate.getId() == id)
                            productFoUpdate.updateProduct(newProduct);
                        dataManager.getLock().writeLock().unlock();
                        return new ServerMessage("Элемент успешно обновлён.");
                    }
                    return new ServerMessage("Как такое могло произойти?! В базе обновлён, а в коллекци - нет?!");
                } else
                    return new ServerMessage("При замене элементов что-то пошло не так.\n" +
                            " Возможно, объект Вам не принаджежит");
            }
        } catch (JsonSyntaxException ex) {
            dataManager.getLock().writeLock().unlock();
            return new ServerMessage("Возникла ошибка при замене элемента");
        }
    }

    private int updateProductSQL(Product product, int id) {
        int idRet = -1;
        if (updateOwnerSQL(product.getOwner(), id) != -1)
            try {
                PreparedStatement statement = dataManager.getSqlManager().getConnection().prepareStatement(
                        "UPDATE products " +
                                "SET name = ?, x=?, y=?, creationdate=?, price=?, partnumber=?, manufacturecost=?, unitofmeasure_id= (select id from unitofmeasures where unitname = ?) " +
                                "WHERE id = ? returning id"
                );
                statement.setString(1, product.getName());
                statement.setFloat(2, product.getCoordinates().getX());
                statement.setDouble(3, product.getCoordinates().getY());
                statement.setTimestamp(4, new Timestamp(product.getCreationDate().toEpochSecond(ZoneOffset.UTC) * 1000));
                statement.setDouble(5, product.getPrice());
                statement.setString(6, product.getPartNumber());
                statement.setDouble(7, product.getManufactureCost());
                statement.setString(8, product.getUnitOfMeasure().toString());
                statement.setInt(9, id);

                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    id = resultSet.getInt("id");
                    System.out.println("Updated Product id: " + id);
                }
            } catch (SQLException lal) {
                lal.printStackTrace();
            }
        return id;
    }

    private int updateOwnerSQL(Person owner, int id) {
        int idOwner = -1;
        try {
            PreparedStatement statement = dataManager.getSqlManager().getConnection().prepareStatement(
                    "UPDATE owners SET " +
                            "ownername = ? , ownerbirthday = ? , ownereyecolor_id = (select id from colors where name = ?) , ownerhaircolor_id = (select id from colors where name = ?) where id = ? returning id"
            );

            statement.setString(1, owner.getName());
            statement.setTimestamp(2, new Timestamp(owner.getBirthday().toEpochSecond(ZoneOffset.UTC) * 1000));
            statement.setString(3, owner.getEyeColor().toString());
            statement.setString(4, owner.getHairColor().toString());
            statement.setInt(5, id);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) idOwner = resultSet.getInt("id");
            System.out.println(id);
        } catch (SQLException lal) {
            lal.printStackTrace();
        }
        return idOwner;
    }

}
