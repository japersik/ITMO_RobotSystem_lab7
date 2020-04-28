
package com.itmo.r3135.Commands;

import com.itmo.r3135.DataManager;
import com.itmo.r3135.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;
import com.itmo.r3135.World.Product;

import java.util.HashSet;

/**
 * Класс обработки комадны group_counting_by_coordinates
 */
public class GroupCountingByCoordinatesCommand extends AbstractCommand {

    public GroupCountingByCoordinatesCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    /**
     * Группирует элементы коллекции по кординатам на 4 четверти.
     */
    @Override
    public ServerMessage activate(Command command) {
        dataManager.getLock().readLock().lock();
        HashSet<Product> products = dataManager.getProducts();
        if (!products.isEmpty()) {
            final String[] s = {new String()};
            s[0] = s[0] + String.format("%20s%n", "Первая четверть");
            products.parallelStream().filter(product -> product.getCoordinates().getX() >= 0 & product.getCoordinates().getY() >= 0)
                    .forEach(product -> s[0] = s[0] + String.format("%-40s%-12s%-25s%n", product.getName(), product.getId(), product.getCoordinates().toString()));
            s[0] = s[0] + String.format("%20s%n", "Вторая четверть");
            products.parallelStream().filter(product -> product.getCoordinates().getX() < 0 & product.getCoordinates().getY() >= 0)
                    .forEach(product -> s[0] = s[0] + String.format("%-40s%-12s%-25s%n", product.getName(), product.getId(), product.getCoordinates().toString()));
            s[0] = s[0] + String.format("%20s%n", "Третья четверть");
            products.parallelStream().filter(product -> product.getCoordinates().getX() < 0 & product.getCoordinates().getY() < 0)
                    .forEach(product -> s[0] = s[0] + String.format("%-40s%-12s%-25s%n", product.getName(), product.getId(), product.getCoordinates().toString()));
            s[0] = s[0] + String.format("%20s%n", "Четвер четверть");
            products.parallelStream().filter(product -> product.getCoordinates().getX() >= 0 & product.getCoordinates().getY() < 0)
                    .forEach(product -> s[0] = s[0] + String.format("%-40s%-12s%-25s%n", product.getName(), product.getId(), product.getCoordinates().toString()));

            dataManager.getLock().readLock().unlock();
            return new ServerMessage(s[0]);
        } else {
            dataManager.getLock().readLock().unlock();
            return new ServerMessage("Коллекция пуста.");
        }
    }
}

