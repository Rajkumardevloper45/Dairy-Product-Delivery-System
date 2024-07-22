package com.demo.raj.kumar;

import java.util.Date;

public class Order {
    private int orderId;
    private int userId;
    private Date orderDate;
    private double totalAmount;

    public Order() {}

    public Order(int userId, double totalAmount) {
        this.userId = userId;
        this.totalAmount = totalAmount;
        this.orderDate = new Date(); // Current date and time
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
