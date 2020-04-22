package com.itmo.r3135.Commands;

import com.google.gson.Gson;
import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

/**
 * Класс обработки комадны save
 */
public class SaveCommand extends AbstractCommand {
    static final Logger logger = LogManager.getLogger("Saver");

    public SaveCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Сохраняет все изменения коллекции в открытый файл.
     */
    @Override
    public ServerMessage activate(Command command) {
        logger.info("Save-process started.");
        HashSet<Product> products = collection.getProducts();
        File jsonFile = collection.getJsonFile();
        Gson gson = new Gson();
        try {
            if (!jsonFile.exists()) {
                logger.warn("Unable to save file. The file at the specified path (" + jsonFile.getAbsolutePath() + ") does not exist.");
//                System.out.println(("Невозможно сохранить файл. Файл по указанному пути (" + jsonFile.getAbsolutePath() + ") не существует."));
                try {
                    logger.info("Create a new file.");
                    jsonFile.createNewFile();
                    collection.setJsonFile(jsonFile);
                    this.activate(new Command(CommandList.SAVE));
                } catch (IOException e) {
                    logger.fatal("Error creating file!!! Not saved!!!");

                }
            } else if (!jsonFile.canRead() || !jsonFile.canWrite()) {
                logger.error("Unable to save file. The file is protected from reading and (or) writing.");
//                System.out.println("Невозможно сохранить файл. Файл защищён от чтения и(или) записи.");
            } else {
                FileWriter fileWriter = new FileWriter(jsonFile);
                try {
                    fileWriter.write(gson.toJson(products));
                    fileWriter.flush();
                    logger.info("File saved.");
//                    System.out.println("Файл успешно сохранён.");
                } catch (Exception ex) {
                    logger.error("File save error.");
//                    System.out.println("При записи файла что-то пошло не так.");
                } finally {
                    fileWriter.close();
                }
            }
            collection.updateDateSave();
        } catch (IOException e) {
            logger.error("Error working with the file");
        }
        return null;
    }
}
