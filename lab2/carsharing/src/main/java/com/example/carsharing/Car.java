package com.example.carsharing;

public class Car {
    private String carId;
    private int fuelLevel;
    private boolean isLocked;

    public Car(String carId, int fuelLevel, boolean isLocked) {
        this.carId = carId;
        this.fuelLevel = fuelLevel;
        this.isLocked = isLocked;
    }

    public void lockCar() {
        isLocked = true;
    }

    public void unlockCar() {
        isLocked = false;
    }

    public String getCarId() {
        return carId;
    }

    public void setCarId(String carId) {
        this.carId = carId;
    }

    public int getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(int fuelLevel) {
        this.fuelLevel = fuelLevel;
    }
}