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

public class SpamerWorker implements Executor {
    private DatagramChannel datagramChannel = DatagramChannel.open();
    private Sender sender;
    private Reader reader;
    private SocketAddress socketAddress;
    private StringCommandManager stringCommandManager;

    private String login = "daniil.marukh";
    private String password = sha384("1");
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
        DatagramChannel datagramChannel = DatagramChannel.open();
        sender = new Sender(datagramChannel);
        reader = new Reader(socketAddress, datagramChannel);
        reader.setExecutor(this);
    }

    public void spam() throws InterruptedException {

        Generator generator = new Generator();
        Random random = new Random();
        //reader.startListening();
        Command command;
        sender.send(new Command(CommandList.PING), socketAddress);
        while (true) {
            CommandList typeCommand = commandLists[random.nextInt(commandLists.length)];
            if (typeCommand == CommandList.PING) {
                ping();
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
            } else continue;
            command.setLoginPassword(login, password);
            sender.send(command, socketAddress);
            Thread.sleep(1);

        }
    }

    public void startWork() throws InterruptedException {
        reader.startListening();
        spam();
    }


    public long ping() {
        return PingChecker.ping(new Command(CommandList.PING), socketAddress);
    }

    @Override
    public void execute(byte[] data, SocketAddress inputAddress) {
        try (InputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream objectInputStream = new ObjectInputStream(inputStream)

        ) {
            ServerMessage serverMessage = (ServerMessage) objectInputStream.readObject();
            if (serverMessage != null)
                System.out.println(serverMessage.getMessage());
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
