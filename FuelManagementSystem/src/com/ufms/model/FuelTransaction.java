package com.ufms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FuelTransaction {
    private int transactionId;
    private int requestId;
    private int vehicleId;
    private int attendantId;
    private LocalDateTime transactionDate;
    private int odometerReading;
    private double quantityDispensed;
    private double unitPrice;
    private double totalCost;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FuelTransaction(int transactionId, int requestId, int vehicleId,
                           int attendantId, int odometerReading,
                           double quantityDispensed, double unitPrice) {
        this.transactionId = transactionId;
        this.requestId = requestId;
        this.vehicleId = vehicleId;
        this.attendantId = attendantId;
        this.transactionDate = LocalDateTime.now();
        this.odometerReading = odometerReading;
        this.quantityDispensed = quantityDispensed;
        this.unitPrice = unitPrice;
        this.totalCost = quantityDispensed * unitPrice;
    }

    // Getters
    public int getTransactionId() { return transactionId; }
    public int getRequestId() { return requestId; }
    public int getVehicleId() { return vehicleId; }
    public int getAttendantId() { return attendantId; }
    public LocalDateTime getTransactionDate() { return transactionDate; }
    public String getTransactionDateFormatted() { return transactionDate.format(FORMATTER); }
    public int getOdometerReading() { return odometerReading; }
    public double getQuantityDispensed() { return quantityDispensed; }
    public double getUnitPrice() { return unitPrice; }
    public double getTotalCost() { return totalCost; }

    @Override
    public String toString() {
        return String.format("Txn #%d | Vehicle:%d | %.1fL | Rs.%.2f", 
                transactionId, vehicleId, quantityDispensed, totalCost);
    }
}
