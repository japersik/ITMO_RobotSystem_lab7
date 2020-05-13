package com.itmo.r3135.Client;

import com.itmo.r3135.Connector.*;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.System.Tools.StringCommandManager;
import com.itmo.r3135.World.Product;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;


public class ClientWorker implements Executor {
    private final Sender sender;
    private final Reader reader;
    private final SocketAddress socketAddress;
    private final StringCommandManager stringCommandManager;

    private String login = "";
    private String password = "";


    {
        stringCommandManager = new StringCommandManager();
    }

    public ClientWorker(SocketAddress socketAddress) throws IOException {
        this.socketAddress = socketAddress;
        DatagramChannel datagramChannel = DatagramChannel.open();
        sender = new Sender(datagramChannel);
        reader = new Reader(datagramChannel);
        reader.setExecutor(this);
    }

    public void startWork() {
        reader.startListening();
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
                System.out.print("//: ");
            }
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

    public long ping() {
        return PingChecker.ping(new Command(CommandList.PING), socketAddress);
    }

    @Override
    public void execute(byte[] data, SocketAddress inputAddress) {
        try (
                ObjectInputStream objectInputStream = new ObjectInputStream(
                        new ByteArrayInputStream(data))
        ) {
            ServerMessage serverMessage = (ServerMessage) objectInputStream.readObject();
            objectInputStream.close();
            if (serverMessage != null) {
                if (serverMessage.getMessage() != null)
                    System.out.println(serverMessage.getMessage());
                if (serverMessage.getProducts() != null)
                    for (Product p : serverMessage.getProducts()) System.out.println(p);
            } else System.out.println("Ответ сервера некорректен.");

        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка десериализации.");
        }

    }
}
