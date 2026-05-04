package model;

public class User {
    public int userId;
    public String username;
    public String password;
    public String role;

    public User(int id, String u, String p, String r) {
        userId = id;
        username = u;
        password = p;
        role = r;
    }
}