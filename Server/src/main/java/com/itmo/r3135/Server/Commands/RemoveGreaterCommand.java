package com.itmo.r3135.Server.Commands;


import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
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
 * Класс обработки комадны remove_greater
 * Удаляет из коллекции все элементы пользователя, превышающие заданный.
 */
public class RemoveGreaterCommand extends AbstractCommand {
    public RemoveGreaterCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        int userId = dataManager.getSqlManager().getUserId(command.getLogin());
        if (userId == -1) return new ServerMessage("Ошибка авторизации!");

        try {
            PreparedStatement statement = dataManager.getSqlManager().getConnection().prepareStatement(
                    "delete from products where user_id = ? and price > ? returning products.id"
            );
            statement.setInt(1, userId);
            statement.setDouble(2, command.getProduct().getPrice());
            ResultSet resultSet = statement.executeQuery();
            ArrayList<Integer> ids = new ArrayList<>();
            while (resultSet.next())
                ids.add(resultSet.getInt("id"));
            dataManager.getLock().writeLock().lock();
            HashSet<Product> products = dataManager.getProducts();
            products.removeAll((products.parallelStream().filter(product -> ids.indexOf(product.getId()) != -1)
                    .collect(Collectors.toCollection(HashSet::new))));
            dataManager.getLock().writeLock().unlock();
            return new ServerMessage("Все элеменды больше " + command.getProduct().getPrice() + "удалены.");
        } catch (SQLException e) {
            dataManager.getLock().writeLock().unlock();
            return new ServerMessage("Ошибка поиска объектов пользователя в базе.");
        }
    }
}
