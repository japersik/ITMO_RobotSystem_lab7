package com.itmo.r3135.System;

import com.itmo.r3135.World.Product;

import java.io.Serializable;
import java.util.ArrayList;

public class Command implements Serializable {
    private ArrayList<Command> ecexuteCommands;
    private CommandList command;
    private Product product;
    private String string;
    private int intValue;

    public Command(CommandList command) {
        this.command = command;
    }

    public Command(CommandList command, Product product) {
        this.command = command;
        this.product = product;
    }

    public Command(CommandList command, String string) {
        this.command = command;
        this.string = string;
    }

    public Command(CommandList command, int intValue) {
        this.command = command;
        this.intValue = intValue;
    }

    public Command(CommandList command, Product product, int intValue) {
        this.product = product;
        this.command = command;
        this.intValue = intValue;
    }
    public Command(CommandList command, ArrayList<Command> ecexuteCommands){
        this.command = command;
        this.ecexuteCommands = ecexuteCommands;
    }

    public ArrayList<Command> getEcexuteCommands() {
        return ecexuteCommands;
    }

    public CommandList getCommand() {
        return command;
    }

    public Product getProduct() {
        return product;
    }

    public int getIntValue() {
        return intValue;
    }

    public String getString() {
        return string;
    }
}
