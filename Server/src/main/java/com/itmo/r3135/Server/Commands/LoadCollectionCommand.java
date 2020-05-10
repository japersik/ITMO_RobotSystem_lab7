package com.itmo.r3135.Server.Commands;

import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;

/**
 * Класс загрузки коллекции из базы
 */
public class LoadCollectionCommand extends AbstractCommand {
    static final Logger logger = LogManager.getLogger("Loader");

    public LoadCollectionCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        dataManager.getLock().writeLock().lock();
        HashSet<Product> products = dataManager.getProducts();
        try {
            logger.info("Loading collection from database: " + dataManager.getSqlManager().getConnection().getCatalog());
            PreparedStatement statement = dataManager.getSqlManager().getConnection().prepareStatement(
                    "select owners.id," +
                            "       products.name," +
                            "       products.x," +
                            "       products.y," +
                            "       products.creationdate," +
                            "       products.price," +
                            "       products.partnumber," +
                            "       products.manufacturecost," +
                            "       unitofmeasures.unitname," +
                            "       owners.ownername," +
                            "       owners.ownerbirthday," +
                            "       e.name ownereyecolor," +
                            "       h.name ownerhaircolor," +
                            "       u.username " +
                            "from products " +
                            " join owners on owners.id = products.id " +
                            " join colors e on owners.ownereyecolor_id = e.id " +
                            " join colors h on owners.ownereyecolor_id = h.id " +
                            " join unitofmeasures on products.unitofmeasure_id = unitofmeasures.id " +
                            " join users u on products.user_id = u.id"
            );
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                Person owner = new Person(resultSet.getString("ownername"),
                        resultSet.getTimestamp("ownerbirthday").toLocalDateTime(),
                        Color.valueOf(resultSet.getString("ownereyecolor")),
                        Color.valueOf(resultSet.getString("ownerhaircolor")));
                Product product = new Product(resultSet.getInt("id"),
                        resultSet.getString("name"),
                        new Coordinates(resultSet.getFloat("x"), resultSet.getDouble("y")),
                        resultSet.getTimestamp("creationdate").toLocalDateTime(),
                        resultSet.getDouble("price"),
                        resultSet.getString("partnumber"),
                        resultSet.getFloat("manufacturecost"),
                        UnitOfMeasure.valueOf(resultSet.getString("unitname")),
                        owner);
                product.setUserName(resultSet.getString("username"));
                products.add(product);
            }
            logger.info("Collections successfully uploaded. Added " + products.size() + " items.");
            dataManager.getLock().writeLock().unlock();
            return new ServerMessage("Good.");
        } catch (SQLException e) {
            logger.fatal("SQL reading error");
            e.printStackTrace();
            dataManager.getLock().writeLock().unlock();
            return null;
        }
    }
}
