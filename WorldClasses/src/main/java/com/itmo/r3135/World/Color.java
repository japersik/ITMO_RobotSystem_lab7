package com.itmo.r3135.World;

import java.io.Serializable;

/**
 * Цвета
 */
public enum Color implements Serializable {
    GREEN("Зелёный"),
    RED("Красный"),
    BLACK("Чёрный"),
    BLUE("Синий"),
    YELLOW("Жёлтый");

    String name;

    Color(String name) {
        this.name = name;
    }

  //  @Override
  //  public String toString() {
  //      return name;
  //  }
}
