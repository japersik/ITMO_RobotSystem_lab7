package com.itmo.r3135;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.System.Tools.StringCommandManager;
import com.itmo.r3135.World.Product;

import java.io.IOException;
import java.math.BigInteger;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;


public class ClientWorker {
    private SendReciveManager manager;
    private DatagramChannel datagramChannel = DatagramChannel.open();
    private SocketAddress socketAddress;
    private StringCommandManager stringCommandManager;
    private String login;
    private String password;


    {
        stringCommandManager = new StringCommandManager();
    }

    public ClientWorker(SocketAddress socketAddress) throws IOException {
        this.socketAddress = socketAddress;
        manager = new SendReciveManager(socketAddress, datagramChannel);
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
                            if (command.getCommand() == CommandList.LOGIN) {
                                login = command.getLogin();
                                password = sha384(command.getPassword());
                                System.out.println(password);
                                this.connectionCheck();
                            } else if (command.getCommand() != CommandList.REG)
                                command.setLoginPassword(login, password);
                            else {
                                login = command.getLogin();
                                password = sha384(command.getPassword());
                                command.setPassword(password);
                            }
                            manager.send(command);
                            ServerMessage message = manager.recive();
                            if (message != null) {
                                if (message.getMessage() != null)
                                    System.out.println(message.getMessage());
                                if (message.getProducts() != null)
                                    for (Product p : message.getProducts()) System.out.println(p);
                            } else System.out.println("Ответ cервера некорректен");
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

    public boolean connectionCheck() throws IOException, InterruptedException {
        System.out.println("Проверка соединения:");
        datagramChannel.connect(socketAddress);
        datagramChannel.disconnect();
        datagramChannel.socket().setSoTimeout(1000);
        Command command = new Command(CommandList.LOGIN, "Привет");
        command.setLoginPassword(login, password);
        manager.send(command);
        ServerMessage recive = manager.recive();
        if (recive != null) {
            if (recive.getLogin() == false) {
                System.out.println(recive.getMessage());
                return true;
            }
            if (recive.getMessage().equals("Good connect. Hello from server!")) {
                return true;
            } else {
                System.out.println("Неверное подтверждение от сервера!");
                return false;
            }
        } else {
            System.out.println("Соединение не установлено.");
            return false;
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
}
