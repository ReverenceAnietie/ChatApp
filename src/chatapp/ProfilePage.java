package chatapp;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class ProfilePage extends JFrame {
    private int userId;
    private JLabel lblUsername, lblEmail, lblPhone, lblProfilePic;

    public ProfilePage(int userId) {
        this.userId = userId;

        setTitle("Profile Page");
        setSize(400, 400);
        setLocationRelativeTo(null);

        lblUsername = new JLabel();
        lblEmail = new JLabel();
        lblPhone = new JLabel();
        lblProfilePic = new JLabel();
        lblProfilePic.setPreferredSize(new Dimension(120, 120));

        JButton btnUpload = new JButton("Upload Image");
        btnUpload.setBackground(Color.GREEN);
        btnUpload.setForeground(Color.WHITE);
        btnUpload.addActionListener(e -> uploadImage());

        loadUserProfile();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(lblProfilePic);
        panel.add(lblUsername);
        panel.add(lblEmail);
        panel.add(lblPhone);
        panel.add(btnUpload);

        add(panel);
        setVisible(true);
    }

    private void loadUserProfile() {
        try {
            ResultSet rs = Database.getUserDetails(userId);
            if (rs != null && rs.next()) {
                lblUsername.setText("Username: " + rs.getString("username"));
                lblEmail.setText("Email: " + rs.getString("email"));
                lblPhone.setText("Phone: " + rs.getString("phone"));

                byte[] imgData = Database.getProfileImage(userId);
                if (imgData != null) {
                    ImageIcon icon = new ImageIcon(imgData);
                    Image scaled = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                    lblProfilePic.setIcon(new ImageIcon(scaled));
                } else {
                    lblProfilePic.setText("No Image");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void uploadImage() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            Database.saveProfileImage(userId, file);
            loadUserProfile();
            JOptionPane.showMessageDialog(this, "Profile image updated!");
        }
    }
}
