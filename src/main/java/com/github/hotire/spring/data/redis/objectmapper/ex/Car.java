package com.github.hotire.spring.data.redis.objectmapper.ex;

import lombok.Getter;

@Getter
public class Car extends Vehicle {
    private final int seatingCapacity;
    private final double topSpeed;

    public Car(String make, String model, int seatingCapacity, double topSpeed) {
      super(make, model);
      this.seatingCapacity = seatingCapacity;
      this.topSpeed = topSpeed;
    }

}