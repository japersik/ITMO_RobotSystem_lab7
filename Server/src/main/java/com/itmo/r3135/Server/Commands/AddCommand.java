package com.itmo.r3135.Server.Commands;

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
 * Класс обработки комадны add
 */
public class AddCommand extends AbstractCommand {
    public AddCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }


    @Override
    public ServerMessage activate(Command command) {
        int userId = dataManager.getSqlManager().getUserId(command.getLogin());
        if (userId == 0) return new ServerMessage("Ошибка авторизации!");
        Product addProduct = command.getProduct();
        addProduct.setCreationDate(java.time.LocalDateTime.now());
        if (addProduct.checkNull()) {
            return new ServerMessage(Product.printRequest());
        } else {
            int id = addObjSql(addProduct, userId);
            addProduct.setUserName(command.getLogin());
            addProduct.setId(id);
            if (id == -1) return new ServerMessage("Ошибка добавления элеемнта в базу данных");
            else {
                dataManager.getLock().writeLock().lock();
                HashSet<Product> products = dataManager.getProducts();
                if (products.add(addProduct)) {
                    dataManager.updateDateChange();
                    dataManager.getLock().writeLock().unlock();
                    return new ServerMessage("Элемент c id " + id + " успешно добавлен.");
                } else {
                    dataManager.getLock().writeLock().unlock();
                    return new ServerMessage("Ошибка добавления элеемнта в коллекцию. Но. В базу он добавлени" +
                            "Сообщите об этом случае в техническую поддержку.('info')");
                }
            }
        }
    }

    private int addObjSql(Product product, int userId) {
        return addOwnerSQL(product.getOwner(), addProductSQL(product, userId));
    }

    private int addProductSQL(Product product, int userId) {
        int id = -1;
        try {
            PreparedStatement statement = dataManager.getSqlManager().getConnection().prepareStatement(
                    "insert into products" +
                            "(name, x, y, creationdate, price, partnumber, manufacturecost, unitofmeasure_id, user_id) " +
                            "values (?,?,?,?,?,?,?,(select id from unitofmeasures where unitname = ?),?) returning id"
            );
            statement.setString(1, product.getName());
            statement.setFloat(2, product.getCoordinates().getX());
            statement.setDouble(3, product.getCoordinates().getY());
            statement.setTimestamp(4, new Timestamp(product.getCreationDate().toEpochSecond(ZoneOffset.UTC) * 1000));
            statement.setDouble(5, product.getPrice());
            statement.setString(6, product.getPartNumber());
            statement.setDouble(7, product.getManufactureCost());
            statement.setString(8, product.getUnitOfMeasure().toString());
            statement.setInt(9, userId);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                id = resultSet.getInt("id");
                logger.info("Added Product id: " + id);
            }
        } catch (SQLException lal) {
            lal.printStackTrace();
        }
        return id;
    }

    private int addOwnerSQL(Person owner, int id) {
        int idOwner = -1;
        try {
            PreparedStatement statement = dataManager.getSqlManager().getConnection().prepareStatement(
                    "insert into owners" +
                            "(id,ownername, ownerbirthday, ownereyecolor_id, ownerhaircolor_id) " +
                            "values (?,?, ?, (select id from colors where name = ?), (select id from colors where name = ?)) returning id"
            );
            statement.setInt(1, id);
            statement.setString(2, owner.getName());
            statement.setTimestamp(3, new Timestamp(owner.getBirthday().toEpochSecond(ZoneOffset.UTC) * 1000));
            statement.setString(4, owner.getEyeColor().toString());
            statement.setString(5, owner.getHairColor().toString());
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) idOwner = resultSet.getInt("id");
        } catch (SQLException lal) {
            lal.printStackTrace();
        }
        return idOwner;
    }
}
