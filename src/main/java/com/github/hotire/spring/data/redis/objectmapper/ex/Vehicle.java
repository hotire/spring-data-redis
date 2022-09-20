package com.github.hotire.spring.data.redis.objectmapper.ex;

import lombok.Getter;

@Getter
public abstract class Vehicle {
    private final String make;
    private final String model;

    protected Vehicle(String make, String model) {
      this.make = make;
      this.model = model;
    }
}