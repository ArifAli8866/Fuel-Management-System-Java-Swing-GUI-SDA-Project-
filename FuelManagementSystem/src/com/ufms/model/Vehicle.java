package com.ufms.model;

import java.time.LocalDateTime;

public class Vehicle {
    public enum Status { ACTIVE, MAINTENANCE, INACTIVE }

    private int vehicleId;
    private String registrationNumber;
    private String make;
    private String model;
    private int year;
    private double fuelCapacity;
    private int currentOdometer;
    private Status status;
    private int assignedDriverId;
    private LocalDateTime lastMaintenance;

    public Vehicle(int vehicleId, String registrationNumber, String make,
                   String model, int year, double fuelCapacity, int assignedDriverId) {
        this.vehicleId = vehicleId;
        this.registrationNumber = registrationNumber;
        this.make = make;
        this.model = model;
        this.year = year;
        this.fuelCapacity = fuelCapacity;
        this.currentOdometer = 0;
        this.status = Status.ACTIVE;
        this.assignedDriverId = assignedDriverId;
    }

    // Getters and Setters
    public int getVehicleId() { return vehicleId; }
    public String getRegistrationNumber() { return registrationNumber; }
    public String getMake() { return make; }
    public String getModel() { return model; }
    public int getYear() { return year; }
    public double getFuelCapacity() { return fuelCapacity; }
    public int getCurrentOdometer() { return currentOdometer; }
    public Status getStatus() { return status; }
    public int getAssignedDriverId() { return assignedDriverId; }
    public LocalDateTime getLastMaintenance() { return lastMaintenance; }

    public void setCurrentOdometer(int odometer) { this.currentOdometer = odometer; }
    public void setStatus(Status status) { this.status = status; }
    public void setAssignedDriverId(int driverId) { this.assignedDriverId = driverId; }
    public void setLastMaintenance(LocalDateTime lastMaintenance) { this.lastMaintenance = lastMaintenance; }
    public void setFuelCapacity(double fuelCapacity) { this.fuelCapacity = fuelCapacity; }

    @Override
    public String toString() {
        return registrationNumber + " - " + make + " " + model + " (" + year + ")";
    }
}
