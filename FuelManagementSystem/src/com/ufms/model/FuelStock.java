package com.ufms.model;

public class FuelStock {
    private double currentLevel;
    private double capacity;
    private double alertThreshold;
    private double pricePerLiter;

    public FuelStock(double capacity, double currentLevel, double alertThreshold, double pricePerLiter) {
        this.capacity = capacity;
        this.currentLevel = currentLevel;
        this.alertThreshold = alertThreshold;
        this.pricePerLiter = pricePerLiter;
    }

    public boolean isLow() {
        return currentLevel <= alertThreshold;
    }

    public boolean dispense(double quantity) {
        if (quantity > currentLevel) return false;
        currentLevel -= quantity;
        return true;
    }

    public void replenish(double quantity) {
        currentLevel = Math.min(currentLevel + quantity, capacity);
    }

    // Getters / Setters
    public double getCurrentLevel() { return currentLevel; }
    public double getCapacity() { return capacity; }
    public double getAlertThreshold() { return alertThreshold; }
    public double getPricePerLiter() { return pricePerLiter; }
    public void setPricePerLiter(double price) { this.pricePerLiter = price; }
    public void setAlertThreshold(double threshold) { this.alertThreshold = threshold; }
    public void setCurrentLevel(double level) { this.currentLevel = level; }

    public double getPercentage() {
        return (currentLevel / capacity) * 100.0;
    }
}
