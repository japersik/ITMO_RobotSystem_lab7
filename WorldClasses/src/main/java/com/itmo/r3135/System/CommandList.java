package com.itmo.r3135.System;

import java.io.Serializable;

/**
 * Класс доступных комадн сервера
 */
public enum CommandList implements Serializable {
    LOAD("load"),
    HELP("help"),
    INFO("info"),
    ADD("add"),
    SHOW("show"),
    UPDATE("update"),
    REMOVE_BY_ID("remove_by_id"),
    CLEAR("clear"),
    EXECUTE_SCRIPT("execute_script"),
    ADD_IF_MIN("add_if_min"),
    REMOVE_GREATER("remove_greater"),
    REMOVE_LOWER("remove_lower"),
    GROUP_COUNTING_BY_COORDINATES("group_counting_by_coordinates"),
    FILTER_CONTAINS_NAME("filter_contains_name"),
    PRINT_FIELD_DESCENDING_PRICE("print_field_descending_price"),

    PING("ping"),
    CODE("code"),
    LOGIN("login"),
    REG("reg");



    private CommandList(String toText) {
        this.toText = toText;
    }

    private String toText;
}
