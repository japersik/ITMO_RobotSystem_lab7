package com.itmo.r3135.World;

import java.io.Serializable;

/**
 * Класс координат.
 */
public class Coordinates implements Serializable {
    private Float x; //Значение поля должно быть больше -50, Поле не может быть null
    private double y;

    /**
     * Конструктор для класса com.itmo.com.itmo.r3135.World.Coordinates.
     *
     * @param x float.
     * @param y double.
     */
    public Coordinates(Float x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Возвращает координату X.
     *
     * @return float значение координаты по X.
     */
    public Float getX() {
        return x;
    }

    /**
     * Устанавливает значение координаты X.
     *
     * @param x float.
     */
    public void setX(Float x) {
        this.x = x;
    }

    /**
     * Устанавливает значение координаты Y.
     *
     * @param y double.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Возвращает координату Y.
     *
     * @return double значение координаты по Y.
     */
    public double getY() {
        return y;
    }

    @Override
    public String toString() {
        return "X: " + String.format("%g", x) + ", Y: " + String.format("%g", y);
    }
}