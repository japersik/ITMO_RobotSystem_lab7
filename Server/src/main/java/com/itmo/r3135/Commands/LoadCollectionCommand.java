package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;


public class LoadCollectionCommand extends AbstractCommand {
    static final Logger logger = LogManager.getLogger("Loader");

    public LoadCollectionCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        HashSet<Product> products = collection.getProducts();
        try {
            logger.info("Loading collection from database: " + collection.getSqlManager().getConnection().getCatalog());
            PreparedStatement statement = collection.getSqlManager().getConnection().prepareStatement(
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
            return new ServerMessage("Good.");
        } catch (SQLException e) {
            logger.fatal("Reading line error");
            e.printStackTrace();
            return null;
        }

//        Gson gson = new Gson();
//        File jsonFile = collection.getJsonFile();
//        HashSet<Product> products = collection.getProducts();
//        int startSize = products.size();logger.info("Loading collection from database: " +collection.getSqlManager().getConnection().getCatalog());
//        if (!jsonFile.exists()) {
//            logger.warn("Unable to save file. The file at the specified path (" + jsonFile.getAbsolutePath() + ") does not exist.");
//            System.exit(666);
//
//        }
//        if (!jsonFile.canRead() || !jsonFile.canWrite()) {
//            logger.fatal("The file is protected from reading and (or) writing. For the correct program to work, both permissions are needed.");
//            System.exit(666);
//        }
//        if (jsonFile.length() == 0) {
//            logger.warn("The file is empty. Only adding items to the collection is possible.");
//            return new ServerMessage("Файл пуст. Возможно только добавление элементов в коллекцию.");
//        }
//
//        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(jsonFile))) {
//            logger.info("Loading collection from " + jsonFile.getAbsolutePath());
//            StringBuilder stringBuilder = new StringBuilder();
//            String nextString;
//            while ((nextString = bufferedReader.readLine()) != null) {
//                stringBuilder.append(nextString);
//            }
//            Type typeOfCollectoin = new TypeToken<HashSet<Product>>() {
//            }.getType();
//            try {
//                HashSet<Product> addedProduct = gson.fromJson(stringBuilder.toString(), typeOfCollectoin);
//                for (Product p : addedProduct) {
//                    if (p.checkNull()) {
//                        throw new JsonSyntaxException("");
//                    }
//                    products.add(p);
//                }
//            } catch (JsonSyntaxException e) {
//                logger.fatal("Json syntax error. File could not be loaded.");
//                System.exit(666);
//            } catch (Exception e) {
//                logger.fatal("Hey, you has a bad json. make a new one.");
//                System.exit(666);
//            }
//            logger.info("Collections successfully uploaded. Added " + (products.size () - startSize) + " items.");
//        } catch (IOException e) {
//            logger.error("Reading line error");
//        }
//        return null;
    }
}
