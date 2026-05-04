package com.ufms.model;

public class User {
    public enum Role { DRIVER, FUEL_ATTENDANT, TRANSPORT_ADMIN, SYSTEM_ADMIN, FINANCE_DEPT }

    private int userId;
    private String username;
    private String password;
    private String fullName;
    private String email;
    private Role role;
    private boolean isActive;

    public User(int userId, String username, String password, String fullName, String email, Role role) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.isActive = true;
    }

    public boolean authenticate(String inputPassword) {
        return this.password.equals(inputPassword);
    }

    // Getters and Setters
    public int getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(Role role) { this.role = role; }

    @Override
    public String toString() {
        return fullName + " (" + role + ")";
    }
}
