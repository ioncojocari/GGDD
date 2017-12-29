package com.ioncoj.data.models;

public class MenuBundle {
    public enum TYPE{co2,h2o;}
    private TYPE type;
    private double quantity;

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
