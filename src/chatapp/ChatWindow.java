package chatapp;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;

public class ChatWindow extends JFrame {
    private String currentUser;
    private String chattingWith = "";
    private JTextArea chatArea;
    private JTextField messageField, searchField;
    private JButton sendButton, searchButton;
    private JLabel lblChatWith;

    public ChatWindow(String username) {
        this.currentUser = username;

        setTitle("Chat App - Logged in as " + currentUser);
        setSize(600, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

       
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("Options");

        JMenuItem profileItem = new JMenuItem("Profile");
        profileItem.addActionListener(e -> {
            int uid = Database.getUserIdByUsername(currentUser);
            if (uid != -1) {
                new ProfilePage(uid);
            } else {
                JOptionPane.showMessageDialog(this, "User not found!");
            }
        });

        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> {
            int uid = Database.getUserIdByUsername(currentUser);
            if (uid != -1) {
                new AboutPage(uid);
            } else {
                JOptionPane.showMessageDialog(this, "User not found!");
            }
        });

        menu.add(profileItem);
        menu.add(aboutItem);
        menuBar.add(menu);
        setJMenuBar(menuBar);

        
        JPanel topPanel = new JPanel(new BorderLayout(5, 5));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        lblChatWith = new JLabel("Not chatting");
        lblChatWith.setFont(new Font("Arial", Font.BOLD, 16));
        lblChatWith.setForeground(new Color(0, 153, 0));

        searchField = new JTextField();
        searchField.setPreferredSize(new Dimension(200, 30));
        searchButton = new JButton("Search");
        searchButton.setBackground(new Color(0, 153, 0));
        searchButton.setForeground(Color.WHITE);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        topPanel.add(lblChatWith, BorderLayout.WEST);
        topPanel.add(searchPanel, BorderLayout.EAST);

        
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        JScrollPane scrollPane = new JScrollPane(chatArea);

        
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 5));
        bottomPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        messageField = new JTextField();
        messageField.setPreferredSize(new Dimension(300, 40));
        sendButton = new JButton("Send");
        sendButton.setBackground(new Color(0, 153, 0));
        sendButton.setForeground(Color.WHITE);

        bottomPanel.add(messageField, BorderLayout.CENTER);
        bottomPanel.add(sendButton, BorderLayout.EAST);

      
        add(topPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        
        searchButton.addActionListener(e -> searchUser());
        sendButton.addActionListener(e -> sendMessage());
        messageField.addActionListener(e -> sendMessage());

        setVisible(true);
    }

   
    private void searchUser() {
        String searchName = searchField.getText().trim();
        if (!searchName.isEmpty() && !searchName.equalsIgnoreCase(currentUser)) {
            int id = Database.getUserIdByUsername(searchName);
            if (id != -1) {
                chattingWith = searchName;
                lblChatWith.setText("Chatting with: " + chattingWith);
                chatArea.setText("");
                loadMessages();
            } else {
                JOptionPane.showMessageDialog(this, "User not found!");
            }
        }
    }

  
    private void sendMessage() {
        if (chattingWith.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search and select a user first!");
            return;
        }

        String msg = messageField.getText().trim();
        if (msg.isEmpty()) return;

        int senderId = Database.getUserIdByUsername(currentUser);
        int receiverId = Database.getUserIdByUsername(chattingWith);

        if (senderId == -1 || receiverId == -1) {
            JOptionPane.showMessageDialog(this, "Invalid user(s)!");
            return;
        }

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO messages (sender_id, receiver_id, message) VALUES (?, ?, ?)")) {
            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setString(3, msg);
            stmt.executeUpdate();

            messageField.setText("");
            loadMessages();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

 
    private void loadMessages() {
        chatArea.setText("");

        int senderId = Database.getUserIdByUsername(currentUser);
        int receiverId = Database.getUserIdByUsername(chattingWith);

        if (senderId == -1 || receiverId == -1) return;

        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT u.username, m.message, m.timestamp " +
                     "FROM messages m " +
                     "JOIN users u ON m.sender_id = u.id " +
                     "WHERE (m.sender_id = ? AND m.receiver_id = ?) " +
                     "   OR (m.sender_id = ? AND m.receiver_id = ?) " +
                     "ORDER BY m.timestamp")) {

            stmt.setInt(1, senderId);
            stmt.setInt(2, receiverId);
            stmt.setInt(3, receiverId);
            stmt.setInt(4, senderId);

            ResultSet rs = stmt.executeQuery();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

            while (rs.next()) {
                String senderName = rs.getString("username");
                String msg = rs.getString("message");
                Timestamp ts = rs.getTimestamp("timestamp");

                chatArea.append(senderName + ":\n" + msg + "\n");
                chatArea.append("⏱ " + ts.toLocalDateTime().format(fmt) + "\n\n");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
