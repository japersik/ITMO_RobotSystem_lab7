package com.itmo.r3135;

import com.google.gson.Gson;
import com.itmo.r3135.Commands.*;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class ServerWorker implements Mediator {
    static final Logger logger = LogManager.getLogger("ServerWorker");
    private static final Semaphore SEMAPHORE = new Semaphore(1, true);
    private int port;
    private DatagramSocket socket;
    private Gson gson;
    private Collection collection;
    private Sender sender;
    private Reader reader;



    private AbstractCommand loadCollectionCommand;
    private AbstractCommand addCommand;
    private AbstractCommand showCommand;
    private AbstractCommand updeteIdCommand;
    private AbstractCommand helpCommand;
    private AbstractCommand removeByIdCommand;
    private AbstractCommand groupCountingByCoordinatesCommand;
    private AbstractCommand addIfMinCommand;
    private AbstractCommand clearCommand;
    private AbstractCommand printFieldDescendingPriceCommand;
    private AbstractCommand filterContainsNameCommand;
    private AbstractCommand removeLowerCommand;
    private AbstractCommand removeGreaterCommand;
    private AbstractCommand executeScriptCommand;
    private AbstractCommand infoCommand;
    private AbstractCommand saveCommand;
    private AbstractCommand exitCommand;

    {
        gson = new Gson();
        collection = new Collection();
        addCommand = new AddCommand(collection, this);
        showCommand = new ShowCommand(collection, this);
        updeteIdCommand = new UpdeteIdCommand(collection, this);
        helpCommand = new HelpCommand(collection, this);
        removeByIdCommand = new RemoveByIdCommand(collection, this);
        groupCountingByCoordinatesCommand = new GroupCountingByCoordinatesCommand(collection, this);
        addIfMinCommand = new AddIfMinCommand(collection, this);
        loadCollectionCommand = new LoadCollectionCommand(collection, this);
        clearCommand = new ClearCommand(collection, this);
        printFieldDescendingPriceCommand = new PrintFieldDescendingPriceCommand(collection, this);
        filterContainsNameCommand = new FilterContainsNameCommand(collection, this);
        removeLowerCommand = new RemoveLowerCommand(collection, this);
        removeGreaterCommand = new RemoveGreaterCommand(collection, this);
        executeScriptCommand = new ExecuteScriptCommand(collection, this);
        infoCommand = new InfoCommand(collection, this);
        saveCommand = new SaveCommand(collection, this);
        exitCommand = new ExitCommand(collection, this);
    }

    public ServerWorker(int port, String fileName) {
        this.port = port;
        logger.info("Server port: " + port);
        if (fileName == null) {
//            System.out.println("Путь к файлу json не обнаружен.");
            logger.warn("File path " + fileName + " is wrong!!!");
            System.exit(1);
        }
        File jsonPath = new File(fileName);
        if (jsonPath.exists()) {
            collection.setJsonFile(jsonPath);
//            System.out.println("Адрес " + this.collection.getJsonFile().toString() + " успешно обнаружен.");
            logger.info("Path " + fileName + " discovered.");
        } else {
//            System.out.println("Указанного пути не существует.");
            logger.warn("Path " + fileName + " does not exist.");
            try {
                logger.info("Create a new file.");
                jsonPath.createNewFile();
                collection.setJsonFile(jsonPath);
            } catch (IOException e) {
                logger.fatal("Error creating file!!!");
                System.exit(666);
            }
        }
        if (!jsonPath.isFile()) {
//            System.out.println("Путь " + jsonPath.toString() + " не содержит имени файла");
            logger.warn("Path " + fileName + " does not contain a file name.");
            System.exit(1);
        } else {
//            System.out.println("Файл " + jsonPath.toString() + " успещно обнаружен.");
            logger.info("File " + fileName + " discovered.");
        }
        if (!(fileName.lastIndexOf(".json") == fileName.length() - 5)) {
//            System.out.println("Заданный файл не в формате .json");
            logger.warn("Non .json file format.");
            System.exit(1);
        }
        logger.info("File is " + jsonPath.getAbsolutePath());
    }

    public void startWork() throws SocketException {
//        System.out.println("Инициализация сервера.");
        logger.info("Server initialization.");
        socket = new DatagramSocket(port);
        sender = new Sender(socket);
        reader = new Reader(socket);
//        System.out.println("Загрузка коллекции.");
        logger.info("Load collection.");
        loadCollectionCommand.activate(new Command(CommandList.LOAD));
//        System.out.println("Запуск прошёл успешно, Потр: " + port);
        logger.info("Server started on port " + port + ".");
        Thread keyBoard = new Thread(() -> keyBoardWork());
        Thread datagramm = new Thread(() -> datagrammWork());
        keyBoard.setDaemon(false);
        datagramm.setDaemon(true);
        keyBoard.start();
        datagramm.start();
    }

    public void keyBoardWork() {
        try (Scanner input = new Scanner(System.in);) {
            input.delimiter();
            while (true) {
                System.out.println("//:");
                if (input.hasNextLine()) {
                    String inputString = input.nextLine();
                    SEMAPHORE.acquire();
                    switch (inputString) {
                        case "exit":
                            logger.info("Command 'exit' from console.");
                            processing(new Command(CommandList.SAVE));
                            processing(new Command(CommandList.EXIT));
                            break;
                        case "save":
                            logger.info("Command 'save' from console.");
                            processing(new Command(CommandList.SAVE));
                            break;
                        default:
                            logger.error("Bad command.");
                            logger.info("Available commands:'save','exit'.");
//                            System.out.println("Доступные команды сервера: save, exit.");
                    }
                    SEMAPHORE.release();
                } else processing(new Command(CommandList.EXIT));
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void datagrammWork() {
        while (true) {
            try {

                Command command = reader.nextCommand();
                SEMAPHORE.acquire();
                logger.info("New command " + command.getCommand() + " from " + reader.getInput().getSocketAddress() + ".");
                ServerMessage message = processing(command);
                logger.info("Command complete.");
                logger.info("Sending server message.");
                sender.send(message, reader.getInput());
//                Thread.sleep(3000);// Для отладки
            } catch (IOException | InterruptedException e) {
                logger.error("Error in receive-send of command!!!");
            } finally {
                SEMAPHORE.release();
            }
        }
    }

    @Override
    public ServerMessage processing(Command command) {

        try {
            switch (command.getCommand()) {
                case CHECK:
                    return new ServerMessage("Good connect. Hello from server!");
                case HELP:
                    return helpCommand.activate(command);
                case INFO:
                    return infoCommand.activate(command);
                case SHOW:
                    return showCommand.activate(command);
                case ADD:
                    return addCommand.activate(command);
                case UPDATE:
                    return updeteIdCommand.activate(command);
                case REMOVE_BY_ID:
                    return removeByIdCommand.activate(command);
                case CLEAR:
                    return clearCommand.activate(command);
                case EXECUTE_SCRIPT:
                    return executeScriptCommand.activate(command);
                case ADD_IF_MIN:
                    return addIfMinCommand.activate(command);
                case REMOVE_GREATER:
                    return removeGreaterCommand.activate(command);
                case REMOVE_LOWER:
                    return removeLowerCommand.activate(command);
                case GROUP_COUNTING_BY_COORDINATES:
                    return groupCountingByCoordinatesCommand.activate(command);
                case FILTER_CONTAINS_NAME:
                    return filterContainsNameCommand.activate(command);
                case PRINT_FIELD_DESCENDING_PRICE:
                    return printFieldDescendingPriceCommand.activate(command);
                case SAVE:
                    return saveCommand.activate(command);
                case EXIT:
                    return exitCommand.activate(command);
                default:
                    logger.warn("Bad command!");
            }
        } catch (NumberFormatException ex) {
            logger.error("Bad number in command!!!");
        }

        return new ServerMessage("Битая команда");
    }
}
