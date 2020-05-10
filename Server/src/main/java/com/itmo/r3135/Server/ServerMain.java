package com.itmo.r3135.Server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.BindException;
import java.util.Properties;

/**
 * @author daniil
 * @author vladislav
 */
public class ServerMain {
    private static final Logger logger = LogManager.getLogger("ServerStarter");
    private final static String defPropFileName = "config.properties";

    private final static String propFileName = "config.properties";

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
        Properties properties = new Properties();

        try (InputStream inputStream = new FileInputStream(propFileName)) {
            logger.info("Loading setting from " + propFile.getAbsolutePath() + ".");
            properties.load(inputStream);
            logger.info("Config from file:" + properties.toString());

            int port = portFromString(properties.getProperty("port"));
            int dbPort = portFromString(properties.getProperty("db_port"));
            int mailPort = portFromString(properties.getProperty("mail.smtp.port"));
            if (port == -1 || dbPort == -1 || mailPort == -1) System.exit(0);
            String dbHost = properties.getProperty("db_host");
            String dbName = properties.getProperty("db_name");
            String dbUser = properties.getProperty("db_user");
            String dbPassword = properties.getProperty("db_password");
            String mailUser = properties.getProperty("mail.username");
            String mailPassword = properties.getProperty("mail.password");
            String mailHost = properties.getProperty("mail.smtp.host");
            boolean smtpAuth = Boolean.valueOf(properties.getProperty("mail.smtp.auth"));
            boolean modeAuth = Boolean.valueOf(properties.getProperty("mail.mode.auth"));
            boolean mailInit = true;

            ServerWorker worker = new ServerWorker(port);

            if (modeAuth) {
                mailInit = worker.mailInit(mailUser, mailPassword, mailHost, mailPort, smtpAuth);
            }
            if (worker.SQLInit(dbHost, dbPort, dbName, dbUser, dbPassword) && mailInit)
                try {
                    worker.startWork();
                } catch (BindException e) {
                    logger.error("The port " + properties.getProperty("port") + " is busy.");
                }
            else {
                logger.fatal("INITIALISATION ERROR!");
            }
        }
    }

    /**
     * Проверяет корректность ввода порта
     *
     * @param stringPort Строка, содержащая номер порта
     * @return Номер порта или -1, если он некорректен
     */
    private static int portFromString(String stringPort) {
        try {
            int port = Integer.valueOf(stringPort);
            if (port < 0 || port > 65535) {
                logger.fatal("Wrong port!");
                logger.fatal("Port is a number from 0 to 65535");
                return -1;
            } else {
                return port;
            }
        } catch (NumberFormatException e) {
            logger.fatal("Invalid number format in '" + stringPort + "' !");
            return -1;
        }
    }

}

