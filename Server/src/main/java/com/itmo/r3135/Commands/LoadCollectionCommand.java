package com.itmo.r3135.Commands;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;


public class LoadCollectionCommand extends AbstractCommand {
    static final Logger logger = LogManager.getLogger("Loader");

    public LoadCollectionCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {

        Gson gson = new Gson();
        File jsonFile = collection.getJsonFile();
        HashSet<Product> products = collection.getProducts();
        int startSize = products.size();
        if (!jsonFile.exists()) {
            logger.warn("Unable to save file. The file at the specified path (" + jsonFile.getAbsolutePath() + ") does not exist.");
            System.exit(666);

        }
        if (!jsonFile.canRead() || !jsonFile.canWrite()) {
            logger.fatal("The file is protected from reading and (or) writing. For the correct program to work, both permissions are needed.");
            System.exit(666);
        }
        if (jsonFile.length() == 0) {
            logger.warn("The file is empty. Only adding items to the collection is possible.");
            return new ServerMessage("Файл пуст. Возможно только добавление элементов в коллекцию.");
        }

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(jsonFile))) {
            logger.info("Loading collection from " + jsonFile.getAbsolutePath());
            StringBuilder stringBuilder = new StringBuilder();
            String nextString;
            while ((nextString = bufferedReader.readLine()) != null) {
                stringBuilder.append(nextString);
            }
            Type typeOfCollectoin = new TypeToken<HashSet<Product>>() {
            }.getType();
            try {
                HashSet<Product> addedProduct = gson.fromJson(stringBuilder.toString(), typeOfCollectoin);
                for (Product p : addedProduct) {
                    if (p.checkNull()) {
                        throw new JsonSyntaxException("");
                    }
                    products.add(p);
                }
            } catch (JsonSyntaxException e) {
                logger.fatal("Json syntax error. File could not be loaded.");
                System.exit(666);
            } catch (Exception e) {
                logger.fatal("Hey, you has a bad json. make a new one.");
                System.exit(666);
            }
            logger.info("Collections successfully uploaded. Added " + (products.size () - startSize) + " items.");
        } catch (IOException e) {
            logger.error("Reading line error");
        }
        return null;
    }
}
