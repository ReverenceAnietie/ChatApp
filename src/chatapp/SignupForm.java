package chatapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class SignupForm extends JFrame {
    private JTextField usernameField, emailField, phoneField;
    private JPasswordField passwordField;

    public SignupForm() {
        setTitle("Signup");
        setSize(420, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Header
        JLabel header = new JLabel("Chat Application - Signup", SwingConstants.CENTER);
        header.setOpaque(true);
        header.setBackground(new Color(0, 128, 0));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(10,10,10,10));
        add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridLayout(4, 2, 10, 10));
        form.setBorder(new EmptyBorder(20, 24, 20, 24));

        form.add(new JLabel("Username:"));
        usernameField = paddedField();
        form.add(usernameField);

        form.add(new JLabel("Email:"));
        emailField = paddedField();
        form.add(emailField);

        form.add(new JLabel("Phone:"));
        phoneField = paddedField();
        form.add(phoneField);

        form.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                passwordField.getBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));
        form.add(passwordField);

        add(form, BorderLayout.CENTER);

        // Buttons
        JPanel buttons = new JPanel();
        JButton signupBtn = greenButton("Signup");
        JButton loginBtn  = greenButton("Login");
        buttons.add(signupBtn);
        buttons.add(loginBtn);
        add(buttons, BorderLayout.SOUTH);

        signupBtn.addActionListener(e -> registerUser());
        loginBtn.addActionListener(e -> { dispose(); new LoginForm(); });

        setVisible(true);
    }

    private JTextField paddedField() {
        JTextField tf = new JTextField();
        tf.setBorder(BorderFactory.createCompoundBorder(
                tf.getBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));
        return tf;
    }

    private JButton greenButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(0,128,0));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(110, 32));
        return b;
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Required checks
        if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "All fields are required.");
            return;
        }
        // Length checks (align with schema)
        if (username.length() > 50) { JOptionPane.showMessageDialog(this, "Username must be ≤ 50 characters."); return; }
        if (email.length() > 100)   { JOptionPane.showMessageDialog(this, "Email must be ≤ 100 characters.");   return; }
        if (phone.length() > 20)    { JOptionPane.showMessageDialog(this, "Phone must be ≤ 20 characters.");    return; }

        // Format checks
        if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
            JOptionPane.showMessageDialog(this, "Enter a valid email address.");
            return;
        }
        if (!phone.matches("^[0-9+\\-()\\s]{7,20}$")) {
            JOptionPane.showMessageDialog(this, "Enter a valid phone number (7–20 chars).");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            // Duplicate username check
            try (PreparedStatement ck = conn.prepareStatement("SELECT 1 FROM users WHERE username=?")) {
                ck.setString(1, username);
                try (ResultSet rs = ck.executeQuery()) {
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(this, "Username already exists. Choose another.");
                        return;
                    }
                }
            }

            // Insert
            try (PreparedStatement ins = conn.prepareStatement(
                    "INSERT INTO users (username, password, email, phone) VALUES (?,?,?,?)")) {
                ins.setString(1, username);
                ins.setString(2, password); // NOTE: plaintext for demo; hash in production
                ins.setString(3, email);
                ins.setString(4, phone);
                ins.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Signup successful! You can now log in.");
            dispose();
            new LoginForm();

        } catch (SQLException ex) {
            // Unique violation fallback (if constraint triggers)
            if ("23505".equals(ex.getSQLState())) {
                JOptionPane.showMessageDialog(this, "Username already exists.");
            } else {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        }
    }
}
