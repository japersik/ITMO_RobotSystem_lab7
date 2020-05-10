package com.itmo.r3135.World;

import com.google.gson.Gson;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Класс генератор элементов коллекции
 */
public class Generator {
    static String[] personNames = {"Аркадий", "Петрович", "Партия", "Ленин", "Сталин", "Колхоз", "МЕГА(ИТМО)", "Царь",
            "Красные", "Буржуй", "Препод", "Тот, чьё имя нельза называть", "Покемон", "Пользователь мака",
            "Уточка","Пациент","Дурка","[Не]должник"};
    static String[] productNames = {"Голова", "Почка", "Пакет крови", "Глаз", "Сердце хомячка", "Рулон бумаги", "Фарш",
            "Запрещёнка", "Вирус", "Антисептик", "Труп", "Мак", "Нормальный компьютер(не мак)", "Уточка","Перчатки",
            "ПрепОд", "Набор \"Очумелые ручки\"", "3 балла за ЛК", "Восьмая лаба", "МЯСО", };

    static UnitOfMeasure[] units = {UnitOfMeasure.PCS, UnitOfMeasure.LITERS, UnitOfMeasure.GRAMS, UnitOfMeasure.MILLIGRAMS};
    static Color[] colors = {Color.GREEN, Color.RED, Color.BLACK, Color.BLUE, Color.YELLOW};

    static char[] chs = "ZXCVBNMASDFGHJKLQWERTYUIOP1234567890zxcvbnmasdfghjklqwertyuiop".toCharArray();
    static Gson gson = new Gson();
    static Random random = new Random();

    /**
     * Метод возвращает объект класса Product в формате json
     */
    public static String nextGsonProduct() {
        return gson.toJson(nextProduct());
    }

    public static Product nextProduct() {
        return new Product(nextName(), nextCoordinates(), nextPrice(), nextPartNumber(), nextManufactureCost(), nextUnitOfMeasure(), nextPerson());
    }

    private static UnitOfMeasure nextUnitOfMeasure() {
        return units[random.nextInt(units.length)];
    }

    private static Float nextManufactureCost() {
        return random.nextFloat();
    }

    private static String nextName() {
        return productNames[random.nextInt(productNames.length)] + " №: " + Math.abs(random.nextInt());
    }

    private static Double nextPrice() {
        return random.nextDouble();
    }

    private static String nextPartNumber() {
        String number = "";
        for (int i = 0; i < 22; i++) {
            number = number + (chs[random.nextInt(chs.length)]);
        }
        return number;
    }

    private static Color nextColor() {
        return colors[random.nextInt(colors.length)];
    }

    private static Person nextPerson() {
        return new Person(nextPersonName(), LocalDateTime.now(), nextColor(), nextColor());
    }

    private static String nextPersonName() {
        return personNames[random.nextInt(personNames.length)];
    }

    private static Coordinates nextCoordinates() {
        return new Coordinates(random.nextFloat() * 500 - 250, random.nextDouble() * 500 - 250);
    }
}
