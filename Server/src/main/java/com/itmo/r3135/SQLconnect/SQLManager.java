package com.itmo.r3135.SQLconnect;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.World.Color;
import com.itmo.r3135.World.UnitOfMeasure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLManager {
    static final Logger logger = LogManager.getLogger("SQLManager");
    private Connection connection;

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
            //Таблица данных пользователей
            statement.execute("create table if not exists users (" +
                    "id serial primary key not null, name text, email text unique, password_hash bytea)"
            );
            //таблица с color
            statement.execute("CREATE TABLE if not exists colors " +
                    "(Id int primary key generated always as  Identity ,Name varchar(20) NOT NULL UNIQUE )");
            Color[] colors = {Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW};
            try {
                for (Color color : colors)
                    statement.execute("insert into colors(name) values('" + color + "') ");
            } catch (SQLException ignore) {//пока не знаю, как избежать ошибок дубликата, пожтому так.
            }

            //кривая таблица Person(owner)
            statement.execute("create table if not exists owners " +
                    "(id serial primary key not null, ownerName text, ownerBirthday timestamp," +
                    "ownerEyeColor_id int,ownerHairColor_id int," +
                    "foreign key (ownerEyeColor_id) references colors(id)," +
                    "foreign key (ownerHairColor_id) references colors(id))");
            //таблица с unitOfMeasure
            statement.execute("CREATE TABLE if not exists unitOfMeasures " +
                    "(Id int primary key generated always as  Identity ,name varchar(20) NOT NULL UNIQUE )");
            UnitOfMeasure[] unitOfMeasures =
                    {UnitOfMeasure.GRAMS, UnitOfMeasure.LITERS, UnitOfMeasure.MILLIGRAMS, UnitOfMeasure.PCS};
            try {
                for (UnitOfMeasure unitOfMeasure : unitOfMeasures)
                    statement.execute("insert into unitOfMeasures(name) values('" + unitOfMeasure + "') ");
            } catch (SQLException ignore) {//пока не знаю, как избежать ошибок дубликата, пожтому так.
            }
            //кривая таблица Product
            statement.execute("create table if not exists products " +
                    "(id serial primary key not null , name text, x float,y double precision," +
                    "creationDate timestamp,price double precision, partNumber text," +
                    "manufactureCost float, unitOfMeasure_id  int,user_id integer," +
                    "foreign key (unitOfMeasure_id) references unitofmeasures(id)," +
                    "foreign key (id) references owners(id)," +
                    "foreign key (user_id) references users(id))"
            );
            //SEQUENCE для генерации ID
            statement.execute("CREATE SEQUENCE idSequence\n" +
                    "    MINVALUE 1000000000\n" +
                    "    MAXVALUE 9999999999\n" +
                    "    START WITH 1000000000\n" +
                    "    INCREMENT BY 2\n" +
                    "    NO CYCLE;");
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
