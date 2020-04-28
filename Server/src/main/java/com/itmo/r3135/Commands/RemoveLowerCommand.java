package com.itmo.r3135.Commands;


import com.google.gson.JsonSyntaxException;
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
 * Класс обработки комадны remove_lower
 */
public class RemoveLowerCommand extends AbstractCommand {

    public RemoveLowerCommand(Collection collection, Mediator serverWorker) {
        super(collection, serverWorker);
    }

    /**
     * Удаляет из коллекции все элементы, меньшие, чем заданный.
     */
    @Override

    public ServerMessage activate(Command command) {
        int userId = collection.getSqlManager().getUserId(command.getLogin());
        if (userId == -1) return new ServerMessage("Ошибка авторизации!");

        try {
            PreparedStatement statement = collection.getSqlManager().getConnection().prepareStatement(
                    "delete from products where user_id = ? and price < ? returning products.id"
            );
            statement.setInt(1, userId);
            statement.setDouble(2, command.getProduct().getPrice());
            ResultSet resultSet = statement.executeQuery();
            ArrayList<Integer> ids = new ArrayList<>();
            while (resultSet.next())
                ids.add(resultSet.getInt("id"));
            collection.getLock().writeLock().lock();
            HashSet<Product> products = collection.getProducts();
            products.removeAll((products.parallelStream().filter(product -> ids.indexOf(product.getId()) != -1)
                    .collect(Collectors.toCollection(HashSet::new))));
            collection.getLock().writeLock().unlock();
            return new ServerMessage("Все элеменды меньше " + command.getProduct().getPrice() + "удалены.");
        } catch (SQLException e) {
            collection.getLock().writeLock().unlock();
            return new ServerMessage("Ошибка поиска объектов пользователя в базе.");
        }
    }
}
//    public ServerMessage activate(Command command) {
//        int userId = collection.getSqlManager().getUserId(command.getLogin());
//        if (userId == -1) return new ServerMessage("Ошибка авторизации!");
//
//        collection.getLock().writeLock().lock();
//        HashSet<Product> products = collection.getProducts();
//        try {
//            int startSize = products.size();
//            if (startSize != 0) {
//                products.removeAll((products.parallelStream().filter(product -> 0 < product.compareTo(command.getProduct()))).collect(Collectors.toCollection(HashSet::new)));
//                collection.uptadeDateChange();
//                collection.getLock().writeLock().unlock();
//                return new ServerMessage("Удалено " + (startSize - products.size()) + " элементов");
//            } else {
//                collection.getLock().writeLock().unlock();
//                return new ServerMessage("Коллекция пуста");
//            }
//        } catch (JsonSyntaxException ex) {
//            collection.getLock().writeLock().unlock();
//            return new ServerMessage("Возникла ошибка синтаксиса Json.");
//        }
//    }
//}
