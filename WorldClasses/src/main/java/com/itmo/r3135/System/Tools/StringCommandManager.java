package com.itmo.r3135.System.Tools;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.World.Product;

import java.util.ArrayList;

/**
 * Класс преобраования строк команд в комадны для отправки серверу
 */
public class StringCommandManager {

    private final Gson gson = new Gson();

    public StringCommandManager() {
    }

    public Command getCommandFromString(String stringCommand) {
        Command command;
        int id;
        Product product;
        String[] trimCommand = stringCommand.trim().split(" ", 2);
        try {
            try {
                switch (trimCommand[0]) {
                    case "code":
                        command = new Command(CommandList.CODE, trimCommand[1]);
                        break;
                    case "login":
                        command = new Command(CommandList.LOGIN);
                        command.setLoginPassword(trimCommand[1].split(" ", 2)[0], trimCommand[1].split(" ", 2)[1]);
                        break;
                    case "reg":
                        command = new Command(CommandList.REG);
                        command.setLoginPassword(trimCommand[1].split(" ", 2)[0], trimCommand[1].split(" ", 2)[1]);
                        break;
                    case "":
                        System.out.println("Команда отсутствует");
                        command = null;
                        break;
                    case "help":
                        command = new Command(CommandList.HELP);
                        break;
                    case "info":
                        command = new Command(CommandList.INFO);
                        break;
                    case "show":
                        command = new Command(CommandList.SHOW);
                        break;
                    case "add":
                        product = gson.fromJson(trimCommand[1], Product.class);
                        if (!product.checkNull()) {
                            command = new Command(CommandList.ADD, product);
                        } else {
                            System.out.println("Элемент " + product.getName() + " не удовлетворяет требованиям коллекции.");
                            product.printCheck();
                            command = null;
                        }
                        break;
                    case "update":
                        id = Integer.valueOf(trimCommand[1].trim().split(" ", 2)[0]);
                        product = gson.fromJson(trimCommand[1].trim().split(" ", 2)[1], Product.class);
                        if (!product.checkNull())
                            command = new Command(CommandList.UPDATE, product, id);
                        else {
                            System.out.println("Элемент " + product.getName() + " не удовлетворяет требованиям коллекции.");
                            product.printCheck();
                            command = null;
                        }
                        break;
                    case "remove_by_id":
                        id = Integer.valueOf(trimCommand[1]);
                        command = new Command(CommandList.REMOVE_BY_ID, id);
                        break;
                    case "clear":
                        command = new Command(CommandList.CLEAR);
                        break;
                    case "execute_script":
                        ArrayList<Command> commands = ScriptReader.read(trimCommand[1]);
                        if (!commands.isEmpty()) {
                            command = new Command(CommandList.EXECUTE_SCRIPT, commands);
                        } else {
                            System.out.println("Скрипт пуст.");
                            command = null;
                        }
                        break;
                    case "exit":
                        System.exit(0);
                        command = null;
                        break;
                    case "add_if_min":
                        product = gson.fromJson(trimCommand[1], Product.class);
                        if (!product.checkNull()) command = new Command(CommandList.ADD_IF_MIN, product);
                        else {
                            System.out.println("Элемент " + product.getName() + " не удовлетворяет требованиям коллекции.");
                            product.printCheck();
                            command = null;
                        }
                        break;
                    case "remove_greater":
                        product = gson.fromJson(trimCommand[1], Product.class);
                        if (!product.checkNull()) return new Command(CommandList.REMOVE_GREATER, product);
                        else {
                            System.out.println("Элемент " + product.getName() + " не удовлетворяет требованиям коллекции");
                            product.printCheck();
                            command = null;
                        }
                        break;
                    case "remove_lower":
                        product = gson.fromJson(trimCommand[1], Product.class);
                        if (!product.checkNull()) return new Command(CommandList.REMOVE_LOWER, product);
                        else {
                            System.out.println("Элемент " + product.getName() + " не удовлетворяет требованиям коллекции.");
                            System.out.println(Product.printRequest());
                            command = null;
                        }
                        break;
                    case "group_counting_by_coordinates":
                        command = new Command(CommandList.GROUP_COUNTING_BY_COORDINATES);
                        break;
                    case "filter_contains_name":
                        command = new Command(CommandList.FILTER_CONTAINS_NAME, trimCommand[1]);
                        break;
                    case "print_field_descending_price":
                        command = new Command(CommandList.PRINT_FIELD_DESCENDING_PRICE);
                        break;
                    default:
                        System.out.println("Неопознанная команда. Наберите 'help' для получения доступных команд.");
                        command = null;
                }
            } catch (NumberFormatException ex) {
                System.out.println("Где-то проблема с форматом записи числа.Команда не выполнена");
                command = null;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            System.out.println("Отсутствует аргумент.");
            command = null;
        } catch (JsonSyntaxException e) {
            System.out.println("Ошибка синтаксиса json.");
            command = null;
        }
        return command;
    }

}

