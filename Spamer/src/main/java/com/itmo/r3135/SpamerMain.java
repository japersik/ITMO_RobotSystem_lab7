package com.itmo.r3135;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.channels.UnresolvedAddressException;
import java.util.Scanner;

public class SpamerMain {
    public static void main(String[] args) {

        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.println("Для начала спам-атаки ведите адрес сервера в формате \"адрес:порт\" или 'exit' для завершенеия программы.");
            System.out.print("//: ");
            if (!input.hasNextLine()) {
                break;
            }
            String inputString = input.nextLine();
            if (inputString.equals("exit")) {
                break;
            } else {
                try {
                    String[] trimString = inputString.trim().split(":", 2);
                    String addres = trimString[0];
                    int port = Integer.valueOf(trimString[1]);
                    SocketAddress socketAddress = new InetSocketAddress(addres, port);
                    System.out.println("Запуск прошёл успешно, Потр: " + port + ". Адрес: " + socketAddress);
                    SpamerWorker worker = new SpamerWorker(socketAddress);
                    if (worker.ping() != -1) {
                        //worker.spam();
                        worker.startWork();
                        break;
                    }
                    ;
                } catch (NumberFormatException e) {
                    System.out.println("Ошибка в записи номера порта.");
                } catch (IndexOutOfBoundsException | UnresolvedAddressException e) {
                    System.out.println("Адрес введён некорректно.");
                } catch (PortUnreachableException e) {
                    System.out.println("Похоже, сервер по этому адрусе недоступен");
                } catch (IOException e) {
                    System.out.println(e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

        }
        System.out.println("Работа программы завершена.");
        System.exit(0);
    }
}


