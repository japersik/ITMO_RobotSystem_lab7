package com.itmo.r3135;

import com.google.gson.Gson;
import com.itmo.r3135.Commands.*;
import com.itmo.r3135.SQLconnect.SQLManager;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.util.PSQLException;

import java.io.File;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWorker implements Mediator {
    static final Logger logger = LogManager.getLogger("ServerWorker");
    //   private static final Semaphore SEMAPHORE = new Semaphore(1, true);
    private int port;
    private DatagramSocket socket;
    private Gson gson;
    private Collection collection;
    private Sender sender;
    private Reader reader;

    private SQLManager sqlManager;
    //Не знаю, зачем нам вообще многопоточное чтение запросов, ибо у нас всё равно udp
    //ExecutorService readPool = Executors.newFixedThreadPool(3);
    //пока один поток на исполнение, т.к. команды НЕпотокобезопасны для тестов
    ExecutorService executePool = Executors.newFixedThreadPool(1);
    ExecutorService sendPool = Executors.newFixedThreadPool(8);

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

    public ServerWorker(int port) {
        logger.info("Server initialization.");
        this.port = port;
        logger.info("Server port set: " + port);
    }

    //метод инициализации базы Данных
    public boolean SQLInit(String host, int port, String dataBaseName, String user, String password) {
        sqlManager = new SQLManager();
        boolean isConnect = sqlManager.initDatabaseConnection(host, port, dataBaseName, user, password);
        boolean isInit = sqlManager.initTables();
        collection.setSqlManager(sqlManager);
        return isConnect && isInit;
    }

    public boolean mailInit() {
        return true;
    }

    public ServerWorker(int port, String fileName) {
        this.port = port;
        logger.info("Server port: " + port);
        if (fileName == null) {
            logger.warn("File path " + fileName + " is wrong!!!");
            System.exit(1);
        }
        File jsonPath = new File(fileName);
        if (jsonPath.exists()) {
            collection.setJsonFile(jsonPath);
            logger.info("Path " + fileName + " discovered.");
        } else {
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
            logger.warn("Path " + fileName + " does not contain a file name.");
            System.exit(1);
        } else {
            logger.info("File " + fileName + " discovered.");
        }
        if (!(fileName.lastIndexOf(".json") == fileName.length() - 5)) {
            logger.warn("Non .json file format.");
            System.exit(1);
        }
        logger.info("File is " + jsonPath.getAbsolutePath());
    }

    public void startWork() throws SocketException {
        logger.info("Server start.");
        socket = new DatagramSocket(port);
        sender = new Sender(socket);
        reader = new Reader(socket);
        logger.info("Load collection.");
        loadCollectionCommand.activate(new Command(CommandList.LOAD));
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
                System.out.print("//: ");
                if (input.hasNextLine()) {
                    String inputString = input.nextLine();
//                    SEMAPHORE.acquire();
                    switch (inputString) {
                        case "exit":
                            logger.info("Command 'exit' from console.");
//                            processing(new Command(CommandList.SAVE));
                            processing(new Command(CommandList.EXIT));
                            System.exit(666);
                            break;
//                        case "save":
//                            logger.info("Command 'save' from console.");
//                            processing(new Command(CommandList.SAVE));
//                            break;
                        default:
                            logger.error("Bad command.");
                            logger.info("Available commands:,'exit'.");
                    }
//                    SEMAPHORE.release();
                } else {
//                    processing(new Command(CommandList.SAVE));
                    processing(new Command(CommandList.EXIT));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void datagrammWork() {
        while (true) {
            try {
                Command command = reader.nextCommand();
                logger.info("New command " + command.getCommand() + " from " + reader.getInput().getSocketAddress() + ".");
                threadProcessing(command, reader.getInput().getSocketAddress());
            } catch (IOException e) {
                logger.error("Error in receive-send of command!!!" + e);
            }
        }
//                Command command = reader.nextCommand();
//                SEMAPHORE.acquire();
//                logger.info("New command " + command.getCommand() + " from " + reader.getInput().getSocketAddress() + ".");
//                ServerMessage message = processing(command);
//                logger.info("Command complete.");
//                logger.info("Sending server message.");
//                sender.send(message, reader.getInput());
////                Thread.sleep(3000);// Для отладки
//            } catch (IOException | InterruptedException e) {
//                logger.error("Error in receive-send of command!!!");
//            } finally {
//                SEMAPHORE.release();
    }


    private void threadProcessing(Command command, SocketAddress inputAddress) {
        executePool.execute(() -> {
            ServerMessage message = processing(command);
            logger.info("Command from " + inputAddress + " complete.");
            threadSend(message, inputAddress);
        });
    }

    private void threadSend(ServerMessage message, SocketAddress inputAddress) {
        sendPool.execute(() -> {
            try {
                logger.info("Sending server message to " + inputAddress + ".");
                sender.send(message, inputAddress);
            } catch (IOException | InterruptedException e) {
                logger.error("Error in send of message on " + inputAddress + "!!!");
            }
        });
    }

    @Override
    public ServerMessage processing(Command command) {
//впихнуть проверку авторизации
        if (command.getPassword() == null & command.getLogin() == null) {
            return new ServerMessage("Good connect. Please write your's login and password!", false);
        }
        try {
            if (command.getCommand() == CommandList.LOGIN) {
                PreparedStatement statement = collection.getSqlManager().getConnection().prepareStatement(
                        "select * from users where email = ? and password_hash = ?"
                );
                statement.setString(1, command.getLogin());
                statement.setBytes(2, command.getPassword().getBytes());
                ResultSet resultSet = statement.executeQuery();
                if (!resultSet.next()) return new ServerMessage("Incorrect login or password!", false);
                else return new ServerMessage("Good connect. Hello from server!");
            }
            if (command.getCommand() == CommandList.REG) {
                PreparedStatement statement = collection.getSqlManager().getConnection().prepareStatement(
                        "insert into users (email, password_hash) values (?, ?)"
                );
                statement.setString(1, command.getLogin());
                statement.setBytes(2, command.getPassword().getBytes());
                try {
                    statement.execute();
                } catch (SQLException e) {
                    logger.error("Попытка добавления по существующему ключу");
                    return new ServerMessage("Такой пользователь уже существует!");
                }
                return new ServerMessage("Successful registration!");
            }
        } catch (SQLException e) {
            logger.error("Бда, бда SQLException");
        }


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
                    return new ServerMessage("Битая команда!");
            }
        } catch (NumberFormatException ex) {
            logger.error("Bad number in command!!!");
            return new ServerMessage("Ошибка записи числа в команде.");
        }
    }
}
