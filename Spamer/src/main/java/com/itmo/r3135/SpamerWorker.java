package com.itmo.r3135;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.System.Tools.StringCommandManager;
import com.itmo.r3135.World.Generator;
import com.itmo.r3135.World.Product;

import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.Scanner;

public class SpamerWorker {
    private SendReceiveManager manager;
    private DatagramChannel datagramChannel = DatagramChannel.open();
    private SocketAddress socketAddress;
    private StringCommandManager stringCommandManager;
    private String login = "";
    private String password = "";

    private CommandList[] commandLists = {
            CommandList.HELP,
            CommandList.INFO,
            CommandList.ADD,
            CommandList.SHOW,
            CommandList.UPDATE,
            CommandList.REMOVE_BY_ID,
            //CommandList.CLEAR,
            CommandList.EXECUTE_SCRIPT,
            CommandList.ADD_IF_MIN,
            //CommandList.REMOVE_GREATER,
            // CommandList.REMOVE_LOWER,
            CommandList.GROUP_COUNTING_BY_COORDINATES,
            CommandList.FILTER_CONTAINS_NAME,
            CommandList.PRINT_FIELD_DESCENDING_PRICE,
            CommandList.PING
    };

    {
        stringCommandManager = new StringCommandManager();
    }

    public SpamerWorker(SocketAddress socketAddress) throws IOException {
        this.socketAddress = socketAddress;
        manager = new SendReceiveManager(socketAddress, datagramChannel);
        datagramChannel.configureBlocking(false);
    }

    public void startWork() throws IOException {
        String commandString = "";
        try (Scanner commandReader = new Scanner(System.in)) {
            System.out.print("//: ");
            while (!commandString.equals("exit")) {
                if (!commandReader.hasNextLine()) {
                    break;
                } else {
                    try {
                        commandString = commandReader.nextLine();
                        Command command = stringCommandManager.getCommandFromString(commandString);
                        if (command != null) {
                            if (command.getCommand() == CommandList.REG || command.getCommand() == CommandList.LOGIN) {
                                login = command.getLogin();
                                password = sha384(command.getPassword());
                                command.setPassword(password);
                            } else {
                               continue;
                            }
                            manager.send(command);
                            ServerMessage message = manager.recive();
                            if (message != null) {
                                if (message.getLogin())
                                    spam();
                                System.out.println(message.getMessage());
                            } else System.out.println("Ответ сервера некорректен");
                        } else {
                            System.out.println("Команда не была отправлена.");
                        }
                    } catch (NullPointerException e) {
                        System.out.println("NullPointerException! Скорее всего неверно указана дата при создании объекта.");
                        e.printStackTrace();
                    }
                }
                System.out.print("//: ");
            }
        } catch (InterruptedException e) {
            System.out.println("Ошибка при попытке считать ответ от сервера.");
        }
    }


    public void spam() throws InterruptedException {
        Generator generator = new Generator();
        Random random = new Random();
        Command command;
        while (true) {
            CommandList typeCommand = commandLists[random.nextInt(commandLists.length)];
            if (typeCommand == CommandList.PING) {
                manager.ping(login,password);
                continue;
            } else if (typeCommand == CommandList.HELP || typeCommand == CommandList.INFO || typeCommand == CommandList.SHOW ||
                    typeCommand == CommandList.CLEAR || typeCommand == CommandList.PRINT_FIELD_DESCENDING_PRICE || typeCommand == CommandList.GROUP_COUNTING_BY_COORDINATES) {
                command = new Command(typeCommand);
            } else if (typeCommand == CommandList.ADD || typeCommand == CommandList.ADD_IF_MIN || typeCommand == CommandList.REMOVE_GREATER ||
                    typeCommand == CommandList.REMOVE_LOWER) {
                Product product;
                while (true) {
                    product = generator.nextProduct();
                    if (!product.checkNull()) break;
                }
                command = new Command(typeCommand, product);
                command.setLoginPassword(login, password);
            } else continue;
            manager.send(command);
            Thread.sleep(1);
//            if (command.getCommand() == CommandList.PING) {
//                connectionCheck();
//            }
        }
    }

    public String sha384(String password) {
        if (password == null) return password;
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

    public long ping() {
        return manager.ping();
    }
    public long ping(String login, String password) {
        return manager.ping(login, password);
    }
}
