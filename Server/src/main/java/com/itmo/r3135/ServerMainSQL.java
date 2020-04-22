package com.itmo.r3135;

import com.itmo.r3135.SQLconnect.SQLconnectToMSserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Scanner;

/**
 * @author daniil
 * @author vladislav
 */
public class ServerMainSQL {
    static final Logger logger = LogManager.getLogger("ServerStarter");

    public static void main(String[] args) throws IOException, SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("PostgreSQL JDBC Driver is not found. Include it in your library path ");
            e.printStackTrace();
            return;
        }
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
                SQLconnectToMSserver server = new SQLconnectToMSserver();
//                try {
//                    SQLconnectToMSserver server = new SQLconnectToMSserver();
//                } catch (SQLException e) {
//                    logger.error(e);
//                }
            }
        }
        //   logger.info("The program has completed.");
        //   System.out.println("Работа сервера заверщена.");
    }
}

