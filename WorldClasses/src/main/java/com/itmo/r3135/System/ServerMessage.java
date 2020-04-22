package com.itmo.r3135.System;

import com.itmo.r3135.World.Product;

import java.io.Serializable;
import java.util.ArrayList;

public class ServerMessage implements Serializable {
    private String message;
    private ArrayList<Product> products;

    public ServerMessage(String message) {
        this.message = message;
    }
    public ServerMessage(String message, ArrayList<Product>products){
        this.message = message;
        this.products = products;
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public String getMessage() {
        return message;
    }


}
