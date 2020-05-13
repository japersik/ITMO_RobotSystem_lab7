package com.itmo.r3135.Server;

import com.itmo.r3135.Server.Commands.*;
import com.itmo.r3135.Connector.Executor;
import com.itmo.r3135.Connector.Reader;
import com.itmo.r3135.Connector.Sender;
import com.itmo.r3135.Server.SQLconnect.MailManager;
import com.itmo.r3135.Server.SQLconnect.SQLManager;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerWorker implements Mediator, Executor {
    static final Logger logger = LogManager.getLogger("ServerWorker");
    private final int port;
    private final DataManager dataManager;
    private final AbstractCommand loadCollectionCommand;
    private final AbstractCommand addCommand;
    private final AbstractCommand showCommand;
    private final AbstractCommand updateIdCommand;
    private final AbstractCommand helpCommand;
    private final AbstractCommand removeByIdCommand;
    private final AbstractCommand groupCountingByCoordinatesCommand;
    private final AbstractCommand addIfMinCommand;
    private final AbstractCommand clearCommand;
    private final AbstractCommand printFieldDescendingPriceCommand;
    private final AbstractCommand filterContainsNameCommand;
    private final AbstractCommand removeLowerCommand;
    private final AbstractCommand removeGreaterCommand;
    private final AbstractCommand executeScriptCommand;
    private final AbstractCommand infoCommand;
    private final AbstractCommand regCommand;
    private final ExecutorService executePool = Executors.newFixedThreadPool(50);
    private final ExecutorService sendPool = Executors.newFixedThreadPool(50);
    private Sender sender;
    private Reader reader;

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

    /**
     * Инициализирует подключение к базе данных
     *
     * @param host         Адрес базы данных
     * @param port         Порт подключеиня к базе данных
     * @param dataBaseName Имя базы
     * @param user         Имя пользователя
     * @param password     Пароль
     * @return статус инициализации
     */
    public boolean SQLInit(String host, int port, String dataBaseName, String user, String password) {
        SQLManager sqlManager = new SQLManager();
        boolean isConnect = sqlManager.initDatabaseConnection(host, port, dataBaseName, user, password);
        boolean isInit = false;
        if (isConnect) isInit = sqlManager.initTables();
        dataManager.setSqlManager(sqlManager);
        return isInit;
    }

    /**
     * Инициализирует почторый клиент
     *
     * @param mailUser     Имя пользователя
     * @param mailPassword Пароль
     * @param mailHost     Адрес почтового сервера
     * @param mailPort     Порт почтового сервера
     * @param smtpAuth     Режим авторизации
     * @return Статус инициализцации
     */
    public boolean mailInit(String mailUser, String mailPassword, String mailHost, int mailPort, boolean smtpAuth) {
        MailManager mailManager = new MailManager(mailUser, mailPassword, mailHost, mailPort, smtpAuth);
        boolean init = mailManager.initMail();
        init = init &&
                mailManager.sendMail(mailUser);//адрес для теста отправки
        dataManager.setMailManager(mailManager);
        return init;
    }

    /**
     * Зарускает работу сервера
     *
     * @throws IOException
     */
    public void startWork() throws IOException {
        logger.info("Server start.");
        DatagramSocket datagramSocket = new DatagramSocket(port);
        sender = new Sender(datagramSocket);
        reader = new Reader(datagramSocket);
        logger.info("Load collection.");
        loadCollectionCommand.activate(new Command(CommandList.LOAD));
        logger.info("Server started on port " + port + ".");
        Thread keyBoard = new Thread(this::keyBoardWork);
        Thread datagram = new Thread(this::listening);
        keyBoard.setDaemon(false);
        datagram.setDaemon(true);
        keyBoard.start();
        datagram.start();
    }

    /**
     * Обработка команд с клавиатуры
     */
    public void keyBoardWork() {
        try (Scanner input = new Scanner(System.in)) {
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

    /**
     * Активирует чтение запросов
     */
    public void listening() {
        reader.setExecutor(this);
        reader.datagramRead();
        logger.info("Command reader started!");
    }

    @Override
    public void execute(byte[] data, SocketAddress inputAddress) {
        try (ObjectInputStream objectInputStream = new ObjectInputStream(
                new ByteArrayInputStream(data))) {
            Command command = (Command) objectInputStream.readObject();
            logger.info(" New command " + command.getCommand() + " from user " + command.getLogin() + "." +
                    " Address: " + inputAddress + ".");
            threadProcessing(command, inputAddress);
        } catch (IOException | ClassNotFoundException e) {
            logger.error("Deserialization error!");

        }
    }

    /**
     * Запускает поток обработки запроса
     *
     * @param command      Команда клиента
     * @param inputAddress Адрес отправителя
     */
    private void threadProcessing(Command command, SocketAddress inputAddress) {
        executePool.execute(() -> {
            ServerMessage message = processing(command);
            logger.info("Command " + command.getCommand() + " from user " + command.getLogin() + "." +
                    " Address: " + inputAddress + " complete.");
            threadSend(message, inputAddress);
        });
    }

    /**
     * Запускает поток отправки ответа
     *
     * @param message      Сообщение сервера
     * @param inputAddress Адрес получателя
     */
    private void threadSend(ServerMessage message, SocketAddress inputAddress) {
        sendPool.execute(() -> {
            logger.info("Sending server message to " + inputAddress + ".");
            sender.send(message, inputAddress);
        });
    }

    /**
     * Обрабатывает запрос клиента
     *
     * @param command Комадна клиента
     * @return Сообщение сервера
     */
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
                    "Command registration: 'reg [email] [password]'", false);
        } else if (command.getCommand() == CommandList.CODE) {
            if (command.getString().equals(dataManager.getSqlManager().getUserCode(
                    dataManager.getSqlManager().getUserId(command.getLogin())))) {
                dataManager.getSqlManager().clearStatus(
                        dataManager.getSqlManager().getUserId(command.getLogin()));
                return new ServerMessage("Подтверждение успешно!");
            } else return new ServerMessage("Код неверный!",false);
        } else if (command.getCommand() != CommandList.LOGIN && dataManager.getSqlManager().isReg(
                dataManager.getSqlManager().getUserId(command.getLogin()))) {
            return new ServerMessage("Аккаунт не подтверждён! Проведьте почту\n" +
                    "Отправьте код подтвеждения командой 'code [код]'",false);
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
