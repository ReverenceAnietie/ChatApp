package chatapp;

public class ChatApp {
    public static void main(String[] args) {
      
        Database.init();
        try {
            Database.getConnection();
        } catch (Exception e) {
            System.out.println("Failed to connect to DB: " + e.getMessage());
            return;
        }

        new LoginForm();
    }
}
