package com.example.lostitfoundit.models;

public class Item {

    String itemName;
    String itemTime;
    String itemDescription;
    String itemAddress;

    public Item() {
        // Generic constructor required by Firebase Database;
    }

    public Item(String itemName, String itemTime, String itemDescription, String itemAddress) {
        this.itemName = itemName;
        this.itemTime = itemTime;
        this.itemDescription = itemDescription;
        this.itemAddress = itemAddress;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemTime() {
        return itemTime;
    }

    public void setItemTime(String itemTime) {
        this.itemTime = itemTime;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public String getItemAddress() {
        return itemAddress;
    }

    public void setItemAddress(String itemAddress) {
        this.itemAddress = itemAddress;
    }
}
