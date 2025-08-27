package chatapp;

import java.sql.*;
import java.io.File;
import java.io.FileInputStream;

public class Database {
    private static Connection connection;
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/chatapp_db";
    private static final String USER = "postgres";
    private static final String PASS = "Admin";

    public static void init() {
        try {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
            System.out.println("Connected to database: " + DB_URL);

            Statement stmt = connection.createStatement();

            String createUsers = "CREATE TABLE IF NOT EXISTS users (" +
                    "id SERIAL PRIMARY KEY, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password VARCHAR(255) NOT NULL, " +
                    "email VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "profile_image BYTEA" +
                    ")";
            stmt.executeUpdate(createUsers);
            System.out.println("Users table ready.");

            String createMessages = "CREATE TABLE IF NOT EXISTS messages (" +
                    "id SERIAL PRIMARY KEY, " +
                    "sender_id INT NOT NULL, " +
                    "receiver_id INT NOT NULL, " +
                    "message TEXT NOT NULL, " +
                    "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE, " +
                    "FOREIGN KEY (receiver_id) REFERENCES users(id) ON DELETE CASCADE" +
                    ")";
            stmt.executeUpdate(createMessages);
            System.out.println("Messages table ready.");

            ResultSet rs = stmt.executeQuery("SELECT table_name FROM information_schema.tables WHERE table_schema='public'");
            System.out.println("Current tables in database:");
            while (rs.next()) {
                System.out.println("   - " + rs.getString("table_name"));
            }

        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL, USER, PASS);
        }
        return connection;
    }

  
    public static boolean registerUser(String username, String password, String email, String phone) {
        try {
            Connection conn = getConnection();
            String sql = "INSERT INTO users(username, password, email, phone) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, email);
            ps.setString(4, phone);
            ps.executeUpdate();
            System.out.println("User registered: " + username);
            return true;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            if (e.getMessage().contains("duplicate key")) {
                System.out.println("Username already exists!");
            }
            return false;
        }
    }

    public static boolean authenticateUser(String username, String password) {
        try {
            Connection conn = getConnection();
            String sql = "SELECT * FROM users WHERE LOWER(username)=LOWER(?) AND password=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            boolean found = rs.next();
            System.out.println(found ? "Login success for " + username : "Login failed for " + username);
            return found;
        } catch (SQLException e) {
            System.err.println("Authentication error: " + e.getMessage());
        }
        return false;
    }

    public static int getUserIdByUsername(String username) {
        try {
            Connection conn = getConnection();
            String sql = "SELECT id FROM users WHERE LOWER(username)=LOWER(?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int id = rs.getInt("id");
                System.out.println("ℹ User ID for " + username + " is " + id);
                return id;
            }
        } catch (SQLException e) {
            System.err.println("Error getting user ID: " + e.getMessage());
        }
        return -1;
    }

    public static String getUsernameById(int userId) {
        try {
            Connection conn = getConnection();
            String sql = "SELECT username FROM users WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("username");
        } catch (SQLException e) {
            System.err.println("Error getting username by ID: " + e.getMessage());
        }
        return null;
    }

    public static void saveProfileImage(int userId, File file) {
        try {
            Connection conn = getConnection();
            String sql = "UPDATE users SET profile_image=? WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            FileInputStream fis = new FileInputStream(file);
            ps.setBinaryStream(1, fis, (int) file.length());
            ps.setInt(2, userId);
            ps.executeUpdate();
            System.out.println("Profile image updated for user ID: " + userId);
        } catch (Exception e) {
            System.err.println("Error saving profile image: " + e.getMessage());
        }
    }

    public static byte[] getProfileImage(int userId) {
        try {
            Connection conn = getConnection();
            String sql = "SELECT profile_image FROM users WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getBytes("profile_image");
        } catch (SQLException e) {
            System.err.println("Error getting profile image: " + e.getMessage());
        }
        return null;
    }

    public static ResultSet getUserDetails(int userId) {
        try {
            Connection conn = getConnection();
            String sql = "SELECT username, email, phone FROM users WHERE id=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error getting user details: " + e.getMessage());
        }
        return null;
    }

    public static void saveMessage(int senderId, int receiverId, String message) {
        try {
            Connection conn = getConnection();
            String sql = "INSERT INTO messages (sender_id, receiver_id, message, timestamp) VALUES (?, ?, ?, NOW())";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, senderId);
            ps.setInt(2, receiverId);
            ps.setString(3, message);
            ps.executeUpdate();
            System.out.println("Message saved from " + senderId + " to " + receiverId);
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
    }

    public static ResultSet getUserMessages(int userId) {
        try {
            Connection conn = getConnection();
            String sql = "SELECT * FROM messages WHERE sender_id=? OR receiver_id=? ORDER BY timestamp ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, userId);
            return ps.executeQuery();
        } catch (SQLException e) {
            System.err.println("Error fetching messages: " + e.getMessage());
        }
        return null;
    }
}
