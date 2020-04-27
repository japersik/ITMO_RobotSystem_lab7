package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
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
    public AddCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }


    @Override
    public ServerMessage activate(Command command) {
        int userId = 0;
        try {
            PreparedStatement s = collection.getSqlManager().getConnection()
                    .prepareStatement("select id from users where email = ?");
            s.setString(1, command.getLogin());
            ResultSet resultSet = s.executeQuery();
            if (resultSet.next()) userId = resultSet.getInt("id");
        } catch (SQLException ignore) {
        }
        if (userId == 0) return new ServerMessage("Ошибка авторизации!");
        collection.getLock().writeLock().lock();
        HashSet<Product> products = collection.getProducts();
        Product addProduct = command.getProduct();
        addProduct.setCreationDate(java.time.LocalDateTime.now());
        if (addProduct.checkNull()) {
            collection.getLock().writeLock().unlock();
            return new ServerMessage(Product.printRequest());
        } else {
            int id = addProductSQL(addProduct, userId);
            if (id == -1) return new ServerMessage("Ошибка добавления элеемнта в базу данных");
            else if (products.add(addProduct)) {
                collection.updateDateChange();
                collection.getLock().writeLock().unlock();
                return new ServerMessage("Элемент успешно добавлен.");
            } else {
                collection.getLock().writeLock().unlock();
                return new ServerMessage("Ошибка добавления элеемнта в коллекцию. НО. В базу он добавлени" +
                        "Сообщите обэном случае в техническую поддержку.('info')");
            }
        }

    }


    public int addProductSQL(Product product, int userId) {
        int id = -1;
        int idOwner = addOwnerSQL(product.getOwner());
        if (idOwner != -1)
            try {
                PreparedStatement statement = collection.getSqlManager().getConnection().prepareStatement(
                        "insert into products" +
                                "(id,name, x, y, creationdate, price, partnumber, manufacturecost, unitofmeasure_id, user_id) " +
                                "values (?,?,?,?,?,?,?,?,(select id from unitofmeasures where unitname = ?),?) returning id"
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
                statement.setInt(10, userId);

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
}
