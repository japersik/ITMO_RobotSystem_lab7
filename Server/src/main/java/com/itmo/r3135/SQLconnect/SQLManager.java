package com.itmo.r3135.SQLconnect;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.World.Color;
import com.itmo.r3135.World.UnitOfMeasure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

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
                    "id serial primary key not null, username text unique , email text unique, password_hash bytea)"
            );
            //таблица с color
            statement.execute("CREATE TABLE if not exists colors " +
                    "(Id serial primary key not null ,name varchar(20) NOT NULL UNIQUE )");
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
                    "(Id serial primary key not null ,unitname varchar(20) NOT NULL UNIQUE )");
            UnitOfMeasure[] unitOfMeasures =
                    {UnitOfMeasure.GRAMS, UnitOfMeasure.LITERS, UnitOfMeasure.MILLIGRAMS, UnitOfMeasure.PCS};
            try {
                for (UnitOfMeasure unitOfMeasure : unitOfMeasures)
                    statement.execute("insert into unitOfMeasures(unitname) values('" + unitOfMeasure + "') ");
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

            return true;
        } catch (SQLException e) {
            logger.fatal("Error in tables initialisation.");
            e.printStackTrace();
            return false;
        }
    }

    public int getUserId(String loginName) {
        int userId = -1;
        try {
            PreparedStatement s = connection
                    .prepareStatement("select id from users where (email = ? or username =?)");
            s.setString(1, loginName);
            s.setString(2, loginName);
            ResultSet resultSet = s.executeQuery();
            if (resultSet.next()) userId = resultSet.getInt("id");
            System.out.println(userId);
        } catch (SQLException ignore) {
        }
        return userId;
    }

    public boolean checkCommandUser(Command command) {
        return true;
    }

    public Connection getConnection() {
        return connection;
    }
}
