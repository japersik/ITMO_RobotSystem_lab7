package com.itmo.r3135;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.BindException;
import java.util.Properties;
import java.util.Scanner;

/**
 * @author daniil
 * @author vladislav
 */
public class ServerMain {
    private static final Logger logger = LogManager.getLogger("ServerStarter");
    private final static String FILENAME = "file.json";
    private final static String propFileName = "config.properties";
    private final static String defPropFileName = "config.properties";

    public static void main(String[] args) throws IOException {
        logger.info("The program started.");
        File propFile = new File(propFileName);

        if (!propFile.exists()) {
            logger.warn("Creating default config file.");
            try (InputStream in = ServerMain.class
                    .getClassLoader()
                    .getResourceAsStream(defPropFileName);
                 OutputStream out = new FileOutputStream(propFileName)) {
                int data;
                while ((data = in.read()) != -1) {
                    out.write(data);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //Добавить проверку файла на пригодность
        try (InputStream inputStream = new FileInputStream(propFileName);) {
            logger.info("Loading setting from " + propFile.getAbsolutePath() + ".");
            Properties properties = new Properties();
            properties.load(inputStream);
            //Отладочная проверка настроек из файла
            System.out.println(properties.getProperty("port"));
            System.out.println(properties.getProperty("db_host"));
            System.out.println(properties.getProperty("db_port"));
            System.out.println(properties.getProperty("db_name"));
            System.out.println(properties.getProperty("db_user"));
            System.out.println(properties.getProperty("db_password"));
        }


        Scanner input = new Scanner(System.in);
        logger.info("The port reader started.");
        while (true) {
            logger.info("To start the server, enter the port or 'exit' to exit the program.");
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
                    } else {
                        ServerWorker worker = new ServerWorker(port, FILENAME);
                        worker.startWork();
                        break;
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid number format in '" + inputString + "' !");
                } catch (BindException e) {
                    logger.error("The port " + inputString + " is busy.");
                }
            }
        }
    }
}

