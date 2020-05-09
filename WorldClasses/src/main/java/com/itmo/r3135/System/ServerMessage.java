package com.itmo.r3135.System;

import com.itmo.r3135.World.Product;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Класс ответа сервера
 */
public class ServerMessage implements Serializable {
    private final String message;
    private ArrayList<Product> products;
    private Boolean login;

    {
        login = true;
    }

    public ServerMessage(String message) {
        this.message = message;
    }

    public ServerMessage(String message, ArrayList<Product> products) {
        this.message = message;
        this.products = products;

    }

    public ServerMessage(String message, Boolean login) {
        this.message = message;
        this.login = login;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public String getMessage() {
        return message;
    }

    public Boolean getLogin() {
        return login;
    }

    public void setLogin(Boolean login) {
        this.login = login;
    }
}
