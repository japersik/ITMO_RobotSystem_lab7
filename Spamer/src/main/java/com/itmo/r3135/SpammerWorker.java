package com.itmo.r3135;

import com.itmo.r3135.Connector.Executor;
import com.itmo.r3135.Connector.PingChecker;
import com.itmo.r3135.Connector.Reader;
import com.itmo.r3135.Connector.Sender;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.System.Tools.StringCommandManager;
import com.itmo.r3135.World.Generator;
import com.itmo.r3135.World.Product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Scanner;

public class SpammerWorker implements Executor {
    private final Reader reader;
    private final Sender sender;
    private final SocketAddress socketAddress;
    private final StringCommandManager stringCommandManager;
    private final CommandList[] commandLists = {
//            CommandList.HELP,
//            CommandList.INFO,
            CommandList.ADD,
//            CommandList.SHOW,
//            CommandList.UPDATE,
//            CommandList.REMOVE_BY_ID,
//            CommandList.CLEAR,
//            CommandList.EXECUTE_SCRIPT,
//            CommandList.ADD_IF_MIN,
//            CommandList.REMOVE_GREATER,
//            CommandList.REMOVE_LOWER,
//            CommandList.GROUP_COUNTING_BY_COORDINATES,
//            CommandList.FILTER_CONTAINS_NAME,
//            CommandList.PRINT_FIELD_DESCENDING_PRICE,
//            CommandList.PING
    };
    private boolean isLogin;
    private boolean isSpam;
    private String login = "";
    private String password = "";

    {
        stringCommandManager = new StringCommandManager();
    }


    public SpammerWorker(SocketAddress socketAddress) throws IOException {
        this.socketAddress = socketAddress;
        DatagramChannel datagramChannel = DatagramChannel.open();
        sender = new Sender(datagramChannel);
        reader = new Reader(socketAddress, datagramChannel);
        reader.setExecutor(this);
    }

    public void spam() {
        Random random = new Random();
        Command command;
        try {
            while (isLogin) {
                CommandList typeCommand = commandLists[random.nextInt(commandLists.length)];
                if (typeCommand == CommandList.PING || typeCommand == CommandList.HELP || typeCommand == CommandList.INFO ||
                        typeCommand == CommandList.SHOW || typeCommand == CommandList.CLEAR ||
                        typeCommand == CommandList.PRINT_FIELD_DESCENDING_PRICE ||
                        typeCommand == CommandList.GROUP_COUNTING_BY_COORDINATES) {
                    command = new Command(typeCommand);
                } else if (typeCommand == CommandList.ADD || typeCommand == CommandList.ADD_IF_MIN ||
                        typeCommand == CommandList.REMOVE_GREATER || typeCommand == CommandList.REMOVE_LOWER) {
                    Product product;
                    do {
                        product = Generator.nextProduct();
                    } while (product.checkNull());
                    command = new Command(typeCommand, product);
                } else continue;
                command.setLoginPassword(login, password);
                System.out.println("Отправка команды " + command.getCommand());
                sender.send(command, socketAddress);
                Thread.sleep(10);
            }
        } catch (InterruptedException e) {
            System.out.println("Спам атака была остановлена.");
        }
        System.out.println("Спам-атака завершена!");
    }

    public void startWork() {
        reader.startListening();
        isLogin = false;
        isSpam = false;
        Thread spamer = new Thread(this::spam);
        spamer.setDaemon(true);
        String commandString;
        try (Scanner commandReader = new Scanner(System.in)) {
            while (true) {
                if (!commandReader.hasNextLine()) break;
                System.out.print("//: ");
                commandString = commandReader.nextLine();
                if (isSpam) {
                    spamer.interrupt();
                    isSpam = false;
                    System.out.println("Спам-атака остановлена");
                }
                if (commandString.equals("exit"))
                    break;
                if (commandString.equals("spam")) {
                    if (isLogin) {
                        spamer = new Thread(this::spam);
                        spamer.start();
                        isSpam = true;
                        System.out.println("Спам-анака начата");
                    } else System.out.println("Вы не авторизованы");
                } else {
                    try {
                        Command command = stringCommandManager.getCommandFromString(commandString);
                        if (command != null) {
                            if (command.getCommand() == CommandList.REG || command.getCommand() == CommandList.LOGIN) {
                                login = command.getLogin();
                                password = sha384(command.getPassword());
                                command.setPassword(password);
                            } else {
                                command.setLoginPassword(login, password);
                            }
                            sender.send(command, socketAddress);
                        } else {
                            System.out.println("Команда не была отправлена.");
                        }
                    } catch (NullPointerException e) {
                        System.out.println("NullPointerException! Скорее всего неверно указана дата при создании объекта.");
                        e.printStackTrace();
                    }
                }
            }
        }
    }


    public long ping() {
        Command command = new Command(CommandList.PING);
        command.setLoginPassword(login, password);
        return PingChecker.ping(command, socketAddress);
    }

    @Override
    public void execute(byte[] data, SocketAddress inputAddress) {
        try (InputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)
        ) {
            ServerMessage serverMessage = (ServerMessage) objectInputStream.readObject();
            if (serverMessage != null) {
                isLogin = serverMessage.getLogin();
                if (!isLogin) System.out.println(serverMessage.getMessage());
                else if (isSpam) {

                } else {
                    if (serverMessage.getMessage() != null)
                        System.out.println(serverMessage.getMessage());
                    if (serverMessage.getProducts() != null)
                        for (Product p : serverMessage.getProducts()) System.out.println(p);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public String sha384(String password) {
        if (password == null) return null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            byte[] messageDigest = md.digest(password.getBytes());
            BigInteger no = new BigInteger(1, messageDigest);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            return password;
        }
    }
}
