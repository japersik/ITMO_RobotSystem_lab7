package com.itmo.r3135.SQLconnect;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.World.Color;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLManager {
    static final Logger logger = LogManager.getLogger("SQLManager");
    private Connection connection;

    public SQLManager() {
//        try (Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/studs", "postgres", "12345678");) {
//            logger.info("Connect to basedate is successful");
////            connection.
//            System.out.println(connection.getClientInfo());
//        } catch (SQLException e) {
//            logger.error(e);
//            logger.error(e);
//        }
    }

    public boolean initDatabaseConnection(String host, int port, String dataBaseName, String user, String password) {
        logger.info("Database connect...");
//        try {
//            Class.forName(driver.getJdbcDriver());
//        } catch (ClassNotFoundException e) {
//            logger.fatal("Чтобы подключиться к базе данных, нужен драйвер: " + config.getJdbcDriver());
//        }
        String databaseUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dataBaseName;
        try {
            logger.info("Database URL: " + databaseUrl);
            connection = DriverManager.getConnection(databaseUrl, user, password);
            logger.info("Database '" + connection.getCatalog() + "' is connected! ");
            return true;
        } catch (SQLException e) {
            logger.fatal("Error SQL connection: " + e.toString());
            return false;
        }

    }

    // Создаёт таблицы, если их ещё нет
    public boolean initTables() {
        try {
            Statement statement = connection.createStatement();
            statement.execute("create table if not exists products " +
                    "(id serial primary key not null , name text, x float,y double precision," +
                    "creationDate timestamp,price double precision, partNumber text, manufactureCost float, unitOfMeasure text," +
                    "ownerName text, ownerBirthday timestamp,ownerEyeColor text,ownerHairColor text, user_id integer)"
            );

//            Color[] colors = {Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW};
//            statement.execute("create table if not exists colors(id int primary key generated always as identity ,name text unique)");
//            for (Color color : colors)
//                statement.execute("insert ignore into colors(name) values('" + color + "') on duplicate ");

            statement.execute("create table if not exists users (" +
                    "id serial primary key not null, name text, email text unique, password_hash bytea)"
            );
            return true;
        } catch (SQLException e) {
            logger.fatal("Error in tables initialisation.");
            return false;
        }
    }

    public boolean checkCommandUser(Command command) {

        return true;
    }

    public Connection getConnection() {
        return connection;
    }
}
