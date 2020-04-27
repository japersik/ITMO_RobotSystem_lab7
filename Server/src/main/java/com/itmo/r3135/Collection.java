package com.itmo.r3135;

import com.itmo.r3135.SQLconnect.SQLManager;
import com.itmo.r3135.World.Product;

import java.io.File;
import java.util.Date;
import java.util.HashSet;

public class Collection {
    private File jsonFile;
    private HashSet<Product> products;
    private SQLManager sqlManager;
    private Date dateInitialization;
    private Date dateSave;
    private Date dateChange;

    {
        products = new HashSet<>();
        dateInitialization = new Date();
        dateChange = new Date();
        dateSave = new Date();
    }

    public void setSqlManager(SQLManager sqlManager) {
        this.sqlManager = sqlManager;
    }


    public SQLManager getSqlManager(){return sqlManager; }


    public Collection(File jsonFile) {
        this.jsonFile = jsonFile;
    }


    public Collection() {
        this(null);
    }

    public Date getDateChange() {
        return dateChange;
    }

    public File getJsonFile() {
        return jsonFile;
    }

    public HashSet<Product> getProducts() {
        return products;
    }

    public void uptadeDateChange() {
        this.dateChange = new Date();
    }

    public void setJsonFile(File jsonFile) {
        this.jsonFile = jsonFile;
    }


    public void updateDateSave() {
        this.dateSave = new Date();
    }

    public void setProducts(HashSet<Product> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "------------------------" +
                "\nИнформация о коллекции:" +
                "\n------------------------" +
                "\n Количество элементов коллекции: " + products.size() +
                "\n Дата инициализации: " + dateInitialization +
                "\n Дата последнего сохранения: " + dateSave +
                "\n Дата последнего изменения: " + dateChange;
    }
}
