package com.itmo.r3135;

import com.itmo.r3135.Commands.*;
import com.itmo.r3135.SQLconnect.MailManager;
import com.itmo.r3135.SQLconnect.SQLManager;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private int port;
    private DatagramSocket socket;
    private DataManager dataManager;
    private Sender sender;
    private Reader reader;

    private SQLManager sqlManager;
    ExecutorService executePool = Executors.newFixedThreadPool(8);
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

    {
        dataManager = new DataManager();
        addCommand = new AddCommand(dataManager, this);
        showCommand = new ShowCommand(dataManager, this);
        updeteIdCommand = new UpdeteIdCommand(dataManager, this);
        helpCommand = new HelpCommand(dataManager, this);
        removeByIdCommand = new RemoveByIdCommand(dataManager, this);
        groupCountingByCoordinatesCommand = new GroupCountingByCoordinatesCommand(dataManager, this);
        addIfMinCommand = new AddIfMinCommand(dataManager, this);
        loadCollectionCommand = new LoadCollectionCommand(dataManager, this);
        clearCommand = new ClearCommand(dataManager, this);
        printFieldDescendingPriceCommand = new PrintFieldDescendingPriceCommand(dataManager, this);
        filterContainsNameCommand = new FilterContainsNameCommand(dataManager, this);
        removeLowerCommand = new RemoveLowerCommand(dataManager, this);
        removeGreaterCommand = new RemoveGreaterCommand(dataManager, this);
        executeScriptCommand = new ExecuteScriptCommand(dataManager, this);
        infoCommand = new InfoCommand(dataManager, this);
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
        dataManager.setSqlManager(sqlManager);
        return isConnect && isInit;
    }

    public boolean mailInit(String mailUser, String mailPassword, String mailHost, int mailPort, boolean smtpAuth) {
        MailManager mailManager = new MailManager(mailUser, mailPassword, mailHost, mailPort, smtpAuth);

        boolean init = mailManager.initMail();

//                init = init &&
        mailManager.sendMail(mailUser);//адрес для теста отправки
        dataManager.setMailManager(mailManager);
        return init;
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
                    switch (inputString) {
                        case "exit":
                            logger.info("Command 'exit' from console.");
                            System.exit(666);
                            break;
                        default:
                            logger.error("Bad command.");
                            logger.info("Available commands:,'exit'.");
                    }
                } else {
                    System.exit(666);
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
                logger.info("New command " + command.getCommand() +
                        " from " + reader.getInput().getSocketAddress() + ". User: " + command.getLogin() + ".");
                threadProcessing(command, reader.getInput().getSocketAddress());
            } catch (IOException e) {
                logger.error("Error in receive-send of command!!!" + e);
            }
        }
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
//Приверка, что команда - регистрация.
        if (command.getCommand() == CommandList.REG) {
            try {
                if (!checkEmail(command.getLogin())) return new ServerMessage("Incorrect login!", false);
                PreparedStatement statement = dataManager.getSqlManager().getConnection().prepareStatement(
                        "insert into users (email, password_hash, username) values (?, ?, ?)"
                );
                statement.setString(1, command.getLogin());
                statement.setBytes(2, command.getPassword().getBytes());
                statement.setString(3, emailParse(command.getLogin()));
                try {
                    statement.execute();
                } catch (SQLException e) {
                    logger.error("Попытка добавления по существующему ключу");
                    return new ServerMessage("Пользователь с именем " + emailParse(command.getLogin()) + " уже существует!");
                }
                if (!dataManager.getMailManager().sendMailHTML(command.getLogin(), emailParse(command.getLogin()))) {
                    logger.error("ERROR IN EMAIL SENDING TO " + command.getLogin());
                    return new ServerMessage("Successful registration!");
                }
                return new ServerMessage("Successful registration check your email :)");

            } catch (SQLException e) {
                logger.error("Бда, бда SQLException");
                return new ServerMessage("Ошибка регистрации");
            }
        }
        //Если это первое сообщение от пользователя
        else if (command.getPassword() == null & command.getLogin() == null) {
            return new ServerMessage("Good connect. Please write your's login and password!\n " +
                    "Command login: 'login [email/name] [password]'\n" +
                    "Command registration: 'reg [email] [password]'", false);
        } else
//если не первое
            if (!checkAccount(command)) {
                return new ServerMessage("Incorrect login or password!\n" +
                        "Command login: 'login [email/name] [password]'\n" +
                        "Command registration: 'reg [email] [password]'\n", false);
            } else
                try {
                    switch (command.getCommand()) {
                        case LOGIN:
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
                        default:
                            logger.warn("Bad command!");
                            return new ServerMessage("Битая команда!");
                    }
                } catch (NumberFormatException ex) {
                    logger.error("Bad number in command!!!");
                    return new ServerMessage("Ошибка записи числа в команде.");
                }
    }

    private String emailParse(String email) {
        String username = email.split("@")[0];
        return username;
    }

    private boolean checkAccount(Command command) {
        try {
            PreparedStatement statement = dataManager.getSqlManager().getConnection().prepareStatement(
                    "select * from users where (email = ? or username =?) and password_hash = ?"
            );
            statement.setString(1, command.getLogin());
            statement.setString(2, command.getLogin());
            statement.setBytes(3, command.getPassword().getBytes());
            ResultSet resultSet = statement.executeQuery();
            if (!resultSet.next()) return false;
            else return true;
        } catch (SQLException e) {
            logger.error(e);
            return false;
        }
    }

    private Boolean checkEmail(String email) {
        try {
            String[] login = email.split("@");
            if (login.length != 2) return false;
            String[] address = login[1].split("\\.");
            if (address.length != 2) return false;
        } catch (Exception e) {
        }
        return true;
    }
}
