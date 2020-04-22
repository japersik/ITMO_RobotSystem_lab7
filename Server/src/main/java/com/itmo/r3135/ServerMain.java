package com.itmo.r3135;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.BindException;
import java.util.Scanner;

/**
 * @author daniil
 * @author vladislav
 */
public class ServerMain {
    static final Logger logger = LogManager.getLogger("ServerStarter");
    private final static String FILENAME = "file.json";

    public static void main(String[] args) throws IOException {

        logger.info("The program started.");
        Scanner input = new Scanner(System.in);
        logger.info("The port reader started.");
        while (true) {
            logger.info("To start the server, enter the port or 'exit' to exit the program.");
//            System.out.println("Для начала работы сервера введите порт или 'exit' для завершенеия программы.");
            System.out.print("//: ");
            if (!input.hasNextLine()) {
                break;
            }
            String inputString = input.nextLine();
            if (inputString.equals("exit")) {
                logger.info("The program has completed.");
                System.exit(0);
            } else {
                try {
                    int port = Integer.valueOf(inputString);
                    if (port < 0 || port > 65535) {
                        logger.error("Wrong port!");
                        logger.error("Port is a number from 0 to 65535");
//                        System.out.println("Порт - число от 0 до 65535.");
                    } else {
                        ServerWorker worker = new ServerWorker(port, FILENAME);
                        worker.startWork();
                        break;
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid number format in '" + inputString + "' !");
//                    System.out.println("Ошибка в записи номера порта.");
                } catch (BindException e) {
                    logger.error("The port " + inputString + " is busy.");
//                    System.out.println("Этот порт уже занят.");
                }
            }
        }
        //   logger.info("The program has completed.");
        //   System.out.println("Работа сервера заверщена.");
    }
}

