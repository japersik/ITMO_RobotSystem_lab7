package com.itmo.r3135.Server.Commands;

import com.itmo.r3135.Server.DataManager;
import com.itmo.r3135.Server.Mediator;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.ServerMessage;


/**
 * Класс обработки комадны help
 * Выводит список доступных команд.
 */

public class HelpCommand extends AbstractCommand {
    /**
     * Формат вывода подстазок
     */
    private final String format = "%-30s%5s%n";

    public HelpCommand(DataManager dataManager, Mediator serverWorker) {
        super(dataManager, serverWorker);
    }

    @Override
    public ServerMessage activate(Command command) {
        String s =
               String.format(format, "add [element]", "Добавить новый элемент в коллекцию") +
               String.format(format, "update [id] [element]", "Обновить значение элемента коллекции, id которого равен заданному") +
               String.format(format, "remove_greater [element]", "Удалить из коллекции все элементы, превышающие заданный") +
               String.format(format, "add_if_min [element]", "Добавить новый элемент в коллекцию, если его значение меньше, чем у наименьшего элемента этой коллекции") +
               String.format(format, "remove_by_id [id]", "Удалить элемент из коллекции по его id") +
               String.format(format, "execute_script [file_name]", "Считать и исполнить скрипт из указанного файла. В скрипте содержатся команды в таком же виде, в котором их вводит пользователь в интерактивном режиме.") +
               String.format(format, "group_counting_by_coordinates", "Сгруппировать элементы коллекции по значению поля coordinates, вывести количество элементов в каждой группе") +
               String.format(format, "filter_contains_name [name]", "Вывести элементы, значение поля name которых содержит заданную подстроку") +
               String.format(format, "print_field_descending_price", "Вывести значения поля price в порядке убывания") +
               String.format(format, "clear", "Очистить коллекцию") +
               String.format(format, "login [email/name] [password]", "Авторизация") +
               String.format(format, "reg [email] [password]", "Регистрация нового пользователя") +
                       
                       ("");
        return new ServerMessage(s);
    }
}
