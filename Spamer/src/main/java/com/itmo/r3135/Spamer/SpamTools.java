package com.itmo.r3135.Spamer;

import com.itmo.r3135.System.Command;
import com.itmo.r3135.System.CommandList;
import com.itmo.r3135.World.Generator;
import com.itmo.r3135.World.Product;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Класс для управления спам-атакой.
 */
public class SpamTools {
    /**
     * Список доступных для спам-атаки команд.
     */
    private final ArrayList<CommandList> allCommands = new ArrayList<>(Arrays.asList(CommandList.HELP,
            CommandList.INFO,
            CommandList.ADD,
            CommandList.SHOW,
            CommandList.UPDATE,
            CommandList.REMOVE_BY_ID,
            CommandList.CLEAR,
            CommandList.EXECUTE_SCRIPT,
            CommandList.ADD_IF_MIN,
            CommandList.REMOVE_GREATER,
            CommandList.REMOVE_LOWER,
            CommandList.GROUP_COUNTING_BY_COORDINATES,
            CommandList.FILTER_CONTAINS_NAME,
            CommandList.PRINT_FIELD_DESCENDING_PRICE,
            CommandList.PING));
    private final ArrayList<CommandList> currentCommand = new ArrayList<>();
    private final SpammerWorker spammerWorker;
    private boolean isSpam = false;
    private int delay = 30;

    public SpamTools(SpammerWorker spammerWorker) {
        this.spammerWorker = spammerWorker;
    }

    /**
     * Активирует спам-атаку
     */
    public void spam() {
        Random random = new Random();
        Command command;
        try {
            while (spammerWorker.isLogin && isSpam) {
                CommandList typeCommand = currentCommand.get(random.nextInt(currentCommand.size()));
                if (typeCommand == CommandList.PING || typeCommand == CommandList.HELP || typeCommand == CommandList.INFO ||
                        typeCommand == CommandList.SHOW || typeCommand == CommandList.CLEAR ||
                        typeCommand == CommandList.PRINT_FIELD_DESCENDING_PRICE ||
                        typeCommand == CommandList.GROUP_COUNTING_BY_COORDINATES) {
                    command = new Command(typeCommand);
                } else if (typeCommand == CommandList.ADD || typeCommand == CommandList.ADD_IF_MIN ||
                        typeCommand == CommandList.REMOVE_GREATER || typeCommand == CommandList.REMOVE_LOWER) {
                    Product product;
                    do {
                        product = Generator.nextProduct();
                    } while (product.checkNull());
                    command = new Command(typeCommand, product);
                } else continue;
                command.setLoginPassword(spammerWorker.login, spammerWorker.password);
                System.out.println("Отправка команды " + command.getCommand());
                spammerWorker.sender.send(command, spammerWorker.socketAddress);
                Thread.sleep(delay);
            }
        } catch (InterruptedException e) {
            System.out.println("Спам атака была остановлена.");
        }
        System.out.println("Спам-атака завершена!");
    }

    /**
     * Выполняет команду по управлению спам-атакой.
     *
     * @param spamCommand Команда для выполнения
     * @return Является ли команда спам-командой
     */
    public boolean execute(String spamCommand) {
        String[] trimCommand = spamCommand.trim().split(" ", 2);
        if (trimCommand[0].equals("spam")) {
            if (spammerWorker.isLogin) {
                if (trimCommand.length == 1) {
                    helpCommand();
                    return false;
                } else
                    trimCommand = trimCommand[1].trim().split(" ", 2);
                try {
                    switch (trimCommand[0]) {
                        case "add":
                            addCommand(trimCommand[1]);
                            break;
                        case "remove":
                            removeCommand(trimCommand[1]);
                            break;
                        case "all":
                            showAllCommand();
                            break;
                        case "show":
                            showCommand();
                            break;
                        case "run":
                            if (!currentCommand.isEmpty()) {
                                Thread spammer = new Thread(this::spam);
                                spammer.setDaemon(true);
                                spammer.start();
                                isSpam = true;
                                System.out.println("Спам-анака начата");
                            } else System.out.println("Список команд пуст. Добавьте их через 'spam add'");
                            break;
                        case "delay":
                            try {
                                int delay = Integer.parseInt(trimCommand[1]);
                                if (delay > 2000) {
                                    System.out.println("Не, ну это слишком долго, давай меньше");
                                } else if (delay < 5) {
                                    System.out.println("Шо за извращенство? При таких значениях за Вами придёт админ!");
                                } else {
                                    this.delay = delay;
                                }
                            } catch (NumberFormatException e) {
                                System.out.println("Ошибка в формате записи числа.");
                            }
                            break;
                        default:
                            System.out.println("Неопознанная команда. Наберите 'spam' для получения доступных команд.");
                    }
                } catch (ArrayIndexOutOfBoundsException ex) {
                    System.out.println("Отсутствует аргумент.Наберите 'spam' для получения пояснений.");
                }
            } else System.out.println("Вы не авторизованы (Авторизуйтесь через 'login' или 'reg')");
            return true;
        } else
            return false;
    }

    /**
     * Добавляет команду в список для атаки.
     *
     * @param commandFromList Команда из CommandList
     */
    private void addCommand(String commandFromList) {
        if (allCommands.contains(enumFromString(commandFromList))) {
            currentCommand.add(enumFromString(commandFromList));
        }
    }

    /**
     * Удаляет команду из списка для атаки.
     *
     * @param commandFromList Команда из CommandList
     */
    private void removeCommand(String commandFromList) {
        currentCommand.remove(enumFromString(commandFromList));

    }

    /**
     * Воводит информацию по спам-командам
     */
    private void helpCommand() {
        String format = "%-30s%5s%n";
        String s =
                String.format(format, "spam", "Показать подсказку по команде spam.") +
                        String.format(format, "spam run", "Запуск спам - атаки.") +
                        String.format(format, "spam show", "Показать используемые комадны для атаки") +
                        String.format(format, "spam all", "Показать все доступные комадны для атаки") +
                        String.format(format, "spam add [command]", "Добавить команду для атаки") +
                        String.format(format, "spam remove [command]", "Удалить команду из списка для атаки") +
                        String.format(format, "spam delay [millis]", "Установить задержку между отправкой команд.") +

                        ("");
        System.out.println(s);
    }

    /**
     * Выводит список автивных команд для атаки.
     */
    private void showCommand() {
        System.out.println("Выбранные комадны для спама:");
        for (CommandList commandList : currentCommand) {
            System.out.println(commandList);
        }
    }

    /**
     * Выводит список всех доступных команд для атаки.
     */
    private void showAllCommand() {
        System.out.println("Доступные комадны для спама:");
        for (CommandList commandList : allCommands) {
            System.out.println(commandList);
        }
    }

    private CommandList enumFromString(String stringEnum) {
        try {
            return CommandList.valueOf(stringEnum);
        } catch (IllegalArgumentException e) {
            System.out.println("Неопознанный тип команды " + stringEnum);
            return null;
        }
    }

    public boolean isSpam() {
        return isSpam;
    }

    public void stopSpam() {
        isSpam = false;
        System.out.println("Спам-атака остановлена");
    }
}
