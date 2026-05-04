package model;

public class FuelRequest {
    public int requestId;
    public int vehicleId;
    public int driverId;
    public double quantity;
    public String status;

    public FuelRequest(int r, int v, int d, double q) {
        requestId = r;
        vehicleId = v;
        driverId = d;
        quantity = q;
        status = "Pending";
    }
}