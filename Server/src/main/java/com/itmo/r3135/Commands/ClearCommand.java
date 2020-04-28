package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Класс обработки комадны clear
 */
public class ClearCommand extends AbstractCommand {
    public ClearCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Очищает коллекцию.
     */
    @Override
    public ServerMessage activate(Command command) {
        int userId = collection.getSqlManager().getUserId(command.getLogin());
        if (userId == -1) return new ServerMessage("Ошибка авторизации!");
        collection.getLock().writeLock().lock();
        try {
            PreparedStatement statement = collection.getSqlManager().getConnection().prepareStatement(
                    "delete from products where user_id = ? returning id"
            );
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            System.out.println(resultSet.getFetchSize());
            PreparedStatement statement2 = collection.getSqlManager().getConnection().prepareStatement(
                    "delete from owners where user_id = ? returning id"
            );
            statement2.setArray(1, resultSet.getArray("id"));
            ResultSet resultSet2 = statement2.executeQuery();

            while (resultSet.next())
                System.out.println(resultSet.getInt("id"));
        } catch (SQLException e) {
            return new ServerMessage("Ошибка поиска объектов пользователя в базе.");
        }
        collection.getLock().writeLock().unlock();
        return new ServerMessage("Ваши объекты удалены.");

    }
}
