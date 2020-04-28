package com.itmo.r3135.Commands;

import com.itmo.r3135.Collection;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.stream.Collectors;

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

        try {
            PreparedStatement statement = collection.getSqlManager().getConnection().prepareStatement(
                    "delete from products where user_id = ? returning products.id"
            );
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            ArrayList<Integer> ids = new ArrayList<>();
            while (resultSet.next())
                ids.add(resultSet.getInt("id"));
            collection.getLock().writeLock().lock();
            HashSet<Product> products = collection.getProducts();
                products.removeAll((products.parallelStream().filter(product -> ids.indexOf(product.getId())!=-1)
                        .collect(Collectors.toCollection(HashSet::new))));
        } catch (SQLException e) {
            return new ServerMessage("Ошибка поиска объектов пользователя в базе.");
        }
        collection.getLock().writeLock().unlock();
        return new ServerMessage("Ваши объекты удалены.");

    }
}
