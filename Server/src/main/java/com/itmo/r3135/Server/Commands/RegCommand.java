package com.itmo.r3135.Server.Commands;

import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Класс обработки комадны reg
 * Регистритует нового пользователя
 */
public class RegCommand extends AbstractCommand {
    public RegCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        try {
            if (!dataManager.getSqlManager().checkEmail(command.getLogin()))
                return new ServerMessage("Incorrect login!", false);
            PreparedStatement statement = dataManager.getSqlManager().getConnection().prepareStatement(
                    "insert into users (email, password_hash, username) values (?, ?, ?) "
            );
            statement.setString(1, command.getLogin());
            statement.setBytes(2, command.getPassword().getBytes());
            statement.setString(3, emailParse(command.getLogin()));
            try {
                statement.execute();
                if (dataManager.getMailManager() != null)
                    dataManager.getSqlManager().userStatusReg(
                            dataManager.getSqlManager().getUserId(command.getLogin()));
            } catch (SQLException e) {
                logger.error("Попытка добавления по существующему ключу");
                return new ServerMessage("Пользователь с именем " + emailParse(command.getLogin()) + " уже существует!");
            }
            if (dataManager.getMailManager() != null &&
                    !dataManager.getMailManager().sendMailHTML(command.getLogin(), emailParse(command.getLogin()),
                            dataManager.getSqlManager().getUserCode(dataManager.getSqlManager().getUserId(command.getLogin())))) {
                logger.error("ERROR IN EMAIL SENDING TO " + command.getLogin());
                dataManager.getSqlManager().clearStatus(dataManager.getSqlManager().getUserId(command.getLogin()));
                return new ServerMessage("Successful registration!");
            }
            if (dataManager.getMailManager() != null)
                return new ServerMessage("Successful registration! Check your email :)",false);

            return new ServerMessage("Successful registration.");

        } catch (SQLException e) {
            logger.error("Бда, бда SQLException");
            return new ServerMessage("Ошибка регистрации");
        }
    }

    private String emailParse(String email) {
        return email.split("@")[0];
    }
}
