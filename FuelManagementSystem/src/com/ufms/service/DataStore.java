package com.ufms.service;

import com.ufms.model.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Singleton DataStore acting as in-memory repository.
 * Pattern: Singleton (ensures one shared data source across all panels).
 */
public class DataStore {
    private static DataStore instance;

    private List<User> users = new ArrayList<>();
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<FuelRequest> fuelRequests = new ArrayList<>();
    private List<FuelTransaction> fuelTransactions = new ArrayList<>();
    private FuelStock fuelStock;
    private List<String> auditLog = new ArrayList<>();

    private int userIdCounter = 10;
    private int vehicleIdCounter = 10;
    private int requestIdCounter = 10;
    private int transactionIdCounter = 10;

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        if (instance == null) {
            instance = new DataStore();
        }
        return instance;
    }

    private void seedData() {
        // Seed Users
        users.add(new User(1, "admin", "admin123", "Muhammad Ali (Admin)", "admin@fast.edu.pk", User.Role.TRANSPORT_ADMIN));
        users.add(new User(2, "driver1", "pass123", "Ahmed Khan", "ahmed@fast.edu.pk", User.Role.DRIVER));
        users.add(new User(3, "driver2", "pass123", "Bilal Raza", "bilal@fast.edu.pk", User.Role.DRIVER));
        users.add(new User(4, "attendant1", "pass123", "Zain Ul Abidin", "zain@fast.edu.pk", User.Role.FUEL_ATTENDANT));
        users.add(new User(5, "finance1", "pass123", "Sara Ahmed", "sara@fast.edu.pk", User.Role.FINANCE_DEPT));
        users.add(new User(6, "sysadmin", "admin123", "Arif Ali", "arif@fast.edu.pk", User.Role.SYSTEM_ADMIN));

        // Seed Vehicles
        vehicles.add(new Vehicle(1, "LEA-1234", "Toyota", "Coaster", 2020, 100.0, 2));
        vehicles.add(new Vehicle(2, "LEA-5678", "Hino", "Bus", 2018, 200.0, 3));
        vehicles.add(new Vehicle(3, "LEA-9012", "Suzuki", "Pickup", 2022, 60.0, 2));
        vehicles.get(0).setCurrentOdometer(45200);
        vehicles.get(1).setCurrentOdometer(112500);
        vehicles.get(2).setCurrentOdometer(8300);

        // Seed FuelStock
        fuelStock = new FuelStock(5000.0, 3200.0, 500.0, 320.0);

        // Seed some past requests and transactions
        FuelRequest r1 = new FuelRequest(1, 1, 2, 80.0);
        r1.approve(1);
        r1.markDispensed();
        fuelRequests.add(r1);

        FuelRequest r2 = new FuelRequest(2, 2, 3, 150.0);
        r2.approve(1);
        fuelRequests.add(r2);

        FuelRequest r3 = new FuelRequest(3, 1, 2, 60.0);
        fuelRequests.add(r3);

        FuelTransaction t1 = new FuelTransaction(1, 1, 1, 4, 45200, 78.5, 320.0);
        fuelTransactions.add(t1);

        requestIdCounter = 4;
        transactionIdCounter = 2;

        addAuditLog("SYSTEM", "System initialized with seed data");
    }

    // ─── AUTH ────────────────────────────────────────────────────────────────
    public User authenticate(String username, String password) {
        return users.stream()
                .filter(u -> u.getUsername().equals(username) && u.authenticate(password) && u.isActive())
                .findFirst().orElse(null);
    }

    // ─── USERS ───────────────────────────────────────────────────────────────
    public List<User> getAllUsers() { return Collections.unmodifiableList(users); }

    public User getUserById(int id) {
        return users.stream().filter(u -> u.getUserId() == id).findFirst().orElse(null);
    }

    public boolean addUser(User user) {
        boolean exists = users.stream().anyMatch(u -> u.getUsername().equals(user.getUsername()));
        if (exists) return false;
        users.add(user);
        addAuditLog("ADMIN", "New user added: " + user.getUsername());
        return true;
    }

    public int getNextUserId() { return ++userIdCounter; }

    // ─── VEHICLES ────────────────────────────────────────────────────────────
    public List<Vehicle> getAllVehicles() { return Collections.unmodifiableList(vehicles); }

    public Vehicle getVehicleById(int id) {
        return vehicles.stream().filter(v -> v.getVehicleId() == id).findFirst().orElse(null);
    }

    public boolean addVehicle(Vehicle vehicle) {
        boolean exists = vehicles.stream().anyMatch(v -> v.getRegistrationNumber().equals(vehicle.getRegistrationNumber()));
        if (exists) return false;
        vehicles.add(vehicle);
        addAuditLog("ADMIN", "Vehicle registered: " + vehicle.getRegistrationNumber());
        return true;
    }

    public int getNextVehicleId() { return ++vehicleIdCounter; }

    public List<Vehicle> getVehiclesByDriver(int driverId) {
        return vehicles.stream().filter(v -> v.getAssignedDriverId() == driverId).collect(Collectors.toList());
    }

    // ─── FUEL REQUESTS ───────────────────────────────────────────────────────
    public List<FuelRequest> getAllRequests() { return Collections.unmodifiableList(fuelRequests); }

    public List<FuelRequest> getPendingRequests() {
        return fuelRequests.stream()
                .filter(r -> r.getStatus() == FuelRequest.Status.PENDING)
                .collect(Collectors.toList());
    }

    public List<FuelRequest> getApprovedRequests() {
        return fuelRequests.stream()
                .filter(r -> r.getStatus() == FuelRequest.Status.APPROVED)
                .collect(Collectors.toList());
    }

    public List<FuelRequest> getRequestsByDriver(int driverId) {
        return fuelRequests.stream()
                .filter(r -> r.getDriverId() == driverId)
                .collect(Collectors.toList());
    }

    public FuelRequest getRequestById(int id) {
        return fuelRequests.stream().filter(r -> r.getRequestId() == id).findFirst().orElse(null);
    }

    public void addRequest(FuelRequest request) {
        fuelRequests.add(request);
        addAuditLog("DRIVER", "Fuel request #" + request.getRequestId() + " submitted");
    }

    public int getNextRequestId() { return ++requestIdCounter; }

    // ─── FUEL TRANSACTIONS ───────────────────────────────────────────────────
    public List<FuelTransaction> getAllTransactions() { return Collections.unmodifiableList(fuelTransactions); }

    public List<FuelTransaction> getTransactionsByVehicle(int vehicleId) {
        return fuelTransactions.stream()
                .filter(t -> t.getVehicleId() == vehicleId)
                .collect(Collectors.toList());
    }

    public void addTransaction(FuelTransaction transaction) {
        fuelTransactions.add(transaction);
        addAuditLog("ATTENDANT", "Fuel dispensed: " + transaction.getQuantityDispensed() + "L to Vehicle #" + transaction.getVehicleId());
    }

    public int getNextTransactionId() { return ++transactionIdCounter; }

    // ─── FUEL STOCK ──────────────────────────────────────────────────────────
    public FuelStock getFuelStock() { return fuelStock; }

    // ─── AUDIT LOG ───────────────────────────────────────────────────────────
    public void addAuditLog(String actor, String action) {
        String entry = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                + " [" + actor + "] " + action;
        auditLog.add(entry);
    }

    public List<String> getAuditLog() { return Collections.unmodifiableList(auditLog); }

    // ─── STATS ───────────────────────────────────────────────────────────────
    public double getTotalFuelDispensed() {
        return fuelTransactions.stream().mapToDouble(FuelTransaction::getQuantityDispensed).sum();
    }

    public double getTotalCost() {
        return fuelTransactions.stream().mapToDouble(FuelTransaction::getTotalCost).sum();
    }
}
