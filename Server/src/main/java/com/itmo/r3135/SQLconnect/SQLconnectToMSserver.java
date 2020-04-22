package com.itmo.r3135.SQLconnect;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLconnectToMSserver {
    static final Logger logger = LogManager.getLogger("ServerWorker");

    public SQLconnectToMSserver() throws SQLException {
        try(Connection connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/studs", "postgres", "12345678");) {
            logger.info("Connect to basedate is successful");
//            connection.
            System.out.println(connection.getClientInfo());
        } catch (SQLException e){
            logger.error(e);
        }
    }
}
