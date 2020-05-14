package com.itmo.r3135.Server;

import com.itmo.r3135.Server.SQLconnect.MailManager;
import com.itmo.r3135.Server.SQLconnect.SQLManager;
import com.itmo.r3135.World.Product;

import java.util.Date;
import java.util.HashSet;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Класс, хранящий всё необходимое для использования команд.
 */
public class DataManager {
    private final Date dateInitialization = new Date();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private HashSet<Product> products = new HashSet<>();
    private SQLManager sqlManager;
    private Date dateChange = new Date();
    private MailManager mailManager;

    public DataManager() {
    }

    public MailManager getMailManager() {
        return mailManager;
    }

    public void setMailManager(MailManager mailManager) {
        this.mailManager = mailManager;
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public void setSqlManager(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }

    public ReadWriteLock getLock() {
        return lock;
    }

    public Date getDateChange() {
        return dateChange;
    }

    public HashSet<Product> getProducts() {
        return products;
    }

    public void setProducts(HashSet<Product> products) {
        this.products = products;
    }

    public void updateDateChange() {
        this.dateChange = new Date();
    }

    @Override
    public String toString() {
        return "------------------------" +
                "\nИнформация о коллекции:" +
                "\n------------------------" +
                "\n Количество элементов коллекции: " + products.size() +
                "\n Дата инициализации: " + dateInitialization +
                "\n Дата последнего изменения: " + dateChange;
    }

}
