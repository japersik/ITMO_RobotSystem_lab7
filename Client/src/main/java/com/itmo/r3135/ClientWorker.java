package com.itmo.r3135;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.System.Tools.StringCommandManager;
import com.itmo.r3135.World.Product;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.util.Scanner;


public class ClientWorker {
    private SendReciveManager manager;
    private DatagramChannel datagramChannel = DatagramChannel.open();
    private SocketAddress socketAddress;
    private StringCommandManager stringCommandManager;

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
                            if (this.connectionCheck()) {
                                manager.send(command);
                                ServerMessage message = manager.recive();
                                if (message != null) {
                                    if (message.getMessage() != null)
                                        System.out.println(message.getMessage());
                                    if (message.getProducts() != null)
                                        for (Product p : message.getProducts()) System.out.println(p);
                                } else System.out.println("Ответ cервера некорректен");
                            } else System.out.println("Подключение потеряно.");
                        } else {
                            System.out.println("Команда не была отправлена.");
                        }
                    } catch (NullPointerException e) {
                        System.out.println("NullPointerException! Скорее всего неверно указана дата при создании объекта.");
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
        manager.send(new Command(CommandList.CHECK, "Привет"));
        ServerMessage recive = manager.recive();
        if (recive != null) {
            System.out.println(recive.getMessage());
            if (recive.getMessage().equals("Good connect. Hello from server!")) {
                return true;
            } else {
                System.out.println("Неверное подтверждение от сервера!");
                return false;
            }
        } else return false;
    }

}
