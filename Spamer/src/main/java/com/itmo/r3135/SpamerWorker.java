package com.itmo.r3135;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.System.Tools.StringCommandManager;
import com.itmo.r3135.World.Generator;
import com.itmo.r3135.World.Product;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.Random;

public class SpamerWorker {
    private SendReciveManager manager;
    private DatagramChannel datagramChannel = DatagramChannel.open();
    private SocketAddress socketAddress;
    private StringCommandManager stringCommandManager;
    private CommandList[] commandLists = {CommandList.HELP, CommandList.INFO, CommandList.ADD, CommandList.SHOW, CommandList.UPDATE, CommandList.REMOVE_BY_ID,
            //CommandList.CLEAR,
            CommandList.EXECUTE_SCRIPT, CommandList.ADD_IF_MIN,
            //CommandList.REMOVE_GREATER,
            // CommandList.REMOVE_LOWER,
             CommandList.GROUP_COUNTING_BY_COORDINATES, CommandList.FILTER_CONTAINS_NAME, CommandList.PRINT_FIELD_DESCENDING_PRICE, CommandList.CHECK};

    {
        stringCommandManager = new StringCommandManager();
    }

    public SpamerWorker(SocketAddress socketAddress) throws IOException {
        this.socketAddress = socketAddress;
        manager = new SendReciveManager(socketAddress, datagramChannel);
        datagramChannel.configureBlocking(false);
    }
    //надо сделать красиое управление
//    public void startWork() throws IOException, InterruptedException {
//        String commandString = "";
//        try (Scanner commandReader = new Scanner(System.in)) {
//            System.out.print("//: ");
//
//            while (!commandString.equals("exit")) {
//                if (!commandReader.hasNextLine()) {
//                    break;
//                } else {
//                    commandString = commandReader.nextLine();
//                    Command command = stringCommandManager.getCommandFromString(commandString);
//                    if (command != null) {
//                        if (this.connectionCheck()) {
//                            manager.send(command);
//                            ServerMessage message = manager.recive();
//                            if (message != null) {
//                                if (message.getMessage() != null)
//                                    System.out.println(message.getMessage());
//                                if (message.getProducts() != null)
//                                    for (Product p : message.getProducts()) System.out.println(p);
//                            } else System.out.println("Ответ cервера некорректен");
//                        } else System.out.println("Подключение потеряно.");
//                    } else {
//                        System.out.println("Команда не была отправлена.");
//                    }
//                }
//                System.out.print("//: ");
//            }
//        }
//    }

    public void spam() throws InterruptedException, IOException {
        Generator generator = new Generator();
        Random random = new Random();
        Command command;
        while (true) {
            CommandList typeCommand = commandLists[random.nextInt(commandLists.length)];
            if (typeCommand == CommandList.CHECK) {
                connectionCheck();
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
            manager.send(command);
            Thread.sleep(1);
            if (command.getCommand() == CommandList.CHECK) {
                connectionCheck();
            }
        }
    }

    public boolean connectionCheck() throws IOException, InterruptedException {

        System.out.println("Проверка соединения:");
        datagramChannel.connect(socketAddress);
        datagramChannel.disconnect();
        datagramChannel.socket().setSoTimeout(1000);
        Date sendDate = new Date();
        manager.send(new Command(CommandList.CHECK, "Привет"));
        ServerMessage recive = manager.recive();
        if (recive != null) {
            System.out.println(recive.getMessage());
            Date resiveDate = new Date();
            System.out.println("Время ответа: " + (resiveDate.getTime() - sendDate.getTime()) + " ms.");
            if (recive.getMessage().equals("Good connect. Hello from server!")) {
                return true;
            } else {
                System.out.println("Неверное подтверждение от сервера!");
                return false;
            }
        } else return false;
    }

}
