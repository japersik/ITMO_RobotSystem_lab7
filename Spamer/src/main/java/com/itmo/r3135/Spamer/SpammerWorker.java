package com.itmo.r3135.Spamer;

import com.itmo.r3135.Connector.Executor;
import com.itmo.r3135.Connector.PingChecker;
import com.itmo.r3135.Connector.Reader;
import com.itmo.r3135.Connector.Sender;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.System.Tools.StringCommandManager;
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
import java.util.Scanner;

public class SpammerWorker implements Executor {
    public final Sender sender;
    public final SocketAddress socketAddress;
    private final Reader reader;
    private final StringCommandManager stringCommandManager = new StringCommandManager();
    private final SpamTools spamrun = new SpamTools(this);
    public boolean isLogin = false;
    public String login = "";
    public String password = "";

    public SpammerWorker(SocketAddress socketAddress) throws IOException {
        this.socketAddress = socketAddress;
        DatagramChannel datagramChannel = DatagramChannel.open();
        sender = new Sender(datagramChannel);
        reader = new Reader(datagramChannel);
        reader.setExecutor(this);
    }


    public void startWork() {
        reader.startListening();
        String commandString;
        try (Scanner commandReader = new Scanner(System.in)) {
            while (true) {
                System.out.print("//: ");
                if (!commandReader.hasNextLine()) break;
                commandString = commandReader.nextLine();
                if (spamrun.isSpam()) {
                    spamrun.stopSpam();
                }
                if (commandString.equals("exit"))
                    break;
                if (!spamrun.execute(commandString) && !commandString.isEmpty()) {
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
                else if (spamrun.isSpam()) {
                    //обработка пришедших сообшений для spam - режима
                } else {
                    if (serverMessage.getMessage() != null)
                        System.out.println(serverMessage.getMessage());
                    if (serverMessage.getProducts() != null)
                        for (Product p : serverMessage.getProducts()) System.out.println(p);
                    System.out.print("//: ");
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
