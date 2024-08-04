package com.samadhan.enums;

public enum SubscriptionPrice {

    SILVER("silver", 199.0),
    GOLD("gold", 299.0),
    PLATINUM("platinum", 399.0);

    private String subscriptionName;
    private Double price;

    private SubscriptionPrice(String subscriptionName, Double price) {
        this.subscriptionName = subscriptionName;
        this.price = price;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public Double getPrice() {
        return price;
    }

    public SubscriptionPrice getSubscriptionByName(String subscriptionName) {
        SubscriptionPrice subscriptionPrice = null;
        for(SubscriptionPrice sp: values()) {
            if(sp.subscriptionName.equalsIgnoreCase(subscriptionName)) {
                subscriptionPrice = sp;
            } else {
                subscriptionPrice = null;
            }
        }
        return subscriptionPrice;
    }
}
