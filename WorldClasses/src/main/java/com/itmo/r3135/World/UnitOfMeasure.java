package com.itmo.r3135.World;

import java.io.Serializable;

/**
 * Класс единиц измерения.
 */
public enum UnitOfMeasure implements Serializable {
    PCS("Штуки"),
    LITERS("Литры"),
    GRAMS("Граммы"),
    MILLIGRAMS("Милиграммы");

    String name;

    UnitOfMeasure(String name) {
        this.name = name;
    }

    //@Override
    //public String toString() {
    //    return name;
    //}
}
