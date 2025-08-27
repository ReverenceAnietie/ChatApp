package chatapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;

public class LoginForm extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;

    public LoginForm() {
        setTitle("Login");
        setSize(420, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JLabel header = new JLabel("Chat Application - Login", SwingConstants.CENTER);
        header.setOpaque(true);
        header.setBackground(new Color(0,128,0));
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setBorder(new EmptyBorder(10,10,10,10));
        add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridLayout(2,2,10,10));
        form.setBorder(new EmptyBorder(20,24,20,24));
        form.add(new JLabel("Username:"));
        usernameField = new JTextField();
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                usernameField.getBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));
        form.add(usernameField);
        form.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                passwordField.getBorder(), BorderFactory.createEmptyBorder(5,5,5,5)));
        form.add(passwordField);
        add(form, BorderLayout.CENTER);

        JPanel buttons = new JPanel();
        JButton loginBtn = greenButton("Login");
        JButton signupBtn = greenButton("Signup");
        buttons.add(loginBtn); buttons.add(signupBtn);
        add(buttons, BorderLayout.SOUTH);

        loginBtn.addActionListener(e -> loginUser());
        signupBtn.addActionListener(e -> { dispose(); new SignupForm(); });

        setVisible(true);
    }

    private JButton greenButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(new Color(0,128,0));
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(110,32));
        return b;
    }

    private void loginUser() {
        String u = usernameField.getText().trim();
        String p = new String(passwordField.getPassword()).trim();

        if (u.isEmpty() || p.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Enter username and password.");
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1 FROM users WHERE username=? AND password=?")) {
            ps.setString(1, u);
            ps.setString(2, p);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    JOptionPane.showMessageDialog(this, "Login successful!");
                    dispose();
                    new ChatWindow(u);
                } else {
                    JOptionPane.showMessageDialog(this, "Invalid username or password.");
                }
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
