package com.demo.raj.kumar;

import java.util.Date;

public class Delivery {
    private int deliveryId;
    private int orderId;
    private String deliveryStatus;
    private Date deliveryDate;
    private String trackingNumber;

    public Delivery() {}

    public Delivery(int orderId, String deliveryStatus, String trackingNumber) {
        this.orderId = orderId;
        this.deliveryStatus = deliveryStatus;
        this.trackingNumber = trackingNumber;
        this.deliveryDate = new Date(); // Current date and time
    }

    public int getDeliveryId() {
        return deliveryId;
    }

    public void setDeliveryId(int deliveryId) {
        this.deliveryId = deliveryId;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(String deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getTrackingNumber() {
        return trackingNumber;
    }

    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
}
