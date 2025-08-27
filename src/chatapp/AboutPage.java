package chatapp;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class AboutPage extends JFrame {
    private int userId;
    private JLabel lblUsername, lblEmail, lblPhone, lblProfilePic;
    private JTextArea txtDescription;

    public AboutPage(int userId) {
        this.userId = userId;

        setTitle("About Page");
        setSize(450, 500);
        setLocationRelativeTo(null);

        lblUsername = new JLabel();
        lblEmail = new JLabel();
        lblPhone = new JLabel();
        lblProfilePic = new JLabel();
        lblProfilePic.setPreferredSize(new Dimension(120, 120));

        txtDescription = new JTextArea(
            "22/SC/CO/062 " +
            " I am a dedicated Computer Science student with a strong foundation " +
            "in programming, problem-solving, and analytical thinking. " +
            "My studies focus on core areas such as software development, " +
            "algorithms, databases, and computer systems, while I actively explore " +
            "emerging technologies including machine learning, cybersecurity, and web development."
        );
        txtDescription.setLineWrap(true);
        txtDescription.setWrapStyleWord(true);
        txtDescription.setEditable(true);
        txtDescription.setFont(new Font("SansSerif", Font.PLAIN, 14));
        txtDescription.setBackground(getBackground());

        loadAboutInfo();

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        panel.add(lblProfilePic);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(lblUsername);
        panel.add(lblEmail);
        panel.add(lblPhone);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(new JLabel("About Me:"));
        panel.add(txtDescription);

        add(new JScrollPane(panel));
        setVisible(true);
    }

    private void loadAboutInfo() {
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
}
