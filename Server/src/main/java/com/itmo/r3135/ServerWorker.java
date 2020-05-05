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
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWorker implements Mediator {
    static final Logger logger = LogManager.getLogger("ServerWorker");
    ExecutorService executePool = Executors.newFixedThreadPool(8);
    ExecutorService sendPool = Executors.newFixedThreadPool(8);
    private int port;
    private DatagramSocket socket;
    private DataManager dataManager;
    private Sender sender;
    private Reader reader;
    private SQLManager sqlManager;
    private AbstractCommand loadCollectionCommand;
    private AbstractCommand addCommand;
    private AbstractCommand showCommand;
    private AbstractCommand updateIdCommand;
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
    private AbstractCommand regCommand;

    {
        dataManager = new DataManager();
        addCommand = new AddCommand(dataManager, this);
        showCommand = new ShowCommand(dataManager, this);
        updateIdCommand = new UpdeteIdCommand(dataManager, this);
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
        regCommand = new RegCommand(dataManager, this);
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
        boolean isInit = false;
        if (isConnect) isInit = sqlManager.initTables();
        dataManager.setSqlManager(sqlManager);
        return isInit;
    }

    public boolean mailInit(String mailUser, String mailPassword, String mailHost, int mailPort, boolean smtpAuth) {
        MailManager mailManager = new MailManager(mailUser, mailPassword, mailHost, mailPort, smtpAuth);
        boolean init = mailManager.initMail();
        init = init &&
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
        Thread keyBoard = new Thread(this::keyBoardWork);
        Thread datagramm = new Thread(this::datagrammWork);
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
                    if ("exit".equals(inputString)) {
                        logger.info("Command 'exit' from console.");
                        System.exit(666);
                    } else {
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
        if (command.getCommand() == CommandList.REG) {
            return regCommand.activate(command);
        } else if (command.getCommand() == CommandList.PING) {
            if (dataManager.getSqlManager().checkAccount(command))
                return new ServerMessage("Good connect and login!", true);
            else return new ServerMessage("Good connect. Please write your's login and password!\n " +
                    "Command login: 'login [email/name] [password]'\n" +
                    "Command registration: 'reg [email] [password]'", false);
        } else if (!dataManager.getSqlManager().checkAccount(command)) {
            return new ServerMessage("Incorrect login or password!\n" +
                    "Command login: 'login [email/name] [password]'\n" +
                    "Command registration: 'reg [email] [password]'\n", false);
        } else if (command.getCommand() == CommandList.CODE) {
            if (command.getString().equals(dataManager.getSqlManager().getUserCode(
                    dataManager.getSqlManager().getUserId(command.getLogin())))) {
                dataManager.getSqlManager().clearStatus(
                        dataManager.getSqlManager().getUserId(command.getLogin()));
                return new ServerMessage("Подтверждение успешно!");
            } else return new ServerMessage("Код неверный!");
        } else if (command.getCommand() != CommandList.LOGIN && dataManager.getSqlManager().isReg(
                dataManager.getSqlManager().getUserId(command.getLogin()))) {
            return new ServerMessage("Аккаунт не подтверждён! Проведьте почту\n" +
                    "Отправьте код подтвеждения командой 'code [код]' ");
        } else
            try {
                switch (command.getCommand()) {
                    case LOGIN:
                        return new ServerMessage("Good login!");
                    case HELP:
                        return helpCommand.activate(command);
                    case INFO:
                        return infoCommand.activate(command);
                    case SHOW:
                        return showCommand.activate(command);
                    case ADD:
                        return addCommand.activate(command);
                    case UPDATE:
                        return updateIdCommand.activate(command);
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


}
