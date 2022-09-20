package com.github.hotire.spring.data.redis.objectmapper.ex;

import lombok.Getter;

@Getter
public class Truck extends Vehicle {
    private final double payloadCapacity;

    public Truck(String make, String model, double payloadCapacity) {
        super(make, model);
        this.payloadCapacity = payloadCapacity;
    }

}
