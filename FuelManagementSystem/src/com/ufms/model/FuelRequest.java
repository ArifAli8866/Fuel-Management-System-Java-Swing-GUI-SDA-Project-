package com.ufms.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FuelRequest {
    public enum Status { PENDING, APPROVED, REJECTED, DISPENSED }

    private int requestId;
    private int vehicleId;
    private int driverId;
    private LocalDateTime requestDate;
    private double estimatedQuantity;
    private Status status;
    private int approvedBy;
    private LocalDateTime approvalDate;
    private String rejectionReason;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public FuelRequest(int requestId, int vehicleId, int driverId, double estimatedQuantity) {
        this.requestId = requestId;
        this.vehicleId = vehicleId;
        this.driverId = driverId;
        this.estimatedQuantity = estimatedQuantity;
        this.requestDate = LocalDateTime.now();
        this.status = Status.PENDING;
        this.approvedBy = -1;
    }

    public void approve(int adminId) {
        this.status = Status.APPROVED;
        this.approvedBy = adminId;
        this.approvalDate = LocalDateTime.now();
    }

    public void reject(int adminId, String reason) {
        this.status = Status.REJECTED;
        this.approvedBy = adminId;
        this.approvalDate = LocalDateTime.now();
        this.rejectionReason = reason;
    }

    public void markDispensed() {
        this.status = Status.DISPENSED;
    }

    // Getters
    public int getRequestId() { return requestId; }
    public int getVehicleId() { return vehicleId; }
    public int getDriverId() { return driverId; }
    public LocalDateTime getRequestDate() { return requestDate; }
    public String getRequestDateFormatted() { return requestDate.format(FORMATTER); }
    public double getEstimatedQuantity() { return estimatedQuantity; }
    public Status getStatus() { return status; }
    public int getApprovedBy() { return approvedBy; }
    public LocalDateTime getApprovalDate() { return approvalDate; }
    public String getRejectionReason() { return rejectionReason; }

    @Override
    public String toString() {
        return "Request #" + requestId + " | Vehicle:" + vehicleId + " | " + estimatedQuantity + "L | " + status;
    }
}
