package com.itmo.r3135.Server.Commands;

import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.stream.Collectors;

public class RemoveByIdCommand extends AbstractCommand {

    /**
     * Класс обработки комадны remove_by_id
     */
    public RemoveByIdCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    /**
     * Удаляет элемент по его id.
     */
    @Override
    public ServerMessage activate(Command command) {
        int userId = dataManager.getSqlManager().getUserId(command.getLogin());
        if (userId == -1) return new ServerMessage("Ошибка авторизации!");

        dataManager.getLock().writeLock().lock();
        HashSet<Product> products = dataManager.getProducts();
        int startSize = products.size();
        if (products.size() > 0) {
            int id = command.getIntValue();
            try {
                PreparedStatement statement = dataManager.getSqlManager().getConnection().prepareStatement(
                        "delete from products where user_id = ? and id = ? returning products.id"
                );
                statement.setInt(1, userId);
                statement.setInt(2, id);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next())
                    products.removeAll((products.parallelStream().filter(product -> product.getId() == id)
                            .collect(Collectors.toCollection(HashSet::new))));
            } catch (SQLException e) {
                return new ServerMessage(" Ошибка работы с базой данных");
            }
            if (startSize == products.size()) {
                dataManager.getLock().writeLock().unlock();
                return new ServerMessage("Элемент с id " + id + " не существует. Или принадлежит не Вам.");
            }
            dataManager.updateDateChange();
            dataManager.getLock().writeLock().unlock();
            return new ServerMessage("Элемент коллекции успешно удалён.");
        } else {
            dataManager.getLock().writeLock().unlock();
            return new ServerMessage("Коллекция пуста.");
        }
    }
}
